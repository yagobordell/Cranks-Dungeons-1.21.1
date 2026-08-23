package com.cranks.dungeons.equipment;

import com.cranks.dungeons.item.CustomRarity;
import com.cranks.dungeons.stat.CustomStat;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;

/**
 * Handles Act 1 Mob Equipment Generation, Quality rolling, Enchantment Slot rolling,
 * and Runic Affixes (Tiers 10-8).
 */
public class MobEquipmentManager {

    private enum ArmorMaterialType {
        LEATHER,
        GOLD,
        CHAINMAIL,
        IRON
    }

    /**
     * Equips a mob with Act 1 gear, rolling quality, enchantment slots, and runic affixes.
     */
    public static void equipAct1Mob(MobEntity mob, Random random, LocalDifficulty difficulty) {
        boolean isZombie = mob instanceof ZombieEntity && !(mob instanceof ZombifiedPiglinEntity);
        boolean isSkeleton = mob instanceof AbstractSkeletonEntity && !(mob instanceof WitherSkeletonEntity);
        boolean isPiglin = mob instanceof PiglinEntity && !(mob instanceof PiglinBruteEntity);

        if (!isZombie && !isSkeleton && !isPiglin) return;

        // 1. Always strip vanilla armor & weapons first so Act 1 tables have 100% authority
        clearArmor(mob);
        if (isZombie) {
            mob.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        // 2. For Skeletons: Ensure their default Bow is clean of enchantments and processed with Act 1 rules
        if (isSkeleton) {
            ItemStack bow = mob.getMainHandStack();
            if (bow.isEmpty() || !bow.isOf(Items.BOW)) {
                bow = new ItemStack(Items.BOW);
            }
            ItemStack processedBow = processAct1Item(bow, random);
            mob.equipStack(EquipmentSlot.MAINHAND, processedBow);
            mob.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.085F);
        }

        // 3. Base ~22% chance for mob to receive armor/weapon gear from Act 1 tables
        if (random.nextFloat() > 0.22f) return;

        // Piece distribution:
        // 60% 1 piece, 25% 2 pieces, 10% weapon only, 5% full set
        float pieceRoll = random.nextFloat();
        ArmorMaterialType armorMat = rollAct1ArmorMaterial(random);

        if (pieceRoll < 0.60f) {
            // 1 Piece of Armor
            EquipmentSlot slot = rollRandomArmorSlot(random);
            ItemStack armorPiece = createArmorPiece(armorMat, slot, random);
            equipPiece(mob, slot, armorPiece);
        } else if (pieceRoll < 0.85f) {
            // 2 Pieces
            if (random.nextBoolean()) {
                // 2 armor pieces (e.g. Helmet + Boots)
                EquipmentSlot slot1 = rollRandomArmorSlot(random);
                EquipmentSlot slot2 = rollDifferentArmorSlot(random, slot1);
                equipPiece(mob, slot1, createArmorPiece(armorMat, slot1, random));
                equipPiece(mob, slot2, createArmorPiece(armorMat, slot2, random));
            } else {
                // 1 armor piece + 1 weapon
                EquipmentSlot slot = rollRandomArmorSlot(random);
                equipPiece(mob, slot, createArmorPiece(armorMat, slot, random));
                if (isZombie || isPiglin) {
                    equipPiece(mob, EquipmentSlot.MAINHAND, createAct1Weapon(random));
                }
            }
        } else if (pieceRoll < 0.95f) {
            // Weapon Only
            if (isZombie || isPiglin) {
                equipPiece(mob, EquipmentSlot.MAINHAND, createAct1Weapon(random));
            } else if (isSkeleton) {
                equipPiece(mob, EquipmentSlot.MAINHAND, createAct1Bow(random));
            }
        } else {
            // Full 4-Piece Set (Elite mini-champion)
            equipPiece(mob, EquipmentSlot.HEAD, createArmorPiece(armorMat, EquipmentSlot.HEAD, random));
            equipPiece(mob, EquipmentSlot.CHEST, createArmorPiece(armorMat, EquipmentSlot.CHEST, random));
            equipPiece(mob, EquipmentSlot.LEGS, createArmorPiece(armorMat, EquipmentSlot.LEGS, random));
            equipPiece(mob, EquipmentSlot.FEET, createArmorPiece(armorMat, EquipmentSlot.FEET, random));
            if (isZombie || isPiglin) {
                equipPiece(mob, EquipmentSlot.MAINHAND, createAct1Weapon(random));
            }
        }
    }

    /**
     * Processes an item applying Quality, Enchantment Slots, and Runic Affixes (T10-T8).
     */
    public static ItemStack processAct1Item(ItemStack stack, Random random) {
        if (stack.isEmpty()) return stack;

        // Ensure 0 enchantments on mob equipment in Act 1
        stack.remove(DataComponentTypes.ENCHANTMENTS);
        stack.remove(DataComponentTypes.STORED_ENCHANTMENTS);

        // 1. Quality Roll (Rare: 10% chance, weighted so higher quality is progressively rarer)
        if (random.nextFloat() < 0.10f) {
            int quality = rollAct1Quality(random);
            setQuality(stack, quality);
        }

        // 2. Enchantment Slots Roll (5% for 1 slot, 0.5% for 2 slots, 0.05% for 3 slots, up to max allowed)
        int maxSlots = EnchantmentSlotHelper.getMaxEnchantmentSlots(stack);
        if (maxSlots > 0 && random.nextFloat() < 0.05f) {
            int slots = 1;
            if (maxSlots >= 2 && random.nextFloat() < 0.10f) {
                slots = 2;
                if (maxSlots >= 3 && random.nextFloat() < 0.10f) {
                    slots = 3;
                }
            }
            EnchantmentSlotHelper.setEnchantmentSlots(stack, slots);
        }

        // 3. Runic Affixes Roll
        // Normal: 70%, Magic (1-2 runes): 24%, Rare (3-4 runes): 6%
        float rarityRoll = random.nextFloat();
        if (rarityRoll < 0.70f) {
            ItemStatManager.setRarity(stack, CustomRarity.NORMAL);
        } else if (rarityRoll < 0.94f) {
            // Magic (1-2 Runes)
            ItemStatManager.setRarity(stack, CustomRarity.MAGIC);
            int runesCount = random.nextBoolean() ? 1 : 2;
            for (int i = 0; i < runesCount; i++) {
                int tier = rollAct1Tier(random); // 10 (60%), 9 (30%), 8 (10%)
                CustomStat.AffixType type = (i == 0) ?
                    (random.nextBoolean() ? CustomStat.AffixType.PREFIX : CustomStat.AffixType.SUFFIX) :
                    (ItemStatManager.canAddStat(stack, CustomStat.AffixType.PREFIX) ? CustomStat.AffixType.PREFIX : CustomStat.AffixType.SUFFIX);
                ItemStatManager.addRandomStatWithFixedTier(stack, tier, type);
            }
        } else {
            // Rare (3-4 Runes)
            ItemStatManager.setRarity(stack, CustomRarity.RARE);
            int runesCount = random.nextBoolean() ? 3 : 4;
            for (int i = 0; i < runesCount; i++) {
                int tier = rollAct1Tier(random);
                CustomStat.AffixType type = random.nextBoolean() ? CustomStat.AffixType.PREFIX : CustomStat.AffixType.SUFFIX;
                if (!ItemStatManager.canAddStat(stack, type)) {
                    type = (type == CustomStat.AffixType.PREFIX) ? CustomStat.AffixType.SUFFIX : CustomStat.AffixType.PREFIX;
                }
                ItemStatManager.addRandomStatWithFixedTier(stack, tier, type);
            }
        }

        // Bake stats and quality into AttributeModifiersComponent
        EquipmentStatApplier.updateItemAttributes(stack);
        return stack;
    }

    private static int rollAct1Quality(Random random) {
        float r = random.nextFloat();
        if (r < 0.55f) return random.nextInt(5) + 1;   // 1% - 5% (55% of quality rolls)
        if (r < 0.80f) return random.nextInt(5) + 6;   // 6% - 10% (25% of quality rolls)
        if (r < 0.92f) return random.nextInt(5) + 11;  // 11% - 15% (12% of quality rolls)
        if (r < 0.98f) return random.nextInt(4) + 16;  // 16% - 19% (6% of quality rolls)
        return 20;                                     // 20% Max Quality Jackpot (2% of quality rolls)
    }

    private static int rollAct1Tier(Random random) {
        float r = random.nextFloat();
        if (r < 0.60f) return 10; // Tier 10 (60% - Starter)
        if (r < 0.90f) return 9;  // Tier 9 (30% - Mid early)
        return 8;                 // Tier 8 (10% - Rare Act 1 ceiling)
    }

    private static void setQuality(ItemStack stack, int quality) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        nbt.putInt("Quality", quality);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private static ArmorMaterialType rollAct1ArmorMaterial(Random random) {
        float r = random.nextFloat();
        if (r < 0.40f) return ArmorMaterialType.LEATHER;   // 40% Leather (Evasion)
        if (r < 0.70f) return ArmorMaterialType.GOLD;      // 30% Gold (Energy Shield)
        if (r < 0.90f) return ArmorMaterialType.CHAINMAIL; // 20% Chainmail (Hybrid)
        return ArmorMaterialType.IRON;                     // 10% Iron (Heavy Tank)
    }

    private static EquipmentSlot rollRandomArmorSlot(Random random) {
        float r = random.nextFloat();
        if (r < 0.35f) return EquipmentSlot.HEAD;
        if (r < 0.65f) return EquipmentSlot.FEET;
        if (r < 0.85f) return EquipmentSlot.CHEST;
        return EquipmentSlot.LEGS;
    }

    private static EquipmentSlot rollDifferentArmorSlot(Random random, EquipmentSlot first) {
        EquipmentSlot[] all = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
        EquipmentSlot second;
        do {
            second = all[random.nextInt(all.length)];
        } while (second == first);
        return second;
    }

    private static ItemStack createArmorPiece(ArmorMaterialType material, EquipmentSlot slot, Random random) {
        Item item = switch (material) {
            case LEATHER -> switch (slot) {
                case HEAD -> Items.LEATHER_HELMET;
                case CHEST -> Items.LEATHER_CHESTPLATE;
                case LEGS -> Items.LEATHER_LEGGINGS;
                default -> Items.LEATHER_BOOTS;
            };
            case GOLD -> switch (slot) {
                case HEAD -> Items.GOLDEN_HELMET;
                case CHEST -> Items.GOLDEN_CHESTPLATE;
                case LEGS -> Items.GOLDEN_LEGGINGS;
                default -> Items.GOLDEN_BOOTS;
            };
            case CHAINMAIL -> switch (slot) {
                case HEAD -> Items.CHAINMAIL_HELMET;
                case CHEST -> Items.CHAINMAIL_CHESTPLATE;
                case LEGS -> Items.CHAINMAIL_LEGGINGS;
                default -> Items.CHAINMAIL_BOOTS;
            };
            case IRON -> switch (slot) {
                case HEAD -> Items.IRON_HELMET;
                case CHEST -> Items.IRON_CHESTPLATE;
                case LEGS -> Items.IRON_LEGGINGS;
                default -> Items.IRON_BOOTS;
            };
        };

        return processAct1Item(new ItemStack(item), random);
    }

    private static ItemStack createAct1Weapon(Random random) {
        float r = random.nextFloat();
        Item weapon;
        if (r < 0.25f) weapon = Items.WOODEN_SWORD;
        else if (r < 0.70f) weapon = Items.STONE_SWORD;
        else if (r < 0.95f) weapon = Items.IRON_SWORD;
        else weapon = Items.STONE_SHOVEL;

        return processAct1Item(new ItemStack(weapon), random);
    }

    private static ItemStack createAct1Bow(Random random) {
        Item bow = (random.nextFloat() < 0.20f) ? Items.CROSSBOW : Items.BOW;
        return processAct1Item(new ItemStack(bow), random);
    }

    private static void equipPiece(MobEntity mob, EquipmentSlot slot, ItemStack stack) {
        mob.equipStack(slot, stack);
        mob.setEquipmentDropChance(slot, 0.085F);
    }

    private static void clearArmor(MobEntity mob) {
        mob.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
        mob.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
        mob.equipStack(EquipmentSlot.LEGS, ItemStack.EMPTY);
        mob.equipStack(EquipmentSlot.FEET, ItemStack.EMPTY);
    }
}
