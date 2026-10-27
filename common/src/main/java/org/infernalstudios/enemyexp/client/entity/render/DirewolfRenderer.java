package org.infernalstudios.enemyexp.client.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.enemyexp.client.entity.model.DirewolfModel;
import org.infernalstudios.enemyexp.content.entity.DirewolfEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

public class DirewolfRenderer extends GeoEntityRenderer<DirewolfEntity> {
    private static final float MODEL_SCALE = 1.5F;

    public DirewolfRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DirewolfModel());
        this.shadowRadius = 0.5f;

        addRenderLayer(new ItemArmorGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getArmorItemForBone(GeoBone bone, DirewolfEntity animatable) {
                return switch (bone.getName()) {
                    case "armorhead" -> this.armorStackForBone(animatable, EquipmentSlot.HEAD);
                    case "armorbody" -> this.armorStackForBone(animatable, EquipmentSlot.CHEST);
                    case "armorleftlegfront", "armorrightlegfront", "armorleftlegback", "armorrightlegback" ->
                            this.armorStackForBone(animatable, EquipmentSlot.LEGS);
                    case "armorleftfootfront", "armorfootfront", "armorleftfootback", "armorrightfootback" ->
                            this.armorStackForBone(animatable, EquipmentSlot.FEET);
                    default -> null;
                };
            }

            @Override
            protected @NotNull EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, DirewolfEntity animatable) {
                return switch (bone.getName()) {
                    case "armorhead" -> EquipmentSlot.HEAD;
                    case "armorbody" -> EquipmentSlot.CHEST;
                    case "armorleftlegfront", "armorrightlegfront", "armorleftlegback", "armorrightlegback" ->
                            EquipmentSlot.LEGS;
                    case "armorleftfootfront", "armorfootfront", "armorleftfootback", "armorrightfootback" ->
                            EquipmentSlot.FEET;
                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
                };
            }

            @Override
            protected @NotNull ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, DirewolfEntity animatable, HumanoidModel<?> baseModel) {
                return switch (bone.getName()) {
                    case "armorhead" -> baseModel.head;
                    case "armorbody" -> baseModel.body;
                    case "armorleftlegfront", "armorleftlegback", "armorleftfootfront", "armorleftfootback" ->
                            baseModel.leftLeg;
                    case "armorrightlegfront", "armorrightlegback", "armorfootfront", "armorrightfootback" ->
                            baseModel.rightLeg;
                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                };
            }

            private ItemStack armorStackForBone(DirewolfEntity entity, EquipmentSlot slot) {
                return entity.getItemBySlot(slot);
            }
        });
    }

    @Override
    public RenderType getRenderType(DirewolfEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, DirewolfEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        super.scaleModelForRender(MODEL_SCALE, MODEL_SCALE, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}