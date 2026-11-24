# Defence, recovery and passive-stat review

This pass preserves attribute IDs, allocated nodes, tree connections and existing
equipment. It does not add skills, professions or new equipment. Armour penalties
still use effective armour (including Iron Reflexes); animal kills still grant
on-kill recovery.

## Defence rules

- Physical armour reduction is `armour / (armour + 10 * hit damage)`, capped at
  90%. The previous hit factor was 12. Large hits still penetrate more effectively.
- Automatic evasion is `rating / (rating + max(1, 8 * hit damage))`, capped at
  75%. Entropy is reset after six seconds without an eligible attempt, not by
  unrelated energy-shield damage timestamps. The extra bias after failed rolls
  is removed. A short Evaded message, quiet sound and three particles identify it.
- Active frontal blocks subtract block value before mitigation, capped at 85%
  of the incoming hit. Ordinary armour/resistance mitigation then applies.
  Resolute Technique bypasses both the custom and vanilla shield block.
- Critical Protection displays the combined reduction to critical **bonus**
  damage. The legacy armour-based specialist stat remains compatible.
- Full-health knockback protection checks health before the hit. Leggings armour
  bonuses refresh when the actual leggings change, including requirement penalties.
- Thorns uses the aggregated entity attribute instead of scanning raw rune rolls.
- Energy-shield recharge remains 12.5%/second after four seconds by default;
  specialist bonuses cannot reduce delay below one second or raise recharge above
  50%/second. Ghost Reaver disables recharge; Crimson Pact disables shield use.
- Armour applying to elemental damage is a late-game specialist roll (minimum
  rune level 80, weight 200); old equipment retains its stat and values.
  It applies to Fire, Cold and Lightning, not Void.

## Recovery

- Effective health regeneration is `(flat + maxHealth * min(percent, 5%))`,
  multiplied by the moving-health-regeneration bonus when moving. The total is
  capped at 10% of maximum health per second. Vaal Pact disables regeneration;
  Zealot's Oath redirects it to shield.
- Life and mana leech recover physical damage actually dealt to life. Each hit
  contributes at most 10% of the relevant maximum resource, the shared pool holds
  at most 20%, and recovery is at most 10%/second. Clipped recovery is not removed
  from the pool. Full resources discard ordinary pending leech.
- Vaal Pact remains instant, with a shared token budget that replenishes by 10%
  of maximum life (or shield for Ghost Reaver) each second. Repeated hits cannot
  obtain a fresh budget. Existing regeneration/on-kill tradeoffs remain.
- Ghost Reaver uses shield capacity for life-leech limits and synchronizes shield
  recovery to nearby clients. Resource-mode changes discard incompatible pools.
- Mana regeneration includes both flat and percentage sources, then regeneration
  and broad recovery multipliers. Moving regeneration affects health only.
  The legacy flat maximum-mana bonus is supported. Skill costs remain future work.
- On-kill health/mana recovery remains immediate and still includes animals.

## Projectile and tree mechanics

- Terrain Ricochet rolls once on the server. Arrows and thrown tridents can
  redirect toward the nearest visible enemy within 12 blocks of the impact.
  No valid target means an ordinary terrain impact. Owned tame animals, the
  shooter, teammates and party members are excluded. Passive creatures are not
  automatically targeted unless engaged with the shooter. Player targets follow
  server PvP rules. Redirects cannot turn through the impacted wall.
- A projectile has at most four terrain redirects. Previously hit targets are
  excluded; counters and initialization are saved, preventing reload rerolls.
  Close targets are not skipped by the remainder of the original flight tick.
- Arrow piercing stacks with existing piercing. Tridents retain their native
  one-hit/return behaviour. Launch-speed bonuses use the shared launch path once,
  covering bows, crossbows and thrown projectiles.
- Positive attack-speed bonuses shorten melee cooldowns. Better Combat uses this
  cooldown path. Crossbow pull time cannot round down to zero ticks.
- Recent-critical and Elemental Overload bonuses begin only after a critical hit
  actually damages life or shield. Misses do not produce critical-hit feedback.
  History is cleared on unload/server shutdown and cannot carry across clocks.
- Increased Ailment Chance is described as Poison, Bleed and Shock chance;
  freeze/electrocute buildup retain their separate modifiers.
- Armour Penetration increases existing flat penetration; it is not a percentage
  of the target's armour. Descriptions explain the distinction.

The shared stats panel uses the gameplay curves, includes effective attack speed,
flat mana regeneration, regeneration limits and critical protection, and formats
duration reductions/conversion fractions as percentages. Known shipped node text
is updated on loading old configurations without changing layouts or custom text.
New item-rune ranges and class base attributes are otherwise preserved.

## Validation

`gradlew build --offline --no-daemon` includes the permanent combat-curve and
recovery-pool regression tests alongside the existing regression suite.

The isolated testing-modpack probe exercises actual passive allocations (141
fixed-stat nodes, 51 distinct attributes), native damage, evasion frequency,
blocking, thorns, conditional knockback, life/mana recovery, animal kills,
maximum-health conversions, changing leggings, armour-based movement, Iron
Reflexes, Better Combat cooldowns, projectile damage scaling, piercing and save
data. A live arrow redirects from terrain into a visible enemy while ignoring a
closer enemy behind cover. Party/PvP/pet/owned-mount exclusions are checked too.
The currency/trading integration suite covers reward eligibility, party splits,
personal chest claims, safe exchanges, save recovery and death rules.

Results are retained in `build/stat-review-integration-results.log` and
`build/stat-review-economy-results.log`; the current native probe is
`build/stat-review-live-probe.java`. These are an initial balance baseline, not
a claim that future bosses, skills or every third-party mod combination are
balanced. All test-world changes are confined to the isolated modpack copy.


## Offensive stats and ailments — November 22, 2026

- Thrown-trident adjustments apply only to flat damage, not critical multiplier,
  application chances, or other dimensionless attributes. The stats panel uses
  the held weapon's effective critical chance and accounts for Elemental Overload.
- Elemental penetration covers Fire, Cold and Lightning; Void has its own
  penetration. Relative penetration is capped at 100% and does not deepen negative
  resistance. Both resistance helper paths share the same calculation.
- A successful hit to either life or Energy Shield can apply ailments. Avoided and
  rejected hits cannot. Leech still uses only the physical portion reaching life.
- Burn and Poison store offensive damage per second before target mitigation.
  Their ticks apply the relevant resistance once. Magnitude increases damage,
  not duration; base durations remain four and two seconds respectively. Burn
  starts at 50% of offensive Fire damage per second; Poison starts at 20% of
  offensive Physical + Void damage per second. Partial final ticks conserve DPS.
- Poison applications keep separate damage, owner and expiry. Base cap remains
  20, with up to 100 extra stacks from the existing stat. At the cap, new equal or
  stronger applications can replace the weakest without refreshing other stacks.
  Tick damage is grouped by owner to reduce native damage calls.
- Bleed retains its five-second delayed burst. It stores 40% of later resolved hit
  damage, including shield damage, multiplied by Bleed Magnitude and capped at
  100%. Reapplication never postpones the fuse or erases stored damage. The burst
  bypasses shield and is not mitigated twice. Ailment ticks cannot feed it.
- Positive Poison/Bleed chance still guarantees their application on critical
  hits. Their minimum damage requirements still apply.
- Chill has one movement modifier, capped at 90%; it never removes unrelated
  Slowness. Freeze/Electrocute stop locomotion and actions while normal entity,
  status-effect, damage-immunity, recovery and death timers continue. They cannot
  overlap and share a one-second recovery window. Animation time is held only in
  rendering, without freezing the entity's logical age.
- Shock lasts a base ten seconds; magnitude affects amplification, capped at 50%.
  Arcs retain unlimited valid enemies within ten blocks, with line of sight and
  party/team/PvP/pet protections. They preserve ownership, apply each target's
  Lightning mitigation and penetration once, and never reroll offensive bonuses,
  criticals, leech or recursive chains. Burn spread uses equivalent targeting
  protections at three blocks and retains its five-generation limit.
- Every damaging tick uses native damage, including nonlethal ticks, so
  invulnerability, permissions, attribution and totem/death-prevention hooks apply.
  Self-applied effects remain functional while in a party. Ordinary generic
  damage is no longer reclassified as Fire merely because the target is burning.
- Applications, source IDs, tick progress, stored Bleed damage, buildup and control
  recovery are saved with the entity. Unloading releases runtime state without
  curing saved ailments; dimension travel preserves player state. Client details
  are cleared on entity unload, world changes and disconnect.
- Nameplate tiles increase from 10 to 16 GUI pixels; their symbols increase from
  6 to 12 pixels. Stack/buildup counters have a separate lower area. Flammability
  reuses the existing flame symbol with a percentage, so no new texture is needed.
  Active-player tooltips show synchronized damage, Shock magnitude and stored
  Bleed values. Item/potion previews keep generic descriptions.

The permanent `AilmentRulesRegression` checks timing, independent owners, DPS,
partial ticks, wet Burn and bounded combat arithmetic. Live fixtures are retained
in `build/offence-ailment-live-probe.java` and
`build/offence-ailment-visual-probe.java`; evidence is written to the corresponding
integration/visual result logs under `build`. Stress timings are fixture-specific,
not a guarantee about overall frame rate or arbitrary modpack combinations.


## Ailment timers, feedback and counterplay — November 22, 2026

- Nameplates show a locally advancing countdown from the latest server snapshot.
  Poison counts down to the next independent stack expiry; the next packet updates
  both count and expiry when a stack ends. Client world ticks drive interpolation,
  so pausing single-player also pauses the displayed countdown. Gameplay expiry
  remains authoritative on the server.
- Symbols, counters and finite timers occupy separate areas. Infinite effects
  reserve no timer footer; their stack count still appears if needed. Short
  durations show tenths of a second; longer ones use seconds/minutes. Text scales
  to the available width, including three-digit stack counts.
- Buildup symbols render at 48% brightness; active effects retain full colour.
  Shader colour is restored after muted icons, with no extra tint flush for active
  symbols. Timer formatting avoids per-frame Formatter allocation.
- A short Resisted nameplate label appears when recovery rejects Freeze or
  Electrocute buildup/application. The server sends at most one visual event per
  target recovery window to tracking players and the affected player. Existing
  active control does not generate this event. Entity UUID checks, expiry, world
  changes and unload cleanup prevent stale feedback on reused entity IDs.
- At the user's selected baseline, Fire hit damage equal to 25% of effective
  ailment threshold builds Burn (coefficient 4, previously 0.2). Resistance still
  reduces the contributing hit; threshold and Flammability modifiers still work.
  Burn DPS, duration, spread and critical rules are otherwise preserved.
- Existing counterplay is documented in active-effect tooltips and the Compendium:
  resistance potions, reduced durations, wet Burn and milk. Native tests verify
  milk clears applications, buildup, stored Bleed and beneficial effects, and
  verify Fire/Void resistance effect modifiers and the resulting damage reduction.
  No new cleansing items or recipe/profession requirements were introduced.

Permanent presentation regression checks cover finite/infinite footer sizes,
clock changes, paused clocks and timer formatting. Live evidence is retained in
`build/ailment-ui-integration-results.log` and `build/ailment-ui-visual-results.log`.
The fixtures are `build/ailment-ui-live-probe.java` and
`build/ailment-ui-visual-probe.java`.

The balance baseline uses explicit synthetic profiles: 100/500/1000 health,
0/250/300 armour, 0%/25%/50% Fire and Void resistance, and matching
10/40/30-damage hits at two hits per second. The native maximum and starting health
are asserted, rather than assuming an attribute accepts an arbitrary value.
Burn, guaranteed Poison and guaranteed Bleed are compared over up to 12 seconds.
The ordinary profile dies in 4.05/3.55/4.55 seconds respectively; the elite in
7.05/7.05/8.05 seconds; the durable profile takes 390/495/468 damage over 12 seconds.
These isolated single-target examples do not price investment, model boss AI,
measure Burn spreading, or establish balance for future bosses and skills. They
support the Burn ignition fix without justifying further broad stat nerfs.

## Ailment visual effects — November 22, 2026

- Freeze replaces the oversized enclosing ice block with a faint model-aligned
  frost layer and five small, angled vanilla-ice crystals (three at distance).
  Formation grows over five ticks; release emits a short ice-shard burst.
  First-person frost opacity is reduced to 20%.
- Electrocute replaces the full charged-creeper skin with short white-blue arcs
  around the body. Application has a brief stronger arc cue; ongoing arcs are
  intermittent, depth-tested and stable between animation steps.
- Burn keeps small vanilla flames, without large smoke. Poison uses small bubbles
  and falling liquid droplets. Bleed replaces Raid Omen drips and oversized bright
  splashes with small, darker gravity-driven droplets and a distinct release burst.
- Confirmed Bleed hits and successful releases send an independent S2C visual
  event with the victim UUID. Damage-number preferences do not affect it, and
  Poison/Burn ticks cannot produce hit splashes. Existing damage arithmetic,
  application chances, durations and attribution are unchanged.
- Chill, Flammability and Electrocute buildup have restrained, increasingly
  frequent cues near their thresholds. Shock keeps intermittent small sparks;
  its repeated ambient sound is removed to avoid crowd audio stacking.
- Body particles skip the first-person camera entity, invisible/obscured targets
  and targets beyond 32 blocks. Spawn positions also avoid the camera. Brief,
  low-opacity side-edge pulses indicate new player ailments without covering the
  crosshair. Existing status icons remain the persistent first-person indicator.
- This manager caps total emission at 48 particles per client world tick, with
  six ambient particles / twelve event particles per entity. Distance reduces
  ambient frequency; entity-specific phases stagger crowds. Visual state and
  budgets reset across world changes, and removed entities release their state.
- Existing vanilla and mod textures are reused; no new texture assets or shader
  dependencies are introduced.

Permanent regression checks cover staggered cadence, buildup intensity and edge
pulse timing. Native fixtures are retained under `build/ailment-vfx-*-probe.java`.
The full build/regression suite and native combat integration pass. Visual checks
verify falling droplets, rising bubbles, short lifetimes and emissive electrical
sparks, first-person particle suppression, reduced frost, UUID guards and unload
cleanup. A 60-entity multi-ailment scene reaches the 48-particle global cap without
exceeding twelve event particles per entity. This verifies emission bounds, not a
universal frame-rate guarantee. Evidence: `build/ailment-vfx-release-build.log`,
`build/ailment-vfx-integration-results.log`, `build/ailment-vfx-visual-results.log`.

### Visibility pass and native captures

- Active Burn uses modest full-size vanilla flame particles; Flammability keeps
  the smaller ember cue. Idle Bleed has more legible falling drops, and humanoid
  torsos reuse the existing `bleed_cut` sprite as a small pose-aligned wound.
- Poison has three small, staggered surface bubbles plus occasional falling
  droplets. Shock has two small animated surface crackles. These sprites remain
  readable while entities move and between particle bursts; redundant ambient
  bubble/spark emissions are removed. Beyond 16 blocks each uses one sprite.
- Electrocute has two or three ongoing arcs and four on application, with one
  arc on the visible side. Broad/low and animal models receive extra clearance
  so discharges are not swallowed by their geometry. Distant entities use one
  arc. A permanent regression checks that no animation phase turns the effect off.
- Ice formations are tapered four-face crystals using vanilla ice pixels rather
  than rectangular panels. The subtle frost layer and small release shards remain.
- F1 now hides projected nameplates along with the rest of the HUD.
- The gallery fixture uses normal rendering at approximately three-block viewing
  distance, with the interface hidden. It captures several ordinary animation
  frames for each effect, Bleed's release, real-duration Electrocute, and animal
  examples. Long-held zero-damage applications isolate the ongoing appearance
  from hurt flashes; production durations and damage remain unchanged.

Fixture: `build/ailment-vfx-gallery-probe.java`. Build and native evidence are
recorded in `build/ailment-gallery-build.log` and gallery result logs.
Final native gallery checks confirm synchronized state for all six ailments and
both animal examples. The final safety pass verifies particle factories, first-
person suppression, UUID/unload guards and the 60-entity emission limits. Evidence:
`build/ailment-gallery-final-results.log`, `build/ailment-gallery-safety-results.log`.
Selected unedited framebuffer captures are in `build/ailment-vfx-captures/`.

### User-directed VFX revision

- Burn's steady flame emission doubles, with a small initial flame burst.
- Bleed's wound decal is removed. Droplets increase in size; confirmed hits emit
  six outward-moving drops and successful delayed releases emit twelve. Existing
  server hit/release events remain separate from DoT damage-number messages.
- Poison removes all surface bubbles and the bubble application cue. Larger,
  more frequent droplets start in a narrow source at head/face height rather than
  around the torso. First-person camera protection still applies.
- Freeze uses a continuous pale ice coat plus crack detail, extending slightly
  beyond the posed model, with the existing small ice crystals. Biped torso depth
  is expanded locally during overlay rendering and restored in `finally` to
  prevent the coat falling inside translated/animated torsos.
- Electrocute returns to Minecraft's animated charged-creeper overlay with cyan
  crackles. Shock keeps its crackles and receives a softer version of the same
  charged coat. RGB, rather than alpha alone, controls the strength of the vanilla
  additive energy layer. The previous thin white beam geometry is removed.
- The same overlays render on the actual equipped armour model, preventing an
  armour layer from hiding the frost/charge. No new textures or shader assets are
  introduced, and emission/camera/world-cache safeguards remain in place.

The feedback gallery fixture checks observed Poison particle head height and
captures bare and armoured models alongside animal examples. Build evidence is
`build/ailment-feedback-build.log`; the fixture is
`build/ailment-feedback-gallery-probe.java`.
Poison's source follows the actual named head mesh and its animated parent chain
when model parts are available. It shares the optional EMF root lookup used by
mount attachments, avoiding empty vanilla head placeholders in animation packs.
The lookup is bounded and runs at most once per affected entity per client world
tick; source positions are world/UUID scoped and validated before use. Unsupported
or baby models retain a head-height fallback.
The old bubble-shaped droplet sprite is replaced by Minecraft's existing filled
`drip_fall` sprite, tinted green. Its size accounts for the sprite's small occupied
area, and its quad offset is compensated so the visible drop starts at the head.
Final validation: full build/regression checks pass; the native head-source test
confirms emitted drops follow the rendered head; first-person particle suppression,
world/UUID cleanup and the 60-entity emission bounds pass on the final jar. Logs:
`build/ailment-feedback-head-final-results.log` and
`build/ailment-feedback-safety-results.log`. Revised unedited captures are retained
in `build/ailment-feedback-captures/`.

### Hip bubbles and joint-aligned coats

This revision supersedes the head-droplet source and Shock coat described above.

- Poison emits green bubbles around hip height. They rise for 28–40 ticks, then
  switch through five vanilla bubble-pop frames, tinting those frames green.
  The pop remains part of the original particle; it does not bypass emission caps
  by spawning child particles. The obsolete animated head-capture path is removed.
- Freeze and Electrocute expand each actual mesh cuboid around its own centre.
  Bone pivots and the animated hierarchy remain intact, covering legs and joints
  without shifting the whole model or temporarily changing torso scale. Cached
  cuboid fitting uses weak keys, and live hierarchy traversal is bounded.
- The mesh pass uses the optional animated-model root for EMF models and also
  coats equipped armour. Freeze retains its pale base, crack detail and crystals;
  Electrocute retains the charged-creeper swirl and cyan crackles.
- Shock renders lightning crackles only. Its model-overlay path requests no
  buffers and emits no coat vertices.

Build evidence: `build/ailment-bubbles-build.log`. The native modpack gallery
checks hip origins, upward travel, all five pop frames, expiry, zero Shock coat
geometry, animated head-to-foot coat geometry, animals and equipped armour.
Evidence: `build/ailment-revision-gallery-results.log` and
`build/ailment-revision-gallery-probe.java`.
The final safety pass also checks baby cow/horse coat geometry, first-person
suppression, UUID/unload handling and 60 afflicted entities: at most 48 emitted
particles per client tick and 12 per entity for event bursts. Evidence:
`build/ailment-revision-safety-results.log`. Selected unedited framebuffer
captures are retained in `build/ailment-revision-captures/`.

### Dedicated Poison bubble artwork

The legacy `poison_bubble.png` artwork was a skull, despite its filename. Poison
now explicitly uses the new `toxic_bubble.png`: a 16-by-16 green bubble with a
rounded silhouette, filled centre and a small pale highlight. The built-in image
generator supplied the source art, reduced with nearest-neighbour sampling for
the native particle texture. The old skull asset remains available for legacy
references, but is no longer in the Poison bubble animation. The five green-tinted
vanilla pop frames and the existing hip-origin/rise/expiry behaviour remain.
The native modpack check verifies the loaded sprite ID is
`cranks-dungeons:toxic_bubble`, checks its five pop frames and expiry, and observes
bubbles rising from the hips. Evidence: `build/poison-bubble-sprite-build.log`,
`build/poison-bubble-sprite-native-results.log`. Artwork prompt and native preview:
`build/poison-bubble-artwork-prompt.txt`, `build/poison-bubble-ingame.png`.

### Vanilla Poison bubbles

Following the latest art direction, Poison now references Minecraft's original
`minecraft:bubble` sprite directly and applies a green tint throughout its life.
Its five pop frames also use Minecraft's sprites with the same tint. The custom
generated bubble texture is removed; hip origins, upward motion, expiry and
particle budgets are unchanged.

### Vanilla bubble silhouette with a green interior

The current Poison bubble uses `poison_bubble_filled.png`, an 8-by-8 sprite
retaining vanilla's exact silhouette and inset highlight. Its 18 previously hollow
interior pixels receive translucent green shading inspired by the earlier filled
version. The built-in image generator supplies the fill/shading reference;
nearest-neighbour export retains the original vanilla silhouette/highlight mask.
The green sprite renders without a second colour tint, while the five vanilla pop
frames retain their green tint. Motion, hip origins and emission budgets are
unchanged. Artwork prompt: `build/poison-filled-vanilla-bubble-artwork-prompt.txt`.
Build/regression checks pass. The native modpack check confirms the filled sprite
ID, untinted shaded bubble vertices, green pop vertices, five distinct pop frames,
expiry and hip-to-air travel. Evidence: `build/poison-filled-vanilla-bubble-build.log`
and `build/poison-filled-vanilla-bubble-native-results.log`. Native capture:
`build/poison-filled-vanilla-bubble-ingame.png`.


## Gathering and World Tree stat implementation � November 22, 2026

This pass changes stat behavior and the available stat catalogue. It does not
allocate, rename, move or populate tree nodes. SHA-256 checks cover both tree
configuration files and both bundled default trees. Existing stat IDs stay
loadable; obsolete Fortune, category-XP and harvesting-speed IDs are omitted from
new editor suggestions rather than removed from old saves.

### Gathering and yield

- The previously inactive block/mob yield hooks are enabled with a shared bounded
  arithmetic path. A 0.1 percentage value consistently means 10%. Mining,
  Woodcutting and Digging Speed accept up to 300% extra. World-tree Gathering
  Speed increases multiply the tool base instead of adding flat speed.
- Yield adds to one original loot result rather than rerolling the table. Resource
  and legacy Fortune bonuses add before item-based stochastic rounding, with a
  combined 300% extra-output cap. Extra output splits into legal stacks.
- Ore yield uses the ore tag, including ancient debris scrap, and checks the tool
  used for the break. Silk Touch blocks never multiply. Log yield requires an
  axe and leaves support sapling/apple yield. Digging yield covers clay balls and
  flint already rolled from natural gravel; it cannot create free clay balls when
  Silk Touch returns the clay block.
- Mature hoe-harvested crops use crop/seed yield, including cocoa and Nether Wart.
  Crop Replant Chance consumes a matching planting item from an accessible
  hotbar, inventory or bag slot. It requires valid soil, a successful permitted
  break, an available seed and no active trade. Sneaking bypasses replanting. The original harvesting tool is
  retained for the final break of a tool. It never consumes locked bag contents.
- Vein Mining retains native protected player breaks and its 31-extra-block cap.
  Nested break contexts preserve the provenance of each separate block.

### Placement provenance

- Player placement markers are stored with chunks, loaded into the proper
  proto/wrapper/world chunk, and removed with destroyed blocks. Loaded memory
  follows chunk lifetime. There is no global 100,000-placement limit that silently
  stops protecting new placements. Old marker files remain readable and migrate
  conservatively when their positions are encountered.
- Markers survive log stripping. Piston markers are captured from the actual
  validated movement plan, after vanilla clears the head during retraction.
  Sources/destinations are cleared before destinations are assigned. Moving
  piston block entities retain the flag through settlement and NBT saves.
- Falling blocks retain placement provenance in their entity data and transfer
  it to the landed block. Naturally generated or grown resources remain eligible.
  World discovery XP continues to use personal knowledge claims, not block history.

### Loot, discovery and survival utility

- Monster/Animal/Meat Yield applies only to tagged ordinary resources on
  player-attributed kills. Equipment and tagged rare loot are not multiplied.
  Nested loot tables receive one quantity pass, including streamed output.
- Rare Loot Find increases weights of tagged rare entries in weighted monster
  loot pools. Item Rarity scales positive-quality entry weights by quality/8,
  including dimension bonuses. Neither bypasses conditions or resurrects a
  zero-weight choice. Fishing ownership and positive-quality nested table entries
  are supported. Fractional low weights use a stable rounding sample per selection
  so small bonuses do not disappear. These are relative selection weights, not flat percentage
  points, guaranteed bonus rolls or equipment-rarity upgrades. Final weighting
  is bounded at four times the original weight. Native Luck from other sources
  remains part of the original weight; the old custom-to-Luck bridge is removed.
- World Discovery XP Bonus applies to all World discovery rewards, capped at 50%
  from this stat. Existing generic XP and legacy category bonuses remain compatible;
  personal claims and fractional accounting are unchanged.
- Tool/Durability Preservation combine additively, capped at 80% total. They avoid
  wear rather than repair items. Bonus block reach adds at most two metres and
  does not alter attack reach. Swimming Speed adds up to 50% water acceleration;
  Breath Efficiency prevents up to 75% of air-loss ticks. Travel Efficiency cuts
  sprinting/swimming hunger costs by up to 50%, without altering combat, mining
  or mount stamina costs.
- The shared searchable stats panel consolidates legacy yield into current values,
  shows the correct speed units and previews Gathering Speed using the same
  multiplier operation as gameplay. Detailed stat descriptions wrap to a bounded
  width. The gathering/discovery Compendium pages document the current rules.

### Validation

Permanent `GatheringRulesRegression` checks yield rounding, stack-size output,
non-finite inputs, speed units, preservation limits and bounded relative weights.
The native fixture is `build/gathering-live-probe.java`, running in the isolated
copy of the testing modpack. It exercises actual protected breaks, placed logs
and ores, Silk Touch, clay, crop yield, bag-seeded replanting, locked slots,
Vein Mining and cancellation events, speed application, weighted rare loot with
separate players, discovery-claim idempotence, preservation, breathing, swimming,
travel hunger, adjacent piston pushes, sticky retraction, falling gravel, chunk
serialization and real restart persistence. No node allocation is needed for the
fixture; it temporarily grants attributes and restores player data afterward.

The movement stress fixture targets the exact intermediary vanilla piston method
(`method_11481`), rather than selecting an arbitrary signature match: mixin
wrapping creates synthetic methods with the same signature. Breathing validation
also selects its exact native entry point. Earlier diagnostic failures from the
ambiguous reflection harness are retained separately from final validation logs.

Final validation passed: `build/gathering-verified-build.log` contains the Gradle
build and regression results; `build/gathering-live-final-results.log` contains
the complete native gameplay and UI checks, including sneaking to bypass
replanting. `build/gathering-movement-stress-results.log` records 20 consecutive
successful push, sticky-retraction and falling-block cycles. The final stats
panel capture is `build/gathering-stats-panel.png`. All four tree files match
the SHA-256 baseline in `build/gathering-tree-baseline-hashes.json`.
