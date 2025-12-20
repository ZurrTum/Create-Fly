## Higher version Create mod port to Fabric

### Download the latest version of Create-Fly:

https://www.curseforge.com/minecraft/mc-mods/create-fly/files/all?page=1&pageSize=20

### 1. What the project specifically does or adds

- This is a Fabric fork of [Create](https://github.com/Creators-of-Create/Create).
  The official [Create Fabric](https://github.com/Fabricators-of-Create/Create) fork has not released a version 1.21.1
  or higher. it has been 8 months since then.
- Minecraft uses a new rendering method in higher versions: item models use a dedicated rendering folder, rendering uses
  a rendering pipeline, Entity, BlockEntity and GUI rendering is changed to extract the state first and then render,
  which requires
  creating special rendering for GUI elements.
- Minecraft uses a new data loading method that can capture error messages, which requires a lot of changes to be
  compatible.
- The original Fabric fork was ported using Porting-Lib, which actually required implementing many NeoForge features.
  This project uses a mixin specifically for Create features to make porting easier.
- The original Fabric fork used a mixed approach to server-side and client-side development, which made it easy for the
  server to call non-existent client code, leading to errors. This project uses a new code separation mode for
  development.
- The original Fabric fork used a builder to generate data, which relied on Registrate-Refabricated and made migration
  difficult. This project registers data in a way that's more consistent with vanilla Minecraft.
- This project implements the full Create feature independently, without the need to install the Fabric API.

### 2. Why someone should want to download the project

- Minecraft will always release new versions, and old versions will always become obsolete. If they cannot be ported in
  time, the accumulated modifications will be huge.
- This project can provide higher version Create content that does not exist in the original Fabric fork.

### 3. Any other critical information the user must know before downloading

- Please do not report issues with this mod to simibubi and NeoForge Create.
- Please do not use old game saves. Because data loading changes, data may be lost.
- Recommended to use REI or JEI to view recipes, and please report any game crashes.
- Using shaders will disable Flywheel optimizations.

### 4. TODO List

- Create Commands
- Compat Fabric Events
- Compat Other Mod

## This project modifies and includes code from the following projects:

- Engine-Room/Flywheel
- Engine-Room/Flywheel/Vanillin
- Creators-of-Create/Create
- Creators-of-Create/Ponder

### Contains partial code

- fabricMC/fabric ItemGroup
- neoforged/NeoForge ObjModel

### The license agreement for the open source code used in this project is stored in the licenses directory.

## Modrinth Maven

```gradle
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    // 26.1-snapshot-1
    implementation "maven.modrinth:create-fly:26.1-snapshot-1-6.0.8-1"
    // 1.21.11
    modImplementation "maven.modrinth:create-fly:1.21.11-6.0.8-3"
    // 1.21.10
    modImplementation "maven.modrinth:create-fly:1.21.10-6.0.8-6"
    // 1.21.8
    modImplementation "maven.modrinth:create-fly:1.21.8-6.0.8-4"
}
```

## Curse Maven

```gradle
repositories {
    exclusiveContent {
        forRepository {
            maven {
                url "https://cursemaven.com"
            }
        }
        filter {
            includeGroup "curse.maven"
        }
    }
}

dependencies {
    // 26.1-snapshot-1
    implementation "curse.maven:create-fly-1346281:7342771-sources-7342781"
    // 1.21.11
    modImplementation "curse.maven:create-fly-1346281:7320679-sources-7320687"
    // 1.21.10
    modImplementation "curse.maven:create-fly-1346281:7357592-sources-7357598"
    // 1.21.8
    modImplementation "curse.maven:create-fly-1346281:7356230-sources-7356237"
}
```

### Donate

- Supporting the Project

[![patreon](https://oss.zurrtum.com/images/patreon.png)](https://www.patreon.com/cw/ZurrTum)

[![afdian](https://oss.zurrtum.com/images/afdian.png)](https://afdian.com/a/zurrtum)
