package org.infernalstudios.enemyexp.setup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.FrigidEntity;
import org.infernalstudios.enemyexp.content.entity.HaulEntity;
import org.infernalstudios.enemyexp.content.entity.SluggerEntity;
import org.infernalstudios.enemyexp.content.entity.SprinterEntity;
import org.infernalstudios.enemyexp.core.DeferredObject;
import org.infernalstudios.enemyexp.core.RegisterFunction;
import org.infernalstudios.enemyexp.core.mixin.SpawnPlacementsAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class EEntities {
    static Map<String, DeferredObject<EntityType<? extends Entity>>> entityTypes = new HashMap<>();

    public static final DeferredObject<EntityType<SprinterEntity>> SPRINTER = entity(SprinterEntity::new, "sprinter", MobCategory.MONSTER, 0.6F, 1.95F);
    public static final DeferredObject<EntityType<HaulEntity>> HAUL = entity(HaulEntity::new, "haul", MobCategory.MONSTER, 0.6F, 1.95F);
    public static final DeferredObject<EntityType<SluggerEntity>> SLUGGER = entity(SluggerEntity::new, "slugger", MobCategory.MONSTER, 0.6F, 2.3F);
    public static final DeferredObject<EntityType<FrigidEntity>> FRIGID = entity(FrigidEntity::new, "frigid", MobCategory.MONSTER, 0.6F, 1F);

    private EEntities() {
    }

    // Registers
    public static void register(RegisterFunction<EntityType<?>> function) {
        entityTypes.forEach(((id, entityType) -> function.register(BuiltInRegistries.ENTITY_TYPE, EEMod.location(id), entityType.get())));
        EEntities.registerSpawns();
    }

    public static void registerSpawns() {
        SpawnPlacementsAccessor.callRegister(EEntities.SPRINTER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EEntities::checkHostileRules);
        SpawnPlacementsAccessor.callRegister(EEntities.HAUL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EEntities::checkHostileRules);
        SpawnPlacementsAccessor.callRegister(EEntities.SLUGGER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EEntities::checkHostileRules);
        SpawnPlacementsAccessor.callRegister(EEntities.FRIGID.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EEntities::checkHostileRules);
    }

    public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> consumer) {
        consumer.accept(SPRINTER.get(), SprinterEntity.createAttributes().build());
        consumer.accept(HAUL.get(), SprinterEntity.createAttributes().build());
        consumer.accept(SLUGGER.get(), SluggerEntity.createAttributes().build());
        consumer.accept(FRIGID.get(), FrigidEntity.createAttributes().build());
    }

    public static Map<String, DeferredObject<EntityType<? extends Entity>>> getEntityTypes() {
        return entityTypes;
    }

    static <T extends Entity> DeferredObject<EntityType<T>> entity(EntityType.EntityFactory<T> factory, String name, MobCategory category, float width, float height) {
        return entity(name, EntityType.Builder.of(factory, category).sized(width, height).clientTrackingRange(64).updateInterval(3));
    }

    @SuppressWarnings("unchecked")
    static <T extends Entity> DeferredObject<EntityType<T>> entity(String name, EntityType.Builder<T> builder) {
        DeferredObject<EntityType<T>> ret = new DeferredObject<>(() -> builder.build(EEMod.location(name).toString()));
        entityTypes.put(name, (DeferredObject<EntityType<? extends Entity>>) (Object) ret);
        return ret;
    }

    /**
     * Common hostile mob spawn rules check used by most hostile mobs.
     */
    public static boolean checkHostileRules(EntityType<?> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(level, pos, random) && Mob.checkMobSpawnRules((EntityType<? extends Mob>) type, level, spawnType, pos, random);
    }
}
