package org.infernalstudios.enemyexp.client.entity.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.infernalstudios.enemyexp.client.entity.model.VampireModel;
import org.infernalstudios.enemyexp.content.entity.VampireEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VampireRenderer extends GeoEntityRenderer<VampireEntity> {
    public VampireRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VampireModel());
        this.shadowRadius = 0.5f;
    }
}