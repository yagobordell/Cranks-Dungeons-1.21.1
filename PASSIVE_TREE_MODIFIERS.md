# Passive Tree Modifiers Reference

This file contains a comprehensive list of all attributes available for use in the Passive Skill Tree configuration (`passive_tree.json`). 

## ⚖️ Scaling Rules
- **Percentage Stats**: Stored as decimals. `0.05` is **5%**, `0.1` is **10%**, `1.0` is **100%**.
- **Calculations**: Final Value = `Base * (1.0 + sum_of_percentages)`.
- **HP Units**: 1.0 Health = **0.5 Hearts**.

---

## ⚔️ Offensive Modifiers

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:crit_chance` | Percentage | Chance to deal critical damage. (Base: 5%) |
| `cranks-dungeons:crit_multiplier` | Percentage | Increases damage dealt by critical strikes. (Base: 1.5x) |
| `cranks-dungeons:bonus_attack_damage` | Flat | Extra physical damage added to every hit. |
| `cranks-dungeons:attack_damage_percent`| Percentage | Increased Physical Damage. |
| `cranks-dungeons:attack_speed` | Percentage | Increases frequency of attacks. |
| `cranks-dungeons:attack_range` | Flat | Increases reach distance for attacks. |
| `cranks-dungeons:knockback` | Percentage | Increases knockback dealt to enemies. |
| `cranks-dungeons:life_steal` | Percentage | Heals for a % of damage dealt on hit. |
| `cranks-dungeons:reload_speed` | Percentage | Increases reload speed for bows/crossbows. |
| `cranks-dungeons:trident_damage_percent`| Percentage | Increased physical/elemental damage with Tridents. |

### ⚔️ Weapon Specialization
| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:sword_damage_percent` | Percentage | Increased physical/elemental damage with Swords. |
| `cranks-dungeons:axe_damage_percent` | Percentage | Increased physical/elemental damage with Axes. |
| `cranks-dungeons:mace_damage_percent` | Percentage | Increased physical/elemental damage with Maces. |
| `cranks-dungeons:spear_damage_percent` | Percentage | Increased physical/elemental damage with Spears. |
| `cranks-dungeons:bow_damage_percent` | Percentage | Increased physical/elemental damage with Bows. |
| `cranks-dungeons:crossbow_damage_percent` | Percentage | Increased physical/elemental damage with Crossbows. |
| `cranks-dungeons:trident_damage_percent` | Percentage | Increased physical/elemental damage with Tridents. |

### ❄️ Elemental Damage
| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:fire_damage` | Flat | Adds flat fire damage to attacks. |
| `cranks-dungeons:fire_damage_percent` | Percentage | Increased Fire damage. |
| `cranks-dungeons:cold_damage` | Flat | Adds flat cold damage to attacks. |
| `cranks-dungeons:cold_damage_percent` | Percentage | Increased Cold damage. |
| `cranks-dungeons:lightning_damage` | Flat | Adds flat lightning damage to attacks. |
| `cranks-dungeons:lightning_damage_percent` | Percentage | Increased Lightning damage. |
| `cranks-dungeons:void_damage` | Flat | Adds flat void damage to attacks. |
| `cranks-dungeons:void_damage_percent` | Percentage | Increased Void damage. |

### ☄️ Extra Damage (Conversions)
| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:physical_extra_percent`| Percentage | Gain % of damage as extra Physical damage. |
| `cranks-dungeons:fire_extra_percent` | Percentage | Gain % of damage as extra Fire damage. |
| `cranks-dungeons:cold_extra_percent` | Percentage | Gain % of damage as extra Cold damage. |
| `cranks-dungeons:lightning_extra_percent`| Percentage | Gain % of damage as extra Lightning damage. |
| `cranks-dungeons:void_extra_percent` | Percentage | Gain % of damage as extra Void damage. |

---

## 🧪 Ailment Modifiers

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:chance_to_burn` | Percentage | Chance to ignite enemies. |
| `cranks-dungeons:burn_damage_percent` | Percentage | Increased Burn damage. |
| `cranks-dungeons:burn_duration` | Percentage | Increased Burn duration. |
| `cranks-dungeons:chance_to_poison` | Percentage | Chance to poison enemies. |
| `cranks-dungeons:poison_damage_percent` | Percentage | Increased Poison damage. |
| `cranks-dungeons:poison_duration` | Percentage | Increased Poison duration. |
| `cranks-dungeons:poison_max_stacks` | Flat | Maximum number of poison stacks. |
| `cranks-dungeons:chance_to_freeze` | Percentage | Chance to freeze enemies. |
| `cranks-dungeons:freeze_duration` | Percentage | Increased Freeze duration. |
| `cranks-dungeons:chance_to_bleed` | Percentage | Chance to cause bleeding. |
| `cranks-dungeons:bleed_duration` | Percentage | Increased Bleed duration. |
| `cranks-dungeons:chance_to_shock` | Percentage | Chance to shock enemies. |
| `cranks-dungeons:shock_duration` | Flat | Duration of shock debuff. |

---

## 🛡️ Defensive Modifiers

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:max_health_bonus` | Flat | Increases maximum health. |
| `cranks-dungeons:life_regeneration` | Flat | Health restored per second (HP/s). |
| `cranks-dungeons:bonus_armor` | Flat | Base armor rating. |
| `cranks-dungeons:bonus_armor_toughness` | Flat | Armor toughness rating. |
| `cranks-dungeons:energy_shield` | Flat | Absorb shield capacity. |
| `cranks-dungeons:evasion_rating` | Flat | Chance to dodge hits. |
| `cranks-dungeons:knockback_resistance` | Percentage | Reduces knockback taken. |
| `cranks-dungeons:leggings_armor_percent` | Percentage | Increases armor from leggings by a percentage. |

---

## ⚒️ Gathering & Utility

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:movement_speed` | Percentage | Increases walking/running speed. |
