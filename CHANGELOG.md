# Changelog

## 1.4.0

### Highlights
- **Multi-Loader & Multi-Version Support**: Added NeoForge (`1.20.2`–`26.3`) alongside Fabric (`1.19.2`–`26.3`) and Forge (`1.19.2`–`26.3` where supported), covering Minecraft `1.19.2` through `26.3`.
- **Sunlight & Power Storage Rework**:
  - Replaced generic light-level checks with true sunlight/sky access checks (`100%` under open sky or glass, leaf-depth attenuation `75%/50%/25%/0%`, weather scaling in rain and thunder, `0%` underground or near torches).
  - Made Level 1 operate purely on active sunlight (`0` stored power), while Levels 2–5 store power with zero idle drain, scale charge/drain rates and smelt speeds cleanly by level, and preserve stored power and smelts when broken and replaced.
  - Synchronized the `LIT` blockstate and GUI flame indicator so flames only appear while actively smelting.
- **Copper Plate Overhaul**:
  - Replaced the 9x9 scan and player-launch bug with 6-sided thermal conduction (`4 -> 3 -> 2 -> 1`, with opposing hot and cold sources neutralizing to `0`) and comparator signal output (`15/12/8/4/0`).
  - Added world interactions: hot plates evaporate water and melt ice; cold plates freeze adjacent water into Frosted Ice that thaws when removed.
- **UI & Commands**:
  - Added level progress bar tooltips and item tooltips; replaced hardcoded max-level checks with `FurnaceConfig.getMaxLevel()`.
  - Merged `/ultimatefurnace get` into `/ultimatefurnace info [pos]` with an 8-block raycast fallback when `[pos]` is omitted.
- **Packaging & Testing**:
  - Fixed the Forge Mods menu icon crash by packaging `icon.png` cleanly at jar root.
  - Added JUnit 5 unit tests and Minecraft GameTests across loader/version boundaries.
