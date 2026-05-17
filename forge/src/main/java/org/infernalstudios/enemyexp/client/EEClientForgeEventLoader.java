package org.infernalstudios.enemyexp.client;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.infernalstudios.enemyexp.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.infernalstudios.enemyexp.setup.client.EERenderers;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EEClientForgeEventLoader {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        EERenderers.registerEntityLayers(event::registerLayerDefinition);
    }
}
