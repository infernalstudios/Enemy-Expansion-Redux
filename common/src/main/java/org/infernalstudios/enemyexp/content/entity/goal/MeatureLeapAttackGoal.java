package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.infernalstudios.enemyexp.content.entity.MeatureEntity;

import java.util.EnumSet;

public class MeatureLeapAttackGoal extends Goal {
    /** block distance that may trigge the leap */
    private static final double LEAP_TRIGGER_DISTANCE = 8.0D;

    /** how far below this distance the goal stops being usable */
    private static final double MIN_TRIGGER_DISTANCE = 3.0D;

    /** Vertical component of the launch impulse */
    private static final double LEAP_Y_IMPULSE = 0.7D;

    /** horizontal direction vector multiplier */
    private static final double LEAP_XZ_IMPULSE = 0.8D;

    /** melee reach used when checking for a landing hit */
    private static final double HIT_REACH_SQ = 1.5D;

    /** cooldown ticks between leaps */
    private static final int COOLDOWN_TICKS = 5;

    /** charge time for loading animation at the start of the attack */
    private static final int WINDUP_END_TICK = 2;

    /** time before restating the attack (this should be the end of the anim */
    private static final int ANIM_TOTAL_TICKS = 30;

    private final MeatureEntity meature;
    private int tickCounter;
    private int cooldown;
    private boolean launched;
    private boolean hitDealt;

    public MeatureLeapAttackGoal(MeatureEntity meature) {
        this.meature = meature;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        tickCooldown();
        if (cooldown > 0) return false;

        LivingEntity target = meature.getTarget();
        if (target == null || !target.isAlive()) return false;

        double distSq = meature.distanceToSqr(target);
        double minSq = MIN_TRIGGER_DISTANCE * MIN_TRIGGER_DISTANCE;
        double maxSq = LEAP_TRIGGER_DISTANCE * LEAP_TRIGGER_DISTANCE;

        return distSq >= minSq && distSq <= maxSq && meature.onGround();
    }

    @Override
    public boolean canContinueToUse() {
        return tickCounter <= ANIM_TOTAL_TICKS;
    }

    @Override
    public void start() {
        tickCounter = 0;
        launched = false;
        hitDealt = false;
        meature.setLeapRule();
        meature.triggerAnim("leap", "leap");
    }

    @Override
    public void tick() {
        tickCounter++;

        LivingEntity target = meature.getTarget();
        if (target != null) meature.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Charge
        if (tickCounter < WINDUP_END_TICK) {
            meature.getNavigation().stop();
            return;
        }

        // Launch
        if (!launched) {
            launched = true;
            if (target != null) applyLeapVelocity(target);
        }

        // landing
        if (!hitDealt && target != null) {
            boolean closeEnough = meature.distanceToSqr(target) <= HIT_REACH_SQ;
            boolean landed = meature.onGround() && tickCounter > WINDUP_END_TICK + 2;

            if (closeEnough && landed) {
                hitDealt = true;
                meature.doHurtTarget(target);
            }
        }
    }

    @Override
    public void stop() {
        cooldown = COOLDOWN_TICKS;
        meature.setIdleRule();
    }

    public void tickCooldown() {
        if (cooldown > 0) cooldown--;
    }

    private void applyLeapVelocity(LivingEntity target) {
        // Direction from meature → target
        Vec3 toTarget = target.position().subtract(meature.position());
        double horizLen = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);

        if (horizLen < 0.001D) return;

        double nx = toTarget.x / horizLen * LEAP_XZ_IMPULSE;
        double nz = toTarget.z / horizLen * LEAP_XZ_IMPULSE;

        Vec3 current = meature.getDeltaMovement();
        meature.setDeltaMovement(current.x + nx, LEAP_Y_IMPULSE, current.z + nz);

        meature.hasImpulse = true;
    }
}