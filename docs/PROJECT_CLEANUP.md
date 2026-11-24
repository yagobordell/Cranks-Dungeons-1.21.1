# Project cleanup

Completed on 2026-11-24.

Removed 98 obsolete files from the working project:

- 65 textures superseded by the shared inventory, station and HUD renderers, or by the current ailment and level-up visuals.
- 13 unreferenced Java files: inactive mixins, superseded helpers, an unused station handler, an empty accessor and an enchantment placeholder. Mixin configurations and entry points were checked before removal.
- An unused Punchy 2.6.2 dependency, an accidentally extracted rendering library, 16 inspection-preview files, an old log and a generated data cache.

The build uses `libs/punchy-3.2b.jar`; it remains available for compilation. Tree sprites and editor icons loaded dynamically, vanilla UI overrides, alternate HUD textures, development tree data, design notes and worlds under `run/` were retained.

Old generated build/test output was cleared before the final clean build. Active Gradle dependencies remain cached for offline development. Local caches, logs, inspection previews and cleanup backups are excluded from Git.

For recovery, the removed source files and assets are preserved in the local, ignored `.cleanup-backups/SmallChanges.zip`, with original paths and verified SHA-256 content. This backup is not included in the mod or the Git commit.

## Validation

- `python scripts/check_resources.py`: 278 JSON documents and 501 asset/class references, including all dynamically selected tree node variants. This check also runs in GitHub Actions.
- Gradle build and regression tests.
- Native modpack regression probe: Compendium references and navigation, both trees, node editor descriptions, equipment tooltips, recipe privacy, input handling, 268 legal specialized-tome/base combinations, 4,288 rolls and 536 paid altar transactions.
- Final clean build and staged-diff validation before publication.
