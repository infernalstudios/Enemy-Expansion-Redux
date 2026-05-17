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

public class HeadBiterArmorModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Constants.MOD_ID, "headbiter"), "main");
    public final ModelPart all2;

    public HeadBiterArmorModel(ModelPart root) {
        all2 = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition all2 = partdefinition.addOrReplaceChild("all2", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition botharms = all2.addOrReplaceChild("botharms", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.1689F, 1.7947F, 0.3927F, 0.0F, 0.0F));

        PartDefinition leftarm2 = botharms.addOrReplaceChild("leftarm2", CubeListBuilder.create().texOffs(33, 39).addBox(-1.0F, -0.8311F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(41, 39).addBox(-1.0F, -0.8311F, 1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -0.5F, 0.0F, -1.5708F, 0.0F, 0.0873F));

        PartDefinition rightarm2 = botharms.addOrReplaceChild("rightarm2", CubeListBuilder.create().texOffs(33, 39).mirror().addBox(-1.0F, -0.8311F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(41, 39).mirror().addBox(-1.0F, -0.8311F, 1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.0F, -0.5F, 0.0F, -1.5708F, 0.0F, -0.0873F));

        PartDefinition head2 = all2.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(0.0F, -5.0866F, 4.4078F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bothlegs = head2.addOrReplaceChild("bothlegs", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition leftleg2 = bothlegs.addOrReplaceChild("leftleg2", CubeListBuilder.create(), PartPose.offset(2.0F, 0.0F, 0.0F));

        PartDefinition leftleg_r1 = leftleg2.addOrReplaceChild("leftleg_r1", CubeListBuilder.create().texOffs(33, 33).mirror().addBox(-1.0F, 0.0F, -1.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition rightleg2 = bothlegs.addOrReplaceChild("rightleg2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightleg_r1 = rightleg2.addOrReplaceChild("rightleg_r1", CubeListBuilder.create().texOffs(33, 33).addBox(-3.0F, 0.0F, -1.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition rightear = head2.addOrReplaceChild("rightear", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.0F, -4.0F, -2.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition rightwing = rightear.addOrReplaceChild("rightwing", CubeListBuilder.create().texOffs(1, 32).mirror().addBox(0.0F, -5.0F, 0.0F, 0.0F, 13.0F, 19.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5672F, 0.0F));

        PartDefinition leftear = head2.addOrReplaceChild("leftear", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, -4.0F, -2.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition leftwing = leftear.addOrReplaceChild("leftwing", CubeListBuilder.create().texOffs(1, 32).addBox(0.0F, -5.0F, 0.0F, 0.0F, 13.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5672F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        all2.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
