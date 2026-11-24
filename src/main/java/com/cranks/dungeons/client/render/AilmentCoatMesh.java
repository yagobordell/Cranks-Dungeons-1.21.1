package com.cranks.dungeons.client.render;

import com.cranks.dungeons.mixin.client.AnimalModelPartsAccessor;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import java.util.*;

/** Inflate each actual cuboid about its own centre, preserving every animated joint and parent pose. */
final class AilmentCoatMesh {
    private record Fit(float x,float y,float z,float inverseX,float inverseY,float inverseZ) {}
    private static final Map<ModelPart.Cuboid,Fit> FITS=new WeakHashMap<>();
    private AilmentCoatMesh() {}

    static void render(EntityModel<?> model,MatrixStack matrices,VertexConsumer vertices,int light,int overlay,int color,float padding) {
        var pass=new Pass(matrices,vertices,light,overlay,color,padding);
        if(model instanceof AnimalModelPartsAccessor animal) {
            if(model.child) {
                matrices.push();
                if(animal.cranks$headScaled()) {
                    float scale=1.5f/animal.cranks$childHeadScale();
                    matrices.scale(scale,scale,scale);
                }
                matrices.translate(0,animal.cranks$childHeadY()/16,animal.cranks$childHeadZ()/16);
                for(var part:animal.cranks$headParts())pass.walk(part,0);
                matrices.pop();
                matrices.push();
                float scale=1/animal.cranks$childBodyScale();
                matrices.scale(scale,scale,scale);
                matrices.translate(0,animal.cranks$childBodyY()/16,0);
                for(var part:animal.cranks$bodyParts())pass.walk(part,0);
                matrices.pop();
            } else {
                for(var part:animal.cranks$headParts())pass.root(part);
                for(var part:animal.cranks$bodyParts())pass.root(part);
            }
        } else if(model instanceof SinglePartEntityModel<?> single)pass.root(single.getPart());
        else model.render(matrices,vertices,light,overlay,color);
    }

    private static final class Pass {
        final MatrixStack matrices;
        final VertexConsumer vertices;
        final int light,overlay,color;
        final float padding;
        final List<ModelPart> roots=new ArrayList<>();
        int visited;
        Pass(MatrixStack matrices,VertexConsumer vertices,int light,int overlay,int color,float padding) {
            this.matrices=matrices;this.vertices=vertices;this.light=light;this.overlay=overlay;this.color=color;this.padding=padding;
        }
        void root(ModelPart part) {
            var animated=AnimatedModelParts.root(part);
            var root=animated==null?part:animated;
            for(var seen:roots)if(seen==root)return;
            roots.add(root);
            walk(root,0);
        }
        void walk(ModelPart part,int depth) {
            if(!part.visible || depth>32 || ++visited>2048)return;
            matrices.push();
            part.rotate(matrices);
            var access=(MountModelPartAccess)(Object)part;
            if(!part.hidden)for(var cube:access.cranks$getCuboids()) {
                var fit=FITS.computeIfAbsent(cube,value->new Fit(
                        (value.minX+value.maxX)/32,(value.minY+value.maxY)/32,(value.minZ+value.maxZ)/32,
                        inverse(value.maxX-value.minX),inverse(value.maxY-value.minY),inverse(value.maxZ-value.minZ)));
                matrices.push();
                matrices.translate(fit.x,fit.y,fit.z);
                matrices.scale(1+2*padding*fit.inverseX,1+2*padding*fit.inverseY,1+2*padding*fit.inverseZ);
                matrices.translate(-fit.x,-fit.y,-fit.z);
                cube.renderCuboid(matrices.peek(),vertices,light,overlay,color);
                matrices.pop();
            }
            for(var child:access.cranks$getChildren().values())walk(child,depth+1);
            matrices.pop();
        }
    }
    private static float inverse(float size) {return size>.001f?1/size:0;}
}
