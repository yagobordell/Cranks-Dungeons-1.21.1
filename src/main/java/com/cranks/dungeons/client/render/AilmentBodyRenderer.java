package com.cranks.dungeons.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** Pose-aligned frost and charged-creeper electricity using existing Minecraft textures. */
public final class AilmentBodyRenderer {
    private static final Identifier FROST_BASE=Identifier.ofVanilla("textures/block/white_concrete.png");
    private static final Identifier FROST_CRACKS=Identifier.ofVanilla("textures/block/frosted_ice_0.png");
    private static final Identifier CHARGE=Identifier.ofVanilla("textures/entity/creeper/creeper_armor.png");
    private static final Identifier ICE=Identifier.ofVanilla("textures/block/ice.png");
    private static final Identifier[] SPARKS={
            Identifier.of("cranks-dungeons","textures/particle/shock_ambient_1.png"),
            Identifier.of("cranks-dungeons","textures/particle/shock_ambient_2.png"),
            Identifier.of("cranks-dungeons","textures/particle/shock_ambient_3.png")};
    private static final float[][] CRYSTAL_CORNERS={{0,0,0},{1,0,0},{1,0,1},{0,0,1}};
    private AilmentBodyRenderer() {}

    public static boolean visible(LivingEntity entity) {
        var client=MinecraftClient.getInstance();
        return client.player!=null && !entity.isInvisibleTo(client.player)
                && entity.squaredDistanceTo(client.player)<=AilmentVfxRules.RANGE*AilmentVfxRules.RANGE
                && (entity!=client.getCameraEntity() || !client.options.getPerspective().isFirstPerson());
    }

    /** Called while the renderer still has the posed model transform pushed. */
    public static void renderModelOverlay(LivingEntity entity,EntityModel<?> model,float tickDelta,
                                           MatrixStack matrices,VertexConsumerProvider vertices,int light) {
        if(model==null || !visible(entity))return;
        if(AilmentParticleManager.active(entity,"freeze")) {
            float grow=MathHelper.clamp(AilmentParticleManager.controlAge(entity,true,tickDelta)/5,0,1);
            AilmentCoatMesh.render(model,matrices,vertices.getBuffer(RenderLayer.getEntityTranslucent(FROST_BASE)),light,
                    OverlayTexture.DEFAULT_UV,((int)(158*grow)<<24)|0xA9D8EE,.70f*grow);
            AilmentCoatMesh.render(model,matrices,vertices.getBuffer(RenderLayer.getEntityTranslucent(FROST_CRACKS)),light,
                    OverlayTexture.DEFAULT_UV,((int)(72*grow)<<24)|0xE0F4FF,.76f*grow);
        }
        boolean electrocuted=AilmentParticleManager.active(entity,"electrocute");
        if(electrocuted) {
            float age=entity.age+tickDelta;
            float speed=.014f;
            var layer=RenderLayer.getEnergySwirl(CHARGE,(age*speed)%1,(age*speed*.23f)%1);
            AilmentCoatMesh.render(model,matrices,vertices.getBuffer(layer),15728880,OverlayTexture.DEFAULT_UV,
                    0xFF9FCFE0,.35f);
        }
    }

    public static void render(LivingEntity entity,float tickDelta,MatrixStack matrices,VertexConsumerProvider vertices,int light) {
        if(!visible(entity))return;
        if(AilmentParticleManager.active(entity,"freeze"))frost(entity,tickDelta,matrices,vertices,light);
        boolean electrocuted=AilmentParticleManager.active(entity,"electrocute");
        if(electrocuted || AilmentParticleManager.active(entity,"shock"))sparks(entity,tickDelta,matrices,vertices,electrocuted);
    }

    private static void sparks(LivingEntity entity,float tickDelta,MatrixStack matrices,VertexConsumerProvider vertices,boolean electrocuted) {
        var client=MinecraftClient.getInstance();
        var camera=client.gameRenderer.getCamera().getPos();
        float angle=(float)Math.atan2(camera.z-entity.getZ(),camera.x-entity.getX());
        float age=entity.age+tickDelta;
        int count=electrocuted?4:2;
        if(entity.squaredDistanceTo(client.player)>16*16)count=electrocuted?2:1;
        for(int i=0;i<count;i++) {
            float phase=((age+i*6+Math.floorMod(entity.getId(),97)*3)%12)/12;
            float fade=.5f+.4f*MathHelper.sin(phase*MathHelper.PI);
            float a=angle+(i-(count-1)*.5f)*.8f;
            float radius=entity.getWidth()*(electrocuted?.72f:.58f);
            float y=entity.getHeight()*(electrocuted?.18f+i*.21f:.38f+i*.27f);
            var buffer=vertices.getBuffer(RenderLayer.getEntityTranslucentEmissive(SPARKS[Math.floorMod(entity.age/5+i,SPARKS.length)]));
            matrices.push();
            matrices.translate(MathHelper.cos(a)*radius,y,MathHelper.sin(a)*radius);
            matrices.multiply(client.getEntityRenderDispatcher().getRotation());
            float size=Math.min(electrocuted?.17f:.14f,entity.getHeight()*.12f);
            spriteVertex(buffer,matrices,-size,-size,0,1,fade);
            spriteVertex(buffer,matrices,size,-size,1,1,fade);
            spriteVertex(buffer,matrices,size,size,1,0,fade);
            spriteVertex(buffer,matrices,-size,size,0,0,fade);
            matrices.pop();
        }
    }

    private static void spriteVertex(VertexConsumer buffer,MatrixStack matrices,float x,float y,float u,float v,float alpha) {
        buffer.vertex(matrices.peek(),x,y,0).color(1f,1f,1f,alpha).texture(u,v)
                .overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(matrices.peek(),0,0,1);
    }

    private static void frost(LivingEntity entity,float tickDelta,MatrixStack matrices,VertexConsumerProvider vertices,int light) {
        float grow=MathHelper.clamp(AilmentParticleManager.controlAge(entity,true,tickDelta)/5,0,1);
        if(grow<=0)return;
        float width=entity.getWidth(),height=entity.getHeight();
        int count=entity.squaredDistanceTo(MinecraftClient.getInstance().player)>16*16?3:5;
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-MathHelper.lerpAngleDegrees(tickDelta,entity.prevBodyYaw,entity.bodyYaw)));
        for(int i=0;i<count;i++) {
            float angle=i*MathHelper.TAU/5+.4f;
            matrices.push();
            matrices.translate(MathHelper.cos(angle)*width*.43,height*(.15+i*.145),MathHelper.sin(angle)*width*.43);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(angle));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i%2==0?24:-19));
            matrices.scale(Math.min(.20f,width*.23f)*grow,Math.min(.38f,height*.16f)*grow,Math.min(.15f,width*.18f)*grow);
            matrices.translate(-.5,0,-.5);
            var buffer=vertices.getBuffer(RenderLayer.getEntityTranslucent(ICE));
            for(int face=0;face<4;face++) {
                var a=CRYSTAL_CORNERS[face];var b=CRYSTAL_CORNERS[(face+1)%4];
                float shade=face%2==0?.85f:.65f;
                iceVertex(buffer,matrices,a[0],a[1],a[2],0,1,shade,light);
                iceVertex(buffer,matrices,b[0],b[1],b[2],1,1,shade,light);
                iceVertex(buffer,matrices,.62f,1,.42f,.5f,0,shade,light);
                iceVertex(buffer,matrices,.62f,1,.42f,.5f,0,shade,light);
            }
            matrices.pop();
        }
        matrices.pop();
    }

    private static void iceVertex(VertexConsumer buffer,MatrixStack matrices,float x,float y,float z,float u,float v,float shade,int light) {
        buffer.vertex(matrices.peek(),x,y,z).color(shade*.8f,shade*.94f,shade,.78f).texture(u,v)
                .overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrices.peek(),0,1,0);
    }
}
