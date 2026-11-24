package com.cranks.dungeons.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

/** A short-lived, owner-bound anchor for the client's level-up motes. */
public class LevelUpVfxEntity extends Entity {
    public static final int DURATION_TICKS = 22;
    public static final float SOUND_VOLUME = .65f;
    private static final TrackedData<Boolean> IS_WORLD_TREE = DataTracker.registerData(LevelUpVfxEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> OWNER_ID = DataTracker.registerData(LevelUpVfxEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public LevelUpVfxEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public static boolean spawn(PlayerEntity player, boolean worldTree) {
        if (player.getWorld().isClient()) return false;
        // A batch of levels is one celebration; overlapping discoveries do not stack visual layers.
        for (var existing : player.getWorld().getEntitiesByClass(LevelUpVfxEntity.class,
                player.getBoundingBox().expand(2), vfx -> vfx.getOwnerId() == player.getId() && vfx.isWorldTree() == worldTree)) {
            return false;
        }
        var effect = new LevelUpVfxEntity(com.cranks.dungeons.registry.ModEntities.LEVEL_UP_VFX, player.getWorld());
        effect.setOwner(player);
        effect.setWorldTree(worldTree);
        return player.getWorld().spawnEntity(effect);
    }

    public static void celebrate(PlayerEntity player, boolean worldTree) {
        if (!spawn(player, worldTree)) return;
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                worldTree ? com.cranks.dungeons.registry.ModSounds.WORLD_LEVEL_UP
                        : com.cranks.dungeons.registry.ModSounds.PASSIVE_LEVEL_UP,
                net.minecraft.sound.SoundCategory.PLAYERS, SOUND_VOLUME, 1);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(IS_WORLD_TREE, false);
        builder.add(OWNER_ID, -1);
    }

    public void setOwner(PlayerEntity player) {
        dataTracker.set(OWNER_ID, player.getId());
        setPosition(player.getPos());
    }

    public int getOwnerId() { return dataTracker.get(OWNER_ID); }
    public void setWorldTree(boolean worldTree) { dataTracker.set(IS_WORLD_TREE, worldTree); }
    public boolean isWorldTree() { return dataTracker.get(IS_WORLD_TREE); }

    @Override
    public void tick() {
        super.tick();
        var owner = getWorld().getEntityById(getOwnerId());
        if (owner instanceof PlayerEntity player && player.isAlive()) {
            setPosition(player.getPos());
        } else if (!getWorld().isClient()) {
            discard();
            return;
        }
        if (age >= DURATION_TICKS) discard();
    }

    @Override public net.minecraft.util.math.Box getVisibilityBoundingBox() { return getBoundingBox().expand(.15, .3, .15); }
    @Override public boolean shouldSave() { return false; }
    @Override public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) { return false; }
    @Override protected void readCustomDataFromNbt(NbtCompound nbt) {}
    @Override protected void writeCustomDataToNbt(NbtCompound nbt) {}
}
