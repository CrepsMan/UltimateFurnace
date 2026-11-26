# Ultimate Furnace Mod

Minecraft multi-loader mod (`1.19.2`, `1.20.0`, `1.20.1`, `1.20.2`, `1.20.3`, `1.20.4`, `1.20.5`, `1.20.6`, `1.21.0`, `1.21.1`, `1.21.2`, `1.21.3`, `1.21.4`, `1.21.5`, `1.21.6`, `1.21.7`, `1.21.8`, `1.21.9`, `1.21.10`, `1.21.11`, `26.1`, `26.1.1`, `26.1.2`, `26.2`, `26.3`; Fabric on all, Forge on all except `1.20.3`/`1.20.5`/`1.21.2`, and NeoForge on `1.20.2`–`26.3` via Stonecraft `1.15.1` + Stonecutter `0.10`, Java `17` for `<1.20.5` / Java `21` for `1.20.5`–`1.21.11` / Java `25` for `>=26.1`, Official Mojang mappings for `<=1.21.11` & unobfuscated for `>=26.1`) implementing the sunlight-powered, leveling Ultimate Furnace and thermally conductive Copper Plate blocks.

## Build & verification

- Full-matrix build (all declared version-loader targets): `./gradlew build` (on Windows: `.\gradlew.bat build`)
- Full-matrix build + collect jars into `build/libs/`: `.\gradlew.bat buildAndCollect`
- Target-specific workflow:
  1. `.\gradlew.bat "Set active project to <version>-<loader>"` (e.g. `1.19.2-forge`, `1.21.0-forge`, `1.21.1-neoforge`, `26.3-fabric`)
  2. `.\gradlew.bat buildActive`
  3. `.\gradlew.bat "Reset active project"` (restores `vcsVersion` = `26.3-fabric`)
- Dependency manifests per Minecraft version: `versions/dependencies/<mcVersion>.properties`
- Main Java sources: `src/main/java/com/crepsman/ultimate_furnace/`
- Assets & data: `src/main/resources/`

## Agent skills

### Issue tracker

GitHub Issues (`CrepsMan/UltimateFurnace`) via the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Domain docs

Single-context layout (`GLOSSARY.md` and `docs/adr/` at the repo root, created lazily). See `docs/agents/domain.md`.

### Installed skills

All engineering and productivity skills live under `.agents/skills/`:

- `code-review`: Two-axis review (coding standards + spec compliance) of changes since a commit/branch.
- `codebase-design`: Principles for designing deep modules, clean seams, and testable interfaces.
- `diagnosing-bugs`: Structured root-cause diagnosis loop for bugs and performance regressions.
- `domain-modeling`: Build and sharpen `GLOSSARY.md` and ADRs in `docs/adr/` as decisions crystallize.
- `grill-me`: Relentless design-tree interview to sharpen a plan or feature before coding.
- `grill-with-docs`: Run `grilling` together with `domain-modeling` to persist glossary terms and ADRs.
- `grilling`: Core design-tree interviewing discipline used by `grill-me` and `grill-with-docs`.
- `handoff`: Summarize session state into a handoff document in the OS temp directory.
- `implement`: Implement work from a spec or set of issues.
- `improve-codebase-architecture`: Scan the codebase for deepening opportunities, produce an HTML report, and grill through a chosen refactor.
- `setup-matt-pocock-skills`: One-time repo configuration for issue tracker and domain docs.
- `stonecraft`: Stonecraft and Stonecutter multi-version/multi-loader workflow guidance (`gg.meza.stonecraft`).
- `tdd`: Red-green-refactor test-driven development discipline.
- `to-spec`: Synthesize the current conversation into a spec and publish it to GitHub Issues.
- `wait-what`: Re-pitch the last explanation in Simplified Technical English using `GLOSSARY.md` terminology.
