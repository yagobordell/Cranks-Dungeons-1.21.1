package com.cranks.dungeons.ailment;
import com.cranks.dungeons.EnergyShieldProvider;
import com.cranks.dungeons.damage.DamageElement;
import com.cranks.dungeons.effect.ModEffects;
import com.cranks.dungeons.mount.MountDataProvider;
import com.cranks.dungeons.network.AilmentSyncPayload;
import com.cranks.dungeons.network.DamageNumberSender;
import com.cranks.dungeons.network.ShockArcPayload;
import com.cranks.dungeons.party.PartyManager;
import com.cranks.dungeons.registry.ModAttributes;
import com.cranks.dungeons.registry.ModSounds;
import com.cranks.dungeons.util.AilmentHistoryProvider;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/** Server-owned applications. Vanilla effects and network packets are views of this state. */
public final class AilmentManager {
    private static final Identifier CHILL_SLOW=Identifier.of("cranks-dungeons","freeze_slow");
    private static final Map<UUID,State> STATES=new ConcurrentHashMap<>();
    private static final ThreadLocal<SecondaryHit> SECONDARY=new ThreadLocal<>();
    private static final Set<UUID> UPDATING_MIRRORS=new HashSet<>();
    private static boolean shockChain;
    private static final double STEP=.05;
    private static final String SAVE_KEY="CranksAilments";
    private static final class State {
        final LivingEntity owner;
        final List<Ailment> ailments=new ArrayList<>();
        float freeze,electrocute,flammability,freezeMagnitude=1;
        long freezeHit,electrocuteHit,flammabilityHit;
        double controlRecovery;
        boolean recoveryFeedbackSent;
        List<AilmentSyncPayload.AilmentEntry> lastSent;
        State(LivingEntity owner) {
            this.owner=owner;
        }
        boolean empty() {
            return ailments.isEmpty()&&freeze<=0&&electrocute<=0&&flammability<=0&&controlRecovery<=0;
        }
    }
    public record SecondaryHit(UUID target,DamageElement element,boolean bypassShield,boolean ailment) {
    }
    private record PoisonOwner(UUID id,boolean player) {
    }
    private static State find(LivingEntity entity) {
        State s=STATES.get(entity.getUuid());
        return s!=null&&s.owner==entity?s:null;
    }
    private static State state(LivingEntity entity) {
        State s=find(entity);
        if(s==null) {
            s=new State(entity);
            STATES.put(entity.getUuid(),s);
        }
        return s;
    }
    public static List<Ailment> getAilments(LivingEntity entity) {
        State s=find(entity);
        return s==null?List.of():List.copyOf(s.ailments);
    }
    public static boolean hasAilment(LivingEntity entity,AilmentType type) {
        State s=find(entity);
        return s!=null&&s.ailments.stream().anyMatch(a->a.getType()==type&&!a.isExpired());
    }
    public static boolean isAfflicted(LivingEntity entity) {
        return entity!=null&&(!getAilments(entity).isEmpty()||getFreezeBuildup(entity.getUuid())>0||entity.isOnFire());
    }
    public static boolean isStunned(LivingEntity entity) {
        return hasAilment(entity,AilmentType.FREEZE)||hasAilment(entity,AilmentType.ELECTROCUTE);
    }
    public static float getFreezeBuildup(UUID id) {
        State s=STATES.get(id);
        return s==null?0:s.freeze;
    }
    public static float getElectrocuteBuildup(UUID id) {
        State s=STATES.get(id);
        return s==null?0:s.electrocute;
    }
    public static float getFlammabilityBuildup(UUID id) {
        State s=STATES.get(id);
        return s==null?0:s.flammability;
    }
    public static float getShockMagnitude(LivingEntity entity) {
        return getAilments(entity).stream().filter(a->a.getType()==AilmentType.SHOCK).map(Ailment::getMagnitude).max(Float::compare).orElse(0f);
    }
    public static double getAilmentDuration(LivingEntity entity,AilmentType type) {
        return getAilments(entity).stream().filter(a->a.getType()==type).mapToDouble(Ailment::getRemainingDuration).min().orElse(0);
    }
    public static SecondaryHit secondaryHit(LivingEntity target) {
        var h=SECONDARY.get();
        return h!=null&&h.target().equals(target.getUuid())?h:null;
    }
    public static boolean isSecondaryDamage(LivingEntity target) {
        return secondaryHit(target)!=null;
    }
    public static boolean isResolvingAilment(LivingEntity target) {
        var h=secondaryHit(target);
        return h!=null&&h.ailment();
    }
    public static boolean bypassesShield(LivingEntity target) {
        var h=secondaryHit(target);
        return h!=null&&h.bypassShield();
    }
    public static boolean isPoisonDamageInProgress(UUID id) {
        var h=SECONDARY.get();
        return h!=null&&h.target().equals(id)&&h.element()==DamageElement.VOID&&h.ailment();
    }
    public static boolean isBleedDamageInProgress(UUID id) {
        var h=SECONDARY.get();
        return h!=null&&h.target().equals(id)&&h.element()==DamageElement.PHYSICAL&&h.ailment();
    }
    private static boolean canApply(LivingEntity target,Entity source) {
        if(target==null||target.getWorld().isClient()||!target.isAlive()||target.isSpectator())return false;
        if(target instanceof PlayerEntity p&&p.getAbilities().invulnerable)return false;
        if(target.isInvulnerableTo(target.getDamageSources().magic()))return false;
        if(source!=null&&source!=target) {
            if(target.isTeammate(source))return false;
            if(source instanceof PlayerEntity attacker&&target instanceof PlayerEntity victim &&(!target.getServer().isPvpEnabled()||!attacker.shouldDamagePlayer(victim) ||PartyManager.areInSameParty(attacker.getUuid(),victim.getUuid())))return false;
        }
        return true;
    }
    private static double duration(LivingEntity target,AilmentType type,double seconds) {
        var attr=switch(type) {
            case BURN->ModAttributes.REDUCED_BURN_DURATION_ON_YOU;
            case POISON->ModAttributes.REDUCED_POISON_DURATION_ON_YOU;
            case BLEED->ModAttributes.REDUCED_BLEED_DURATION_ON_YOU;
            case FREEZE->ModAttributes.REDUCED_FREEZE_DURATION_ON_YOU;
            case SHOCK,ELECTROCUTE->ModAttributes.REDUCED_SHOCK_DURATION_ON_YOU;
        };
        double reduction=AilmentRules.bounded(target.getAttributeValue(attr),0,1);
        if(reduction>=1||Double.isNaN(seconds)||seconds<=0)return 0;
        return seconds==Double.POSITIVE_INFINITY?seconds:Math.min(seconds,3600)*(1-reduction);
    }
    public static void addFlammability(LivingEntity target,float amount,double seconds,Entity source,float dps) {
        if(!canApply(target,source)||target.isFireImmune()||!(amount>0)||!Float.isFinite(amount))return;
        State s=state(target);
        var burn=s.ailments.stream().filter(a->a.getType()==AilmentType.BURN).findFirst().orElse(null);
        if(burn!=null&&burn.getBaseDamage()>dps)return;
        s.flammability=Math.min(1,s.flammability+amount);
        s.flammabilityHit=target.getWorld().getTime();
        if(s.flammability>=1) {
            s.flammability=0;
            applyAilment(target,AilmentType.BURN,dps,seconds,source);
        }
        syncAilments(target);
    }
    public static void updateFreezeSlowModifier(LivingEntity entity,float buildup,float magnitude) {
        var attr=entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if(attr==null)return;
        double slow=AilmentRules.chillSlow(buildup,magnitude);
        var old=attr.getModifier(CHILL_SLOW);
        if(old!=null&&Math.abs(old.value()+slow)<.000001)return;
        attr.removeModifier(CHILL_SLOW);
        if(slow>0)attr.addTemporaryModifier(new EntityAttributeModifier(CHILL_SLOW,-slow,EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    public static void addFreezeBuildup(LivingEntity target,float amount,double seconds,Entity source) {
        addFreezeBuildup(target,amount,seconds,source,1);
    }
    public static void addFreezeBuildup(LivingEntity target,float amount,double seconds,Entity source,float magnitude) {
        if(!canApply(target,source)||!(amount>0)||!Float.isFinite(amount))return;
        State s=state(target);
        if(controlBlocked(target,s))return;
        s.freeze=Math.min(1,s.freeze+amount);
        s.freezeHit=target.getWorld().getTime();
        s.freezeMagnitude=(float)AilmentRules.bounded(magnitude,1,11);
        if(s.freeze>=1) {
            s.freeze=0;
            applyAilment(target,AilmentType.FREEZE,0,seconds,source);
        }
        updateFreezeSlowModifier(target,s.freeze,s.freezeMagnitude);
        syncAilments(target);
    }
    public static void addElectrocuteBuildup(LivingEntity target,float amount,double seconds,Entity source) {
        if(!canApply(target,source)||!(amount>0)||!Float.isFinite(amount))return;
        State s=state(target);
        if(controlBlocked(target,s))return;
        s.electrocute=Math.min(1,s.electrocute+amount);
        s.electrocuteHit=target.getWorld().getTime();
        if(s.electrocute>=1) {
            s.electrocute=0;
            applyAilment(target,AilmentType.ELECTROCUTE,0,seconds,source);
        }
        syncAilments(target);
    }
    public static void applyAilment(LivingEntity target,AilmentType type,float dps,double seconds,Entity source) {
        applyAilment(target,type,dps,seconds,source,null,null,0);
    }
    public static void applyAilment(LivingEntity target,AilmentType type,float dps,double seconds,Entity source,Entity parent,UUID chain,int generation) {
        applyAilment(target,type,dps,seconds,source,parent,chain,generation,type==AilmentType.SHOCK?.15f:type==AilmentType.BLEED?.4f:1);
    }
    public static void applyAilment(LivingEntity target,AilmentType type,float dps,double seconds,Entity source,Entity parent,UUID chain,int generation,float magnitude) {
        if(!canApply(target,source)||!Float.isFinite(dps)||dps<0||!Float.isFinite(magnitude))return;
        if(type==AilmentType.BURN&&target.isFireImmune())return;
        seconds=duration(target,type,seconds);
        if(!(seconds>0))return;
        State s=state(target);
        if(type==AilmentType.BURN&&chain!=null&&target instanceof AilmentHistoryProvider history&&history.cranks$wasHitByChain(chain))return;
        boolean control=type==AilmentType.FREEZE||type==AilmentType.ELECTROCUTE;
        if(control&&controlBlocked(target,s))return;
        if(type==AilmentType.SHOCK)magnitude=(float)AilmentRules.bounded(magnitude,0,.5);
        if(type==AilmentType.BLEED)magnitude=(float)AilmentRules.bounded(magnitude,0,1);
        Ailment next=new Ailment(type,seconds,dps,source==null?null:source.getUuid(),parent==null?null:parent.getUuid(),chain==null?UUID.randomUUID():chain,Math.clamp(generation,0,5),magnitude);
        next.setPlayerSource(source instanceof PlayerEntity);
        if(type==AilmentType.POISON) {
            int bonus=source instanceof LivingEntity living?(int)living.getAttributeValue(ModAttributes.POISON_MAX_STACKS):0;
            int cap=Math.clamp(AilmentRules.BASE_POISON_CAP+bonus,1,AilmentRules.MAX_POISON_CAP);
            var poison=s.ailments.stream().filter(a->a.getType()==type).toList();
            if(poison.size()>=cap) {
                var weakest=poison.stream().min(Comparator.comparingDouble(Ailment::getBaseDamage).thenComparingDouble(Ailment::getRemainingDuration)).orElseThrow();
                if(dps<weakest.getBaseDamage())return;
                s.ailments.remove(weakest);
            }
        } else {
            var current=s.ailments.stream().filter(a->a.getType()==type).findFirst().orElse(null);
            if(current!=null) {
                if(type==AilmentType.BURN) {
                    if(dps<current.getBaseDamage())return;
                    if(dps==current.getBaseDamage()) {
                        current.setRemainingDuration(Math.max(seconds,current.getRemainingDuration()));
                        syncAilments(target);
                        return;
                    }
                } else if(type==AilmentType.SHOCK) {
                    if(magnitude<current.getMagnitude())return;
                } else if(type==AilmentType.BLEED) {
                    // Preserve the burst and expiry. Stronger applications improve future storage.
                    if(magnitude>current.getMagnitude())current.setMagnitude(magnitude);
                    syncAilments(target);
                    return;
                } else return;
                s.ailments.remove(current);
            }
        }
        if(type==AilmentType.BURN&&target instanceof AilmentHistoryProvider history)history.cranks$recordBurnChain(next.getChainUuid());
        s.ailments.add(next);
        if(control) {
            s.freeze=0;
            s.electrocute=0;
            updateFreezeSlowModifier(target,0,1);
            controlFeedback(target,type,false);
        }
        syncAilments(target);
    }
    public static void accumulateBleedDamage(LivingEntity entity,float damage) {
        if(entity.getWorld().isClient()||!(damage>0)||!Float.isFinite(damage))return;
        State s=find(entity);
        if(s==null)return;
        boolean bleeding=false;
        for(var a:s.ailments)if(a.getType()==AilmentType.BLEED&&!a.isExpired()) {
            a.addStoredDamage(damage*a.getMagnitude());
            bleeding=true;
        }
        if(bleeding)bleedFeedback(entity,false);
    }
    public static void refreshAilmentDuration(LivingEntity target,AilmentType type) {
        if(type==AilmentType.POISON)return;
        for(var a:getAilments(target))if(a.getType()==type)a.setRemainingDuration(a.getMaxDuration());
        syncAilments(target);
    }
    public static void tickAilments(LivingEntity entity) {
        if(entity.getWorld().isClient())return;
        State s=find(entity);
        if(s==null)return;
        if(!entity.isAlive()) {
            clearAllAilments(entity);
            return;
        }
        s.controlRecovery=Math.max(0,s.controlRecovery-STEP);
        if(s.controlRecovery<.000001)s.controlRecovery=0;
        long now=entity.getWorld().getTime();
        if(now-s.freezeHit>40&&s.freeze>0) {
            s.freeze=Math.max(0,s.freeze-(float)(.0025/Math.max(.01,1-entity.getAttributeValue(ModAttributes.REDUCED_CHILL_DURATION_ON_YOU))));
            updateFreezeSlowModifier(entity,s.freeze,s.freezeMagnitude);
        }
        if(now-s.electrocuteHit>40)s.electrocute=Math.max(0,s.electrocute-(float)(.01/Math.max(.01,1-entity.getAttributeValue(ModAttributes.REDUCED_SHOCK_DURATION_ON_YOU))));
        if(now-s.flammabilityHit>40)s.flammability=Math.max(0,s.flammability-.005f);
        boolean changed=false;
        Map<PoisonOwner,Float> poisonTicks=new LinkedHashMap<>();
        for(var a:List.copyOf(s.ailments)) {
            if(!entity.isAlive())break;
            float damage=a.advance(STEP,entity.isTouchingWaterOrRain()||entity.inPowderSnow);
            if(damage>0) {
                if(a.getType()==AilmentType.POISON) {
                    poisonTicks.merge(new PoisonOwner(a.getSourceUuid(),a.isPlayerSource()),damage,Float::sum);
                } else {
                    var element=a.getType()==AilmentType.BURN?DamageElement.FIRE:DamageElement.VOID;
                    float actual=dealSecondary(entity,damage,a.getSourceUuid(),a.isPlayerSource(),element,a.getType()==AilmentType.POISON,true,a.getType());
                    if(actual>0&&a.getType()==AilmentType.BURN&&a.getGeneration()<5)spreadBurn(entity,a);
                }
            }
            if(a.isExpired()) {
                s.ailments.remove(a);
                changed=true;
                if(a.getType()==AilmentType.BLEED&&a.getStoredDamage()>0) dealSecondary(entity,a.getStoredDamage(),a.getSourceUuid(),a.isPlayerSource(),DamageElement.PHYSICAL,true,true,AilmentType.BLEED);
                if(a.getType()==AilmentType.FREEZE||a.getType()==AilmentType.ELECTROCUTE) {
                    s.controlRecovery=AilmentRules.CONTROL_RECOVERY_SECONDS;
                    s.recoveryFeedbackSent=false;
                    controlFeedback(entity,a.getType(),true);
                }
            }
        }
        for(var entry:poisonTicks.entrySet())if(entity.isAlive())dealSecondary(entity,entry.getValue(),entry.getKey().id(),entry.getKey().player(),DamageElement.VOID,true,true,AilmentType.POISON);
        if(entity.isAlive()&&(changed||now%5==0))syncAilments(entity);
        if(!entity.isAlive())clearAllAilments(entity);
        else if(s.empty())STATES.remove(entity.getUuid(),s);
    }
    /** Native damage handles permissions, absorption, attribution, and death prevention for every tick. */
    private static float dealSecondary(LivingEntity target,float raw,UUID owner,boolean playerSource,DamageElement element,boolean bypassShield,boolean ailment,AilmentType type) {
        if(!(target.getWorld() instanceof ServerWorld world)||!target.isAlive()||!(raw>0))return 0;
        Entity attacker=owner==null?null:world.getEntity(owner);
        if(attacker==null&&owner!=null)attacker=world.getServer().getPlayerManager().getPlayer(owner);
        if(!canApply(target,attacker))return 0;
        if(playerSource&&target instanceof PlayerEntity victim&&owner!=null&&!owner.equals(victim.getUuid()) &&(!world.getServer().isPvpEnabled()||PartyManager.areInSameParty(owner,victim.getUuid())))return 0;
        if(element==DamageElement.FIRE&&target.isFireImmune())return 0;
        float damage=raw;
        // Bleed stores resolved damage; resistance and Shock have already affected that damage.
        if(ailment&&type!=AilmentType.BLEED) {
            double resistance=switch(element) {
                case FIRE->target.getAttributeValue(ModAttributes.FIRE_RESISTANCE);
                case LIGHTNING->target.getAttributeValue(ModAttributes.LIGHTNING_RESISTANCE);
                case VOID->target.getAttributeValue(ModAttributes.VOID_RESISTANCE);
                default->0;
            };
            if(element==DamageElement.VOID&&target.getAttributeValue(ModAttributes.KEYSTONE_VOID_INOCULATION)>0)return 0;
            damage*=(float)(1-AilmentRules.penetratedResistance(resistance,0));
            damage*=1+getShockMagnitude(target);
        }
        if(!(damage>0)||!Float.isFinite(damage))return 0;
        var magic=world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.MAGIC);
        DamageSource source=new DamageSource(magic,attacker,attacker);
        float before=target.getHealth()+target.getAbsorptionAmount();
        float shieldBefore=((EnergyShieldProvider)target).cranksdungeons$getCurrentEnergyShield();
        int immunity=target.timeUntilRegen;
        SecondaryHit previous=SECONDARY.get();
        boolean accepted;
        try {
            SECONDARY.set(new SecondaryHit(target.getUuid(),element,bypassShield,ailment));
            target.timeUntilRegen=0;
            accepted=target.damage(source,damage);
        } finally {
            target.timeUntilRegen=immunity;
            if(previous==null)SECONDARY.remove();
            else SECONDARY.set(previous);
        }
        float life=Math.max(0,before-target.getHealth()-target.getAbsorptionAmount());
        float shield=Math.max(0,shieldBefore-((EnergyShieldProvider)target).cranksdungeons$getCurrentEnergyShield());
        float actual=life+shield;
        if(!accepted||actual<=0)return 0;
        if(type==AilmentType.POISON)DamageNumberSender.broadcastPoison(world,target,actual,owner);
        else if(type==AilmentType.BLEED)DamageNumberSender.broadcastBleed(world,target,actual,owner);
        else DamageNumberSender.broadcastPeriodic(world,target,actual,element,owner);
        if(type==AilmentType.BLEED) {
            world.playSound(null,target.getBlockPos(),ModSounds.BLEED_BURST,SoundCategory.HOSTILE,.6f,1);
            bleedFeedback(target,true);
        }
        return actual;
    }
    private static void bleedFeedback(LivingEntity entity,boolean burst) {
        var packet=new com.cranks.dungeons.network.BleedVisualPayload(entity.getId(),entity.getUuid(),burst);
        for(var player:PlayerLookup.tracking(entity))ServerPlayNetworking.send(player,packet);
        if(entity instanceof ServerPlayerEntity player)ServerPlayNetworking.send(player,packet);
    }
    private static boolean visible(LivingEntity from,LivingEntity to) {
        return from.getWorld().raycast(new RaycastContext(from.getBoundingBox().getCenter(),to.getBoundingBox().getCenter(),RaycastContext.ShapeType.COLLIDER,RaycastContext.FluidHandling.NONE,from)).getType()==HitResult.Type.MISS;
    }
    private static boolean validSpreadTarget(LivingEntity target,Entity source) {
        if(target==source||!canApply(target,source)||!target.isAttackable())return false;
        if(target instanceof TameableEntity pet&&pet.isTamed())return false;
        if(target instanceof AbstractHorseEntity horse&&horse.isTame())return false;
        if(target instanceof MountDataProvider mount&&mount.cranksdungeons$getMountData().ownerUuid!=null)return false;
        if(target instanceof PlayerEntity player) {
            if(source instanceof PlayerEntity)return true;
            return source instanceof MobEntity mob&&mob.getTarget()==player||source instanceof LivingEntity living&&living.getAttacker()==player;
        }
        return target instanceof HostileEntity||target instanceof SlimeEntity ||source instanceof LivingEntity living&&(living.getAttacker()==target||target.getAttacker()==living) ||target instanceof MobEntity mob&&mob.getTarget()==source;
    }
    private static void spreadBurn(LivingEntity entity,Ailment burn) {
        if(burn.getSourceUuid()==null||!(entity.getWorld() instanceof ServerWorld world))return;
        Entity source=world.getEntity(burn.getSourceUuid());
        if(source==null)return;
        for(var target:world.getEntitiesByClass(LivingEntity.class,entity.getBoundingBox().expand(3),e->e!=entity&&e.squaredDistanceTo(entity)<=9&&validSpreadTarget(e,source)&&visible(entity,e))) {
            if(target instanceof AilmentHistoryProvider history&&history.cranks$wasHitByChain(burn.getChainUuid()))continue;
            float damage=burn.getBaseDamage()*.5f;
            if(damage<.2f)continue;
            applyAilment(target,AilmentType.BURN,damage,Math.max(.05,burn.getRemainingDuration()*.5),source,entity,burn.getChainUuid(),burn.getGeneration()+1);
        }
    }
    public static void handleShockChain(LivingEntity entity,float lightning,Entity source) {
        if(hasAilment(entity,AilmentType.SHOCK))executeShockChain(entity,lightning,source);
    }
    public static void executeShockChain(LivingEntity entity,float lightning,Entity source) {
        if(!(entity.getWorld() instanceof ServerWorld world)||!(lightning>0)||!Float.isFinite(lightning))return;
        for(var target:world.getEntitiesByClass(LivingEntity.class,entity.getBoundingBox().expand(10),e->e!=entity&&e.squaredDistanceTo(entity)<=100&&validSpreadTarget(e,source)&&visible(entity,e))) {
            float damage=lightning*(source instanceof PlayerEntity&&target instanceof PlayerEntity?.25f:1);
            float actual=dealSecondary(target,damage,source==null?null:source.getUuid(),source instanceof PlayerEntity,DamageElement.LIGHTNING,false,false,null);
            if(actual<=0)continue;
            float threshold=com.cranks.dungeons.util.CombatUtility.getEffectiveAilmentThreshold(target,AilmentType.SHOCK);
            applyAilment(target,AilmentType.SHOCK,0,AilmentType.SHOCK.getBaseDuration(),source,null,null,0,Math.clamp(actual/threshold*.3f,.05f,.3f));
            world.playSound(null,target.getBlockPos(),ModSounds.SHOCK_CHAIN,SoundCategory.HOSTILE,.6f,1);
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,target.getX(),target.getBodyY(.5),target.getZ(),4,.2,.2,.2,.02);
            var packet=new ShockArcPayload(entity.getBoundingBox().getCenter(),target.getBoundingBox().getCenter(),0xFFFFFF);
            for(var player:world.getPlayers())if(player.squaredDistanceTo(entity)<64*64)ServerPlayNetworking.send(player,packet);
        }
    }
    public static boolean isShockChainInProgress() {
        return shockChain;
    }
    public static boolean acquireShockChainLock() {
        if(shockChain)return false;
        shockChain=true;
        return true;
    }
    private static boolean controlBlocked(LivingEntity target,State state) {
        if(isStunned(target))return true;
        if(state.controlRecovery<=0)return false;
        if(!state.recoveryFeedbackSent) {
            state.recoveryFeedbackSent=true;
            var packet=new com.cranks.dungeons.network.AilmentResistedPayload(target.getId(),target.getUuid());
            for(var player:PlayerLookup.tracking(target))ServerPlayNetworking.send(player,packet);
            if(target instanceof ServerPlayerEntity player)ServerPlayNetworking.send(player,packet);
        }
        return true;
    }
    public static void releaseShockChainLock() {
        shockChain=false;
    }
    private static void controlFeedback(LivingEntity entity,AilmentType type,boolean end) {
        if(!(entity.getWorld() instanceof ServerWorld world)||type==AilmentType.ELECTROCUTE&&end)return;
        world.playSound(null,entity.getBlockPos(),end?ModSounds.FREEZE_END:type==AilmentType.FREEZE?ModSounds.FREEZE_APPLY:ModSounds.ELECTROCUTED_APPLY,SoundCategory.HOSTILE,.8f,1);
        // The client owns bounded formation/break particles; don't duplicate them through vanilla packets.
    }
    public static AilmentType effectType(RegistryEntry<StatusEffect> effect) {
        for(var type:AilmentType.values())if(effect.equals(effect(type)))return type;
        return null;
    }
    private static RegistryEntry<StatusEffect> effect(AilmentType type) {
        return switch(type) {
            case BURN->ModEffects.BURN;
            case POISON->ModEffects.POISON;
            case BLEED->ModEffects.BLEED;
            case FREEZE->ModEffects.FREEZE;
            case SHOCK->ModEffects.SHOCK;
            case ELECTROCUTE->ModEffects.ELECTROCUTE;
        };
    }
    private static Identifier texture(AilmentType type) {
        return Identifier.of("cranks-dungeons","textures/mob_effect/"+type.name().toLowerCase(Locale.ROOT)+".png");
    }
    public static void onEffectRemoved(LivingEntity entity,RegistryEntry<StatusEffect> effect) {
        if(entity.getWorld().isClient()||UPDATING_MIRRORS.contains(entity.getUuid()))return;
        var type=effectType(effect);
        if(type!=null)clearAilment(entity,type);
    }
    private static void mirror(LivingEntity entity,AilmentType type,List<Ailment> active) {
        var effect=effect(type);
        var old=entity.getStatusEffect(effect);
        if(active.isEmpty()) {
            if(old!=null)entity.removeStatusEffect(effect);
            return;
        }
        double seconds=active.stream().mapToDouble(Ailment::getRemainingDuration).min().orElse(0);
        int ticks=seconds==Double.POSITIVE_INFINITY?StatusEffectInstance.INFINITE:Math.max(1,(int)Math.ceil(seconds*20-.000001));
        int amplifier=type==AilmentType.POISON?active.size()-1:type==AilmentType.SHOCK?Math.round(active.get(0).getMagnitude()*100):0;
        if(old!=null&&old.getAmplifier()==amplifier&&Math.abs(old.getDuration()-ticks)<=1)return;
        if(old!=null)entity.removeStatusEffect(effect);
        entity.addStatusEffect(new StatusEffectInstance(effect,ticks,amplifier,false,false,true));
    }
    private static List<AilmentSyncPayload.AilmentEntry> snapshot(LivingEntity entity) {
        State s=find(entity);
        if(s==null)return List.of();
        var entries=new ArrayList<AilmentSyncPayload.AilmentEntry>();
        UPDATING_MIRRORS.add(entity.getUuid());
        try {
            for(var type:AilmentType.values()) {
                var active=s.ailments.stream().filter(a->a.getType()==type&&!a.isExpired()).toList();
                mirror(entity,type,active);
                if(active.isEmpty())continue;
                double seconds=active.stream().mapToDouble(Ailment::getRemainingDuration).min().orElse(0);
                float magnitude=active.stream().map(Ailment::getMagnitude).max(Float::compare).orElse(0f);
                float dps=0,stored=0;
                for(var a:active) {
                    dps+=a.getBaseDamage();
                    stored+=a.getStoredDamage();
                }
                if(type!=AilmentType.BURN&&type!=AilmentType.POISON)dps=0;
                entries.add(new AilmentSyncPayload.AilmentEntry(texture(type),active.size(),false,magnitude,dps,stored,seconds==Double.POSITIVE_INFINITY?-1:(float)seconds));
            }
        } finally {
            UPDATING_MIRRORS.remove(entity.getUuid());
        }
        if(s.flammability>0&&!hasAilment(entity,AilmentType.BURN))entries.add(new AilmentSyncPayload.AilmentEntry(texture(AilmentType.BURN),Math.max(1,(int)(s.flammability*100)),true,0,0,0,0));
        if(s.freeze>0&&!hasAilment(entity,AilmentType.FREEZE))entries.add(new AilmentSyncPayload.AilmentEntry(Identifier.of("cranks-dungeons","textures/mob_effect/freeze_buildup.png"),Math.max(1,(int)(s.freeze*100)),true,(float)AilmentRules.chillSlow(s.freeze,s.freezeMagnitude),0,0,0));
        if(s.electrocute>0&&!hasAilment(entity,AilmentType.ELECTROCUTE))entries.add(new AilmentSyncPayload.AilmentEntry(Identifier.of("cranks-dungeons","textures/mob_effect/electrocute_buildup.png"),Math.max(1,(int)(s.electrocute*100)),true,0,0,0,0));
        return List.copyOf(entries);
    }
    public static void syncAilments(LivingEntity entity) {
        if(entity.getWorld().isClient())return;
        State s=find(entity);
        if(s==null)return;
        var entries=snapshot(entity);
        if(entries.equals(s.lastSent))return;
        s.lastSent=entries;
        var packet=new AilmentSyncPayload(entity.getId(),entries);
        for(var player:PlayerLookup.tracking(entity))ServerPlayNetworking.send(player,packet);
        if(entity instanceof ServerPlayerEntity player)ServerPlayNetworking.send(player,packet);
    }
    public static void syncTo(LivingEntity entity,ServerPlayerEntity player) {
        ServerPlayNetworking.send(player,new AilmentSyncPayload(entity.getId(),snapshot(entity)));
    }
    public static void clearAilment(LivingEntity entity,AilmentType type) {
        State s=find(entity);
        if(s!=null) {
            boolean control=hasAilment(entity,type)&&(type==AilmentType.FREEZE||type==AilmentType.ELECTROCUTE);
            s.ailments.removeIf(a->a.getType()==type);
            if(control) {
                s.controlRecovery=AilmentRules.CONTROL_RECOVERY_SECONDS;
                s.recoveryFeedbackSent=false;
            }
        }
        UPDATING_MIRRORS.add(entity.getUuid());
        try {
            entity.removeStatusEffect(effect(type));
        } finally {
            UPDATING_MIRRORS.remove(entity.getUuid());
        }
        syncAilments(entity);
    }
    public static void clearAllAilments(LivingEntity entity) {
        if(entity==null||entity.getWorld().isClient())return;
        State s=find(entity);
        if(s!=null) {
            s.ailments.clear();
            s.freeze=s.electrocute=s.flammability=0;
            s.controlRecovery=0;
            syncAilments(entity);
            STATES.remove(entity.getUuid(),s);
        }
        updateFreezeSlowModifier(entity,0,1);
        UPDATING_MIRRORS.add(entity.getUuid());
        try {
            for(var type:AilmentType.values())entity.removeStatusEffect(effect(type));
        } finally {
            UPDATING_MIRRORS.remove(entity.getUuid());
        }
    }
    /** Release memory without curing the applications written to the entity's save. */
    public static void onEntityDespawn(LivingEntity entity) {
        // Players keep the same instance during dimension travel. Disconnect cleanup runs after saving.
        if(entity instanceof ServerPlayerEntity player&&player.getServer().getPlayerManager().getPlayer(player.getUuid())==player)return;
        State s=find(entity);
        if(s!=null)STATES.remove(entity.getUuid(),s);
    }
    public static void cleanupPlayers(net.minecraft.server.MinecraftServer server) {
        STATES.entrySet().removeIf(entry->entry.getValue().owner instanceof ServerPlayerEntity player &&player.getServer()==server&&server.getPlayerManager().getPlayer(entry.getKey())!=player);
    }
    public static void onEntityDeath(LivingEntity entity) {
        clearAllAilments(entity);
    }
    public static void clearAll() {
        STATES.clear();
        UPDATING_MIRRORS.clear();
        SECONDARY.remove();
        shockChain=false;
    }
    public static void writeNbt(LivingEntity entity,NbtCompound nbt) {
        State s=find(entity);
        if(s==null||s.empty()) {
            nbt.remove(SAVE_KEY);
            return;
        }
        var root=new NbtCompound();
        var list=new NbtList();
        for(var a:s.ailments) {
            var tag=new NbtCompound();
            tag.putString("Type",a.getType().name());
            tag.putDouble("Remaining",a.getRemainingDuration());
            tag.putDouble("Maximum",a.getMaxDuration());
            tag.putDouble("Tick",a.getTimeSinceLastTick());
            tag.putFloat("Dps",a.getBaseDamage());
            tag.putFloat("Magnitude",a.getMagnitude());
            tag.putFloat("Stored",a.getStoredDamage());
            tag.putBoolean("PlayerSource",a.isPlayerSource());
            tag.putInt("Generation",a.getGeneration());
            if(a.getSourceUuid()!=null)tag.putUuid("Source",a.getSourceUuid());
            if(a.getParentUuid()!=null)tag.putUuid("Parent",a.getParentUuid());
            if(a.getChainUuid()!=null)tag.putUuid("Chain",a.getChainUuid());
            list.add(tag);
        }
        root.put("Applications",list);
        root.putFloat("Freeze",s.freeze);
        root.putFloat("Electrocute",s.electrocute);
        root.putFloat("Flammability",s.flammability);
        root.putFloat("FreezeMagnitude",s.freezeMagnitude);
        root.putDouble("ControlRecovery",s.controlRecovery);
        long now=entity.getWorld().getTime();
        root.putLong("FreezeAge",Math.clamp(now-s.freezeHit,0,41));
        root.putLong("ElectrocuteAge",Math.clamp(now-s.electrocuteHit,0,41));
        root.putLong("FlammabilityAge",Math.clamp(now-s.flammabilityHit,0,41));
        nbt.put(SAVE_KEY,root);
    }
    public static void readNbt(LivingEntity entity,NbtCompound nbt) {
        if(entity.getWorld().isClient())return;
        // Migrate the previous overlapping movement modifiers without touching unrelated effects.
        var speed=entity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if(speed!=null) {
            speed.removeModifier(CHILL_SLOW);
            speed.removeModifier(Identifier.of("cranks-dungeons","freeze_movement_slow"));
        }
        State old=find(entity);
        if(old!=null)STATES.remove(entity.getUuid(),old);
        if(!nbt.contains(SAVE_KEY,NbtElement.COMPOUND_TYPE))return;
        var root=nbt.getCompound(SAVE_KEY);
        State s=state(entity);
        var list=root.getList("Applications",NbtElement.COMPOUND_TYPE);
        int poisons=0;
        var seen=EnumSet.noneOf(AilmentType.class);
        for(int i=0; i<Math.min(list.size(),AilmentRules.MAX_POISON_CAP+5); i++) {
            var tag=list.getCompound(i);
            AilmentType type;
            try {
                type=AilmentType.valueOf(tag.getString("Type"));
            } catch(IllegalArgumentException ignored) {
                continue;
            }
            if(type==AilmentType.POISON) {
                if(++poisons>AilmentRules.MAX_POISON_CAP)continue;
            } else if(!seen.add(type))continue;
            double remaining=tag.getDouble("Remaining");
            if(!(remaining>0)||Double.isNaN(remaining))continue;
            remaining=remaining==Double.POSITIVE_INFINITY?remaining:Math.min(3600,remaining);
            float dps=(float)AilmentRules.bounded(tag.getFloat("Dps"),0,1_000_000);
            float magnitude=(float)AilmentRules.bounded(tag.getFloat("Magnitude"),0,type==AilmentType.SHOCK?.5:type==AilmentType.BLEED?1:1_000_000);
            var a=new Ailment(type,remaining,dps,tag.containsUuid("Source")?tag.getUuid("Source"):null,tag.containsUuid("Parent")?tag.getUuid("Parent"):null,tag.containsUuid("Chain")?tag.getUuid("Chain"):UUID.randomUUID(),Math.clamp(tag.getInt("Generation"),0,5),magnitude);
            a.setMaxDuration(remaining);
            a.addTimeSinceLastTick(AilmentRules.bounded(tag.getDouble("Tick"),0,.5));
            a.setStoredDamage((float)AilmentRules.bounded(tag.getFloat("Stored"),0,1_000_000));
            a.setPlayerSource(tag.getBoolean("PlayerSource"));
            s.ailments.add(a);
            if(type==AilmentType.BURN&&entity instanceof AilmentHistoryProvider history)history.cranks$recordBurnChain(a.getChainUuid());
        }
        s.freeze=(float)AilmentRules.bounded(root.getFloat("Freeze"),0,.999);
        s.electrocute=(float)AilmentRules.bounded(root.getFloat("Electrocute"),0,.999);
        s.flammability=(float)AilmentRules.bounded(root.getFloat("Flammability"),0,.999);
        s.freezeMagnitude=(float)AilmentRules.bounded(root.getFloat("FreezeMagnitude"),1,11);
        s.controlRecovery=AilmentRules.bounded(root.getDouble("ControlRecovery"),0,AilmentRules.CONTROL_RECOVERY_SECONDS);
        long now=entity.getWorld().getTime();
        s.freezeHit=now-Math.clamp(root.getLong("FreezeAge"),0,41);
        s.electrocuteHit=now-Math.clamp(root.getLong("ElectrocuteAge"),0,41);
        s.flammabilityHit=now-Math.clamp(root.getLong("FlammabilityAge"),0,41);
        updateFreezeSlowModifier(entity,s.freeze,s.freezeMagnitude);
        // Initial networking happens on tracking, after deserialization has completed.
    }
}
