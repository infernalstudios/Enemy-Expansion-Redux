package com.binaris.enemyexpansion.client.renderer;

import com.binaris.enemyexpansion.client.model.HaulModel;
import com.binaris.enemyexpansion.client.model.SprinterModel;
import com.binaris.enemyexpansion.content.entity.HaulEntity;
import com.binaris.enemyexpansion.content.entity.SprinterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
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
