package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;

import java.util.function.BooleanSupplier;

public class ControlPanicGoal extends PanicGoal {
    private final BooleanSupplier shouldExecutePredicate;

    public ControlPanicGoal(PathfinderMob mob, double speedModifier, BooleanSupplier supplier) {
        super(mob, speedModifier);
        this.shouldExecutePredicate = supplier;
    }

    @Override
    protected boolean shouldPanic() {
        return shouldExecutePredicate.getAsBoolean();
    }
}
