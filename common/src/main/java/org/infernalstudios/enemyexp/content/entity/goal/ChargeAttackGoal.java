package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.infernalstudios.enemyexp.content.entity.IChargeable;

import java.util.EnumSet;
import java.util.List;

/**
 * Goal for performing a charge attack, which consists of a windup phase (loading energy) and then a dash forward (the charge).
 *
 * @see IChargeable
 */
public class ChargeAttackGoal<T extends Mob & IChargeable> extends Goal {
    /** Callbacks to modify mob's texture/animation during the charge attack */
    public interface ChargeAttackCallbacks {
        /** Called once when the windup phase begins */
        void onWindupStart();

        /** Called every tick while the mob is dashing */
        void onChargeTick();

        /** Called on the last charge tick. Restore the normal texture/animation here if needed */
        void onChargeEnd();

        /** Called when the goal terminates for any reason (finished, target lost, externally interrupted) */
        void onStop();
    }

    protected final T mob;
    private final ChargeAttackCallbacks callbacks;

    private final int windupDuration;
    private final int chargeDuration;
    private final float chargeSpeed;
    private final float chargeDamage;
    private final float chargeKnockback;

    private float dirX;
    private float dirZ;

    public ChargeAttackGoal(T mob, ChargeAttackCallbacks callbacks, int windupDuration, int chargeDuration, float chargeSpeed, float chargeDamage, float chargeKnockback) {
        this.mob = mob;
        this.callbacks = callbacks;
        this.windupDuration = windupDuration;
        this.chargeDuration = chargeDuration;
        this.chargeSpeed = chargeSpeed;
        this.chargeDamage = chargeDamage;
        this.chargeKnockback = chargeKnockback;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.getChargeTime() > 0;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.dirX = mob.getChargeDirX();
        this.dirZ = mob.getChargeDirZ();
        callbacks.onWindupStart();
        if (mob.getTarget() != null) mob.lookAt(EntityAnchorArgument.Anchor.EYES, mob.getTarget().getEyePosition());
    }

    @Override
    public void tick() {
        int chargeTime = mob.getChargeTime();
        if (chargeTime <= 0) return;

        if (chargeTime > chargeDuration) {
            mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0);
        } else {
            performCharge(chargeTime);
        }

        mob.setChargeTime(chargeTime - 1);
        mob.hurtMarked = true;
    }

    @Override
    public void stop() {
        callbacks.onStop();
        mob.setChargeTime(0);
        mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0);
    }

    private void performCharge(int chargeTime) {
        callbacks.onChargeTick();

        mob.setDeltaMovement(
                dirX * chargeSpeed,
                mob.getDeltaMovement().y,
                dirZ * chargeSpeed
        );

        if (!mob.level().isClientSide) {
            damageCollidingEntities();
        }

        if (chargeTime == 1) {
            callbacks.onChargeEnd();
        }
    }

    private void damageCollidingEntities() {
        List<LivingEntity> collided = mob.level().getEntitiesOfClass(
                LivingEntity.class,
                mob.getBoundingBox().inflate(0.5)
        );
        collided.remove(mob);

        for (LivingEntity entity : collided) {
            entity.hurt(mob.damageSources().mobAttack(mob), chargeDamage);
            Vec3 knockbackDir = entity.position().subtract(mob.position()).normalize();
            entity.push(knockbackDir.x * chargeKnockback, 0.3, knockbackDir.z * chargeKnockback);
        }
    }
}