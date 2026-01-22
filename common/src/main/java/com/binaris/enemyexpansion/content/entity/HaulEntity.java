package com.binaris.enemyexpansion.content.entity;

import com.binaris.enemyexpansion.core.mixin.SpawnPlacementsAccessor;
import com.binaris.enemyexpansion.setup.EEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public class HaulEntity extends SprinterEntity {
    public HaulEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static void spawn() {
        SpawnPlacementsAccessor.callRegister(EEntities.HAUL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EEntities::checkHostileRules);
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
