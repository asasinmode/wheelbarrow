import { $, type ShellError } from 'bun';
import confirm from '@inquirer/confirm';
import select from '@inquirer/select';

const branch = await $`git branch`.text();

let currentBranch;
for (const line of branch.split('\n')) {
	if (line.startsWith('*')) {
		currentBranch = line.slice(2);
	}
}

if (currentBranch !== 'main') {
	console.error('this script should be run only on `main` branch');
	process.exit(1);
}

const status = await $`git status -s`.text();
if (status.length) {
	console.warn('uncommited changes found, commit before releasing');
	process.exit(1);
}

const configFile = await Bun.file("../gradle.properties").text();

let deployBranches: string[] | undefined;
let mainVersion: number[] | undefined;
for (const line of configFile.split('\n')) {
	let branchesFound = false;
	let versionFound = false;

	if (line.startsWith('deploy_branches')) {
		const value = line.split('=')[1].trim();
		deployBranches = value ? value.split(',') : [];
		branchesFound = true;
	} else if (line.startsWith('mod_version')) {
		const value = line.split('=')[1].trim();
		if (value) {
			mainVersion = value.split('.').map(v => Number.parseInt(v));
		}
		versionFound = true;
	}

	if (branchesFound && versionFound) {
		break;
	}
}

if (!mainVersion) {
	console.error("version property not found in 'gradle.properties'");
	process.exit(1);
}

if (!mainVersion.length) {
	console.error('version property is invalid');
	process.exit(1);
}

if (!deployBranches) {
	console.error("deploy_branches property not found in 'gradle.properties'")
	process.exit(1);
}

const mergeConflictBranches: string[] = [];
const mismatchedVersionBranches: string[] = [];
await executeOnBranches(async (branch) => {
	console.log(`\x1b[37m[${branch}] merging main...\x1b[0m`);
	try {
		await $`git merge --no-commit --no-ff main`.quiet();
	} catch (e) {
		console.error(`[${branch}] merge failed`);
		console.log((e as ShellError).stdout.toString());

		await $`git merge --abort`;
		mergeConflictBranches.push(branch);

		return;
	}

	await $`git commit --no-edit`.quiet();
	console.log(`\x1b[32m[${branch}] merge succesful\x1b[0m]`);

	console.log(`\x1b[37m[${branch}] checking version...\x1b[0m`);

	const configFile = await Bun.file('../gradle.properties').text();
	let branchVersion: number[] | undefined;
	for (const line of configFile.split('\n')) {
		if (line.startsWith('mod_version')) {
			const value = line.split('=')[1].trim();
			if (value) {
				branchVersion = value.split('.').map(v => Number.parseInt(v));
			}
		}
	}

	if (!branchVersion) {
		console.error(`[${branch}] version property not found in 'gradle.properties'`);
		mismatchedVersionBranches.push(branch);
		return;
	}

	if (!branchVersion.length) {
		console.error(`[${branch}] version property is invalid`);
		mismatchedVersionBranches.push(branch);
		return;
	}

	if (branchVersion.length !== mainVersion.length) {
		console.error(`[${branch}] version mismatch. Expected: "${mainVersion.join('.')}", found: "${branchVersion.join()}"`)
		mismatchedVersionBranches.push(branch);
		return;
	} else {
		for (let i = 0; i < mainVersion.length; i++) {
			if (mainVersion[i] !== branchVersion[i]) {
				console.error(`[${branch}] version mismatch. Expected: "${mainVersion.join('.')}", found: "${branchVersion.join()}"`)
				mismatchedVersionBranches.push(branch);
				return;
			}
		}
	}

	console.log(`\x1b[32m[${branch}] version matches\x1b[0m]`);
})

if (mergeConflictBranches.length) {
	console.error(`\nmerge conflicts encountered on branches: ${mergeConflictBranches}`);
}
if (mismatchedVersionBranches.length) {
	console.error(`${mergeConflictBranches.length ? '' : '\n'}merge conflicts encountered on branches: ${mergeConflictBranches}`);
}
if (mergeConflictBranches.length || mismatchedVersionBranches.length) {
	process.exit(1);
}

// TMP until https://github.com/oven-sh/bun/issues/10087
const newVersion = [mainVersion[0], mainVersion[1], mainVersion[2] + 1].join('.');
const proceed = await confirm({ message: `will bump to: \x1b[32m${newVersion}\x1b[0m` });

if (!proceed) {
	process.exit(0);
}

const patchValue = [mainVersion[0], mainVersion[1], mainVersion[2] + 1].join('.');
const minorValue = [mainVersion[0], mainVersion[1] + 1, 0].join('.');
const majorValue = [mainVersion[0] + 1, 0, 0].join('.');

const answer = await select({
	message: `current version: \x1b[32m${mainVersion.join('.')}\x1b[0m`,
	choices: [
		{
			description: `patch \x1b[1m${patchValue}\x1b[0m`,
			value: patchValue,
		},
		{
			description: `minor \x1b[1m${minorValue}\x1b[0m`,
			value: minorValue,
		},
		{
			description: `major \x1b[1m${majorValue}\x1b[0m`,
			value: majorValue,
		},
	],
});

console.log({ answer });

process.exit(0);

// if (!newVersion) {
// 	process.exit(1);
// }

const contents = configFile.split('\n');
const versionLineIndex = contents.findIndex(line => line.startsWith('mod_version'));

const newContents = contents
	.slice(0, versionLineIndex)
	.concat(`mod_version=${newVersion}`)
	.concat(contents.slice(versionLineIndex + 1));

await Bun.write('../gradle.properties', newContents.join('\n'));
await $`git commit -a -n -m "chore: release v${newVersion}"`.quiet();
await $`git tag -a v${newVersion} -m "chore: release v${newVersion}"`

console.log(`\x1b[37mversion bumped, merging 'main' to branches...\x1b[0m`);

await executeOnBranches(async (branch) => {
	try {
		await $`git merge --no-commit --no-ff main`.quiet();
	} catch (e) {
		console.error(`[${branch}] merge failed`);
		console.log((e as ShellError).stdout.toString());

		await $`git merge --abort`;
		return;
	}
	await $`git commit --no-edit`.quiet();
})

console.log(`\x1b[37mmerge succesful, pushing changes...\x1b[0m`);

try {
	await $`git push --all`.quiet();
	await $`git push --tags`.quiet();
} catch (e) {
	console.error('push failed');
	console.log((e as ShellError).stderr.toString());
	process.exit(1);
}

console.log(`\x1b[32mrelease finished\x1b[0m`);
process.exit(0);

async function executeOnBranches(callback: (branchName: string) => Promise<void>) {
	for (const branch of deployBranches!) {
		await $`git checkout ${branch}`.quiet();
		await callback(branch);
	}
	await $`git checkout main`.quiet();
}
