# Programmable Crafter

The vanilla Crafter opens the full player loadout and an ornamented station
panel. Its recipe list uses individual material discovery, Crafting Table
categories, recipe/material search and a Craftable filter. Craftable checks the
machine's nine material slots, not the player's items.

Select a recipe and choose **Program recipe**. Programming consumes nothing.
The recipe must currently belong to Hand Crafting or the Crafting Table; recipes
assigned to the other stations cannot be programmed. A player must know a recipe
to program it. Once saved, its operation is independent of that player's presence
or knowledge. Other players may supply materials or reprogram it with recipes
they know. There is no profession or profession XP system in this change.

Each rising redstone edge performs one recipe operation. Holding power does not
repeat it. Materials may be stacked or distributed anywhere in the buffer.
Unrelated materials are left untouched. The Crafter pulls nothing from players.
Hoppers can fill or empty its material buffer using normal inventory transfers.
The former grid-slot disabling and balancing rules are replaced by regular
material storage. Legacy input items survive this change.

For recipes with variants, place sample ingredients in the buffer before
programming. Each occupied slot describes one ingredient; its stack size is
reserve stock. The preview shows the result that will be saved. After programming,
the saved ingredients can be consolidated or moved freely, and unrelated stock
does not change the selected variant. Reprogram to change a variant.

The original front output handles insertion into inventories and ejection when
there is no room. Returned containers also leave through this output; reusable
originals remain in the buffer. Native timing, orientation, sounds, animation
and comparator inventory signals are retained. An unprogrammed machine never
falls back to shaped-grid crafting. Missing or reassigned recipes stop safely.

The program and variant are saved with the existing block entity. It runs while
its chunk is ticking, including with the programmer offline; it does not force
chunks to remain loaded. Nine non-interactive hidden slots synchronize variant
data to viewers and cannot be inserted into or taken from. Programming requests
validate the menu, distance, player, recipe discovery, input snapshot and program
revision. Input consumption and output preparation occur on the server thread.

## Verification

The build and isolated Minecraft checks passed for 364 fixed-output recipes,
individual discovery and station restrictions, unordered input allocation,
stale-request rejection, slot transfers and protected template synchronization.
Real redstone checks cover single pulses, sustained power, clearing the program,
hopper feeding, native front output and animation reset. Returned buckets and
full-destination ejection were checked for item conservation. Save/load tests
cover old disabled slots and exact variant templates. A separate full-world
restart verified persistent configuration and crafting with an offline
programmer fixture and an uninformed second viewer. These integrated-server
checks do not replace a multi-client dedicated-server playtest.
