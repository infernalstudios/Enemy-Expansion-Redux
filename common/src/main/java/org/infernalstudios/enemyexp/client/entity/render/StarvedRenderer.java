package org.infernalstudios.enemyexp.client.entity.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.client.entity.model.StarvedModel;
import org.infernalstudios.enemyexp.content.entity.StarvedEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StarvedRenderer extends GeoEntityRenderer<StarvedEntity> {
    public StarvedRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new StarvedModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(StarvedEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}