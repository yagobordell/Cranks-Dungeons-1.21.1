package com.cranks.dungeons.util;

import com.cranks.dungeons.ailment.AilmentManager;
import com.cranks.dungeons.ailment.AilmentType;
import net.minecraft.entity.LivingEntity;

/** Per-call state shared by the damage hooks. Nested hits restore the outer hit. */
public final class DamageHit implements AutoCloseable {
    private static final ThreadLocal<DamageHit> CURRENT = new ThreadLocal<>();

    private final DamageHit parent;
    private final LivingEntity target;
    private final boolean hadShock;
    private final boolean fullHealth;
    private boolean critical;
    private float critMultiplier = 1.0f;
    private boolean customBlock;
    private boolean shieldHandled;
    private Runnable blockFeedback;
    private DamageConversionHelper.DamageBreakdown breakdown;
    private DamageConversionHelper.DamageBreakdown beforeMitigation;
    private float damageToLife;
    private float damageToShield;

    private DamageHit(LivingEntity target) {
        this.parent = CURRENT.get();
        this.target = target;
        this.hadShock = AilmentManager.hasAilment(target, AilmentType.SHOCK);
        this.fullHealth = target.getHealth() >= target.getMaxHealth() - 0.01f;
        CURRENT.set(this);
    }

    public static DamageHit begin(LivingEntity target) {
        return new DamageHit(target);
    }

    public static DamageHit current() {
        return CURRENT.get();
    }

    public static DamageHit forTarget(LivingEntity target) {
        DamageHit hit = current();
        return hit != null && hit.target == target ? hit : null;
    }

    public boolean hadShock() { return hadShock; }
    public boolean fullHealthBeforeHit() { return fullHealth; }
    public boolean critical() { return critical; }
    public float critMultiplier() { return critMultiplier; }
    public boolean customBlock() { return customBlock; }
    public boolean shieldHandled() { return shieldHandled; }
    public void bypassBlock() { shieldHandled = true; }
    public DamageConversionHelper.DamageBreakdown breakdown() { return breakdown; }
    public DamageConversionHelper.DamageBreakdown beforeMitigation() { return beforeMitigation; }
    public void beforeMitigation(DamageConversionHelper.DamageBreakdown value) {
        beforeMitigation = value.scaledTo(value.getTotal());
    }
    public float damageToLife() { return damageToLife; }
    public float damageToShield() { return damageToShield; }

    public void critical(float multiplier) {
        critical = true;
        critMultiplier = multiplier;
    }

    public void block(Runnable feedback) {
        customBlock = true;
        shieldHandled = true;
        blockFeedback = feedback;
    }

    public void finishBlock() {
        if (blockFeedback != null) {
            Runnable feedback = blockFeedback;
            blockFeedback = null;
            feedback.run();
        }
    }

    public void setBreakdown(DamageConversionHelper.DamageBreakdown breakdown) {
        this.breakdown = breakdown;
    }

    public void recordDamageToLife(float damage) { damageToLife += damage; }
    public void recordDamageToShield(float damage) { damageToShield += damage; }

    @Override
    public void close() {
        if (parent == null) CURRENT.remove();
        else CURRENT.set(parent);
    }
}
