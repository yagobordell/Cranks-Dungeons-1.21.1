/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1690
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package punchy.mixin.client;

import net.minecraft.class_1690;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import punchy.client.render.BoatLidState;

@Mixin(value={class_1690.class})
public class BoatRenderStateMixin
implements BoatLidState {
    @Unique
    private double punchy$vy;
    @Unique
    private boolean punchy$chestBoat;
    @Unique
    private boolean punchy$inHand;
    @Unique
    private boolean punchy$lidApplied;
    @Unique
    private boolean punchy$chestPoseValid;
    @Unique
    private float punchy$chestX;
    @Unique
    private float punchy$chestY;
    @Unique
    private float punchy$chestZ;
    @Unique
    private float punchy$chestRotX;
    @Unique
    private float punchy$chestRotY;
    @Unique
    private float punchy$chestRotZ;
    @Unique
    private float punchy$lidOpen;
    @Unique
    private float punchy$lidVel;
    @Unique
    private long punchy$lidLastUpdateNanos;
    @Unique
    private int punchy$light = -1;

    @Override
    public void punchy$setVy(double vy) {
        this.punchy$vy = vy;
    }

    @Override
    public double punchy$getVy() {
        return this.punchy$vy;
    }

    @Override
    public void punchy$setChestBoat(boolean chestBoat) {
        this.punchy$chestBoat = chestBoat;
    }

    @Override
    public boolean punchy$isChestBoat() {
        return this.punchy$chestBoat;
    }

    @Override
    public void punchy$setInHand(boolean inHand) {
        this.punchy$inHand = inHand;
    }

    @Override
    public boolean punchy$isInHand() {
        return this.punchy$inHand;
    }

    @Override
    public void punchy$setLidApplied(boolean applied) {
        this.punchy$lidApplied = applied;
    }

    @Override
    public boolean punchy$isLidApplied() {
        return this.punchy$lidApplied;
    }

    @Override
    public void punchy$setChestPose(float x, float y, float z, float rotX, float rotY, float rotZ) {
        this.punchy$chestPoseValid = true;
        this.punchy$chestX = x;
        this.punchy$chestY = y;
        this.punchy$chestZ = z;
        this.punchy$chestRotX = rotX;
        this.punchy$chestRotY = rotY;
        this.punchy$chestRotZ = rotZ;
    }

    @Override
    public float punchy$getChestX() {
        return this.punchy$chestX;
    }

    @Override
    public float punchy$getChestY() {
        return this.punchy$chestY;
    }

    @Override
    public float punchy$getChestZ() {
        return this.punchy$chestZ;
    }

    @Override
    public float punchy$getChestRotX() {
        return this.punchy$chestRotX;
    }

    @Override
    public float punchy$getChestRotY() {
        return this.punchy$chestRotY;
    }

    @Override
    public float punchy$getChestRotZ() {
        return this.punchy$chestRotZ;
    }

    @Override
    public boolean punchy$isChestPoseValid() {
        return this.punchy$chestPoseValid;
    }

    @Override
    public void punchy$setChestPoseValid(boolean valid) {
        this.punchy$chestPoseValid = valid;
    }

    @Override
    public void punchy$setLidOpen(float open) {
        this.punchy$lidOpen = open;
    }

    @Override
    public float punchy$getLidOpen() {
        return this.punchy$lidOpen;
    }

    @Override
    public void punchy$setLidVel(float vel) {
        this.punchy$lidVel = vel;
    }

    @Override
    public float punchy$getLidVel() {
        return this.punchy$lidVel;
    }

    @Override
    public void punchy$setLidLastUpdateNanos(long nanos) {
        this.punchy$lidLastUpdateNanos = nanos;
    }

    @Override
    public long punchy$getLidLastUpdateNanos() {
        return this.punchy$lidLastUpdateNanos;
    }

    @Override
    public void punchy$setLight(int light) {
        this.punchy$light = light;
    }

    @Override
    public int punchy$getLight() {
        return this.punchy$light;
    }
}

