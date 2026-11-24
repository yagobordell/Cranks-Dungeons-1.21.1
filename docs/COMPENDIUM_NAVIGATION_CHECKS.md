# Compendium names and navigation

Verified on 2026-11-24 using an isolated copy of the testing modpack.

## Directory and effect articles

- Electrocute is translated in the effect registry, directory and article heading.
- All 50 registered effects were audited for readable names; all of the mod's effects have translations.
- All 545 available canonical directory topics were loaded and checked for missing articles, untranslated directory labels and duplicate titles within a category. No duplicates remained.
- Vanilla and custom Poison links both resolve to the same Poison article. It retains combat scaling, potion/arrow application, potion sources and creature encounters.
- The sidebar label and tooltip both say **Compendium**.

## Back, Forward and directory context

- Equipment → Helmet → Back restores Equipment and its scroll position.
- Helmet → Index → Runes and Enchantments → Body Enchantment → Back restores Runes and Enchantments rather than Helmet.
- A searched article returns to its exact query, visible search field, results and scroll position.
- Forward restores the corresponding article or directory view.
- Editing a query does not create one history entry per keystroke; navigating after Back discards the abandoned forward branch.
- Resource reloads refresh category data while retaining a valid return destination.
- Changing GUI scale retains the query and navigation state. Screenshots were inspected at GUI scales 2 and 3.

The Gradle build and regression suite passed. The native client probe completed **1,179 checks** without failures.

Local verification artifacts:

- `build/compendium-navigation-build.log`
- `build/compendium-navigation-probe.java`
- `build/mount-modpack-smoke/alchemy.log` (`[CompendiumNav] COMPLETE`)
- `build/mount-modpack-smoke/compendium-navigation-gallery/`
