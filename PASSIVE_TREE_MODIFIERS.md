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
| `cranks-dungeons:melee_damage_percent` | Percentage | Increased Melee damage (Swords, Axes, Maces, etc). |
| `cranks-dungeons:ranged_damage_percent` | Percentage | Increased Ranged damage (Bows, Crossbows, Thrown Tridents). |
| `cranks-dungeons:one_handed_damage_percent` | Percentage | Increased damage when using one weapon and a shield/offhand. |
| `cranks-dungeons:dual_wielding_damage_percent` | Percentage | Increased damage when using two weapons of the same type. |
| `cranks-dungeons:two_handing_damage_percent` | Percentage | Increased damage when using a weapon with the offhand empty. |
| `cranks-dungeons:attack_speed` | Percentage | Increases frequency of attacks. |
| `cranks-dungeons:attack_range` | Flat | Increases reach distance for attacks. |
| `cranks-dungeons:knockback` | Percentage | Increases knockback dealt to enemies. |
| `cranks-dungeons:life_steal` | Percentage | Heals for a % of damage dealt on hit. |
| `cranks-dungeons:reload_speed` | Percentage | Increases reload speed for bows/crossbows. |

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
| `cranks-dungeons:elemental_damage_percent` | Percentage | Increased Elemental Damage (Fire, Cold, Lightning, Void). |
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
| `cranks-dungeons:lightning_extra_percent` | Percentage | Gain % of damage as extra Lightning damage. |
| `cranks-dungeons:void_extra_percent` | Percentage | Gain % of damage as extra Void damage. |
| `cranks-dungeons:physical_as_extra_fire_percent` | Percentage | Gain % of Physical damage as extra Fire damage. |
| `cranks-dungeons:physical_as_extra_cold_percent` | Percentage | Gain % of Physical damage as extra Cold damage. |
| `cranks-dungeons:physical_as_extra_lightning_percent` | Percentage | Gain % of Physical damage as extra Lightning damage. |
| `cranks-dungeons:physical_as_extra_void_percent` | Percentage | Gain % of Physical damage as extra Void damage. |

### 🎯 Penetration Modifiers

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:armor_penetration_percent` | Percentage | Penetrates a percentage of enemy armor. |
| `cranks-dungeons:fire_penetration_percent` | Percentage | Penetrates a percentage of enemy Fire Resistance. |
| `cranks-dungeons:cold_penetration_percent` | Percentage | Penetrates a percentage of enemy Cold Resistance. |
| `cranks-dungeons:lightning_penetration_percent` | Percentage | Penetrates a percentage of enemy Lightning Resistance. |
| `cranks-dungeons:void_penetration_percent` | Percentage | Penetrates a percentage of enemy Void Resistance. |
| `cranks-dungeons:elemental_penetration_percent` | Percentage | Penetrates a percentage of ALL enemy Elemental Resistances. |

---

## 🧪 Ailment Modifiers

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:damage_against_afflicted_percent` | Percentage | Increased damage against enemies with an ailment. |
| `cranks-dungeons:damage_while_afflicted_percent` | Percentage | Increased damage while YOU are afflicted with an ailment. |
| `cranks-dungeons:crit_chance_against_afflicted` | Percentage | Increased Critical Strike chance against enemies with an ailment. |
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
| `cranks-dungeons:shock_duration` | Percentage | Duration of shock debuff. |

### 💥 Elemental Reactions
| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:reaction_damage_percent` | Percentage | Increased damage of Shatter, Cauterize, Thermal Shock, and Supercharge. |
| `cranks-dungeons:reaction_cooldown_percent` | Percentage | Reduces internal cooldown of all reactions. (e.g. 0.5 = 50% faster). |
| `cranks-dungeons:reaction_radius_percent` | Percentage | Increased radius of Shatter, Miasma, and Shock Chain. |

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
| `cranks-dungeons:armor_crit_mitigation` | Percentage | Percentage of armor that reduces critical extra damage. |
| `cranks-dungeons:max_health_percent` | Percentage | Increased maximum health. |
| `cranks-dungeons:armor_percent` | Percentage | Increased armor rating. |
| `cranks-dungeons:armor_toughness_percent` | Percentage | Increased armor toughness rating. |
| `cranks-dungeons:energy_shield_percent` | Percentage | Increased energy shield capacity. |
| `cranks-dungeons:evasion_rating_percent` | Percentage | Increased evasion rating. |

---

## ⚒️ Gathering & Utility

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:movement_speed` | Percentage | Increases walking/running speed. |
| `cranks-dungeons:armor_speed_penalty_reduction` | Percentage | Reduces the movement speed penalty from wearing armor. (e.g. 1.0 = no penalty). |
| `cranks-dungeons:low_armor_speed_bonus` | Percentage | Gain movement speed when your total armor is below 150. Scales with the distance from 150. |
