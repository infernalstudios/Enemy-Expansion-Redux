package org.infernalstudios.enemyexp;

import org.infernalstudios.enemyexp.setup.client.EERenderers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class EEModForgeClient {
    @SuppressWarnings("unchecked")
    public static void clientSetup(final FMLClientSetupEvent event) {
        EERenderers.registerRenderers();
        EERenderers.getRenderers().forEach((entity, renderer) ->
                EntityRenderers.register(entity.get(), (EntityRendererProvider<Entity>) renderer)
        );
    }
}
