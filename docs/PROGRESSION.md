# Progression

The server owns two separate tracks. Player XP comes only from eligible combat. World XP comes from persistent, individual discovery claims. Mount training is a separate system and is unchanged.

## Combat

- Natural hostile mobs and supported bosses are eligible. Passive creatures, players, automated kills, command/egg/spawner/trial-spawner mobs are excluded. Unknown legacy spawn origin is conservatively ineligible; newly spawned natural mobs work normally.
- Spawn provenance persists in mob NBT and propagates through conversions, slime splits and zombie reinforcements. Withers built through the skull ritual are explicitly eligible.
- Contributions expire after 30 seconds by default. A fighter must deal at least 5% of maximum life (minimum 0.1 damage), remain alive and within 64 blocks in the same dimension. Active party tanks, beneficial effect providers and effective external healers can join a group's reward. Idle party members cannot.
- Actual damage to life, absorption and energy shield counts. Overkill and damage beyond the encounter's effective-health budget cannot inflate shares. Fatal DoT bookkeeping completes before the end-of-tick payout. A mob pays only once.
- Groups split the base reward by qualified damage contribution. Each group then gets `min(2, 1 + 0.2 × (eligible members − 1))`, divided equally in millionths of XP. Personal Experience Bonus + Combat Experience is applied once, additively. Fractional XP persists.
- Native untyped XP calls/orbs cannot grant either progression track. Vanilla `/experience` is not an authority for RPG progress. XP bottles therefore do not grant progression XP.

## World

Default first rewards: resources 60, recipe knowledge 15, successful formula 30, biome 150, generated structure instance 300, first structure type 300, first tame/breed/catch species 75. Discovery-based gathering no longer uses placed-block coordinates. Existing drop/Fortune placement tracking remains separate.

Resource families collapse ore/raw/ingot/nugget/block, crop/seed and stripped-log variants. Recipe knowledge keys collapse alternate recipes with the same output, station duplicates, colours and wood variants. Salvaging/recycling and dynamic copy/repair recipes grant no knowledge XP. Successful alchemy formulas reward once; failures and repeated experiments do not.

Claims are server-only, per player, persisted with CCA and copied on death. Existing item/formula/recipe knowledge is claimed silently during migration; old structure/biome history was not recorded and cannot be reconstructed. Exploration reads only already-loaded full chunks, checks actual structure pieces, and requires three seconds inside. Returning to a claimed structure or biome gives no reward.

General XP bonuses apply once; mining, crops, fishing and breeding bonuses apply to their matching first-discovery rewards. No nearest-player XP or durability-cost fishing rewards remain.

## Levels and persistence

`config/cranks-dungeons/progression.json` controls server caps, curve coefficients, discovery rewards and combat eligibility bounds; restart to reload. Default hard caps are combat 90 and world 311, independent of tree size. Combat curve: `floor(50 + 5L + 0.2L²)`. Discovery-based world curve: `floor(75 + 6L + 0.02L²)`. Pacing will need playtesting as future campaign content arrives.

Existing levels, tree allocations and points remain. World XP progress is normalized to the new curve on migration. A highest-rewarded-level watermark prevents re-earning points after a level is restored downward. Death keeps levels and world XP; without keepInventory, combat progress loses 30 percentage points of its current bar, bounded at zero.

The owner receives small progression packets; growing discovery claim sets are not transmitted. Native player XP fields mirror RPG combat progress for existing HUD/stat consumers. Inventory/tree stat panels and the HUD show maximum-level states. No profession progress is implemented.

Level-up feedback uses a 22-tick owner-bound effect of twelve small rising vanilla glow sprites: muted gold for combat, pale silver-blue for world levels. The existing custom sounds play at 65% volume. Overlapping rewards of the same track do not stack or restart the visual or sound.
