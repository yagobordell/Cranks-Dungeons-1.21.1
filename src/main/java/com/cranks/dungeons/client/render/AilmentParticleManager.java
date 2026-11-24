package com.cranks.dungeons.client.render;

import com.cranks.dungeons.network.AilmentSyncPayload.AilmentEntry;
import com.cranks.dungeons.network.BleedVisualPayload;
import com.cranks.dungeons.particle.ModParticles;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import java.util.*;

/** Small, depth-tested body cues. First-person feedback never spawns particles around the camera. */
public final class AilmentParticleManager {
    private static final Map<Integer, Visual> VISUALS = new HashMap<>();
    private static final Map<Integer, Integer> SPENT = new HashMap<>();
    private static ClientWorld world;
    private static long budgetTick = Long.MIN_VALUE;
    private static int spent;
    private static long pulseTick = Long.MIN_VALUE;
    private static int pulseColor;
    private static final int BURN=1, POISON=2, BLEED=4, FREEZE=8, SHOCK=16, ELECTROCUTE=32;

    private static final class Visual {
        final UUID uuid;
        int mask;
        long seen, freezeStart, electrocuteStart;
        boolean visible;
        Visual(LivingEntity entity) { uuid=entity.getUuid(); }
    }

    private AilmentParticleManager() {}

    private static void checkWorld(MinecraftClient client) {
        if (world != client.world) {
            VISUALS.clear();
            SPENT.clear();
            spent=0;
            budgetTick=Long.MIN_VALUE;
            pulseTick=Long.MIN_VALUE;
            world=client.world;
        }
        if (world != null && budgetTick != world.getTime()) {
            budgetTick=world.getTime();
            spent=0;
            SPENT.clear();
        }
    }

    public static void tick(MinecraftClient client) {
        checkWorld(client);
        if (world==null || client.player==null || client.isPaused()) return;
        long now=world.getTime();
        for (int id:AilmentIconRenderer.getTrackedEntityIds()) {
            if (!(world.getEntityById(id) instanceof LivingEntity entity) || !entity.isAlive()) continue;
            var entries=AilmentIconRenderer.getAilments(id);
            var state=VISUALS.get(id);
            if (state==null || !state.uuid.equals(entity.getUuid())) {
                state=new Visual(entity);
                VISUALS.put(id,state);
            }
            int mask=0;
            for (var entry:entries) if (!entry.buildup() && AilmentIconRenderer.remainingSeconds(id,entry)!=0) mask|=kind(entry);
            int added=mask & ~state.mask;
            int removed=state.mask & ~mask;
            if ((added&FREEZE)!=0) state.freezeStart=now;
            if ((added&ELECTROCUTE)!=0) state.electrocuteStart=now;
            state.seen=now;
            state.mask=mask;
            if (entity==client.player && added!=0) pulse(client,color(Integer.highestOneBit(added)));
            if (AilmentVfxRules.due(now,id,5,0) || added!=0 || removed!=0)
                state.visible=canView(client,entity);
            if (!state.visible) continue;
            if (added!=0) transition(client,entity,added,false);
            if ((removed&FREEZE)!=0) transition(client,entity,FREEZE,true);
            for (var entry:entries) {
                if (!entry.buildup() && AilmentIconRenderer.remainingSeconds(id,entry)==0) continue;
                ambient(client,entity,entry,now);
            }
        }
        var iterator=VISUALS.entrySet().iterator();
        while (iterator.hasNext()) {
            var item=iterator.next();
            var state=item.getValue();
            if (state.seen==now) continue;
            var entity=world.getEntityById(item.getKey());
            if (entity instanceof LivingEntity living && state.uuid.equals(entity.getUuid()) && living.isAlive()
                    && (state.mask&FREEZE)!=0 && canView(client,living)) transition(client,living,FREEZE,true);
            iterator.remove();
        }
    }

    private static int kind(AilmentEntry entry) {
        String path=entry.texture().getPath();
        if (path.endsWith("/burn.png")) return BURN;
        if (path.endsWith("/poison.png")) return POISON;
        if (path.endsWith("/bleed.png")) return BLEED;
        if (path.endsWith("/freeze.png") || path.endsWith("/freeze_buildup.png")) return FREEZE;
        if (path.endsWith("/shock.png")) return SHOCK;
        if (path.endsWith("/electrocute.png") || path.endsWith("/electrocute_buildup.png")) return ELECTROCUTE;
        return 0;
    }

    private static boolean canView(MinecraftClient client,LivingEntity entity) {
        if (client.player==null || entity.isInvisibleTo(client.player)
                || entity.squaredDistanceTo(client.player)>AilmentVfxRules.RANGE*AilmentVfxRules.RANGE) return false;
        if (entity==client.getCameraEntity() && client.options.getPerspective().isFirstPerson()) return false;
        return entity==client.player || client.player.canSee(entity);
    }

    private static void ambient(MinecraftClient client,LivingEntity entity,AilmentEntry entry,long now) {
        int type=kind(entry), id=entity.getId();
        if (entry.buildup()) {
            if (!AilmentVfxRules.due(now,id,AilmentVfxRules.buildupInterval(entry.stacks()),type)) return;
            if (type==FREEZE) emit(client,entity,ParticleTypes.SNOWFLAKE,.005,-.007,false);
            else if (type==BURN && entry.stacks()>=35) emit(client,entity,ParticleTypes.SMALL_FLAME,.004,.012,false);
            else if (type==ELECTROCUTE && entry.stacks()>=35) emit(client,entity,ModParticles.SHOCK_AMBIENT_1,.005,0,false);
            return;
        }
        double distance=entity.squaredDistanceTo(client.player);
        int rate=distance>16*16?2:1;
        switch(type) {
            case BURN -> {
                if(AilmentVfxRules.due(now,id,2*rate,1)) emit(client,entity,ParticleTypes.FLAME,.012,.018,false);
            }
            case POISON -> {
                if(AilmentVfxRules.due(now,id,5*rate,3))
                    emit(client,entity,ModParticles.POISON_BUBBLE,.012,.04,false);
            }
            case BLEED -> {
                int interval=entity.getVelocity().horizontalLengthSquared()>.01?9:14;
                if(AilmentVfxRules.due(now,id,interval*rate,4)) {
                    emit(client,entity,ModParticles.BLEED_SPLASH_1,.005,-.02,false);
                    emit(client,entity,ModParticles.BLEED_SPLASH_2,.008,-.025,false);
                }
                float remaining=AilmentIconRenderer.remainingSeconds(id,entry);
                if(entry.storedDamage()>0 && remaining>0 && remaining<1 && AilmentVfxRules.due(now,id,8*rate,5))
                    emit(client,entity,ModParticles.BLEED_SPLASH_2,.012,.015,false);
            }
            case FREEZE -> {
                if(AilmentVfxRules.due(now,id,20*rate,6)) emit(client,entity,ParticleTypes.SNOWFLAKE,.005,-.008,false);
            }
            case SHOCK -> {
                // Lightning crackles only; the charged body coat belongs to Electrocute.
            }
            // The charged model carries Electrocute's ongoing cue; no additional particle cloud.
            default -> { }
        }
    }

    private static void transition(MinecraftClient client,LivingEntity entity,int mask,boolean end) {
        if(!end && (mask&BURN)!=0)for(int i=0;i<3;i++)emit(client,entity,ParticleTypes.FLAME,.015,.02,true);
        if(!end && (mask&POISON)!=0)for(int i=0;i<2;i++)emit(client,entity,ModParticles.POISON_BUBBLE,.012,.04,true);
        if(!end && (mask&BLEED)!=0)for(int i=0;i<3;i++)emit(client,entity,ModParticles.BLEED_SPLASH_1,.14,.08,true);
        if ((mask&FREEZE)!=0) {
            ParticleEffect particle=end?new BlockStateParticleEffect(ParticleTypes.BLOCK,Blocks.ICE.getDefaultState()):ParticleTypes.SNOWFLAKE;
            for(int i=0;i<(end?6:4);i++) emit(client,entity,particle,end?.045:.015,end?.04:.008,true);
        }
        if ((mask&ELECTROCUTE)!=0) for(int i=0;i<3;i++) emit(client,entity,ModParticles.SHOCK_AMBIENT_3,.025,.01,true);
    }

    public static void bleedEvent(BleedVisualPayload payload) {
        MinecraftClient client=MinecraftClient.getInstance();
        checkWorld(client);
        if(world==null || client.player==null) return;
        var target=world.getEntityById(payload.entityId());
        if(!(target instanceof LivingEntity entity) || !entity.getUuid().equals(payload.entityUuid())) return;
        if(entity==client.player && payload.burst()) pulse(client,color(BLEED));
        if(!canView(client,entity)) return;
        for(int i=0;i<(payload.burst()?12:6);i++) {
            var particle=switch(i%4) {
                case 0->ModParticles.BLEED_SPLASH_1;
                case 1->ModParticles.BLEED_SPLASH_2;
                case 2->ModParticles.BLEED_SPLASH_3;
                default->ModParticles.BLEED_SPLASH_4;
            };
            emit(client,entity,particle,payload.burst()?.34:.28,payload.burst()?.14:.11,true);
        }
    }

    private static void emit(MinecraftClient client,LivingEntity entity,ParticleEffect particle,double speed,double rise,boolean event) {
        if(entity==client.getCameraEntity() && client.options.getPerspective().isFirstPerson()) return;
        if(spent>=AilmentVfxRules.PARTICLES_PER_TICK) return;
        int used=SPENT.getOrDefault(entity.getId(),0);
        if(used>=(event?AilmentVfxRules.EVENT_PER_ENTITY:AilmentVfxRules.AMBIENT_PER_ENTITY)) return;
        var random=entity.getRandom();
        double angle=random.nextDouble()*Math.PI*2;
        double radius=entity.getWidth()*.53;
        double x=entity.getX()+Math.cos(angle)*radius;
        double y=entity.getY()+entity.getHeight()*(.18+random.nextDouble()*.65);
        double z=entity.getZ()+Math.sin(angle)*radius;
        if(particle==ModParticles.POISON_BUBBLE) {
            radius=entity.getWidth()*.72;
            x=entity.getX()+Math.cos(angle)*radius;
            z=entity.getZ()+Math.sin(angle)*radius;
            y=entity.getY()+entity.getHeight()*(.45+(random.nextDouble()-.5)*.08);
        }
        if(client.gameRenderer.getCamera().getPos().squaredDistanceTo(x,y,z)<.8*.8) return;
        spent++;
        SPENT.put(entity.getId(),used+1);
        world.addParticle(particle,x,y,z,(random.nextDouble()-.5)*speed,rise,(random.nextDouble()-.5)*speed);
    }

    public static boolean active(LivingEntity entity,String name) {
        return AilmentIconRenderer.getAilments(entity.getId()).stream().anyMatch(entry->!entry.buildup()
                && entry.texture().getPath().equals("textures/mob_effect/"+name+".png")
                && AilmentIconRenderer.remainingSeconds(entity.getId(),entry)!=0);
    }

    public static float controlAge(LivingEntity entity,boolean frozen,float tickDelta) {
        var state=VISUALS.get(entity.getId());
        return state==null || !state.uuid.equals(entity.getUuid()) || world==null?20:
                world.getTime()-(frozen?state.freezeStart:state.electrocuteStart)+tickDelta;
    }

    private static int color(int type) {
        return switch(type) {
            case BURN->0xBD713F;
            case POISON->0x819950;
            case BLEED->0xBA4B4A;
            case FREEZE->0x8CBCCD;
            default->0x8FC6DB;
        };
    }

    private static void pulse(MinecraftClient client,int color) {
        pulseTick=client.world.getTime();
        pulseColor=color;
    }

    public static void renderHud(DrawContext context) {
        MinecraftClient client=MinecraftClient.getInstance();
        checkWorld(client);
        if(world==null || client.player==null || client.options.hudHidden || client.currentScreen!=null
                || !client.options.getPerspective().isFirstPerson()) return;
        float fade=AilmentVfxRules.applicationFade(world.getTime()-pulseTick);
        if(fade<=0) return;
        int width=context.getScaledWindowWidth(),height=context.getScaledWindowHeight();
        for(int i=0;i<3;i++) {
            int ink=((int)(fade*(28-i*7))<<24)|pulseColor;
            int inset=i*2;
            context.fill(inset,height/4,inset+2,height*3/4,ink);
            context.fill(width-inset-2,height/4,width-inset,height*3/4,ink);
        }
    }
}
