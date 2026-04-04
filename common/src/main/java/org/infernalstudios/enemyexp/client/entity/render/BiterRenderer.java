package org.infernalstudios.enemyexp.client.entity.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.infernalstudios.enemyexp.client.entity.model.BiterModel;
import org.infernalstudios.enemyexp.content.entity.BiterEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BiterRenderer extends GeoEntityRenderer<BiterEntity> {
    public BiterRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BiterModel());
        this.shadowRadius = 0.3f;
    }
}