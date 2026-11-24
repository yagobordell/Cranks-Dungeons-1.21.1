package com.cranks.dungeons.client.render;

import com.cranks.dungeons.mount.MountKind;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import java.util.*;

/** Optional EMF adapter: locate the live torso mesh, including its animated parents.
 * No dependency on EMF is linked when it is absent. Variant children are read live.
 */
final class MountChestRig {
    record Fit(float halfWidth,float z){}
    private static final class Search {
        float volume;
        ModelPart.Cuboid torso;
        List<ModelPart> path;
        int visited;
    }
    private MountChestRig(){}

    static Fit apply(EntityModel<?> model,MatrixStack matrices,MountKind kind){
        ModelPart seed=model instanceof MountBodyModelAccess access?access.cranksdungeons$getMountBody()
                :model instanceof SinglePartEntityModel<?> single?single.getPart():null;
        ModelPart root=AnimatedModelParts.root(seed);
        if(root==null)return null;
        var search=new Search();findTorso(root,new ArrayList<>(),search);
        if(search.path==null)return null;
        for(var part:search.path)part.rotate(matrices);
        var cube=search.torso;
        matrices.translate((cube.minX+cube.maxX)/32f,(cube.minY+cube.maxY)/32f,(cube.minZ+cube.maxZ)/32f);
        // Pig torso meshes run lengthwise along local Y. Normalize that mesh
        // orientation after its full animated chain, rather than guessing a
        // bind rotation from the now-empty vanilla body placeholder.
        boolean verticalPig=kind==MountKind.PIG&&cube.maxY-cube.minY>cube.maxZ-cube.minZ;
        if(verticalPig)matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90));
        float depth=verticalPig?cube.maxY-cube.minY:cube.maxZ-cube.minZ;
        return new Fit((cube.maxX-cube.minX)/2f,Math.min(2f,depth*.10f));
    }

    private static void findTorso(ModelPart part,ArrayList<ModelPart> path,Search best){
        if(!part.visible||++best.visited>256||path.size()>24)return;
        path.add(part);
        var access=(MountModelPartAccess)(Object)part;
        for(var cube:access.cranks$getCuboids()){
            float volume=(cube.maxX-cube.minX)*(cube.maxY-cube.minY)*(cube.maxZ-cube.minZ);
            if(volume>best.volume){best.volume=volume;best.torso=cube;best.path=List.copyOf(path);}
        }
        for(var child:access.cranks$getChildren().values())findTorso(child,path,best);
        path.removeLast();
    }
}
