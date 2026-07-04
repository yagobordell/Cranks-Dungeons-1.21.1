# World Tree Modifiers Documentation

The World Tree uses specialized modifiers focused on gathering, experience, and loot rather than combat stats. These modifiers can be added to standard nodes or used as options in choice nodes.

> [!NOTE]
> All yield/chance modifiers are percentages where `1.0` = `+100%`. For example, `0.5` gives a 50% chance for an extra drop, while `1.5` guarantees 1 extra drop and gives a 50% chance for a second extra drop.
>
> Speed modifiers are percentages where `1.0` = `+100%` faster speed.

## 🪓 Woodcutting Modifiers

| Modifier ID | Type | Description |
| :--- | :--- | :--- |
| `log_yield` | Value (`0.0+`) | Grants a chance to drop extra logs when chopping logs (Oak, Spruce, Cherry, etc.). |
| `sapling_yield` | Value (`0.0+`) | Grants a chance to drop extra apples or saplings when breaking leaves. |
| `woodcutting_speed_percent` | Value (`0.0+`) | Increases block breaking speed when chopping logs. `1.0` is +100% speed. |

## ⛏️ Mining Modifiers

| Modifier ID | Type | Description |
| :--- | :--- | :--- |
| `ore_yield` | Value (`0.0+`) | Chance to drop extra raw ores (Iron, Gold, Copper) or gems when breaking any ore block. |
| `gem_yield` | Value (`0.0+`) | Provides an **additional** drop chance specifically for gem ores (Diamond, Emerald, Lapis). |
| `mining_speed_percent` | Value (`0.0+`) | Increases block breaking speed when mining pickaxe-mineable blocks (stone, ores, deepslate). |
| `flint_yield` | Value (`0.0+`) | Chance to drop extra flint when digging gravel (only applies if flint successfully dropped). |

## 🌾 Farming Modifiers

| Modifier ID | Type | Description |
| :--- | :--- | :--- |
| `crop_yield` | Value (`0.0+`) | Chance to drop extra crop food (wheat, potatoes, carrots, beetroot) when harvesting mature crops. |
| `seed_yield` | Value (`0.0+`) | Chance to drop extra seeds when harvesting mature crops. |
| `farming_speed_percent` | Value (`0.0+`) | Increases block breaking speed when breaking crop blocks or digging dirt. |

## 🗡️ Mob Drop Modifiers

| Modifier ID | Type | Description |
| :--- | :--- | :--- |
| `monster_drop_yield` | Value (`0.0+`) | Chance to multiply all drops from hostile mobs (Zombies, Skeletons, Creepers). |
| `animal_drop_yield` | Value (`0.0+`) | Chance to multiply all drops from passive animals (Cows, Pigs, Sheep). |
| `meat_yield` | Value (`0.0+`) | Provides an **additional** drop multiplier specifically for meat drops (Beef, Pork, Mutton, Chicken, Rabbit). |
| `rare_mob_drop_chance` | Value (`0.0+`) | Currently acts as a general drop multiplier. Useful for scaling rare table items. |

## 🌟 Experience Modifiers

| Modifier ID | Type | Description |
| :--- | :--- | :--- |
| `mining_experience` | Value (`0.0+`) | Percentage increase to experience orbs dropped by mining blocks. `0.5` = +50% XP. |
| `combat_experience` | Value (`0.0+`) | Percentage increase to experience dropped when killing mobs. |
| `breeding_experience` | Value (`0.0+`) | Grants extra experience orbs when successfully breeding animals. |
| `fishing_experience` | Value (`0.0+`) | Grants extra experience orbs when successfully catching items while fishing. |

## 🌌 Dimensional Modifiers

These modifiers apply only when the player is in a specific dimension. They stack additively with their base counterparts!

| Modifier ID | Type | Description |
| :--- | :--- | :--- |
| `ore_yield_in_overworld` | Value (`0.0+`) | Extra ore yield that only applies in the Overworld. |
| `ore_yield_in_nether` | Value (`0.0+`) | Extra ore yield that only applies in the Nether (e.g. Nether Gold Ore, Quartz). |
| `rare_mob_drop_chance_in_nether` | Value (`0.0+`) | Extra rare mob drop chance that only applies in the Nether. |
| `item_rarity_in_overworld` | Value (`0.0+`) | (Reserved for future Rarity systems) |
| `item_rarity_in_nether` | Value (`0.0+`) | (Reserved for future Rarity systems) |
| `item_rarity_in_end` | Value (`0.0+`) | (Reserved for future Rarity systems) |
