# Character stats progression

The shared inventory, skill-tree and world-tree stats panel begins with a
scrollable Progression section. It shows Player Level and World Level, each with
its own XP meter and current/required XP label. The meters use the shared HUD
frame and muted player-XP green/world-XP teal fills. Hovering shows the full
level and exact XP values.

Player progress reads `experienceLevel`, `experienceProgress` and the modded
`getNextLevelExperience()` curve, using the same rounding as the gameplay XP HUD.
World progress reads the local player's synced world-tree component and its
`getNextLevelXp()` threshold. At the world-level cap, the meter is full and reads
Max level instead of showing a zero denominator. No XP rules, level caps, rewards
or networking are changed.

The rows participate in stat search through player/world, level, experience, XP,
combat/gathering and progression keywords. Unrelated stat searches hide them.
They share the panel's existing scissor area and scrollbar rather than reducing
the permanent stats viewport. Rows read live values on every render, so no screen
reopening is required after XP changes.

The renderer separates progression tracks from ordinary stat rows so a profession
track can be added when its data exists. No profession mechanic or placeholder
row is introduced yet.

Build and regression checks passed. The isolated testing-modpack probe verified
the actual player/world XP curves and levels, live updates, maximum world level,
progression and ordinary stat searching, scrollbar dragging and rendering in the
inventory and both trees. Original test-world progression was restored afterward.
Results: `build/stats-progression-build.log`,
`build/stats-progression-integration.log`; screenshots:
`build/stats-progression-previews/`.

XP progression now uses the server-owned combat ledger and discovery-based world progression. See [PROGRESSION.md](PROGRESSION.md) for sources, caps, migration and configuration.
