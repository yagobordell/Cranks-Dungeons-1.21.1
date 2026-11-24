# Storage containers

Chests (single and double), trapped chests, barrels, Ender Chests, Shulker Boxes,
hoppers, droppers, dispensers, chest/hopper minecarts and chest boats open the
shared player loadout with a separate storage panel on the right.
The panel uses the existing dark, silver-bordered ornament style. It retains
the container's title, item icon and original slot count and arrangement. Hoppers
retain five slots in one row; droppers and dispensers retain a 3-by-3 grid.
The panel has no slot-count label. On narrow screens,
the Inventory/Storage button switches between the two panels.

Search highlights matching item names or registry IDs without moving or hiding
slots. All words in the query must match. Text-field focus is exposed to InvMove
in the same way as the station search fields.

## Transfers and sorting

Shift-click into player storage first merges compatible stacks throughout the
hotbar, regular inventory and unlocked bags, then fills empty slots in that
order. Shift-click from the player moves items into the container. Equipped
bags with occupied storage remain protected against removal.

The button beside search sorts only the open container. It merges stacks with
identical item components, then orders blocks, weapons, armor, tools, food,
potions and remaining items by category and name. Named items, potion contents,
durability, enchantments and runes remain attached to their original stacks.
Items with different component data never merge.

Sorting is computed and committed on the server. The request includes a bounded
snapshot of the visible contents. If a hopper or another viewer changes any
stack before the request is processed, sorting refuses the stale request and
asks the player to try again. It also checks the current menu, interaction
distance, player state, slot restrictions and that the cursor is empty.

## Native behavior

The original screen-handler factory still runs first, preserving locks and loot
generation. Its container slots and inventory are reused, and it remains
responsible for access checks and closing the container. Opening and closing
occur once. Viewer recounts recognize the new handler, retaining chest lids,
barrel open state, trapped-chest signals and Shulker animations. Ender Chest
storage remains per player. Native Shulker slots still reject nested boxes.

No inventory data migration is required. Container blocks and saved inventories
retain their vanilla identities. Automation continues to use those inventories
directly, including hopper transfers, dropper insertion and dispenser behaviors.
Sorting is an explicit action; opening or searching never changes slot order.
Vehicle containers retain their original loot generation, access checks and
contents. The Crafter has a separate material-based programming interface;
see [Automatic Crafting](AUTOMATIC_CRAFTING.md). Other menus, including mount
equipment, retain their existing handlers.

## Verification

`gradlew build` passes. The isolated Minecraft integration probe covers:

- All supported native storage kinds, titles, capacity and block icons.
- Component/count conservation, partial-stack merging and repeat-sort stability.
- Rejection of stale contents, held-cursor sorting, invalid menus and distant use.
- Player transfer priorities, filled inventories and occupied-bag protection.
- Cursor return on close, locked chests and generated chest loot.
- Independent Ender Chest inventories and Shulker nesting restrictions.
- Actual client/server sorting packets and tenth-hotbar-slot swaps.
- Hopper extraction while viewing, viewer recounts and barrel close behavior.
- Double-chest sorting across both halves and right-click splitting/placing.
- Search focus, rendered screens and compact inventory/storage switching.
- Five-slot and nine-slot sorting packets, transfers, locks and removed-block checks.
- Hopper push/pull, dropper insertion and dispenser arrows while menus are open.
- Chest boats and chest/hopper minecarts: original capacity, icons, sorting,
  transfers, distance/removal checks and compact view switching.

The isolated probe uses a local integrated server plus a second server-side
player fixture for shared-container and Ender Chest checks. It does not replace
a multi-client dedicated-server playtest.
