# Runic Altar

The Runic Altar uses the full loadout screen and an ornamented station panel.
Successful crafts modify the equipment in place. One selected tome is spent per
successful operation, drawn first from an existing stored stack in the altar and
then from normal inventory, unlocked bag storage and hotbar. Equipped bags, worn
gear and the spare armor set are never consumed as currency.

The Tomes tab lists carried tome stacks, searchable by name/effect, with a Usable
filter and explicit eligibility reasons. The Runes tab shows prefix/suffix
occupancy, tiers, rolled values, and any reserved artificial rune. Added or changed
runes are highlighted after a craft; feedback reports added, removed and changed
counts. Random rolls occur only on the server after application.
Hybrid runes display every component directly beneath their tier, with long text
wrapped rather than truncated. The Runes list supports wheel and scrollbar
dragging when needed; highlights cover the whole rune and hover targets stay
inside the visible list. Equipment changes reset the list to the top.
Values and range descriptions share a formatter with item/tome tooltips and the
Artificial Runic Altar. Numeric placeholders are substituted in sentences, leading
percent markers do not duplicate the unit, and family labels omit numeric tokens.
Multi-tier range descriptions use the full numeric bounds of the eligible tiers.

The Possible Runes tab is a read-only catalogue for the selected equipment. Search
matches every component of a rune family. The left column groups prefixes and
suffixes and marks existing families and full affix sides. Rune rows have no hover
tooltips, keeping the list unobstructed. The right column lists each source separately, with its eligible tiers
and all component value ranges. Both columns support wheel and scrollbar dragging.
Selected tome restricts the catalogue to that tome's outcomes, including minimum
rune levels, guaranteed targeted tiers, Divine's existing tiers, and the reserved
side of an artificial rune. Without this filter, targeted and artificial sources
remain visible for planning. Targeted sources retain their special tier and armor
affinity rules; artificial sources retain their own tiers and armor affinity.
Browsing never rolls values
or generates artificial options. The Minimum Rune Level label has a tooltip
distinguishing definition level from player level and rune tier.
Tome eligibility messages appear only in the Tomes tab; rune inspection tabs
retain crafting feedback without showing the selected tome's rarity requirements.

Empty station inputs use shared monochrome hints. Actual equipped items, chosen
bottles, recipe outputs and material costs retain their normal item textures.

Alchemy on an item with runes, Chaos, Annulment, and Artificial on a full item
require confirmation. Costs, rarity rules, rune weights, tier rules and tome
effects retain their previous behavior. Preflight eligibility checks consume no
random values and reject empty outcome pools. Failed rolls leave inputs unchanged.
An unrevealed artificial rune counts towards occupancy and cannot be overwritten
by an ordinary affix addition.

## Persistence and multiplayer

Block/item/entity registry identifiers and all three saved inventory slots are
unchanged. Equipment stays in slot 0. Previously stored tomes and results stay in
slots 1 and 2 and remain retrievable; stored tomes also appear in the available
tome list. A previous result blocks new crafting until collected. Closing the
screen preserves the station inventory. Breaking the block drops its contents as
before.

The block entity increments a persistent revision whenever its inventory changes.
Two synchronized 16-bit properties carry the full revision through vanilla screen
property packets. Apply requests contain the expected revision, equipment snapshot
and tome components. The server validates the current handler, player state,
station reach, revision, equipment, currency, eligibility and confirmation before
rolling. Stale or repeated requests consume nothing. Shared-altar viewers use the
same revision, preventing a confirmation from applying to another player's change.

The old standalone screen is replaced by `RunicAltarPanel` in
`PlayerLoadoutScreen`. `RunicAltarRules` shares non-random eligibility/effect
information between client and server. The existing stat-roll helpers continue to
produce outcomes and bake attributes; no extra combat or quality multipliers are
introduced.
