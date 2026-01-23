package org.infernalstudios.enemyexpansion.client.entity.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexpansion.client.entity.model.HaulModel;
import org.infernalstudios.enemyexpansion.content.entity.HaulEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HaulRenderer extends GeoEntityRenderer<HaulEntity> {
    public HaulRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HaulModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(HaulEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
