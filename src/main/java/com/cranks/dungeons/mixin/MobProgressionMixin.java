package com.cranks.dungeons.mixin;

import com.cranks.dungeons.progression.*;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(MobEntity.class)
public abstract class MobProgressionMixin implements CombatEncounterAccess {
    @Unique private final CombatEncounter cranks$combatEncounter=new CombatEncounter();
    @Override public CombatEncounter cranks$encounter() { return cranks$combatEncounter; }
    @Inject(method="initialize",at=@At("HEAD"))
    private void origin(ServerWorldAccess world,LocalDifficulty difficulty,SpawnReason reason,EntityData data,CallbackInfoReturnable<EntityData> cir) {
        cranks$combatEncounter.origin=switch(reason) {
            case NATURAL,CHUNK_GENERATION,STRUCTURE,PATROL,EVENT,JOCKEY,REINFORCEMENT,MOB_SUMMONED,TRIGGERED -> 1;
            default -> 2;
        };
    }
    @Inject(method="writeCustomDataToNbt",at=@At("TAIL"))
    private void save(NbtCompound tag,CallbackInfo ci) {
        tag.putInt("cranksXpOrigin",cranks$combatEncounter.origin); tag.putBoolean("cranksXpPaid",cranks$combatEncounter.paid);
    }
    @Inject(method="readCustomDataFromNbt",at=@At("TAIL"))
    private void load(NbtCompound tag,CallbackInfo ci) {
        cranks$combatEncounter.origin=Math.clamp(tag.getInt("cranksXpOrigin"),0,2);
        cranks$combatEncounter.paid=tag.getBoolean("cranksXpPaid");
    }
    @Inject(method="convertTo",at=@At("RETURN"))
    private <T extends MobEntity> void conversion(EntityType<T> type,boolean keepEquipment,CallbackInfoReturnable<T> cir) {
        if(cir.getReturnValue() instanceof CombatEncounterAccess child) {
            child.cranks$encounter().origin=cranks$combatEncounter.origin;
            child.cranks$encounter().paid=cranks$combatEncounter.paid;
            child.cranks$encounter().hits.putAll(cranks$combatEncounter.hits);
            child.cranks$encounter().support.putAll(cranks$combatEncounter.support);
        }
    }
}
