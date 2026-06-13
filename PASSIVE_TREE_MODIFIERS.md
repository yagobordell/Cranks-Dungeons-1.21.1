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

| `cranks-dungeons:attack_damage_percent`| Percentage | Increased Physical Damage. |
| `cranks-dungeons:melee_damage_percent` | Percentage | Increased Melee damage (Swords, Axes, Maces, etc). |
| `cranks-dungeons:ranged_damage_percent` | Percentage | Increased Ranged damage (Bows, Crossbows, Thrown Tridents). |
| `cranks-dungeons:one_handed_damage_percent` | Percentage | Increased damage when using one weapon and a shield/offhand. |
| `cranks-dungeons:dual_wielding_damage_percent` | Percentage | Increased damage when using two weapons of the same type. |
| `cranks-dungeons:two_handing_damage_percent` | Percentage | Increased damage when using a weapon with the offhand empty. |
| `cranks-dungeons:attack_speed` | Percentage | Increases frequency of attacks. |

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

| `cranks-dungeons:fire_damage_percent` | Percentage | Increased Fire damage. |

| `cranks-dungeons:cold_damage_percent` | Percentage | Increased Cold damage. |

| `cranks-dungeons:lightning_damage_percent` | Percentage | Increased Lightning damage. |

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

### 🔄 Pure Damage Conversions
| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:physical_to_fire_conversion_percent` | Percentage | Converts a % of Physical Damage entirely to Fire Damage. |
| `cranks-dungeons:physical_to_cold_conversion_percent` | Percentage | Converts a % of Physical Damage entirely to Cold Damage. |
| `cranks-dungeons:physical_to_lightning_conversion_percent` | Percentage | Converts a % of Physical Damage entirely to Lightning Damage. |
| `cranks-dungeons:physical_to_void_conversion_percent` | Percentage | Converts a % of Physical Damage entirely to Void Damage. |

### 🎯 Penetration Modifiers

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:armor_penetration_percent` | Percentage | Penetrates a percentage of enemy armor. |
| `cranks-dungeons:fire_penetration_percent` | Percentage | Penetrates a percentage of enemy Fire Resistance. |
| `cranks-dungeons:cold_penetration_percent` | Percentage | Penetrates a percentage of enemy Cold Resistance. |
| `cranks-dungeons:lightning_penetration_percent` | Percentage | Penetrates a percentage of enemy Lightning Resistance. |
| `cranks-dungeons:void_penetration_percent` | Percentage | Penetrates a percentage of enemy Void Resistance. |


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

| `cranks-dungeons:chance_to_bleed` | Percentage | Chance to cause bleeding. |
| `cranks-dungeons:bleed_duration` | Percentage | Increased Bleed duration. |
| `cranks-dungeons:chance_to_shock` | Percentage | Chance to shock enemies. |
| `cranks-dungeons:shock_duration` | Percentage | Duration of shock debuff. |



---

## 🛡️ Defensive Modifiers

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:max_health_bonus` | Flat | Increases maximum health. |
| `cranks-dungeons:life_regeneration` | Flat | Health restored per second (HP/s). |
| `cranks-dungeons:bonus_armor` | Flat | Base armor rating. |

| `cranks-dungeons:energy_shield` | Flat | Absorb shield capacity. |
| `cranks-dungeons:evasion_rating` | Flat | Chance to dodge hits. |
| `cranks-dungeons:knockback_resistance` | Percentage | Reduces knockback taken. |
| `cranks-dungeons:leggings_armor_percent` | Percentage | Increases armor from leggings by a percentage. |
| `cranks-dungeons:armor_crit_mitigation` | Percentage | Percentage of armor that reduces critical extra damage. |
| `cranks-dungeons:max_health_percent` | Percentage | Increased maximum health. |
| `cranks-dungeons:armor_percent` | Percentage | Increased armor rating. |

| `cranks-dungeons:energy_shield_percent` | Percentage | Increased energy shield capacity. |
| `cranks-dungeons:evasion_rating_percent` | Percentage | Increased evasion rating. |

---

## ⚒️ Gathering & Utility

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:movement_speed` | Percentage | Increases walking/running speed. |
| `cranks-dungeons:armor_speed_penalty_reduction` | Percentage | Reduces the movement speed penalty from wearing armor. (5% per 100 armor). |
| `cranks-dungeons:low_armor_speed_bonus` | Percentage | Gain movement speed when your total armor is below 150. Scales with the distance from 150. |

## ➕ Additional Modifiers

| Identifier | Type | Description |
| :--- | :--- | :--- |
| `cranks-dungeons:fire_resistance` | Percentage | Fire resistance. |
| `cranks-dungeons:cold_resistance` | Percentage | Cold resistance. |
| `cranks-dungeons:lightning_resistance` | Percentage | Lightning resistance. |
| `cranks-dungeons:void_resistance` | Percentage | Void resistance. |
| `cranks-dungeons:armor_applies_to_elemental` | Percentage | Armor mitigation applies to elemental damage. |
| `cranks-dungeons:thorns_damage` | Flat | Damage reflected back to attackers. |

| `cranks-dungeons:projectile_speed` | Percentage | Increases projectile speed. |
| `cranks-dungeons:burn_base_damage` | Flat | Flat burn damage. |
| `cranks-dungeons:poison_base_damage` | Flat | Flat poison damage. |
| `cranks-dungeons:freeze_buildup_percent` | Percentage | Increased freeze buildup. |
| `cranks-dungeons:bleed_burst_damage_percent` | Percentage | Increased bleed burst damage. |
| `cranks-dungeons:electrocute_buildup_percent` | Percentage | Increased electrocute buildup. |


| `cranks-dungeons:strength` | Flat | Increases physical damage and health. |
| `cranks-dungeons:dexterity` | Flat | Increases attack speed and evasion. |
| `cranks-dungeons:intelligence` | Flat | Increases elemental damage and energy shield. |
| `cranks-dungeons:chance_to_block` | Percentage | Chance to block attacks. |
| `cranks-dungeons:block_value` | Flat | Damage mitigated when blocking. |
| `cranks-dungeons:health_on_block` | Flat | Health restored on successful block. |

