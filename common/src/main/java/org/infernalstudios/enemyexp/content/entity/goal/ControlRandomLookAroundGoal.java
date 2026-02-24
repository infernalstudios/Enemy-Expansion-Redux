package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;

import java.util.function.BooleanSupplier;

/**
 * A controllable extension of {@link RandomLookAroundGoal} that allows toggling
 * whether the goal should execute.
 * <p>
 * This goal wraps the vanilla random look around behavior and adds the ability
 * to enable or disable it dynamically through a {@code BooleanSupplier} predicate.
 */
public class ControlRandomLookAroundGoal extends RandomLookAroundGoal {
    private final BooleanSupplier shouldExecutePredicate;

    public ControlRandomLookAroundGoal(Mob mob, BooleanSupplier shouldExecutePredicate) {
        super(mob);
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
