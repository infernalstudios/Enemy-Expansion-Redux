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
import org.infernalstudios.enemyexp.client.entity.model.MeatureModel;
import org.infernalstudios.enemyexp.content.entity.MeatureEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

public class MeatureRenderer extends GeoEntityRenderer<MeatureEntity> {
    public MeatureRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MeatureModel());
        this.shadowRadius = 0.5f;

        addRenderLayer(new ItemArmorGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getArmorItemForBone(GeoBone bone, MeatureEntity animatable) {
                return switch (bone.getName()) {
                    case "armorhead" -> this.armorStackForBone(animatable, EquipmentSlot.HEAD);
                    default -> null;
                };
            }

            @Override
            protected @NotNull EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, MeatureEntity animatable) {
                return switch (bone.getName()) {
                    case "armorhead" -> EquipmentSlot.HEAD;
                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
                };
            }

            @Override
            protected @NotNull ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, MeatureEntity animatable, HumanoidModel<?> baseModel) {
                return switch (bone.getName()) {
                    case "armorhead" -> baseModel.head;
                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                };
            }

            private ItemStack armorStackForBone(MeatureEntity entity, EquipmentSlot slot) {
                return entity.getItemBySlot(slot);
            }
        });
    }

    @Override
    public RenderType getRenderType(MeatureEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, MeatureEntity animatable, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        int age = animatable.getAge();
        float scale = 1.0f + (age * 0.05f); // Each age increases the size by 5%
        super.scaleModelForRender(scale, scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
