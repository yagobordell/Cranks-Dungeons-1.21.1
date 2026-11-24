# Cartography Table

The vanilla Cartography Table opens the shared player inventory and station panel.
Maps remain vanilla maps for exploration and decoration. This change adds no
treasure charts, expeditions, profession restrictions or minimap dependencies.

## Operations

- Create an empty map: the existing eight-paper and one-compass recipe.
- Copy a filled map: one empty map per new copy. The selected source map is retained.
  Copies share its map ID and data, as in vanilla.
- Expand a filled map: one paper per map, increasing the scale by one, up to four.
  Locked maps cannot be expanded. The expanded map gets a new ID; existing copies
  of the source keep their previous scale.
- Lock a filled map: one glass pane per map. The result gets a new, locked map ID
  with a copy of the terrain data. Existing copies of the source stay unlocked.

Empty-map crafting and map modifications are exclusive to this station. Paper and
compass crafting remain at their existing stations. Recipe discovery is individual
and uses each operation's required ingredients.

The panel includes Maps, Copy Maps, Expand Maps and Lock Maps categories, name and
material search, Craftable filtering, typed batch quantities and total output.
Operations require an explicit map choice; supporting materials are pulled from
regular inventory, unlocked bags and hotbar. The picker shows filled maps only.
The preview shows the selected map image, ID, scale, resulting coverage and lock
state. Expansion's thumbnail shows the current map; the larger area must be explored
again, following vanilla scaling behavior.

## Data and validation

`CartographyRecipe` represents the three operations inside `cartography_assembly`.
The common transaction planner preserves reusable source maps when copying,
checks available space, and commits inputs/results atomically. Scale and lock use
vanilla map post-processing, producing independent result map IDs without changing
existing copies. Each transformed map in a batch is finalized separately.

`LoadoutCartographyHandler` sends the player a vanilla map-data packet once per map
ID during the open session, including maps stored in bags. There is no per-frame
server request. Normal vanilla map packets continue to update held maps.

The server validates handler identity, station reach, discovery, the selected map's
components, map-state availability, quantity, costs, scale/lock restrictions and
output capacity. Recipe reload and synchronization retain station ownership.
