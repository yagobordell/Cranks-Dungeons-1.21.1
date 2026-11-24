# Tailoring Bench

The vanilla loom opens the shared inventory and Tailoring Bench panel. It has no
profession restriction yet. Search, material search, Craftable, individual recipe
discovery, typed quantities and inventory transactions use the existing station
systems. Materials come from regular inventory, unlocked bag storage and hotbar;
equipped gear and equipped bags are never consumed as crafting ingredients.

## Recipe ownership

- Leather armor and leather horse armor retain their original ingredients/output.
- Bundle retains six rabbit hides and two string; it unlocks six player-storage
  slots when equipped, as before.
- Leads retain their original recipe.
- Saddle: five leather and two iron ingots for one saddle.
- All banner colors and craftable banner pattern templates move here.
- Leather dyeing, banner duplication and shield decoration move here.
- Applying banner patterns preserves the original loom choices, required pattern
  templates and six-layer limit. Templates are reusable.

These recipes are removed from hand/table crafting. Chainmail and metal equipment
remain at the Anvil; bows, crossbows and arrows remain at the Fletching Table.

The tabs are Leather Equipment, Bags, Accessories, Banners, Banner Patterns and
Dyeing (plus All). Tabs and recipes appear only when their required ingredient
types have been discovered by that player. Basic patterns require a discovered
banner and dye; special patterns additionally require their template. Discovering
one dye or banner color reveals the operation; selecting actual items determines
the output, with a preview before crafting.

Banner template recipe labels identify the design (Flower Pattern, Creeper Pattern,
etc.) in the recipe list and result heading. Pattern application shows a reusable
template note. Salvaging stays at the Crafting Table and Foundry.

## Implementation notes

`TailoringRecipes` reassigns shaped/shapeless and the three supported special
recipes to `tailoring_assembly`. Banner operations are generated from the banner
pattern registry and datapack tags, including template-item tags. Recipe reload
reads incoming tags through Minecraft's tag loader because registry tags are bound
after recipe loading. Recipe synchronization preserves station ownership and the
selected pattern/template ingredients.

`TailoringPatternRecipe` accepts exactly one banner, one dye, and its template
when required. It returns the template and copies the banner's existing components.
The common transaction planner previews the operation, checks storage capacity and
consumes all inputs atomically. Dyeing uses the vanilla dye-mixing calculation and
preserves equipment components, including name, damage, quality and enchantments.

Server crafting validates the open handler, station reach, recipe ownership,
discovery, selected-stack snapshots, quantity, input counts and output capacity.
