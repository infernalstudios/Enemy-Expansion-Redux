package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

import java.util.function.BooleanSupplier;

/**
 * A controllable extension of {@link MeleeAttackGoal} that allows toggling
 * whether the goal should execute.
 * <p>
 * This goal wraps the vanilla melee attack behavior and adds the ability
 * to enable or disable it dynamically through a {@code BooleanSupplier} predicate.
 */
public class ControlAttackGoal extends MeleeAttackGoal {
    private final BooleanSupplier shouldExecutePredicate;

    public ControlAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen, BooleanSupplier shouldExecutePredicate) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.shouldExecutePredicate = shouldExecutePredicate;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && shouldExecutePredicate.getAsBoolean();
    }
}
