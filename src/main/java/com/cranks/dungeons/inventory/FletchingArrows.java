package com.cranks.dungeons.inventory;

import com.cranks.dungeons.alchemy.AlchemyItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;
import java.util.Optional;

/** A curated ammunition catalogue, with effects balanced independently of drinkable potions. */
public final class FletchingArrows {
    public static final double VOID_CONVERSION = 0.30;
    public enum Kind {
        POISON("poison", "spider_eye_extract", 0x628A32),
        SLOWING("slowing", "mushroom_extract", 0x6887A0),
        WEAKENING("weakening", "fermented_eye_extract", 0x716B80),
        VOID("void", "amethyst_essence", 0x9B69C7),
        MARKING("marking", "glowing_catalyst", 0xE2CF60);
        public final String id, component;
        public final int color;
        private RegistryEntry<Potion> potion;
        Kind(String id, String component, int color) { this.id = id; this.component = component; this.color = color; }
        public ItemStack stack(int count) {
            var stack = new ItemStack(Items.TIPPED_ARROW, count);
            stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.of(potion), Optional.of(color), List.of()));
            return stack;
        }
    }
    private FletchingArrows() {}
    public static void register() {
        for (var kind : Kind.values()) {
            // Vanilla arrows divide their base potion duration by eight on impact.
            StatusEffectInstance[] effects = switch (kind) {
                case POISON -> new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.POISON, 6 * 20 * 8, 1)};
                case SLOWING -> new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SLOWNESS, 4 * 20 * 8, 1)};
                case WEAKENING -> new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.WEAKNESS, 5 * 20 * 8, 0)};
                case MARKING -> new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.GLOWING, 10 * 20 * 8, 0)};
                case VOID -> new StatusEffectInstance[0];
            };
            kind.potion = Registry.registerReference(Registries.POTION, AlchemyItems.id("arrow_" + kind.id), new Potion("arrow_" + kind.id, effects));
        }
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.getDisplayStacks().removeIf(stack -> stack.isOf(Items.TIPPED_ARROW) || stack.isOf(Items.SPECTRAL_ARROW));
            entries.getSearchTabStacks().removeIf(stack -> stack.isOf(Items.TIPPED_ARROW) || stack.isOf(Items.SPECTRAL_ARROW));
            for (var kind : Kind.values()) entries.add(kind.stack(1));
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.getDisplayStacks().removeIf(FletchingArrows::hasArrowProfile);
            entries.getSearchTabStacks().removeIf(FletchingArrows::hasArrowProfile);
        });
    }
    private static boolean hasArrowProfile(ItemStack stack) {
        var potion = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion();
        return potion.isPresent() && java.util.Arrays.stream(Kind.values()).anyMatch(kind -> potion.get().equals(kind.potion));
    }
    public static Kind kind(ItemStack stack) {
        if (!stack.isOf(Items.TIPPED_ARROW)) return null;
        var potion = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion();
        for (var kind : Kind.values()) if (potion.isPresent() && potion.get().equals(kind.potion)) return kind;
        return null;
    }
    public static boolean isVoid(net.minecraft.entity.Entity entity) {
        return entity instanceof PersistentProjectileEntity arrow && kind(arrow.getItemStack()) == Kind.VOID;
    }
    public static ItemStack canonicalize(ItemStack stack) {
        if (stack.isOf(Items.SPECTRAL_ARROW)) return convert(stack, Kind.MARKING);
        if (!stack.isOf(Items.ARROW) && !stack.isOf(Items.TIPPED_ARROW) && stack.getItem() instanceof net.minecraft.item.ArrowItem) return convert(stack, null);
        if (!stack.isOf(Items.TIPPED_ARROW) && !(stack.isOf(Items.ARROW) && stack.contains(DataComponentTypes.POTION_CONTENTS))) return stack;
        var contents = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        var id = contents.potion().flatMap(RegistryEntry::getKey).map(key -> key.getValue()).orElse(null);
        var own = kind(stack);
        if (own == null && contents.potion().isPresent()) {
            for (var candidate : Kind.values()) if (contents.potion().get().equals(candidate.potion)) { own = candidate; break; }
        }
        if (own != null) {
            var canonical = own.stack(1).get(DataComponentTypes.POTION_CONTENTS);
            return stack.isOf(Items.TIPPED_ARROW) && contents.equals(canonical) && !stack.contains(DataComponentTypes.CUSTOM_MODEL_DATA) ? stack : convert(stack, own);
        }
        String family = id == null || !id.getNamespace().equals("minecraft") && !id.getNamespace().equals("cranks-dungeons") ? "" : id.getPath().replaceFirst("^(lesser_|long_|strong_)", "");
        var kind = switch (family) {
            case "poison" -> Kind.POISON;
            case "slowness" -> Kind.SLOWING;
            case "weakness" -> Kind.WEAKENING;
            case "harming" -> Kind.VOID;
            default -> null;
        };
        return convert(stack, kind);
    }
    private static ItemStack convert(ItemStack previous, Kind kind) {
        var result = previous.copyComponentsToNewStack(kind == null ? Items.ARROW : Items.TIPPED_ARROW, previous.getCount());
        result.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (kind == null) result.remove(DataComponentTypes.POTION_CONTENTS);
        else result.set(DataComponentTypes.POTION_CONTENTS, kind.stack(1).get(DataComponentTypes.POTION_CONTENTS));
        return result;
    }
}
