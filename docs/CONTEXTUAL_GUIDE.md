# Compendium wiki and contextual references

The Compendium has one canonical directory and article source for the full wiki and contextual reader. Contextual help is a client-side layer on the current screen. It does not replace a screen handler, send slot actions, or mutate tree drafts. Closing it returns to the inventory, station or tree view underneath. Links inside the full wiki navigate in place instead of opening a second reader.

## Interaction

- Hold the configurable Show Details key (Shift by default), then move from a linked tooltip into its card to anchor it. Releasing the key clears the anchor and disables tooltip links; ordinary clicks pass through. Linked text gains an underline on hover; clicking opens its reference. Equipment names/icons, rarity, fields, runes and enchantments carry typed references.
- Statistics and effect tooltip headings, tree node titles and modifier names, station headings, recipe tooltips and authored article references are also linked.
- The guide provides search, Index, back/forward, section anchors and a scrollbar. `Escape` closes the guide first. `Alt+Left/Right` navigates history, `Ctrl+F` focuses search, `Tab` selects visible links, and `Enter` opens the selection. Page keys scroll.
- Scrolling inside an anchored equipment tooltip scrolls that card. Its existing `Alt+wheel` shortcut still works outside the card.
- Guide input is consumed before the underlying UI. Keyboard movement is suppressed while the layer is open, including when inventory movement mods are present. Show Details is a passive, physically polled binding and does not displace Sneak or another gameplay binding when they share a key.

## Explicit references

Use language-independent `GuideTarget` IDs. Supported kinds are `article`, `stat`, `rune`, `item`, `effect`, `enchantment`, `mob` and `recipe`. The key is everything after the first slash, so namespaces, recipe paths and article anchors survive round trips.

Resource article bodies and authored notes on nodes without bonuses support:

```text
[[stat/cranks-dungeons:crit_chance|Critical Chance]]
[[article/ailments#poison|Poison]]
[[item/minecraft:coal|Coal]]
```

Use `GuideText.link(Text, target)` for structured tooltip text and `GuideText.itemName(stack)` / `itemTarget(stack)` for items. Potion item references preserve the potion component, using `item/<item-id>~<potion-id>` internally. Do not infer references by matching translated prose or modify `ItemStack.getName()` globally.

`GuideInteraction.drawLinked` records rich text hit regions for custom panels; `GuideWidgets` handles station headings. Both observe scissor bounds. Direct station/article links work normally without holding Show Details. The tooltip renderer records only text actually drawn, preserving scroll clipping and the existing rarity styling. Events consume linked clicks and their releases before slot actions or tree allocation.

## Content and discovery

`GuideBook` serves the same resource articles and generated references to the overlay and full Compendium. Stat descriptions use shared runtime metadata and `GuideStatText`; rune ranges come from `StatRegistry`. The directory includes individually discovered items and learned potion variants. Adding future supported statistics also requires a meaningful description.

Ordinary recipe references check `RecipeDiscovery` before exposing their title, result or ingredients. Ingredient alternatives are limited to discovered items. Alchemy recipe references check `ALCHEMY_KNOWLEDGE`; ingredient discovery alone never reveals a formula. Item references may explain an item but cannot bypass these recipe checks. Personal recipe documents are generated on demand and are not globally cached.

Client resource reload clears article/directory caches and interaction state. Screen removal and disconnect also clear the layer and anchored tooltip. The reader does not alter progression, professions or recipe-unlock rules. The specialized-tome eligibility correction is described below.

## Validation

`GuideTargetRegression` covers all target kinds, namespace/path/anchor preservation, potion variants and invalid targets. It is included in the normal Gradle regression suite.

The isolated native modpack probe is retained under `build/contextual-guide-native-probe.java`, with captures in `build/mount-modpack-smoke/contextual-guide-gallery`. It verifies authored references, generated stat pages, recipe privacy, potion identity/knowledge, anchored tooltip clicks, navigation/search, input suppression, Escape, a real client/server crafting container, the full Compendium, and links across the full passive/world trees. Test helpers are not shipped in the production jar.

## Tree node descriptions

`NodeBonusFormat` formats additive tree bonuses with their actual units and conditional wording. `TreeNodeDescriptions` builds linked descriptions directly from a node's modifier map and selection amount. The tooltip and search index use these descriptions instead of older handwritten stat prose; there is no separate Related Statistics section. Passive attribute choices name Strength, Dexterity and Intelligence explicitly, and World Tree choices list their selectable bonuses. Empty graph nodes and class introductions retain their authored text.

The node editor makes stat descriptions read-only, updates its preview as modifier values change, and saves the generated plain description for compatibility with existing JSON and packet formats. Hovering its description field previews the full linked tooltip. No existing node IDs, bonuses, connections, coordinates or unlocks are migrated. `NodeBonusFormatRegression` covers flat, percentage, conditional, mount, negative and fractional bonuses.

## Player reference content

Articles should answer player questions: what changes, how to obtain it, where it applies, how to calculate a useful result, and which related choices matter. Do not explain numeric storage, internal attribute IDs, migrations or authoring details in player prose. A range or worked example is useful; a sentence merely explaining that percentages are percentages is not.

The wiki contains 58 authored system/station articles, 133 curated material and utility entries, and 82 creature entries, plus live equipment, statistic, rune, effect, enchantment and personally learned potion references. Creature knowledge is available from the beginning. Item navigation preserves personal discoveries; direct references from another article can still explain an item. Neither route unlocks a recipe.

`GuideRuneArticles` reads live rune definitions, compatible equipment, tier ranges and affix families. Both guide views share its compact rune catalogue. Hybrid components remain one rune. `GuideTomes` uses the altar's actual targeting rules and `RuneCrafting.targetedTiers`. Specialized outcomes use the equipment material tier. Eligibility is presented as equipment categories and applicable defence affinities, not lists of individual material bases. Specialized tomes retain a short tooltip; the linked item name opens their article. The article groups identical rune/affix outcomes into one row, with individual equipment-category links. Rune pages link back to each supplying tome. Fixed Unique properties are listed separately from ordinary random rolls.

`GuideWriting`, `GuideStatArticles`, `GuideEffectArticles` and `GuideEnchantmentArticles` provide useful explanations, calculations, sources and related links. Enchantment per-copy bonuses are read through compatible equipment and the real enchantment modifier pipeline. Effect/item pages respect individually learned potion formulas. Search caches only public facts, clears on resource/world changes, and never caches personal recipe or formula documents.

## Facts and illustrations

Resource-backed catalogues live in `assets/cranks-dungeons/compendium/reference/`:

- `items.json`: authored purpose, acquisition routes and general uses. Recipe ingredients and quantities belong in discovery-gated recipe references, not a repeated unlocked-recipe wall.
- `creatures.json`: habitat/encounter notes, behaviour, and ordinary drop IDs/conditions. The initial habitat/drop snapshot comes from Minecraft 1.21.1 data plus this repository's overrides. Coded exceptions, such as the Wither's Nether Star and carried Drowned equipment, are included explicitly. Other mods/datapacks can alter spawns and drops; maintain or override these facts with their changes.
- `fixed_runes.json`: fixed Unique rune IDs and actual supplied tiers, maintained alongside Unique initialization. Fixed Unique properties do not use the ordinary base-material cap.

Creature base statistics use the registered species defaults. Individual equipment, difficulty modifiers and mount genetics can alter actual values. Thematic tome sources and equipment-based tiers use the same definitions as gameplay; a creature's appearance alone does not imply a drop. Thematic loot behavior was not changed for this feature.

Use `[[mob/minecraft:stray|Stray]]` for creature links. Use an `item_image` article block with the item key as its value to draw the real item, for example `minecraft:iron_ingot` or a potion-variant key. Both guide views draw the active resource pack's item model in a compact framed illustration. This does not introduce new item textures.

The native probe audits the expanded directory, all reachable rune articles, specialized tome links and eligible bases, creature/drop IDs, illustrations and contextual targets. It also checks that Bowls are not classified as Bows, alongside the existing recipe privacy, tooltip, navigation, container and tree checks. The normal regression suite includes player-prose and calculation checks in `GuideWritingRegression`.

## Canonical subjects and navigation

`GuideTopics.resolve` is the single alias router. Its output is idempotent. Legacy rune/equipment links stay valid, but the directory deduplicates their canonical roots:

- Helmet, Chestplate, Leggings, Boots and weapon/tool categories each have one equipment article. Material variants are sections inside it, with real native statistics, durability, requirements, repair materials and enchantment-slot limits. Unique items keep their own fixed-property article.
- Single-stat rune links open the corresponding statistic article. It contains the mechanic, relevant modifiers/calculations, compatible rune families and tier tables. Hybrid runes keep one family article with all linked components. Identical equipment ranges share a row; consecutive tiers with identical values are combined. Fixed Unique-only properties are never advertised as ordinary rolls.
- Normal/Greater specialized tomes share an article with separate variant sections. Lesser/regular enchantments and potion families also share their topic. Potion delivery and personally learned formula links appear within the family article.
- `GuideBook.index` is the source of the full reader and contextual search. Public facts are searchable; private formula documents are not cached. A reload invalidates generated content and search caches.

`GuidePageLayout` renders both readers. It provides real-item illustrations, introductory definitions, a clickable contents list, named section anchors, alternating table rows, rich-text links and responsive wrapping. Tables with several columns become stacked records at narrow widths. The full wiki preserves visited pages and scroll positions with back/forward controls and Alt+Left/Right; contextual references preserve their own history.

`GuideEquipmentArticles`, `GuideStatArticles`, `GuideRuneArticles`, `GuideTomeArticles`, `GuidePotionArticles` and `GuideEnchantmentArticles` generate category-appropriate content. Resource articles provide system explanations; they must not duplicate every equipment/rune permutation. Removed redundant advice paragraphs should not be restored as automatically appended boilerplate.

## Specialized tome summaries and gameplay

`AbstractTomeItem.summaries` is shared by item tooltips and wiki articles. It emits only applicable coarse categories: Armour, 1H Martial Weapons and 2H Martial Weapons. Distinct armour outcomes such as Swiftness on chestplates versus boots retain their slot-category labels. Each tooltip has its general description followed by those concise linked outcomes, without a per-slot rune directory or obsolete fixed T4/T1–T3 ranges.

Greater Flames, Ice and Electricity target extra elemental damage on weapons, not armour resistance. Normal elemental tomes retain their armour resistance outcomes. Greater Ruin retains Physical Damage Taken as Void on armour; Greater Abrasion retains its existing armour/weapon mappings. Neither exception is generalized into the other Greater elemental tomes.

All specialized outcomes use `RuneTiers.best(gear)`, including paid replacement proposals. Early material bases therefore cannot receive high-tier specialized runes. Pending comparisons and saved proposals retain the normal one-tome payment and keep/replace behavior.

The isolated native wiki probe covers all 268 legal specialized tome/base combinations: 4,288 add/replacement rolls and 536 paid altar transactions. It checks exact T10/T9/T8/T7 outcomes, one-tome consumption, pending comparisons and saved proposals accepted later. It also audits canonical aliases and links, fixed Unique separation, table bounds at widths 90/210/430, independent recipe/formula privacy, real station handler preservation, full-tree links, editor updates, in-place navigation/history and captures at GUI scales 2 and 3. Test helpers stay outside the production jar.

Shared equipment illustrations use `item_image` style `caption=<category title>`; the chosen example item never names the category article. `GuideTopics.title` resolves aliases before labeling search results, including potion/tome variants. Full-reader headers use the loaded document title. The native probe audits aliased item/potion/enchantment/rune titles, equipment section anchors and captions, and clicks the Wooden Sword tooltip name in both readers.
