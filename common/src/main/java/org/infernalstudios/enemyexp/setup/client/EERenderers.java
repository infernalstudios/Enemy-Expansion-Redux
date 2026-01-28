package org.infernalstudios.enemyexp.setup.client;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.infernalstudios.enemyexp.client.entity.render.HaulRenderer;
import org.infernalstudios.enemyexp.client.entity.render.SluggerRenderer;
import org.infernalstudios.enemyexp.client.entity.render.SprinterRenderer;
import org.infernalstudios.enemyexp.core.DeferredObject;
import org.infernalstudios.enemyexp.setup.EEntities;

import java.util.Map;

public final class EERenderers {
    private static final Map<DeferredObject<EntityType<? extends Entity>>, EntityRendererProvider<?>> providers = Maps.newHashMap();


    private EERenderers() {
    }

    public static void registerRenderers() {
        registerEntityRender(EEntities.SPRINTER, SprinterRenderer::new);
        registerEntityRender(EEntities.HAUL, HaulRenderer::new);
        registerEntityRender(EEntities.SLUGGER, SluggerRenderer::new);
    }

    public static Map<DeferredObject<EntityType<? extends Entity>>, EntityRendererProvider<?>> getRenderers() {
        return providers;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void registerEntityRender(DeferredObject<EntityType<T>> entityType, EntityRendererProvider provider) {
        providers.put((DeferredObject<EntityType<? extends Entity>>) (Object) entityType, provider);
    }
}
