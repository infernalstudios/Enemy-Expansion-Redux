package org.infernalstudios.enemyexp.client.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.enemyexp.client.entity.model.VampireModel;
import org.infernalstudios.enemyexp.content.entity.VampireEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

public class VampireRenderer extends GeoEntityRenderer<VampireEntity> {
    public VampireRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VampireModel());
        this.shadowRadius = 0.5f;

        addRenderLayer(new ItemArmorGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getArmorItemForBone(GeoBone bone, VampireEntity animatable) {
                return switch (bone.getName()) {
                    case "armorhead" -> this.armorStackForBone(animatable, EquipmentSlot.HEAD);

                    case "armorbody", "armorrightarm", "armorleftarm" ->
                            this.armorStackForBone(animatable, EquipmentSlot.CHEST);

                    case "armorpelvis", "armorrightleg", "armorleftleg" ->
                            this.armorStackForBone(animatable, EquipmentSlot.LEGS);

                    case "armorrightfoot", "armorleftfoot" -> this.armorStackForBone(animatable, EquipmentSlot.FEET);

                    default -> null;
                };
            }

            @Override
            protected @NotNull EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, VampireEntity animatable) {
                return switch (bone.getName()) {
                    case "armorhead" -> EquipmentSlot.HEAD;
                    case "armorbody", "armorrightarm", "armorleftarm" -> EquipmentSlot.CHEST;
                    case "armorpelvis", "armorrightleg", "armorleftleg" -> EquipmentSlot.LEGS;
                    case "armorrightfoot", "armorleftfoot" -> EquipmentSlot.FEET;
                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
                };
            }

            @Override
            protected @NotNull ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, VampireEntity animatable, HumanoidModel<?> baseModel) {
                return switch (bone.getName()) {
                    case "armorhead" -> baseModel.head;
                    case "armorbody", "armorpelvis" -> baseModel.body;
                    case "armorrightarm" -> baseModel.rightArm;
                    case "armorleftarm" -> baseModel.leftArm;
                    case "armorrightleg", "armorrightfoot" -> baseModel.rightLeg;
                    case "armorleftleg", "armorleftfoot" -> baseModel.leftLeg;
                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                };
            }

            private ItemStack armorStackForBone(VampireEntity entity, EquipmentSlot slot) {
                return entity.getItemBySlot(slot);
            }
        });
    }

    @Override
    protected void applyRotations(VampireEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        int tempDeathTime = animatable.deathTime;
        animatable.deathTime = 0;
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);
        animatable.deathTime = tempDeathTime;
    }
}