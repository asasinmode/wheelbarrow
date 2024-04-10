# scripts

## why javascript? skill issues.

To install dependencies:

```bash
bun install
```

## publish

```bash
bun release.ts
```

This script should be run for publishing new versions. It does the following

1. Get the version from `gradle.properties`
2. Check for uncommited changes (all changes have to be commited before running)
3. Merge the `main` branch to other deploy branches specified in `gradle.properties`
  - If any merge fails, the command exists and merge conflicts have to be resolved manually
4. Compare the `version` property in `gradle.properties` from specified branches to the `main` branch. If any is different, it has to be resolved before publishing
5. Prompt for new version
6. Update version in `main` branch `gradle.properties`
7. Add a git tag and create a release commit
8. Merge `main` branch to other specified branches
9. Push changes to github which triggers the release workflow
