package com.binaris.enemyexpansion.setup.client;

import com.binaris.enemyexpansion.client.renderer.SprinterRenderer;
import com.binaris.enemyexpansion.core.DeferredObject;
import com.binaris.enemyexpansion.setup.EEntities;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.Map;

public final class EERenderers {
    private static final Map<DeferredObject<EntityType<? extends Entity>>, EntityRendererProvider<?>> providers = Maps.newHashMap();


    public static void registerRenderers() {
        // Example registration:
        registerEntityRender(EEntities.SPRINTER, SprinterRenderer::new);
    }

    public static Map<DeferredObject<EntityType<? extends Entity>>, EntityRendererProvider<?>> getRenderers() {
        return providers;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void registerEntityRender(DeferredObject<EntityType<T>> entityType, EntityRendererProvider provider) {
        providers.put((DeferredObject<EntityType<? extends Entity>>) (Object) entityType, provider);
    }

    private EERenderers() {
    }
}
