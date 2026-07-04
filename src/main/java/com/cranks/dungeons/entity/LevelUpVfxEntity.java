package com.cranks.dungeons.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.world.World;

public class LevelUpVfxEntity extends Entity {
    // True for World Tree (Green), False for Passive Tree (Gold)
    private static final TrackedData<Boolean> IS_WORLD_TREE = DataTracker.registerData(LevelUpVfxEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public LevelUpVfxEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(IS_WORLD_TREE, false);
    }

    public void setWorldTree(boolean isWorldTree) {
        this.dataTracker.set(IS_WORLD_TREE, isWorldTree);
    }

    public boolean isWorldTree() {
        return this.dataTracker.get(IS_WORLD_TREE);
    }

    @Override
    public void tick() {
        super.tick();
        
        // Follow nearest player
        net.minecraft.entity.player.PlayerEntity player = this.getEntityWorld().getClosestPlayer(this, 2.0);
        if (player != null) {
            this.setPosition(player.getX(), player.getY(), player.getZ());
        }

        if (this.getEntityWorld().isClient()) {
            // Ascending spiral particles
            float radius = 1.5f;
            for (int i = 0; i < 3; i++) {
                double angle = (this.age * 10 + i * 120) * Math.PI / 180.0;
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;
                double y = this.getY() + (this.age * 0.4) % 10.0;
                
                this.getEntityWorld().addParticleClient(
                    net.minecraft.particle.ParticleTypes.END_ROD, 
                    x, y, z, 
                    0.0, 0.1, 0.0
                );
            }
            
            // Central explosion particles
            if (this.age < 5) {
                for (int i = 0; i < 10; i++) {
                    this.getEntityWorld().addParticleClient(
                        net.minecraft.particle.ParticleTypes.FIREWORK, 
                        this.getX() + (this.random.nextDouble() - 0.5) * 2.0, 
                        this.getY() + this.random.nextDouble() * 2.0, 
                        this.getZ() + (this.random.nextDouble() - 0.5) * 2.0, 
                        (this.random.nextDouble() - 0.5) * 0.5, 
                        this.random.nextDouble() * 0.5, 
                        (this.random.nextDouble() - 0.5) * 0.5
                    );
                }
            }
        } else {
            if (this.age > 60) {
                this.discard();
            }
        }
    }

    @Override
    public boolean damage(net.minecraft.server.world.ServerWorld world, net.minecraft.entity.damage.DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readCustomData(net.minecraft.storage.ReadView nbt) {
    }

    @Override
    protected void writeCustomData(net.minecraft.storage.WriteView nbt) {
    }
}
