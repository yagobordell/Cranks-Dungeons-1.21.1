# Artificial Runic Altar

The Artificial Runic Altar uses the shared loadout inventory, including all ten
hotbar slots, bag equipment/storage, armor sets and stats. Its taller station
panel adapts to compact screens and uses the existing dark ornamented style.
Registry IDs and the single saved equipment slot are unchanged.

Place Rare, supported equipment with an unrevealed Artificial rune in the input.
The server generates up to three nonduplicate, compatible choices on the reserved
affix side. Each receives a T1, T2 or T3 tier and exact component values. Existing
choice IDs, tiers and values remain on the item across removal, other altars,
closing, breaking the block, save/load and restart. Older choices without numeric
values are migrated once, preserving their IDs and tiers. Invalid saved choices
are not regenerated; the marker remains available for recovery.

Choice cards show every component, its exact value and its tier range. Long
cards wrap and the list supports wheel and scrollbar dragging. Selecting a card
only builds an equipment copy using the stored values; it never rolls or modifies
the real item. Hover the preview item to inspect its complete equipment tooltip.
Reveal rune opens confirmation showing every selected stat. Cancelling keeps all
choices. Confirming modifies the input in place, removes the cryptic marker and
stored alternatives, and shows the revealed rune. No additional items are spent.
The original apply sound plays once after a successful server response; preview,
cancellation, rejected requests and repeated responses do not trigger it.

Apply requests contain handler ID, request ID, selected index, confirmation,
equipment snapshot and a persistent 32-bit altar revision synchronized through
two screen properties. The server checks the current handler, player state,
reach, revision, equipment, choice validity, affix capacity and duplicate group.
Replayed or stale requests cannot reveal a different player's equipment. Legacy
screen-button packets no longer select choices; armor swapping uses the shared
loadout behavior. Full choice data uses normal item-slot synchronization instead
of the old first-component-only text packet.
