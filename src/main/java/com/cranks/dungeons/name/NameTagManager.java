package com.cranks.dungeons.name;

import com.cranks.dungeons.mount.MountManager;
import com.cranks.dungeons.network.ApplyNameTagPayload;
import com.cranks.dungeons.network.OpenNameTagScreenPayload;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Coordinates the client naming screen and validates every name-tag action on the server. */
public final class NameTagManager {
    public static final int MAX_NAME_LENGTH = 64;
    private record FreeName(UUID animal,RegistryKey<World> world,long expires){}
    private static final Map<ServerPlayerEntity,LinkedHashMap<UUID,FreeName>> FREE_NAMES=new WeakHashMap<>();

    private NameTagManager() {
    }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->FREE_NAMES.remove(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(server->FREE_NAMES.clear());
        UseEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {
            if (!isNameableTarget(target) || !player.getStackInHand(hand).isOf(Items.NAME_TAG)) {
                return ActionResult.PASS;
            }

            if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
                if(!canName(player,(LivingEntity)target)){
                    player.sendMessage(Text.literal("Only this animal's owner can rename it."),true);
                    return ActionResult.FAIL;
                }
                String currentName = target.getCustomName() == null
                        ? "" : limit(target.getCustomName().getString());
                ServerPlayNetworking.send(serverPlayer,
                        new OpenNameTagScreenPayload(target.getId(), currentName, hand == Hand.OFF_HAND));
            }
            return ActionResult.SUCCESS;
        });
    }

    public static void applyName(ServerPlayerEntity player, ApplyNameTagPayload payload) {
        String name = normalize(payload.name());
        if(!OpenNameTagScreenPayload.PAID.equals(payload.token())){
            applyFirstName(player,payload,name);
            return;
        }
        if (!isValidName(name)) return;

        Entity entity = player.getEntityWorld().getEntityById(payload.entityId());
        if (!isNameableTarget(entity)
                || !player.canInteractWithEntity(entity, 4.0)) return;

        Hand hand = payload.offHand() ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack held = player.getStackInHand(hand);
        if (!held.isOf(Items.NAME_TAG)) return;

        LivingEntity living = (LivingEntity) entity;
        if(!canName(player,living)){
            player.sendMessage(Text.literal("Only this animal's owner can rename it."),true);
            return;
        }
        living.setCustomName(Text.literal(name));
        if (entity instanceof MobEntity mob) mob.setPersistent();

        // Naming is also the ownership claim for camels, pigs, and striders.
        MountManager.claimNamedMount(player, living);
        if (!player.isCreative()) held.decrement(1);
    }

    /** Called only after a successful new tame, never on load, breeding or transfer. */
    public static UUID offerFirstName(ServerPlayerEntity player,AnimalEntity animal){
        if(!animal.isAlive()||!player.isAlive()||player.isSpectator()||!player.getUuid().equals(owner(animal)))return null;
        var access=(InitialNamingAccess)animal;
        if(access.cranks$wasNamingOffered())return null;
        access.cranks$setNamingOffered(true);
        com.cranks.dungeons.progression.WorldDiscovery.firstAnimal(player,animal,"tame");
        if(animal.hasCustomName())return null;
        var offers=FREE_NAMES.computeIfAbsent(player,p->new LinkedHashMap<>());
        offers.entrySet().removeIf(entry->entry.getValue().expires<player.getWorld().getTime());
        if(offers.size()>=16)offers.remove(offers.keySet().iterator().next());
        UUID token=UUID.randomUUID();
        offers.put(token,new FreeName(animal.getUuid(),animal.getWorld().getRegistryKey(),player.getWorld().getTime()+6000));
        ServerPlayNetworking.send(player,new OpenNameTagScreenPayload(animal.getId(),"",false,token));
        return token;
    }

    private static void applyFirstName(ServerPlayerEntity player,ApplyNameTagPayload payload,String name){
        var offers=FREE_NAMES.get(player);
        var offer=offers==null?null:offers.remove(payload.token());
        if(offers!=null&&offers.isEmpty())FREE_NAMES.remove(player);
        // Closing the prompt sends an empty name and spends the opportunity, not an item.
        if(name.isEmpty())return;
        var entity=player.getServerWorld().getEntityById(payload.entityId());
        if(offer==null||!isValidName(name)||offer.expires<player.getWorld().getTime()
                ||!offer.world.equals(player.getWorld().getRegistryKey())
                ||!(entity instanceof AnimalEntity animal)||!animal.isAlive()||animal.hasCustomName()
                ||!offer.animal.equals(animal.getUuid())||!player.isAlive()||player.isSpectator()
                ||player.squaredDistanceTo(animal)>256||!player.getUuid().equals(owner(animal))){
            player.sendMessage(Text.literal("This free naming opportunity has ended. Use a name tag to name the animal."),true);
            return;
        }
        animal.setCustomName(Text.literal(name));animal.setPersistent();
    }

    private static UUID owner(LivingEntity animal){
        if(animal instanceof TameableEntity pet)return pet.isTamed()?pet.getOwnerUuid():null;
        return com.cranks.dungeons.mount.MountOrders.owner(animal);
    }

    private static boolean isNameableTarget(Entity entity) {
        return entity instanceof LivingEntity living
                && !(entity instanceof PlayerEntity)
                && living.isAlive();
    }

    public static boolean canName(PlayerEntity player,LivingEntity target){
        // Unowned animals may still be named (and custom-owned species claimed).
        UUID owner=owner(target);
        return owner==null||owner.equals(player.getUuid())||player.isCreative();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.strip();
    }

    private static boolean isValidName(String name) {
        return !name.isEmpty()
                && name.length() <= MAX_NAME_LENGTH
                && name.codePoints().noneMatch(Character::isISOControl);
    }

    private static String limit(String value) {
        return value.length() <= MAX_NAME_LENGTH
                ? value : value.substring(0, MAX_NAME_LENGTH);
    }
}
