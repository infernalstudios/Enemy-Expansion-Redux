package org.infernalstudios.enemyexp.client.entity.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.client.entity.model.FrigidModel;
import org.infernalstudios.enemyexp.content.entity.FrigidEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FrigidRenderer extends GeoEntityRenderer<FrigidEntity> {
    public FrigidRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FrigidModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(FrigidEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
