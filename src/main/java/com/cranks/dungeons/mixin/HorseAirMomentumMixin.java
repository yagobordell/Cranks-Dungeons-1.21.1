package com.cranks.dungeons.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps ridden equine-mount momentum while making non-jump ledge drops follow nearby terrain. */
@Mixin(LivingEntity.class)
public abstract class HorseAirMomentumMixin {
    private static final double SURFACE_CHECK_DISTANCE = 1.35;
    private static final double SURFACE_STICK_VELOCITY = -0.28;

    @Unique private boolean cranks$travelStartedGrounded;
    @Unique private boolean cranks$preservingHorseMomentum;
    @Unique private boolean cranks$surfaceDescending;
    @Unique private double cranks$preservedHorseSpeed;
    @Unique private Vec3d cranks$preservedHorseDirection = Vec3d.ZERO;
    @Unique private double cranks$travelStartX;
    @Unique private double cranks$travelStartZ;
    @Unique private double cranks$lastGroundTravelSpeed;
    @Unique private Vec3d cranks$lastGroundTravelDirection = Vec3d.ZERO;

    @Inject(method = "travel", at = @At("HEAD"))
    private void cranks$captureHorseTakeoffMomentum(Vec3d movementInput, CallbackInfo ci) {
        if (!((Object) this instanceof AbstractHorseEntity horse)
                || !(horse.getControllingPassenger() instanceof PlayerEntity)) {
            cranks$clearAllHorseMomentum();
            return;
        }

        cranks$travelStartedGrounded = horse.isOnGround();
        cranks$travelStartX = horse.getX();
        cranks$travelStartZ = horse.getZ();
        Vec3d velocity = horse.getVelocity();
        Vec3d horizontal = new Vec3d(velocity.x, 0.0, velocity.z);
        double speed = horizontal.horizontalLength();

        if (cranks$travelStartedGrounded && cranks$lastGroundTravelSpeed <= 0.0001) {
            cranks$lastGroundTravelSpeed = speed;
            cranks$lastGroundTravelDirection = speed > 0.0001
                    ? horizontal.multiply(1.0 / speed)
                    : Vec3d.ZERO;
        }
    }

    @Inject(method = "travel", at = @At("RETURN"))
    private void cranks$restoreHorseAirMomentum(Vec3d movementInput, CallbackInfo ci) {
        if (!((Object) this instanceof AbstractHorseEntity horse)
                || !(horse.getControllingPassenger() instanceof PlayerEntity rider)) {
            cranks$clearAllHorseMomentum();
            return;
        }

        boolean endedGrounded = horse.isOnGround();
        boolean airborneThisTravel = !cranks$travelStartedGrounded || !endedGrounded;
        double movedX = horse.getX() - cranks$travelStartX;
        double movedZ = horse.getZ() - cranks$travelStartZ;
        double travelSpeed = Math.sqrt(movedX * movedX + movedZ * movedZ);

        if (!airborneThisTravel) {
            if (travelSpeed > 0.0001) {
                cranks$lastGroundTravelSpeed = travelSpeed;
                cranks$lastGroundTravelDirection = new Vec3d(
                        movedX / travelSpeed, 0.0, movedZ / travelSpeed);
            }
            cranks$clearActiveHorseMomentum();
            return;
        }

        if (rider.forwardSpeed <= 0.01f || horse.horizontalCollision || horse.isTouchingWater()) {
            cranks$clearAllHorseMomentum();
            return;
        }

        Vec3d velocity = horse.getVelocity();
        Vec3d horizontal = new Vec3d(velocity.x, 0.0, velocity.z);
        double currentSpeed = horizontal.horizontalLength();

        if (!cranks$preservingHorseMomentum) {
            cranks$preservingHorseMomentum = true;
            // Position delta is the actual pre-friction travel speed the player
            // sees on the ground. The stored velocity at this point has already
            // been reduced by ground friction and was the source of the remaining
            // slowdown when walking off a block.
            double naturalHorseSpeed = Math.max(0.05,
                    horse.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
            double candidateSpeed = Math.max(
                    cranks$lastGroundTravelSpeed, Math.max(travelSpeed, currentSpeed));
            // AbstractHorseEntity adds a forward impulse when jump() receives
            // forward input. Keep the momentum that was already built up, but
            // never let that one impulse raise an intentional jump above the
            // mount's normal movement-speed attribute. Without this cap,
            // repeatedly jumping while holding forward compounds the impulse
            // every landing. Ledge drops keep their existing full-momentum
            // behavior because they are not upward jumps.
            boolean intentionalJump = velocity.y > 0.0;
            cranks$preservedHorseSpeed = intentionalJump
                    ? Math.min(naturalHorseSpeed, candidateSpeed)
                    : candidateSpeed;
            if (cranks$lastGroundTravelDirection.lengthSquared() > 0.0001) {
                cranks$preservedHorseDirection = cranks$lastGroundTravelDirection;
            }
        }

        // A horse that simply walks off a one-block ledge should follow the
        // terrain instead of carrying its ground speed through a long, flat
        // glide. A positive takeoff velocity means this is an intentional
        // jump, so it is never converted into a surface-following drop.
        if (!cranks$surfaceDescending
                && cranks$travelStartedGrounded
                && velocity.y <= 0.0
                && cranks$hasNearbySurface(horse)) {
            cranks$surfaceDescending = true;
        }

        Vec3d direction;
        if (currentSpeed > 0.0001) {
            direction = horizontal.multiply(1.0 / currentSpeed);
            cranks$preservedHorseDirection = direction;
        } else {
            direction = cranks$preservedHorseDirection;
        }

        if (cranks$preservedHorseSpeed > 0.0001 && direction.lengthSquared() > 0.0001) {
            double verticalVelocity = velocity.y;
            if (cranks$surfaceDescending && cranks$hasNearbySurface(horse)) {
                // Pull the mount down onto the next terrain step while still
                // allowing ordinary gravity to handle cliffs and real jumps.
                verticalVelocity = Math.min(verticalVelocity, SURFACE_STICK_VELOCITY);
            }
            horse.setVelocity(
                    direction.x * cranks$preservedHorseSpeed,
                    verticalVelocity,
                    direction.z * cranks$preservedHorseSpeed);
            horse.velocityDirty = true;
        }

        if (endedGrounded) cranks$clearActiveHorseMomentum();
    }

    @Unique
    private void cranks$clearActiveHorseMomentum() {
        cranks$preservingHorseMomentum = false;
        cranks$surfaceDescending = false;
        cranks$preservedHorseSpeed = 0.0;
        cranks$preservedHorseDirection = Vec3d.ZERO;
    }

    @Unique
    private void cranks$clearAllHorseMomentum() {
        cranks$clearActiveHorseMomentum();
        cranks$lastGroundTravelSpeed = 0.0;
        cranks$lastGroundTravelDirection = Vec3d.ZERO;
    }

    @Unique
    private boolean cranks$hasNearbySurface(AbstractHorseEntity horse) {
        var box = horse.getBoundingBox();
        Vec3d origin = new Vec3d(horse.getX(), box.minY + 0.05, horse.getZ());
        Vec3d target = origin.add(0.0, -SURFACE_CHECK_DISTANCE, 0.0);
        return horse.getWorld().raycast(new RaycastContext(
                origin, target, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, horse)).getType() != HitResult.Type.MISS;
    }
}
