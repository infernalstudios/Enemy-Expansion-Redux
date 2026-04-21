package org.infernalstudios.enemyexp.client.entity.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.client.entity.model.GoblinThiefModel;
import org.infernalstudios.enemyexp.content.entity.GoblinThiefEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GoblinThiefRenderer extends GeoEntityRenderer<GoblinThiefEntity> {
    public GoblinThiefRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GoblinThiefModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(GoblinThiefEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
