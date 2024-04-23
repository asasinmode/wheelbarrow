# wheelbarrow

_This is the 1.20.4 branch of the repository and it contains code for the 1.20.4 and 1.20.3 Minecraft versions. Code for the newest Minecraft version can be found on the main branch._

# ANNOUNCEMENT: I'm working on the forge version. I plan to have it done in 2 weeks (may 5th latest).

If you want to make a port/were already making a port and still want to work on it - contact me in issues here/twitter/discord/wherever so we can sort it out.

## showcase

You can download the mod through [modrinth](https://modrinth.com/mod/wheelbarrow) and [curseforge](https://www.curseforge.com/minecraft/mc-mods/wheelbarrow).

![the showcase image for the mod describing features: taking entities for a ride by bumping into them with a wheelbarrow, ejecting them with the default "Z" keybind and with the wheelbarrow being made of copper: showing the 4 oxidation variants](https://github.com/asasinmode/wheelbarrow/blob/main/docs/showcase.jpg?raw=true)

## how to dev

Refer to the information on [fabric wiki](https://fabricmc.net/wiki/start).

## how to script

Go to the `scripts` directory then install dependencies

```bash
bun install
```

### publish

```bash
bun release.ts
```

This script should be run for publishing new versions. It does the following

1. Get the version from `gradle.properties`
2. Check for uncommited changes (all changes have to be commited before running)
3. Merge the `main` branch to other deploy branches specified in `gradle.properties` **deploy_branches** property
  - If any merge fails, the command exists and merge conflicts have to be resolved manually
4. Compare the `version` property in `gradle.properties` from specified branches to the `main` branch. If any is different, it has to be resolved before publishing
5. Prompt for new version
6. Update version in `main` branch `gradle.properties`
7. Add a git tag and create a release commit
8. Merge `main` branch to other specified branches
9. Push changes to github which triggers the release workflow

### why are scripts in the java( )script(-inting language)? skill issues.
