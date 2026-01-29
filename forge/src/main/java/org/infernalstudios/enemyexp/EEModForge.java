package org.infernalstudios.enemyexp;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class EEModForge {
    public EEModForge(FMLJavaModLoadingContext context) {
        MinecraftForge.EVENT_BUS.register(this);
        EEMod.init();

        if (FMLEnvironment.dist.isClient()) {
            context.getModEventBus().addListener(EEModForgeClient::clientSetup);
        }
    }
}