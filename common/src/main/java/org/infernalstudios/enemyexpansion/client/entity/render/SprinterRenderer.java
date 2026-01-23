package org.infernalstudios.enemyexpansion.client.entity.render;

import org.infernalstudios.enemyexpansion.client.entity.model.SprinterModel;
import org.infernalstudios.enemyexpansion.content.entity.SprinterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SprinterRenderer extends GeoEntityRenderer<SprinterEntity> {
    public SprinterRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SprinterModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(SprinterEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
