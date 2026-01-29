package org.infernalstudios.enemyexp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import org.infernalstudios.enemyexp.setup.EECreativeTabs;
import org.infernalstudios.enemyexp.setup.EEItems;
import org.infernalstudios.enemyexp.setup.EEntities;

public class EEModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        EEMod.init();

        EEntities.register(Registry::register);
        EEntities.registerAttributes(FabricDefaultAttributeRegistry::register);
        EEItems.register(Registry::register);

        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((itemGroup, entries) -> {
            if (itemGroup == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SPAWN_EGGS)) {
                EECreativeTabs.getSpawnEggs().forEach(item -> entries.accept(item.get()));
            }
        });
    }
}
