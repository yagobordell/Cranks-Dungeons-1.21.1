package com.cranks.dungeons.alchemy;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.*;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.*;

/** Six bottle forms. Contents use vanilla potion components so thrown effects retain the combat pipeline. */
public final class AlchemyItems {
    public enum Delivery { DRINK, SPLASH, LINGERING }
    public static final Map<String, Item> REAGENTS = new LinkedHashMap<>();
    private static final Item[][] EMPTY = new Item[2][3];
    private static final Item[][] FILLED = new Item[2][3];
    private AlchemyItems() {}

    public static Identifier id(String path) { return Identifier.of("cranks-dungeons", path); }
    public static Item reagent(String name) { return Objects.requireNonNull(REAGENTS.get(name), name); }
    public static Item empty(boolean lesser, Delivery delivery) { return EMPTY[lesser ? 0 : 1][delivery.ordinal()]; }
    public static Item filled(boolean lesser, Delivery delivery) { return FILLED[lesser ? 0 : 1][delivery.ordinal()]; }

    public static void register() {
        for (String name : List.of("melon_extract", "honey_extract", "bone_salt", "sugar_extract", "heat_salt",
                "turtle_extract", "rabbit_foot_extract", "golden_carrot_extract", "pufferfish_extract", "membrane_extract",
                "fermented_eye_extract", "spider_eye_extract", "mushroom_extract", "lapis_salt", "wind_essence",
                "thread_extract", "slime_extract", "stone_salt", "frost_salt", "copper_salt", "amethyst_essence",
                "amethyst_dust", "glowing_catalyst", "wheat_suspension", "cane_suspension", "dandelion_suspension",
                "clay_suspension", "flint_suspension", "calcite_suspension", "slime_suspension", "flesh_suspension", "leather_suspension")) {
            REAGENTS.put(name, Registry.register(Registries.ITEM, id(name), new Reagent(name)));
        }
        for (int tier = 0; tier < 2; tier++) {
            boolean lesser = tier == 0;
            for (Delivery delivery : Delivery.values()) {
                String stem = (lesser ? "lesser_" : "") + switch (delivery) {
                    case DRINK -> "bottle";
                    case SPLASH -> "splash_bottle";
                    case LINGERING -> "lingering_bottle";
                };
                EMPTY[tier][delivery.ordinal()] = !lesser && delivery == Delivery.DRINK ? Items.GLASS_BOTTLE
                        : Registry.register(Registries.ITEM, id(stem), new Bottle(lesser, delivery, new Item.Settings()));
                FILLED[tier][delivery.ordinal()] = Registry.register(Registries.ITEM, id(stem + "_filled"),
                        new Brew(lesser, delivery, new Item.Settings().maxCount(16)
                                .component(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.WATER))));
            }
        }
        // Resolve old saves and data packs to the same vanilla item, rather than keeping a second bottle.
        ((net.fabricmc.fabric.api.event.registry.FabricRegistry) Registries.ITEM)
                .addAlias(id("bottle"), Registries.ITEM.getId(Items.GLASS_BOTTLE));
        AlchemyCauldrons.register();
        net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents.MODIFY.register(context ->
                context.modify(item -> item instanceof PotionItem, (builder, item) -> builder.add(DataComponentTypes.MAX_STACK_SIZE, 16)));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            REAGENTS.values().forEach(entries::add);
            for (Item[] tier : EMPTY) for (Item bottle : tier) if (bottle != Items.GLASS_BOTTLE) entries.add(bottle);
        });
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            for (var formula : AlchemyPotions.FORMULAS) for (Delivery delivery : Delivery.values()) entries.add(formula.stack(delivery));
        });
        for (boolean lesser : new boolean[]{true, false}) {
            net.minecraft.block.DispenserBlock.registerBehavior(filled(lesser, Delivery.SPLASH),
                    new net.minecraft.block.dispenser.ProjectileDispenserBehavior(Items.SPLASH_POTION));
            net.minecraft.block.DispenserBlock.registerBehavior(filled(lesser, Delivery.LINGERING),
                    new net.minecraft.block.dispenser.ProjectileDispenserBehavior(Items.LINGERING_POTION));
        }
    }

    public static ItemStack water(boolean lesser, Delivery delivery) {
        ItemStack stack = filled(lesser, delivery).getDefaultStack();
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.WATER));
        return stack;
    }

    private static final class Reagent extends Item {
        private final String name;
        Reagent(String name) { super(new Settings()); this.name = name; }
        @Override public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            tooltip.add(Text.translatable("alchemy.cranks-dungeons.hint." + name).formatted(Formatting.GRAY));
        }
    }

    public static final class Bottle extends GlassBottleItem {
        public final boolean lesser;
        public final Delivery delivery;
        Bottle(boolean lesser, Delivery delivery, Settings settings) {
            super(settings);
            this.lesser = lesser;
            this.delivery = delivery;
        }

        @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
            ItemStack stack = player.getStackInHand(hand);
            HitResult hit = raycast(world, player, RaycastContext.FluidHandling.SOURCE_ONLY);
            if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK
                    || !world.canPlayerModifyAt(player, block.getBlockPos())
                    || !world.getFluidState(block.getBlockPos()).isIn(FluidTags.WATER)) return TypedActionResult.pass(stack);
            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.PLAYERS, 1, 1);
            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, block.getBlockPos());
            return TypedActionResult.success(fill(stack, player, water(lesser, delivery)), world.isClient());
        }
    }

    public static final class Brew extends PotionItem {
        public final boolean lesser;
        public final Delivery delivery;
        Brew(boolean lesser, Delivery delivery, Settings settings) {
            super(settings);
            this.lesser = lesser;
            this.delivery = delivery;
        }

        @Override public Text getName(ItemStack stack) { return AlchemyPotions.name(stack, lesser, delivery); }

        @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
            if (delivery == Delivery.DRINK) return super.use(world, player, hand);
            ItemStack stack = player.getStackInHand(hand);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), delivery == Delivery.LINGERING
                    ? SoundEvents.ENTITY_LINGERING_POTION_THROW : SoundEvents.ENTITY_SPLASH_POTION_THROW,
                    SoundCategory.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!world.isClient()) {
                PotionEntity projectile = new PotionEntity(world, player);
                projectile.setItem(stack.copyWithCount(1));
                projectile.setVelocity(player, player.getPitch(), player.getYaw(), -20, 0.5F, 1);
                world.spawnEntity(projectile);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            stack.decrementUnlessCreative(1, player);
            return TypedActionResult.success(stack, world.isClient());
        }

        @Override public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
            if (delivery != Delivery.DRINK) return stack;
            PlayerEntity player = user instanceof PlayerEntity p ? p : null;
            if (player instanceof ServerPlayerEntity server) net.minecraft.advancement.criterion.Criteria.CONSUME_ITEM.trigger(server, stack);
            if (!world.isClient()) {
                for (var effect : stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects()) {
                    if (effect.getEffectType().value().isInstant()) effect.getEffectType().value().applyInstantEffect(player, player, user, effect.getAmplifier(), 1);
                    else user.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(effect));
                }
            }
            if (player != null) player.incrementStat(Stats.USED.getOrCreateStat(this));
            user.emitGameEvent(GameEvent.DRINK);
            if (player == null || !player.isCreative()) {
                stack.decrement(1);
                ItemStack bottle = empty(lesser, delivery).getDefaultStack();
                if (stack.isEmpty()) return bottle;
                if (player != null && !world.isClient() && !player.getInventory().insertStack(bottle)) player.dropItem(bottle, false);
            }
            return stack;
        }

        @Override public ActionResult useOnBlock(ItemUsageContext context) {
            ItemStack stack = context.getStack();
            PlayerEntity player = context.getPlayer();
            World world = context.getWorld();
            if (delivery != Delivery.DRINK || player == null
                    || !stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).matches(Potions.WATER)
                    || !world.getBlockState(context.getBlockPos()).isIn(net.minecraft.registry.tag.BlockTags.CONVERTABLE_TO_MUD)) return ActionResult.PASS;
            world.playSound(player, context.getBlockPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1, 1);
            if (!world.isClient()) {
                world.setBlockState(context.getBlockPos(), net.minecraft.block.Blocks.MUD.getDefaultState());
                player.setStackInHand(context.getHand(), ItemUsage.exchangeStack(stack, player, empty(lesser, delivery).getDefaultStack()));
                player.incrementStat(Stats.USED.getOrCreateStat(this));
                world.emitGameEvent(player, GameEvent.FLUID_PLACE, context.getBlockPos());
            }
            return ActionResult.success(world.isClient());
        }

        @Override public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, net.minecraft.item.tooltip.TooltipType type) {
            stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)
                    .buildTooltip(tooltip::add, delivery == Delivery.LINGERING ? 0.25F : 1, context.getUpdateTickRate());
        }
    }
}
