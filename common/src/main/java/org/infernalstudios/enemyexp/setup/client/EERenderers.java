package org.infernalstudios.enemyexp.setup.client;

import com.google.common.collect.Maps;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.infernalstudios.enemyexp.client.entity.model.armor.HeadBiterArmorModel;
import org.infernalstudios.enemyexp.client.entity.model.armor.HorseHeadArmorModel;
import org.infernalstudios.enemyexp.client.entity.model.armor.MeatHeadArmorModel;
import org.infernalstudios.enemyexp.client.entity.render.*;
import org.infernalstudios.enemyexp.core.DeferredObject;
import org.infernalstudios.enemyexp.setup.EEntities;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class EERenderers {
    private static final Map<DeferredObject<EntityType<? extends Entity>>, EntityRendererProvider<?>> providers = Maps.newHashMap();


    private EERenderers() {
    }

    public static void registerEntityLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> consumer) {
        consumer.accept(HeadBiterArmorModel.LAYER_LOCATION, HeadBiterArmorModel::createBodyLayer);
        consumer.accept(HorseHeadArmorModel.LAYER_LOCATION, HorseHeadArmorModel::createBodyLayer);
        consumer.accept(MeatHeadArmorModel.LAYER_LOCATION, MeatHeadArmorModel::createBodyLayer);
    }

    public static void registerRenderers() {
        registerEntityRender(EEntities.SPRINTER, SprinterRenderer::new);
        registerEntityRender(EEntities.HAUL, HaulRenderer::new);
        registerEntityRender(EEntities.SLUGGER, SluggerRenderer::new);
        registerEntityRender(EEntities.FRIGID, FrigidRenderer::new);
        registerEntityRender(EEntities.MEATURE, MeatureRenderer::new);
        registerEntityRender(EEntities.VAMPIRE, VampireRenderer::new);
        registerEntityRender(EEntities.BITER, BiterRenderer::new);
        registerEntityRender(EEntities.EQUESTRIAN, EquestrianRenderer::new);
        registerEntityRender(EEntities.GOBLIN_THIEF, GoblinThiefRenderer::new);
    }

    public static Map<DeferredObject<EntityType<? extends Entity>>, EntityRendererProvider<?>> getRenderers() {
        return providers;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> void registerEntityRender(DeferredObject<EntityType<T>> entityType, EntityRendererProvider provider) {
        providers.put((DeferredObject<EntityType<? extends Entity>>) (Object) entityType, provider);
    }
}
