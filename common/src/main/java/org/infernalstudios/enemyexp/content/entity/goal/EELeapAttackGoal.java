package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class EELeapAttackGoal<T extends Mob> extends Goal {
    public interface ILeapCallbacks {
        void onWindUpStart();

        void onWindUpEnd();

        void onLeapStart();

        void onLeapEnd();

        void onStop();
    }

    private final T mob;
    private final ILeapCallbacks callbacks;
    private final double minTriggerDistance;
    private final double maxTriggerDistance;
    private final int maxCooldown;
    private final int windUpEnd;
    private final int totalTicks;

    private final double yImpulse;
    private final double xzImpulse;
    private final double hitSq;

    private int cooldown;
    private int tickCounter;
    private boolean launched;
    private boolean hitDealt;

    public EELeapAttackGoal(T mob, ILeapCallbacks callbacks, double minTriggerDistance, double maxTriggerDistance, int cooldown, int windUpEnd, int totalTicks, double yImpulse, double xzImpulse, double hitSq) {
        this.mob = mob;
        this.callbacks = callbacks;
        this.minTriggerDistance = minTriggerDistance;
        this.maxTriggerDistance = maxTriggerDistance;
        this.maxCooldown = cooldown;
        this.windUpEnd = windUpEnd;
        this.totalTicks = totalTicks;
        this.yImpulse = yImpulse;
        this.xzImpulse = xzImpulse;
        this.hitSq = hitSq;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public EELeapAttackGoal(T mob, ILeapCallbacks callbacks, double minTriggerDistance, double maxTriggerDistance, int cooldown, int windUpEnd, int totalTicks) {
        this(mob, callbacks, minTriggerDistance, maxTriggerDistance, cooldown, windUpEnd, totalTicks, 0.7D, 0.9D, 1.5D);
    }

    @Override
    public boolean canUse() {
        tickCooldown();
        if (cooldown > 0) return false;

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        double distSq = mob.distanceToSqr(target);
        double minSq = minTriggerDistance * minTriggerDistance;
        double maxSq = maxTriggerDistance * maxTriggerDistance;

        return distSq >= minSq && distSq <= maxSq && mob.onGround();
    }

    @Override
    public boolean canContinueToUse() {
        // It already landed and make the attack, no need to wait!
        if (mob.onGround() && tickCounter > windUpEnd + 2) {
            return false;
        }

        return tickCounter <= totalTicks;
    }

    @Override
    public void start() {
        tickCounter = 0;
        launched = false;
        hitDealt = false;
        callbacks.onWindUpStart();
    }

    @Override
    public void tick() {
        tickCounter++;

        LivingEntity target = mob.getTarget();
        if (target != null) mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Windup - charge
        if (tickCounter < windUpEnd) {
            mob.getNavigation().stop();
            return;
        }

        if (tickCounter == windUpEnd) {
            callbacks.onWindUpEnd();
        }

        // launch - leap
        if (!launched) {
            launched = true;
            if (target != null) applyLeapVelocity(target);
        }

        // landing
        if (!hitDealt && target != null) {
            boolean closeEnough = mob.distanceToSqr(target) <= hitSq;
            boolean landed = mob.onGround() && tickCounter > windUpEnd + 2;

            if (closeEnough && landed) {
                hitDealt = true;
                mob.doHurtTarget(target);
            }

            if (tickCounter == totalTicks) {
                callbacks.onLeapEnd();
            }
        }
    }

    @Override
    public void stop() {
        cooldown = maxCooldown;
        callbacks.onStop();
    }

    private void tickCooldown() {
        if (cooldown > 0) cooldown--;
    }

    private void applyLeapVelocity(LivingEntity target) {
        callbacks.onLeapStart();
        Vec3 toTarget = target.position().subtract(mob.position());
        double horizLen = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);

        if (horizLen < 0.001D) return;

        double nx = toTarget.x / horizLen * xzImpulse;
        double nz = toTarget.z / horizLen * xzImpulse;

        Vec3 current = mob.getDeltaMovement();
        mob.setDeltaMovement(current.x + nx, yImpulse, current.z + nz);
        mob.hasImpulse = true;
    }
}
