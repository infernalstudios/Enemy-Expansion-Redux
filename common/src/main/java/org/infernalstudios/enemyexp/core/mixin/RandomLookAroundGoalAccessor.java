package org.infernalstudios.enemyexp.core.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RandomLookAroundGoal.class)
public interface RandomLookAroundGoalAccessor {
    @Accessor
    Mob getMob();
}
