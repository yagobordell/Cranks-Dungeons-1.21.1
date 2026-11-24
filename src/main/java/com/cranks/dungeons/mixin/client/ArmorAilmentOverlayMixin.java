package com.cranks.dungeons.mixin.client;

import com.cranks.dungeons.client.render.AilmentBodyRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Coat the actual armour geometry too; a skin-only overlay would be hidden under equipment. */
@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorAilmentOverlayMixin {
    @Inject(method="renderArmor",at=@At("TAIL"))
    private void cranks$coatArmor(MatrixStack matrices,VertexConsumerProvider vertices,LivingEntity entity,
                                  EquipmentSlot slot,int light,BipedEntityModel<?> model,CallbackInfo ci) {
        if(!(entity.getEquippedStack(slot).getItem() instanceof ArmorItem armor) || armor.getSlotType()!=slot)return;
        AilmentBodyRenderer.renderModelOverlay(entity,model,0,matrices,vertices,light);
    }
}
