package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import org.infernalstudios.enemyexp.core.mixin.SpawnPlacementsAccessor;
import org.infernalstudios.enemyexp.setup.EEntities;
import org.jetbrains.annotations.NotNull;

public class HaulEntity extends SprinterEntity {
    public HaulEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static void spawn() {
        SpawnPlacementsAccessor.callRegister(EEntities.HAUL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EEntities::checkHostileRules);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        boolean flag = super.doHurtTarget(entity);
        if (flag && this.getMainHandItem().isEmpty() && entity instanceof LivingEntity) {
            float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.HUNGER, 140 * (int)f), this);
        }

        return flag;
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected String getNormalTexture() {
        return "haul";
    }

    @Override
    protected String getStaggeredTexture() {
        return "haul_staggered";
    }
}
