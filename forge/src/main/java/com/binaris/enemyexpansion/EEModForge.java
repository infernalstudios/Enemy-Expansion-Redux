package com.binaris.enemyexpansion;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class EEModForge {
    
    public EEModForge() {
        Constants.LOG.info("Hello Forge world!");
        EEMod.init();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        if (FMLEnvironment.dist.isClient()) {
            modBus.addListener(EEModForgeClient::clientSetup);
        }
    }
}