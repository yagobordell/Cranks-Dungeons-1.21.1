# Rune crafting and equipment progression

Current caps are T10 for wood, stone, leather, shields and bows; T9 for iron,
chainmail, gold and crossbows; T8 for diamond; and T7 for netherite. T1 is
strongest. Material/class checks replace registry-name substring matching.
Unknown bases default to T10. Optional `cranks-dungeons:runes/tier_7` through
`tier_10` item tags support explicitly assigned future bases.

Every new random, targeted, artificial, fixed-tier drop and command-added rune
uses the same cap. Fixed Unique presets are preserved. Targeted-only extra-damage
and physical-taken-as-Void definitions now scale their numeric ranges by tier.
Already saved ordinary runes are not silently rerolled by viewing equipment;
new crafts and value rerolls obey the cap. Saved over-cap artificial options
are converted once, with their capped numeric values saved on the item.

Random crafting selects a family, a definition within the family, then a tier.
Family weight is the maximum eligible definition weight in the group. Existing
families are excluded before selection. Greater random tomes exclude T10 where
possible; Perfect excludes T9 and T10 where possible. Neither bypasses the cap
or filters build families by the legacy minimum-level field. Numeric values
remain uniformly sampled within the displayed tier ranges.

Chaos replaces a selected rune on the same affix side. Annulment removes a
selected rune. Divine rerolls one selected rune's values. Targeted tomes work
on Magic and Rare gear, adding or replacing their family. Alchemy also reforges
Rare equipment. Artificial Tome replacements use an explicitly selected rune.
An entry with `protected: true` inside `CustomStats` cannot be selected and is
preserved by Alchemy, supporting future signature properties. Existing fixed
Uniques retain their existing tome restrictions.

Paid replacements store `RunicProposal` on the equipment. The proposal contains
only before/after rune state, not another extractable equipment stack. One tome
is consumed at proposal generation. Keep current and Use new resolve the same
paid result with no refund. The original runes remain active until acceptance.
Closing a screen, moving the item, saving or reopening preserves the proposal.
No new rune operation is allowed until it is resolved. Accepting changes only
rune state, preserving damage, names, enchantments, quality and unrelated data.
All requests validate the handler, reach, revision and expected server item;
stale or replayed requests cannot spend another tome or choose a second result.

The custom inventory includes a larger Runic Altar panel, a scrollable target
selector and a two-column paid comparison. Possible Runes uses the same tier
policy as crafting. The Compendium and tome descriptions document the new rules.

Validation sources: `RuneRollRulesRegression` and
`build/rune-crafting-live-probe.java`. Native checks cover 33 equipment bases,
all random tome variants, 340 targeted operations, artificial generation/reveal
and saved-choice migration, fixed-tier drops, catalogue caps, independent family
weights, selected changes, protected properties, one-time payment, item NBT
round trips, stale/replayed operations and unlocked/locked bag payment. UI checks
exercise the actual confirmation, network roll and keep/use actions.

Final evidence: `build/rune-crafting-verified-build.log` and
`build/rune-crafting-verified-native-results.log`. Visual captures are
`build/runic-paid-comparison.png`, `build/runic-chosen-rune.png` and
`build/runic-paid-confirmation.png`. Build, regression and native checks passed;
all four tree files still match their pre-review SHA-256 baseline.

The follow-up UI polish groups comparisons and selectors into Prefixes then
Suffixes, preserving tooltip order within each group and the original server
indices. Selector headings are not actionable. Full targeted affix sides are
identified explicitly. Payment confirmation explains that cancellation keeps
the tome; rolling spends it even when the original runes are retained afterward.
Creative catalogue cells accept held-item disposal, including empty cells and
drag-release, with scrollbar and text-selection drags excluded. Existing server
checks reject Survival use and stale cursor snapshots.

Follow-up validation passed in `build/altar-creative-polish-verified-build.log`
and `build/altar-creative-polish-verified-native-results.log`. The native fixture
is `build/altar-creative-polish-live-probe.java`; it exercises grouped row clicks,
heading clicks, cancellation, both full-side messages, result choices and
creative disposal through the actual custom screens.
