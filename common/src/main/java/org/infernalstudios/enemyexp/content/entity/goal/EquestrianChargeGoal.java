package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import org.infernalstudios.enemyexp.content.entity.EquestrianEntity;

/**
 * Charge goal for {@link EquestrianEntity}.
 * <p>
 * Unlike the Slugger (whose charge is triggered externally from {@code hurt()}),
 * this goal activates on its own when the mob has a valid target and is not in
 * a special state (panic, kiting, etc.).
 */
public class EquestrianChargeGoal extends ChargeAttackGoal<EquestrianEntity> {
    private static final int COOLDOWN_TICKS = 35;
    private int cooldown = 0;

    public EquestrianChargeGoal(EquestrianEntity mob) {
        super(mob, new EquestrianChargeCallbacks(mob), EquestrianEntity.CHARGE_WINDUP, EquestrianEntity.CHARGE_DURATION, EquestrianEntity.CHARGE_SPEED, EquestrianEntity.CHARGE_DAMAGE, EquestrianEntity.CHARGE_KNOCKBACK);
    }

    @Override
    public boolean canUse() {
        if (mob.getChargeTime() > 0) return true;
        if (mob.getState() == EquestrianEntity.STATE_SITTING) return false;

        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (!mob.getSensing().hasLineOfSight(target)) return false;

        // Don't start a charge while panicking.
        if (mob.isInSpecialState()) return false;

        Vec3 toTarget = target.position().subtract(mob.position()).normalize();
        mob.setChargeDirX((float) toTarget.x);
        mob.setChargeDirZ((float) toTarget.z);
        mob.setChargeTime(EquestrianEntity.CHARGE_DURATION + EquestrianEntity.CHARGE_WINDUP);
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getChargeTime() > 0;
    }

    @Override
    public void stop() {
        super.stop();
        cooldown = COOLDOWN_TICKS;
    }

    private static class EquestrianChargeCallbacks implements ChargeAttackCallbacks {
        private final EquestrianEntity mob;

        EquestrianChargeCallbacks(EquestrianEntity mob) {
            this.mob = mob;
        }

        @Override
        public void onWindupStart() {
            mob.setState(EquestrianEntity.STATE_CHARGING_GALLOP);
        }

        @Override
        public void onChargeTick() {
        }

        @Override
        public void onChargeEnd() {
        }

        @Override
        public void onStop() {
            mob.setState(EquestrianEntity.STATE_NORMAL);
        }

        @Override
        public boolean canBeHurtNormally(LivingEntity entity) {
            return !(entity instanceof Zombie);
        }
    }
}