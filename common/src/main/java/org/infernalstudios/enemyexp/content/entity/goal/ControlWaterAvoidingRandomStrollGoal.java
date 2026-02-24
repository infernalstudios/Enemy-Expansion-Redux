package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

import java.util.function.BooleanSupplier;

/**
 * A controllable extension of {@link WaterAvoidingRandomStrollGoal} that allows toggling
 * whether the goal should execute.
 * <p>
 * This goal wraps the vanilla water-avoiding random stroll behavior and adds the ability
 * to enable or disable it dynamically through a {@code BooleanSupplier} predicate.
 */
public class ControlWaterAvoidingRandomStrollGoal extends WaterAvoidingRandomStrollGoal {
    private final BooleanSupplier shouldExecutePredicate;

    public ControlWaterAvoidingRandomStrollGoal(PathfinderMob mob, double speedModifier, BooleanSupplier shouldExecutePredicate) {
        super(mob, speedModifier);
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
