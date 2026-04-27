package org.infernalstudios.enemyexp.content.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;

import java.util.function.BooleanSupplier;

public class ControlAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
    BooleanSupplier shouldExecute;

    public ControlAvoidEntityGoal(PathfinderMob mob, Class entityClassToAvoid, float maxDistance, double walkSpeedModifier, double sprintSpeedModifier, BooleanSupplier shouldExecute) {
        super(mob, entityClassToAvoid, maxDistance, walkSpeedModifier, sprintSpeedModifier);
        this.shouldExecute = shouldExecute;
    }

    @Override
    public boolean canUse() {
        return shouldExecute.getAsBoolean() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && shouldExecute.getAsBoolean();
    }
}
