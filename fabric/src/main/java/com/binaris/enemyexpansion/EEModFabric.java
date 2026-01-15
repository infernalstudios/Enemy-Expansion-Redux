package com.binaris.enemyexpansion;

import com.binaris.enemyexpansion.setup.EEItems;
import com.binaris.enemyexpansion.setup.EEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;

public class EEModFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world!");
        EEMod.init();

        EEntities.register(Registry::register);
        EEntities.registerAttributes(FabricDefaultAttributeRegistry::register);
        EEItems.register(Registry::register);
    }
}
