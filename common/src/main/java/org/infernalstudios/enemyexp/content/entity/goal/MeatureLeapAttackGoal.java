package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.infernalstudios.enemyexp.content.entity.MeatureEntity;

import java.util.EnumSet;

/**
 * Custom leap attack goal for MeatureEntity.
 * <p>
 * Timeline (total 30 ticks, matching the leap animation): <p>
 * Ticks  0 – 7  → Wind-up phase: entity stops moving, plays the start of the animation. <p>
 * Tick   8 → Launch: velocity is applied toward the target. <p>
 * Ticks  8 – 29 → In-air phase: the entity flies toward the target. <p>
 * Tick  30 → Landing: damage is dealt if close enough, goal resets <p>.
 */
public class MeatureLeapAttackGoal extends Goal {
    /** Horizontal distance (in blocks) at which the leap may trigger. */
    private static final double LEAP_TRIGGER_DISTANCE = 6.0D;
    /** How far below this distance the goal stops being usable (prevents spam at melee range). */
    private static final double MIN_TRIGGER_DISTANCE = 1.5D;
    /** Vertical component of the launch impulse. */
    private static final double LEAP_Y_IMPULSE = 0.5D;
    /** Horizontal multiplier on the normalized direction vector. */
    private static final double LEAP_XZ_IMPULSE = 0.5D;
    /** Melee reach used when checking for a landing hit (sq). */
    private static final double HIT_REACH_SQ = 1.5D;
    /** Ticks between two consecutive leaps. */
    private static final int COOLDOWN_TICKS = 60;

    // tick at which launch happens
    private static final int WINDUP_END_TICK = 8;
    // full animation length
    private static final int ANIM_TOTAL_TICKS = 30;

    private final MeatureEntity meature;
    private int tickCounter;   // counts from 0 up to ANIM_TOTAL_TICKS
    private int cooldown;
    private boolean launched;
    private boolean hitDealt;

    public MeatureLeapAttackGoal(MeatureEntity meature) {
        this.meature = meature;
        // We control movement ourselves during the leap, so flag accordingly.
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        tickCooldown();
        if (cooldown > 0) return false;

        LivingEntity target = meature.getTarget();
        if (target == null || !target.isAlive()) return false;

        // Only trigger inside the leap window, not at melee range.
        double distSq = meature.distanceToSqr(target);
        double minSq = MIN_TRIGGER_DISTANCE * MIN_TRIGGER_DISTANCE;
        double maxSq = LEAP_TRIGGER_DISTANCE * LEAP_TRIGGER_DISTANCE;

        return distSq >= minSq && distSq <= maxSq && meature.onGround();
    }

    @Override
    public boolean canContinueToUse() {
        // Keep running until the animation finishes.
        return tickCounter <= ANIM_TOTAL_TICKS;
    }

    @Override
    public void start() {
        tickCounter = 0;
        launched = false;
        hitDealt = false;

        // Tell the entity (and its renderer) that a leap is happening.
        meature.setLeapRule();
        // Trigger the leap animation
        meature.triggerAnim("leap", "leap");
    }

    @Override
    public void tick() {
        tickCounter++;

        LivingEntity target = meature.getTarget();
        if (target != null) meature.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Wind-up phase (ticks 0 – 7)
        if (tickCounter < WINDUP_END_TICK) {
            meature.getNavigation().stop();
            return;
        }

        // Launch (exactly at tick 8)
        if (!launched) {
            launched = true;
            if (target != null) applyLeapVelocity(target);
        }

        // landing phase (ticks 8 – 30)
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
        meature.setIdleRule();   // return control to normal movement goals
    }

    public void tickCooldown() {
        if (cooldown > 0) cooldown--;
    }

    private void applyLeapVelocity(LivingEntity target) {
        // Direction from meature → target, ignoring Y for the horizontal plane.
        Vec3 toTarget = target.position().subtract(meature.position());
        double horizLen = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);

        if (horizLen < 0.001D) return; // safety – same XZ position

        // Normalize horizontal, then scale by impulse constant.
        double nx = toTarget.x / horizLen * LEAP_XZ_IMPULSE;
        double nz = toTarget.z / horizLen * LEAP_XZ_IMPULSE;

        Vec3 current = meature.getDeltaMovement();
        meature.setDeltaMovement(
                current.x + nx,
                LEAP_Y_IMPULSE,
                current.z + nz
        );

        meature.hasImpulse = true;
    }
}