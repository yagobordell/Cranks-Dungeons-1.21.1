# Passive Tree Modifiers Reference

This file contains a comprehensive list of all attributes available for use in the Passive Skill Tree configuration (`passive_tree.json`). 

## ⚖️ Scaling Rules
- **Percentage Stats**: Stored as decimals. `0.05` is **5%**, `0.1` is **10%**, `1.0` is **100%**.
- **Ailment Damage**: `burn_damage_percent` uses `100.0` as baseline. `poison_damage_percent` uses `0.0` as baseline.
- **HP Units**: 1.0 Health = **0.5 Hearts**.

---

## ⚔️ Offensive Modifiers (cranks-dungeons)

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:crit_chance` | Percentage | Chance to deal critical damage. (Base: 5%) |
| `cranks-dungeons:crit_multiplier` | Percentage | Increases damage dealt by critical strikes. (Base: 1.5x / 150%) |
| `cranks-dungeons:bonus_attack_damage` | Flat | Extra physical damage added to every hit. |
| `cranks-dungeons:attack_speed` | Percentage | Increases frequency of attacks. |
| `cranks-dungeons:fire_damage` | Flat | Adds flat fire damage to attacks. |
| `cranks-dungeons:cold_damage` | Flat | Adds flat cold damage to attacks. |
| `cranks-dungeons:lightning_damage` | Flat | Adds flat lightning damage to attacks. |
| `cranks-dungeons:void_damage` | Flat | Adds flat void damage to attacks. |
| `cranks-dungeons:life_steal` | Percentage | Heals for a % of damage dealt on hit. |
| `cranks-dungeons:attack_range` | Flat | Increases reach distance for attacks. |
| `cranks-dungeons:knockback` | Percentage | Increases knockback dealt to enemies. |
| `cranks-dungeons:reload_speed` | Percentage | Increases reload speed for ranged weapons. |

---

## 🧪 Ailment Modifiers (cranks-dungeons)

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:chance_to_burn` | Percentage | Chance to inflict Burn. |
| `cranks-dungeons:burn_damage_percent` | Percentage | Increases damage of Burn status effect. (Base: 100) |
| `cranks-dungeons:burn_duration` | Percentage | Increases duration of Burn. |
| `cranks-dungeons:chance_to_poison` | Percentage | Chance to inflict Poison. |
| `cranks-dungeons:poison_damage_percent` | Percentage | Increases damage of Poison status effect. (Base: 0) |
| `cranks-dungeons:poison_duration` | Percentage | Increases duration of Poison. |
| `cranks-dungeons:poison_max_stacks` | Flat | Increases maximum poison stacks on a target. (Base: 20) |
| `cranks-dungeons:chance_to_freeze` | Percentage | Chance to inflict Freeze. |
| `cranks-dungeons:freeze_duration` | Percentage | Increases duration of Freeze. |
| `cranks-dungeons:chance_to_bleed` | Percentage | Chance to inflict Bleed. |
| `cranks-dungeons:bleed_duration` | Percentage | Increases duration of Bleed. |
| `cranks-dungeons:chance_to_shock` | Percentage | Chance to inflict Shock. |
| `cranks-dungeons:shock_duration` | Flat | Fixed duration for Shock in seconds. (Base: 3s) |

---

## 🛡️ Defensive Modifiers (cranks-dungeons)

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:max_health_bonus` | Flat | Increases maximum health safely. |
| `cranks-dungeons:bonus_armor` | Flat | Increases base armor rating. |
| `cranks-dungeons:bonus_armor_toughness` | Flat | Increases armor toughness. |
| `cranks-dungeons:life_regeneration` | Flat | Amount of health regenerated per second. |
| `cranks-dungeons:energy_shield` | Flat | Base capacity for Energy Shield. |
| `cranks-dungeons:evasion_rating` | Flat | Increases chance to completely dodge a hit. |
| `cranks-dungeons:knockback_resistance` | Percentage | Reduces knockback taken from enemies. |
| `cranks-dungeons:feather_falling` | Percentage | Reduces fall damage taken. |

---

## ✦ Utility Modifiers (cranks-dungeons)

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:movement_speed` | Percentage | Increases walking/running speed. |

---
