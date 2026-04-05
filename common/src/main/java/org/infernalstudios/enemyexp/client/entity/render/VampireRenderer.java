package org.infernalstudios.enemyexp.client.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.infernalstudios.enemyexp.client.entity.model.VampireModel;
import org.infernalstudios.enemyexp.content.entity.VampireEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VampireRenderer extends GeoEntityRenderer<VampireEntity> {
    public VampireRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VampireModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    protected void applyRotations(VampireEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        if (animatable.deathTime > 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotationYaw));
        } else {
            super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);
        }
    }
}