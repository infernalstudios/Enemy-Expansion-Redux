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

public class MeatHeadArmorModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "meathead"), "main");
    public final ModelPart head;

    public MeatHeadArmorModel(ModelPart root) {
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.6F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightArm_r1 = head.addOrReplaceChild("rightArm_r1", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.0F, -2.0F, -0.7854F, 0.0F, 0.7854F));

        PartDefinition leftArm_r1 = head.addOrReplaceChild("leftArm_r1", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.0F, -1.0F, -2.0F, -0.7854F, 0.0F, -0.7854F));

        PartDefinition rightLeg_r1 = head.addOrReplaceChild("rightLeg_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -1.0F, 2.0F, 0.7854F, 0.0F, 0.7854F));

        PartDefinition leftLeg_r1 = head.addOrReplaceChild("leftLeg_r1", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -1.0F, 2.0F, 0.7854F, 0.0F, -0.7854F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(T t, float v, float v1, float v2, float v3, float v4) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, float v, float v1, float v2, float v3) {
        head.render(poseStack, vertexConsumer, i, i1, v, v1, v2, v3);
    }
}
