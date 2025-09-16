## Higher version Create mod port to Fabric

### Download the latest version of Create-Fly:

https://www.curseforge.com/minecraft/mc-mods/create-fly/files/all?page=1&pageSize=20

### 1. What the project specifically does or adds

- This is a Fabric fork of [Create](https://github.com/Creators-of-Create/Create).
  The official [Create Fabric](https://github.com/Fabricators-of-Create/Create) fork was last released nine
  months ago, which is a long time ago.
- Minecraft uses a new rendering method in higher versions: item models use a dedicated rendering folder, rendering uses
  a rendering pipeline, Entity and GUI rendering is changed to extract the state first and then render, which requires
  creating special rendering for GUI elements.
- Minecraft uses a new data loading method that can capture error messages, which requires a lot of changes to be
  compatible.
- The original Fabric fork was ported using Porting-Lib, which actually required implementing many NeoForge features.
  This project uses a mixin specifically for Create features to make porting easier.
- The original Fabric fork used the Parchment mapping, which is based on Mojang. This project uses the Yarn mapping,
  which is more consistent with Fabric development.
- The original Fabric fork used a mixed approach to server-side and client-side development, which made it easy for the
  server to call non-existent client code, leading to errors. This project uses a new code separation mode for
  development.
- The original Fabric fork used a builder to generate data, which relied on Registrate-Refabricated and made migration
  difficult. This project registers data in a way that's more consistent with vanilla Minecraft.
- This project implements the full Create feature independently, without the need to install the Fabric API (
  compatibility is still in progress)

### 2. Why someone should want to download the project

- Minecraft will always release new versions, and old versions will always become obsolete. If they cannot be ported in
  time, the accumulated modifications will be huge.
- This project can provide higher version Create content that does not exist in the original Fabric fork.

### 3. Any other critical information the user must know before downloading

- Please do not report issues with this mod to simibubi and NeoForge Create.
- Because it uses a lot of mixins, it may be incompatible with other mods. Recommended not to install the Fabric API to
  play the game.
- Please do not use old game saves. Because data loading changes, data may be lost.
- This project is under development and may be unstable and contain many errors.

### 4. TODO List

- Create Commands
- Create Mechanical Mixer Potion Recipe
- Create Server Config Sync
- Recipe Viewer Plugin
- Create Ponder
- Compat Fabric API
- Compat Other Mod

## This project modifies and includes code from the following projects:

- Engine-Room/Flywheel
- Engine-Room/Flywheel/Vanillin
- Creators-of-Create/Create
- Creators-of-Create/Ponder (TODO)

### Contains partial code

- fabricMC/fabric ItemGroup
- neoforged/NeoForge ObjModel

### The license agreement for the open source code used in this project is stored in the licenses directory.

### Donate

- Supporting the Project

[![patreon](https://oss.zurrtum.com/images/patreon.png)](https://www.patreon.com/cw/ZurrTum)

[![afdian](https://oss.zurrtum.com/images/afdian.png)](https://afdian.com/a/zurrtum)