package org.infernalstudios.enemyexp.client.entity.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.infernalstudios.enemyexp.Constants;

public class HorseHeadArmorModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "horsehead"), "main");
    public final ModelPart all;

    public HorseHeadArmorModel(ModelPart root) {
        all = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition all = partdefinition.addOrReplaceChild("all", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition reignsleft_r1 = all.addOrReplaceChild("reignsleft_r1", CubeListBuilder.create().texOffs(46, 24).mirror().addBox(0.0F, 0.1F, -8.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.1F)).mirror(false)
                .texOffs(46, 24).addBox(7.0F, 0.1F, -8.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-4.0F, -4.0F, -1.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition horsehead = all.addOrReplaceChild("horsehead", CubeListBuilder.create().texOffs(0, 8).addBox(-4.5F, -8.0F, -7.0F, 9.0F, 8.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(41, 17).addBox(-1.49F, -8.0F, 4.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.01F))
                .texOffs(0, 28).addBox(-3.0F, -7.0F, -13.4F, 6.0F, 7.0F, 7.0F, new CubeDeformation(0.01F))
                .texOffs(26, 28).addBox(-3.0F, -7.0F, -13.4F, 6.0F, 7.0F, 7.0F, new CubeDeformation(0.11F)), PartPose.offsetAndRotation(0.0F, -5.75F, 1.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition earleft = horsehead.addOrReplaceChild("earleft", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-1.5F, -4.0F, 0.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, -8.0F, 2.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition earright = horsehead.addOrReplaceChild("earright", CubeListBuilder.create().texOffs(0, 12).addBox(-1.5F, -4.0F, 0.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -8.0F, 2.0F, 0.0F, 0.0F, -0.1745F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T t, float v, float v1, float v2, float v3, float v4) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, float v, float v1, float v2, float v3) {
        all.render(poseStack, vertexConsumer, i, i1, v, v1, v2, v3);
    }
}
