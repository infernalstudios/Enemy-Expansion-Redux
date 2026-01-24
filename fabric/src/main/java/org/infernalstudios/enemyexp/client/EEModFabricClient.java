package org.infernalstudios.enemyexp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import org.infernalstudios.enemyexp.setup.client.EERenderers;

public class EEModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EERenderers.registerRenderers();
        EERenderers.getRenderers().forEach((entity, renderer) -> EntityRendererRegistry.register(entity.get(), (EntityRendererProvider<Entity>) renderer));
    }
}
