package com.cranks.dungeons.mixin;

import com.cranks.dungeons.registry.ModAttributes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class MobAttributeMixin {

    @Mixin(BlazeEntity.class)
    public static class Blaze {
        @Inject(method = "createBlazeAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.FIRE_RESISTANCE, 0.75)
                .add(ModAttributes.COLD_RESISTANCE, -0.5));
        }
    }

    @Mixin(MagmaCubeEntity.class)
    public static class MagmaCube {
        @Inject(method = "createMagmaCubeAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.FIRE_RESISTANCE, 0.75)
                .add(ModAttributes.COLD_RESISTANCE, -0.5));
        }
    }

    @Mixin(GhastEntity.class)
    public static class Ghast {
        @Inject(method = "createGhastAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.FIRE_RESISTANCE, 0.5)
                .add(ModAttributes.COLD_RESISTANCE, -0.5));
        }
    }

    @Mixin(SnowGolemEntity.class)
    public static class SnowGolem {
        @Inject(method = "createSnowGolemAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.COLD_RESISTANCE, 0.75)
                .add(ModAttributes.FIRE_RESISTANCE, -0.5));
        }
    }

    @Mixin(PolarBearEntity.class)
    public static class PolarBear {
        @Inject(method = "createPolarBearAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.COLD_RESISTANCE, 0.5)
                .add(ModAttributes.FIRE_RESISTANCE, -0.5));
        }
    }

    @Mixin(GuardianEntity.class)
    public static class Guardian {
        @Inject(method = "createGuardianAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.LIGHTNING_RESISTANCE, 0.75)
                .add(ModAttributes.VOID_RESISTANCE, -0.5));
        }
    }

    @Mixin(ElderGuardianEntity.class)
    public static class ElderGuardian {
        @Inject(method = "createElderGuardianAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.LIGHTNING_RESISTANCE, 0.75)
                .add(ModAttributes.VOID_RESISTANCE, -0.5));
        }
    }

    @Mixin(net.minecraft.entity.mob.BreezeEntity.class)
    public static class Breeze {
        @Inject(method = "createBreezeAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.LIGHTNING_RESISTANCE, 0.75)
                .add(ModAttributes.VOID_RESISTANCE, -0.5));
        }
    }

    @Mixin(EndermanEntity.class)
    public static class Enderman {
        @Inject(method = "createEndermanAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.VOID_RESISTANCE, 0.75)
                .add(ModAttributes.LIGHTNING_RESISTANCE, -0.5));
        }
    }

    @Mixin(EndermiteEntity.class)
    public static class Endermite {
        @Inject(method = "createEndermiteAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.VOID_RESISTANCE, 0.75)
                .add(ModAttributes.LIGHTNING_RESISTANCE, -0.5));
        }
    }

    @Mixin(ShulkerEntity.class)
    public static class Shulker {
        @Inject(method = "createShulkerAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.VOID_RESISTANCE, 0.5)
                .add(ModAttributes.LIGHTNING_RESISTANCE, -0.5));
        }
    }

    @Mixin(EvokerEntity.class)
    public static class Evoker {
        @Inject(method = "createEvokerAttributes", at = @At("RETURN"), cancellable = true)
        private static void cranks$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
            cir.setReturnValue(cir.getReturnValue()
                .add(ModAttributes.VOID_RESISTANCE, 0.5)
                .add(ModAttributes.LIGHTNING_RESISTANCE, -0.5));
        }
    }

    // Special handling for Stray since it doesn't have a static attribute method
    @Mixin(StrayEntity.class)
    public static class Stray {
        @Inject(method = "<init>", at = @At("TAIL"))
        private void cranks$applyStrayStats(EntityType<? extends StrayEntity> entityType, net.minecraft.world.World world, CallbackInfo ci) {
            StrayEntity stray = (StrayEntity) (Object) this;
            // Since we added these attributes to ALL entities in LivingEntityMixin, 
            // we can safely set their base values here.
            var fireRes = stray.getAttributeInstance(ModAttributes.FIRE_RESISTANCE);
            if (fireRes != null) fireRes.setBaseValue(-0.5);
            
            var coldRes = stray.getAttributeInstance(ModAttributes.COLD_RESISTANCE);
            if (coldRes != null) coldRes.setBaseValue(0.75);
        }
    }
}
