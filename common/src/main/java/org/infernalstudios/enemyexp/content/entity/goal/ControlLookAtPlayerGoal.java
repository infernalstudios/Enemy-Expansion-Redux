package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

import java.util.function.BooleanSupplier;

/**
 * A controllable extension of {@link LookAtPlayerGoal} that allows toggling
 * whether the goal should execute.
 * <p>
 * This goal wraps the vanilla look at player behavior and adds the ability
 * to enable or disable it dynamically through a {@code BooleanSupplier} predicate.
 */
public class ControlLookAtPlayerGoal extends LookAtPlayerGoal {
    private final BooleanSupplier shouldExecutePredicate;

    public ControlLookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance, BooleanSupplier shouldExecutePredicate) {
        super(mob, lookAtType, lookDistance);
        this.shouldExecutePredicate = shouldExecutePredicate;
    }

    public ControlLookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance, float probability, BooleanSupplier shouldExecutePredicate) {
        super(mob, lookAtType, lookDistance, probability);
        this.shouldExecutePredicate = shouldExecutePredicate;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && shouldExecutePredicate.getAsBoolean();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && shouldExecutePredicate.getAsBoolean();
    }
}
