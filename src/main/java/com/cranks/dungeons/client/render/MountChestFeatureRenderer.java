package com.cranks.dungeons.client.render;

import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.mount.MountKind;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

/** Vanilla donkey panniers fitted to, and animated with, each mount's actual torso. */
public final class MountChestFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends FeatureRenderer<T, M> {
    private static final Identifier DONKEY_TEXTURE=Identifier.ofVanilla("textures/entity/horse/donkey.png");
    private static final ModelPart PANNIER=createPannier();
    private static final Placement HORSE=new Placement(5.05f,-4.0f,0.0f,1.0f);
    private static final Placement CAMEL=new Placement(7.5f,-5.0f,-3.0f,1.0f);
    private static final Placement STRIDER=new Placement(8.0f,3.5f,2.5f,0.8f);
    private static final Placement PIG=new Placement(5.0f,2.0f,1.0f,0.75f);

    public MountChestFeatureRenderer(FeatureRendererContext<T,M> context){super(context);}

    @Override public void render(MatrixStack matrices,VertexConsumerProvider vertices,int light,T entity,
            float limbAngle,float limbDistance,float tickDelta,float animationProgress,float headYaw,float headPitch){
        renderPanniers(getContextModel(),matrices,vertices,light,entity);
    }

    public static void renderPanniers(EntityModel<?> model,MatrixStack matrices,VertexConsumerProvider vertices,
                                      int light,LivingEntity entity){
        if(!(entity instanceof MountDataProvider provider)||!provider.cranksdungeons$hasMountChest()
                ||entity.isBaby()||entity.isInvisible())return;
        Placement placement=switch(MountKind.of(entity)){
            case HORSE,SKELETON_HORSE -> HORSE;
            case CAMEL -> CAMEL;
            case STRIDER -> STRIDER;
            case PIG -> PIG;
            // Donkeys, mules and llamas already have native body-attached chests.
            default -> null;
        };
        if(placement==null)return;
        matrices.push();
        var rig=MountChestRig.apply(model,matrices,MountKind.of(entity));
        if(rig!=null){
            var fitted=new Placement(rig.halfWidth(),0,rig.z(),placement.scale);
            renderSide(matrices,vertices,light,fitted,1);
            renderSide(matrices,vertices,light,fitted,-1);
        }else if(applyBodyTransform(model,matrices)){
            renderSide(matrices,vertices,light,placement,1);
            renderSide(matrices,vertices,light,placement,-1);
        }
        matrices.pop();
    }

    /** Apply animated parent/body transforms, then remove only the body's bind rotation.
     * Pig torsos are built vertically and rotated 90 degrees at rest; keeping that
     * bind rotation would turn their bags sideways. Animated rotation/scale remains.
     */
    static boolean applyBodyTransform(EntityModel<?> model,MatrixStack matrices){
        ModelPart body;
        if(model instanceof MountBodyModelAccess access){
            body=access.cranksdungeons$getMountBody();
        }else if(model instanceof SinglePartEntityModel<?> single){
            ModelPart root=single.getPart();
            if(!root.visible||!root.hasChild("body"))return false;
            root.rotate(matrices);
            body=root.getChild("body");
        }else return false;
        if(!body.visible)return false;
        body.rotate(matrices);
        ModelTransform bind=body.getDefaultTransform();
        if(bind.pitch!=0||bind.yaw!=0||bind.roll!=0){
            matrices.multiply(new Quaternionf().rotationZYX(bind.roll,bind.yaw,bind.pitch).conjugate());
        }
        return true;
    }

    private static void renderSide(MatrixStack matrices,VertexConsumerProvider vertices,int light,Placement fit,int side){
        matrices.push();
        // Slightly inset the inner face into the flank so there is no floating seam.
        float center=fit.halfWidth+1.5f*fit.scale-0.15f;
        matrices.translate(side*center/16.0f,fit.y/16.0f,fit.z/16.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-side*90.0f));
        matrices.scale(fit.scale,fit.scale,fit.scale);
        PANNIER.render(matrices,vertices.getBuffer(RenderLayer.getEntityCutoutNoCull(DONKEY_TEXTURE)),
                light,OverlayTexture.DEFAULT_UV,0xffffffff);
        matrices.pop();
    }

    private static ModelPart createPannier(){
        ModelData data=new ModelData();
        data.getRoot().addChild("pannier",ModelPartBuilder.create().uv(26,21)
                .cuboid(-4.0f,-4.0f,-1.5f,8.0f,8.0f,3.0f),ModelTransform.NONE);
        return TexturedModelData.of(data,64,64).createModel().getChild("pannier");
    }

    /** Body-local model pixels, in the torso's rest orientation. */
    private record Placement(float halfWidth,float y,float z,float scale){}
}
