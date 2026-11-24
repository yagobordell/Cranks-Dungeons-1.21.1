# Supported potion, arrow, and enchantment policy

`SupportedContent` applies the same policy when an item is constructed, copied, decoded from NBT or packets, or edited through Minecraft's component APIs. `SupportedItemStackMixin` covers all constructors, both `apply` overloads, direct setters/removal, bulk component changes, and count updates. Ordinary non-potion/non-arrow stacks with no enchantments take a fast path.

## Supported content

- The 23 alchemy formula families, each with Lesser and regular tiers and drinking, splash, and lingering forms (138 formula/form combinations).
- Water in each of the six bottle forms, plus the three defined failed-experiment profiles in those forms.
- Normal arrows and the five regular Fletching Table arrows: Poison, Slowing, Weakening, Void, and Marking.
- The 18 Lesser/regular stat-enchantment definitions, Silk Touch, and Vein Mining. The fixed identifier allowlist is shared by filtering and station/trade eligibility; adding an unsupported enchantment to a Minecraft tag does not approve it.

## Normalization

Vanilla potion variants with a supported equivalent become that equivalent. Unsupported potion profiles become water, retaining bottle delivery and an existing custom bottle's tier. Arbitrary extra effects and colors cannot override a supported profile. Defined failures retain their exact finite effects, colors, and failed-mixture model.

Old combat arrows become their current counterparts, Spectral arrows become Marking arrows, and unsupported arrows become normal arrows. Arbitrary custom-effect arrows and foreign arrow types are subject to this policy too. Counts and unrelated metadata such as item names are retained.

Disallowed enchantments are removed from both regular and stored-enchantment components. Existing supported repeat copies remain intact. Empty enchanted books continue to follow the existing loot/creative filtering.

Registry entries remain available for loading Minecraft data; their presence in a registry or command autocomplete does not make their item contents obtainable. Actual item creation and mutation are validated, including command-created components. Other systems such as Ominous Bottles and status effects from non-potion sources are separate systems.

## Creative inventory

Creative tabs first build their raw entries in an isolated scope, then filter them before exposure. This prevents canonicalizing multiple old variants into the same entry while vanilla is building hashed sets. Display and search entries are checked. Server packet decoding still applies the normal policy to items taken from creative inventory.

## Verification

The isolated in-game probe supports `supported-content-only`. It tests every registered potion across vanilla and custom bottle forms, forged custom effects, every registered enchantment on gear/books, every potion as arrow contents, supported factories, NBT and packet decoding, nested shulker contents, charged crossbows, bulk component updates, generated chest loot, villager trades, real `/give` and `/enchant` commands, creative display/search entries, and existing cauldron/projectile behavior.
