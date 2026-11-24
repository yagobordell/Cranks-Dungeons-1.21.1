# Fletching Table

The Fletching Table uses the inventory station panel, including category tabs, name and material search, Craftable filtering, typed batch quantities, and total output. It consumes materials from regular inventory, unlocked bag storage, and the ten-slot hotbar. Output merges existing stacks before using empty hotbar, inventory, and bag slots.

Bow, crossbow, and normal arrow recipes retain their original ingredients and output, but are exclusive to this station. The old spectral-arrow and potion-tipping recipes are retired. No profession restrictions are implemented yet.

## Component arrows

Each batch uses **8 normal arrows + 1 component** and produces **8 special arrows**. There are no Lesser tiers, potion ingredients, or suspension ingredients.

| Arrow | Component | Effect on an accepted, unblocked hit |
| --- | --- | --- |
| Poison | Spider Eye Extract | Two Poison stacks, six seconds per stack before duration modifiers; uses the existing Poison stack cap and damage rules. |
| Slowing | Mushroom Extract | 30% reduced movement speed for four seconds. |
| Weakening | Fermented Eye Extract | 20% reduced physical damage for five seconds. |
| Void | Amethyst Essence | Adds 30 percentage points of physical-to-Void conversion to the shot, sharing the existing 100% conversion limit. |
| Marking | Glowing Catalyst | Outlines the target through obstacles for ten seconds. |

Slowing and Weakening refresh rather than accumulating strength. Void is part of the ordinary damage breakdown, so it retains projectile draw scaling, applies resistances once, affects undead as damage, and uses the existing weighted Energy Shield cost. Other physical conversions share the same normalization; conversion cannot create extra base damage.

The five profiles reuse vanilla tipped-arrow art and rendering. Their internal potion components are ammunition data only; they are excluded from drinkable creative potions. The creative Combat tab contains the five regular variants.

## Discovery and compatibility

Each player's recipe visibility follows their own obtained ingredient types. Learning how to brew a potion is not required to craft an arrow from an obtained component.

Existing Poison, Slowness, Weakness, and Harming arrows migrate to Poison, Slowing, Weakening, and Void arrows respectively. Spectral arrows become Marking arrows. Other retired vanilla/mod-potion variants become normal arrows, preserving count. Normalization runs on item construction and component updates, including NBT/packet decoding, as well as the existing pickup, inventory, loot, and projectile paths. Stray-created slowing ammunition also uses the new profile. Custom effects, other potion namespaces, and foreign arrow types cannot bypass the supported catalogue; see `SUPPORTED_CONTENT.md`.

## Verification

The isolated in-game probe at `build/alchemy-smoke/test/AlchemySmoke.java` has a `fletching-only` mode. It checks native station opening, recipe exclusivity and discovery, all five component recipes, multi-stack crafting across inventory sections, output-space rollback, wrong-station rejection, arrow migration/save data, projectile effects and conversion limits, blocked/evaded hits, Stray arrows, creative entries, and the real client search/filter/quantity/network flow.
