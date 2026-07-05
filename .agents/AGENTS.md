# Cranks Dungeons - Stat System Architecture

## Overview

This document explains how item stats (runes), local percentages, and attribute modifiers flow through the codebase. It exists to prevent future bugs related to double-counting, attribute comparison failures, and stat pipeline ordering.

---

## Core Classes

| Class | Role |
|---|---|
| `StatRegistry` | Defines all possible stats (runes) with their attributes, ranges, and equipment type filters |
| `CustomStat` / `StatComponent` | Data class for a stat definition. Contains attribute references, value ranges, and display info |
| `ItemStatManager` | Stores rolled stat values on items via NBT. Calls `EquipmentStatApplier.updateItemAttributes()` |
| `EquipmentStatApplier` | **Central hub**: Bakes rune stats into an item's `AttributeModifiersComponent` |
| `ItemStackTooltipMixin` | Reads the baked component + remaining rune stats to render the tooltip |
| `LivingEntityMixin` | Handles requirement penalties, intrinsic stat bonuses, and armor speed |
| `ItemDefaultComponentsMixin` | Sets base stats for armor pieces (e.g., leather gets Evasion Rating, gold gets Energy Shield) |

---

## Stat Application Pipeline

```
┌──────────────────────────────────────────────────────────────────┐
│  1. Player applies rune / uses Alchemy Tome / Quality Station   │
│     → ItemStatManager stores values in NBT                      │
│     → Calls EquipmentStatApplier.updateItemAttributes(stack)    │
└────────────────────────────┬─────────────────────────────────────┘
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│  2. EquipmentStatApplier.updateItemAttributes(stack)            │
│                                                                  │
│  Step A: Classify each rune stat by attribute type:              │
│    • LOCAL PERCENT (ARMOR_PERCENT, EVASION_RATING_PERCENT,       │
│      ENERGY_SHIELD_PERCENT, ATTACK_DAMAGE_PERCENT)               │
│      → Accumulated into localXPercent buffers                    │
│      → NOT added as direct modifiers (continue)                  │
│    • FLAT STAT (e.g. +Armor, +Evasion Rating)                   │
│      → Stored in flatRuneValues map                              │
│    • OTHER PERCENTAGE (resistances, crit chance, etc.)           │
│      → Added directly to builder as ADD_VALUE                    │
│    • MULTIPLICATIVE (attack speed, reload speed)                 │
│      → Added directly to builder as ADD_MULTIPLIED_BASE          │
│                                                                  │
│  Step B: Read base item defaults from a fresh ItemStack          │
│    • Apply quality scaling                                       │
│    • Capture actual RegistryEntry references for lookup           │
│                                                                  │
│  Step C: Merge flat rune values into base values                 │
│    • finalValue = base + flatRune                                │
│                                                                  │
│  Step D: Apply local percentages                                 │
│    • finalValue = (base + flatRune) * (1 + localPercent)         │
│                                                                  │
│  Step E: Write everything into a new AttributeModifiersComponent │
│    • stack.set(ATTRIBUTE_MODIFIERS, builder.build())             │
└────────────────────────────┬─────────────────────────────────────┘
                             ▼
┌──────────────────────────────────────────────────────────────────┐
│  3. Minecraft applies the component automatically when equipped  │
│     → Player gets the stat bonuses                               │
└──────────────────────────────────────────────────────────────────┘
```

---

## Critical Rules

### 1. Attribute Comparison: NEVER use `.equals()` on RegistryEntry

Fabric's registry can return **different RegistryEntry objects** for the same attribute depending on when/how they were loaded. This means:

```java
// ❌ WRONG - may fail even for the same attribute
attribute.equals(ModAttributes.EVASION_RATING_PERCENT)

// ✅ CORRECT - falls back to translation key comparison
isAttribute(attribute, ModAttributes.EVASION_RATING_PERCENT)   // in EquipmentStatApplier
attributesMatch(runeAttr, ModAttributes.EVASION_RATING)        // in ItemStackTooltipMixin
```

### 2. Local Percent Detection: Use attribute TYPE, not stat ID strings

```java
// ❌ WRONG - fragile, breaks when stat IDs don't contain expected substrings
boolean isLocalEvasion = stat.getId().contains("local_evasion_percent");

// ✅ CORRECT - works for ALL stats that use this attribute
if (isAttribute(attribute, ModAttributes.EVASION_RATING_PERCENT)) {
    localEvasionPercent += val;
}
```

The stat `evasion_rating_percent` generates IDs like `chestplate_evasion_rating_percent`, which does NOT contain `local_evasion_percent`. Checking by attribute type is always correct.

### 3. Tooltip Double-Counting Prevention

`EquipmentStatApplier` bakes these values into the item's `AttributeModifiersComponent`:
- Flat rune values for armor, evasion, energy shield, attack damage
- Local percentage scaling of those values

Therefore, `ItemStackTooltipMixin.calculateItemAttributeTotals()` must **skip** these stats when reading from `ItemStatManager.getStats()`, or they'll be counted twice.

Stats that ARE safe to read from ItemStatManager in the tooltip:
- Resistances (fire, cold, lightning, void)
- Max health
- Movement speed
- Crit stats
- Any stat NOT baked into the AttributeModifiersComponent

### 4. Requirement Penalty System

When a player doesn't meet an item's stat requirements (STR/DEX/INT), `LivingEntityMixin.updateRequirementPenalties()` applies a **50% penalty** as a negative modifier with the **same operation type** as the original.

- Flat stats: penalty = `-value * 0.5` with `ADD_VALUE`
- Attack speed: special-cased to halve the final speed, not the raw modifier

### 5. Stat Registration Patterns

| Registration Method | ID Format | Example |
|---|---|---|
| `registerForTypes("evasion_rating_percent", ...)` | `{type}_evasion_rating_percent` | `chestplate_evasion_rating_percent` |
| `registerHybridForTypes("hybrid_dex_eva_life", ...)` | `{type}_hybrid_dex_eva_life` | `chestplate_hybrid_dex_eva_life` |

The base name is just a label — the actual stat ID always has the equipment type prefix.

---

## Common Pitfalls (for future agents)

1. **Adding a new local percent stat?** Add it to the detection block in `EquipmentStatApplier.updateItemAttributes()` AND to the skip list in `ItemStackTooltipMixin.calculateItemAttributeTotals()`.

2. **Adding a new flat stat that gets baked into the component?** Add it to the skip list in `ItemStackTooltipMixin.calculateItemAttributeTotals()`.

3. **Comparing attributes anywhere?** Use `isAttribute()` or `attributesMatch()`, never `.equals()`.

4. **Why isn't my percent stat working?** Check if the stat's `StatComponent` has a non-null attribute. If the attribute is `null`, the component's attribute list is empty and no loops will execute.
