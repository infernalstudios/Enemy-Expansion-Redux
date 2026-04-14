package org.infernalstudios.enemyexp.client.entity.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.client.entity.model.EquestrianModel;
import org.infernalstudios.enemyexp.content.entity.EquestrianEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EquestrianRenderer extends GeoEntityRenderer<EquestrianEntity> {
    public EquestrianRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EquestrianModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(EquestrianEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
