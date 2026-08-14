# Cranks Dungeons - Architecture & Developer Guidelines

## Overview

This document explains the core systems of the Cranks Dungeons mod:
1. **Equipment & Rune Stat Pipeline** (item stats, local percentages, quality scaling, baking)
2. **Combat Damage & Conversion Pipeline** (physical scaling, conversions, elemental multipliers)
3. **Passive Skill Tree & World Tree Architecture** (whitelisting, player attribute modifiers, UI sync)

---

## 1. Core Classes & Roles

| Class | Role |
|---|---|
| `ModAttributes` | Registry of all custom entity attributes (`RegistryEntry<EntityAttribute>`) |
| `StatRegistry` | Defines equipment rune stats with ranges, slots, and equipment types |
| `CustomStat` / `StatComponent` | Data definition for an item rune stat |
| `ItemStatManager` | Stores rolled rune stat values in item NBT |
| `EquipmentStatApplier` | Bakes rune stats into item's `AttributeModifiersComponent` |
| `ItemComponentModifier` | Defines baseline stats for armor/weapons (e.g. Leather = Evasion, Gold = Energy Shield) |
| `CombatUtility` | **Central utility**: Contains `attributesMatch()`, weapon determination, and slot-aware queries |
| `BonusAttackDamageMixin` | Global physical/weapon/wielding percentage damage scaling on `LivingEntity.damage()` |
| `DamageConversionHelper` | Elemental conversions, extra damage calculation, armor/resistance reduction, and breakdown |
| `ElementalDamageMixin` | Post-damage elemental handling (ailments, leech, damage numbers) |
| `PassiveStatHelper` | Whitelists passive/world tree attributes, aggregates total bonuses from unlocked nodes |
| `PassiveSkillApplier` | Applies aggregated passive tree bonuses as `ADD_VALUE` modifiers to player attribute instances |
| `LivingEntityMixin` | Dynamic requirement penalties, on-kill effects, intrinsic attribute bonuses, armor speed |
| `ItemStackTooltipMixin` | Tooltip rendering with quality scaling and base stat computation |

---

## 2. Damage Application Pipeline (Path of Exile 2 Standard)

The mod uses strict explicit mixin priorities on `LivingEntity.damage()` to ensure the exact PoE 2 sequence:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. Attacker Calculation: BonusAttackDamageMixin.java (Priority 500 @ HEAD)  │
│    • Adds flat BONUS_ATTACK_DAMAGE                                          │
│    • Multiplies by total global percentage bonuses:                         │
│      - PHYSICAL_DAMAGE_PERCENT, weapon-specific, melee/ranged/projectile,   │
│        wielding bonuses, DAMAGE_AGAINST_AFFLICTED_PERCENT                   │
│    • Point Blank distance scaling for projectiles                           │
│    • Critical strike roll & multiplier (Armor Crit Mitigation / Overload)   │
│    • Sets CriticalHitState (isCriticalHit, critMultiplier)                  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. Target Avoidance: EvasionMixin (Priority 600) & ShieldBlockMixin (700)   │
│    • EvasionMixin: Deterministic entropy counter (0.5 start, dodges at 1.0) │
│    • ShieldBlockMixin: Subtracts flat Block Value before mitigation         │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. Target Mitigation: DamageReductionMixin.java (Priority 800 @ HEAD)       │
│    • Converts physical to Fire/Cold/Lightning/Void & applies extra damage   │
│    • Scales elements individually with element-specific % and elemental %   │
│    • Applies Resistances vs Penetration & Armor Reduction (PoE 2 PDR curve) │
│    • Multiplies by Shock magnitude                                          │
│    • Caches mitigated breakdown in DamageBreakdownProvider                  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. Energy Shield: EnergyShieldDamageMixin.java (Priority 900 @ HEAD)        │
│    • Absorbs mitigated damage (Void deals 2x weighted damage to shield)     │
│    • Poison/Bleed bypasses shield                                           │
│    • Syncs packets & plays break/hit effects                                │
│    • Returns remaining unabsorbed damage to Health                          │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 5. Post-Damage Resolution: ElementalDamageMixin.java (Priority 1000 @ RETURN)│
│    • Leech (life & mana leech from physical portion)                        │
│    • Ailments (burn/ignite, shock, poison, bleed, freeze, electrocute)      │
│      * Scaled directly from actual damage dealt to target                   │
│    • Broadcasts floating damage numbers with crit flag                      │
│    • Resets CriticalHitState                                                │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Passive Skill Tree Architecture

### How Passive Nodes Work
1. When a player unlocks a node (or chooses an attribute on a selection node), the choice is stored in `ModComponents.PASSIVE_DATA` (or `WORLD_TREE_DATA`).
2. `PassiveSkillApplier.updatePlayerPassives(player)` is called.
3. `PassiveStatHelper.getPassiveTreeSummary(player)` aggregates all unlocked nodes, filtering through `WHITELISTED_PASSIVE_ATTRIBUTES` (or `WHITELISTED_WORLD_ATTRIBUTES`).
4. `PassiveSkillApplier` creates an `EntityAttributeModifier` with `Operation.ADD_VALUE` and adds it to the player's attribute instance:
   - Identifier: `cranks-dungeons:passive_{attrKey}`
   - Operation: `ADD_VALUE`

### Step-by-Step Guide: Adding a New Passive Modifier

1. **Register Attribute in `ModAttributes.java`**:
   ```java
   public static final RegistryEntry<EntityAttribute> MY_STAT = register("my_stat", defaultVal, minVal, maxVal);
   ```
   *Attributes registered here are automatically added to `ALL_ATTRIBUTES` and attached to players on spawn.*

2. **Whitelist in `PassiveStatHelper.java`**:
   - Add `"my_stat"` (without `cranks-dungeons:`) to `WHITELISTED_PASSIVE_ATTRIBUTES` (or `WHITELISTED_WORLD_ATTRIBUTES`).
   - Add player-friendly name formatting to `PassiveStatHelper.getReadableName()`.

3. **Add Translation in `assets/cranks-dungeons/lang/en_us.json`**:
   ```json
   "attribute.name.cranks-dungeons.my_stat": "My Stat Name"
   ```

4. **Implement Gameplay Logic**:
   - Query the attribute with `player.getAttributeValue(ModAttributes.MY_STAT)` or `CombatUtility.getAttributeValue(...)`.
   - If it's a damage modifier, add it to `BonusAttackDamageMixin.java` or `DamageConversionHelper.java`.
   - If it's a defensive modifier, hook it into `DamageReductionMixin.java`, `ArmorDamageMixin.java`, or `LivingEntityMixin.java`.

5. **Register UI Icon in `PassiveIconManager.java`** (if applicable):
   - Map the attribute to an icon tag/texture in `PassiveIconManager.java`.

6. **Add to Stats Display** (if applicable):
   - `InventoryScreenStatsMixin.java` / `CheckStatsCommand.java`.

---

## 4. Critical Invariant Rules (DO NOT BREAK)

### Rule 1: Attribute Comparison: NEVER use `.equals()` on RegistryEntry
Fabric's registry can return **different RegistryEntry instances** for the same attribute depending on class loading order.
```java
// ❌ WRONG - can silently fail
attr1.equals(attr2)
entry.attribute().equals(ModAttributes.MY_STAT)

// ✅ CORRECT - uses identity, equals, registry key, and translation key fallback
CombatUtility.attributesMatch(attr1, attr2)
```

### Rule 2: No Combat Double-Dipping
- Global damage modifiers (`MELEE_DAMAGE_PERCENT`, `PHYSICAL_DAMAGE_PERCENT`, weapon types, wielding, `DAMAGE_AGAINST_AFFLICTED_PERCENT`) are applied **once** in `BonusAttackDamageMixin`.
- `DamageConversionHelper` must **never** re-apply these modifiers in `baseMultiplier`.
- `DamageConversionHelper`'s `baseMultiplier` only contains `DAMAGE_WHILE_AFFLICTED_PERCENT` (`selfAfflictedBonus`).
- `ELEMENTAL_DAMAGE_PERCENT` only applies to Fire, Cold, and Lightning (NOT Void).

### Rule 3: Preserving Projectile Damage
- `PersistentProjectileEntity` in Minecraft already computes draw-scaled damage (`velocity * baseDamage`) before `damage()` is called.
- `DamageConversionHelper` must **preserve** the incoming `amount` and not discard or recalculate it from scratch.

### Rule 4: On-Kill Attribute Queries
- On-kill logic in `LivingEntityMixin` must query `player.getAttributeValue(ModAttributes.HEALTH_ON_KILL)` and `ModAttributes.MANA_ON_KILL` directly from the player attribute instance.
- Do NOT parse only the weapon's `AttributeModifiersComponent` (or passive tree nodes will be ignored).
- Requirement penalties are dynamically managed by `updateRequirementPenalties()` via negative attribute modifiers.

### Rule 5: Local vs Global Equipment Percent Detection
- Use attribute type, not stat ID strings:
  ```java
  // ❌ WRONG
  boolean isLocal = stat.getId().contains("local_evasion_percent");
  
  // ✅ CORRECT
  if (CombatUtility.attributesMatch(attribute, ModAttributes.EVASION_RATING_PERCENT)) { ... }
  ```

### Rule 6: Tooltip Double-Counting Prevention
- `EquipmentStatApplier` bakes flat values (Armor, Evasion, ES, Attack Damage) and local percentages directly into the item's `AttributeModifiersComponent`.
- `ItemStackTooltipMixin.calculateItemAttributeTotals()` must skip these baked stats when reading rune NBT from `ItemStatManager`.
