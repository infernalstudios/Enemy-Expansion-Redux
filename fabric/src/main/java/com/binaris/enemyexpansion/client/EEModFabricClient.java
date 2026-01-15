package com.binaris.enemyexpansion.client;

import com.binaris.enemyexpansion.setup.client.EERenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class EEModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EERenderers.registerRenderers();
        EERenderers.getRenderers().forEach((entity, renderer) -> EntityRendererRegistry.register(entity.get(), (EntityRendererProvider<Entity>) renderer));
    }
}
