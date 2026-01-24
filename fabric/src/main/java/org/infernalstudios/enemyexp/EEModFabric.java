package org.infernalstudios.enemyexp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import org.infernalstudios.enemyexp.setup.EEItems;
import org.infernalstudios.enemyexp.setup.EEntities;

public class EEModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        EEMod.init();

        EEntities.register(Registry::register);
        EEntities.registerAttributes(FabricDefaultAttributeRegistry::register);
        EEItems.register(Registry::register);
    }
}
