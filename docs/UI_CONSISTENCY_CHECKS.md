# UI consistency checks

The custom UI was checked in the isolated test modpack with real window resizing and Minecraft GUI-scale changes. The build and regression suite passed. The combined native audits completed 887 layout and input-state checks with no remaining reported failures.

## Window and GUI scale matrix

| Window content size | Minecraft GUI scale |
| --- | --- |
| 854 × 480 | 1 and 2 |
| 1024 × 768 | 3 |
| 1280 × 720 | Auto |
| 1600 × 900 | 2 |
| 1920 × 1080 | 1, 3 and 4 |
| 2560 × 1080 | 4 |
| 2560 × 1440 | Auto |
| 900 × 1200 | 2 |
| 1280 × 960 | 4 |
| 1024 × 1200 | 3 |

The minimum tested GUI area was 320 × 240. Coverage included compact and docked inventories/stations, Creative, trade, storage, mount cargo, the Compendium and both trees, class selection/confirmation, settings, both node editors, HUD Layout, naming/transfer dialogs, whistle controls and tooltips. The focused passes also checked entered text after reinitialization, maximum HUD sizes, long names, cargo scrolling and actual rendered tooltip bounds.

## Repairs

- Reserve vertical room for the inventory toggle at the smallest GUI height.
- Align compact return controls with the actual station panel header, including tall altar panels.
- Shrink mount previews on short screens and keep cargo rows and their scrollbar above access controls.
- Share party HUD viewport fitting between gameplay and the layout editor, including oversized configured grids; support resetting its vertical position.
- Preserve an unsubmitted trade coin amount when the screen is resized.
- Size mount-transfer confirmations from their wrapped text so long names do not collide with the buttons.
- Wrap generic text tooltips to the viewport width while retaining formatting and guide links. Equipment cards keep their existing layout and scrolling.

## Evidence and follow up

- Build and regression output: `build/ui-consistency-build.log`.
- Native reports: `build/ui-consistency-broad-report.json`, `build/ui-consistency-minimum-report.json`, `build/ui-consistency-final-report.json`.
- Installation checksum and combined results: `build/ui-consistency-validation.json`.
- Native helpers: `build/ui-consistency-probe.java`, `build/ui-consistency-focused-probe.java`, `build/ui-consistency-final-targeted-probe.java`.
- Captures: `build/mount-modpack-smoke/ui-consistency-gallery/`.

The tested production JAR is installed in the Cranks Dungeons Testing profile. Restart Minecraft to load it. Multiplayer execution remains separate; use [the multiplayer checklist](MULTIPLAYER_TEST_CHECKLIST.md) with two clients before the approximately ten-player group tests.
