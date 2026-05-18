package org.infernalstudios.enemyexp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import org.infernalstudios.enemyexp.setup.EECreativeTabs;
import org.infernalstudios.enemyexp.setup.EEItems;
import org.infernalstudios.enemyexp.setup.EEMobEffects;
import org.infernalstudios.enemyexp.setup.EEntities;

public class EEModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        EEMod.init();

        EEntities.register(Registry::register);
        EEntities.registerAttributes(FabricDefaultAttributeRegistry::register);
        EEItems.register(Registry::register);
        EEMobEffects.register(Registry::register);

        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((itemGroup, entries) -> {
            if (itemGroup == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SPAWN_EGGS)) {
                EECreativeTabs.getSpawnEggs().forEach(item -> entries.accept(item.get()));
            }
        });

        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((itemGroup, entries) -> {
            if (itemGroup == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.COMBAT)) {
                EECreativeTabs.getEquipment().forEach(item -> entries.accept(item.get()));
            }
        });

        BiomeModifications.addSpawn(BiomeSelectors.tag(ConventionalBiomeTags.PLAINS)
                        .or(BiomeSelectors.tag(ConventionalBiomeTags.DESERT)),
                MobCategory.MONSTER, EEntities.EQUESTRIAN.get(), 25, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.tag(ConventionalBiomeTags.FOREST)
                        .or(BiomeSelectors.tag(ConventionalBiomeTags.CAVES).or(BiomeSelectors.tag(ConventionalBiomeTags.JUNGLE))),
                MobCategory.MONSTER, EEntities.GOBLIN_THIEF.get(), 25, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.tag(ConventionalBiomeTags.CAVES),
                MobCategory.MONSTER, EEntities.VAMPIRE.get(), 25, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.tag(ConventionalBiomeTags.SNOWY),
                MobCategory.MONSTER, EEntities.FRIGID.get(), 50, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                MobCategory.MONSTER, EEntities.MEATURE.get(), 50, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                MobCategory.MONSTER, EEntities.SPRINTER.get(), 50, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                MobCategory.MONSTER, EEntities.HAUL.get(), 50, 1, 1);

        BiomeModifications.addSpawn(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                MobCategory.MONSTER, EEntities.SLUGGER.get(), 50, 1, 1);
    }
}
