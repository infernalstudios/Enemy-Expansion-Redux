package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.infernalstudios.enemyexp.content.entity.IChargeable;

import java.util.EnumSet;
import java.util.List;

/**
 * Goal for performing a charge attack, which consists of a windup phase (loading energy) and then a dash forward (the charge).
 * <p>
 * This ignores the path navigation system and forces the mob to move in a straight line, we made this to have more control
 * and avoid creating more complex systems with mixins or creating a new path navigation. This goal shouldn't be used
 * with mobs that doesn't have a normal walking movement (like flying mobs).
 *
 * @see IChargeable
 */
public class ChargeAttackGoal<T extends Mob & IChargeable> extends Goal {
    public interface ChargeAttackCallbacks {
        void onWindupStart();
        void onChargeTick();
        void onChargeEnd();
        void onStop();
        boolean canBeHurtNormally(LivingEntity entity);
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
        Vec3 currentMotion = mob.getDeltaMovement();

        mob.setDeltaMovement(dirX * chargeSpeed, currentMotion.y, dirZ * chargeSpeed);

        handleStepUp();

        if (!mob.level().isClientSide) damageCollidingEntities();
        if (chargeTime == 1) callbacks.onChargeEnd();
    }

    private void handleStepUp() {
        if (mob.horizontalCollision && mob.onGround()) {
            if (!canStepUp()) return;
            mob.setDeltaMovement(mob.getDeltaMovement().x, 0.42f, mob.getDeltaMovement().z);
        }
    }

    private boolean canStepUp() {
        AABB checkBox = mob.getBoundingBox().move(dirX * 0.3, mob.maxUpStep(), dirZ * 0.3);
        return mob.level().noCollision(mob, checkBox);
    }

    private void damageCollidingEntities() {
        List<LivingEntity> collided = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(0.5));
        collided.remove(mob);

        for (LivingEntity entity : collided) {
            if (!callbacks.canBeHurtNormally(entity)) {
                entity.hurt(entity.damageSources().generic(), chargeDamage);
            } else {
                entity.hurt(mob.damageSources().mobAttack(mob), chargeDamage);
            }
            Vec3 knockbackDir = entity.position().subtract(mob.position()).normalize();
            entity.push(knockbackDir.x * chargeKnockback, 0.3, knockbackDir.z * chargeKnockback);
        }
    }
}