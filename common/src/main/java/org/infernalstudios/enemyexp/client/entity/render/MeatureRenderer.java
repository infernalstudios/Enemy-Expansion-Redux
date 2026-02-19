package org.infernalstudios.enemyexp.client.entity.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.client.entity.model.MeatureModel;
import org.infernalstudios.enemyexp.content.entity.MeatureEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MeatureRenderer extends GeoEntityRenderer<MeatureEntity> {
    public MeatureRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MeatureModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(MeatureEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
