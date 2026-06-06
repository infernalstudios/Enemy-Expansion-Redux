package org.infernalstudios.enemyexp.client.entity.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.enemyexp.client.entity.model.BiterModel;
import org.infernalstudios.enemyexp.content.entity.BiterEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

public class BiterRenderer extends GeoEntityRenderer<BiterEntity> {
    public BiterRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BiterModel());
        this.shadowRadius = 0.3f;

        addRenderLayer(new ItemArmorGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getArmorItemForBone(GeoBone bone, BiterEntity animatable) {
                return switch (bone.getName()) {
                    case "armorhead" -> this.armorStackForBone(animatable, EquipmentSlot.HEAD);
                    default -> null;
                };
            }

            @Override
            protected @NotNull EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, BiterEntity animatable) {
                return switch (bone.getName()) {
                    case "armorhead" -> EquipmentSlot.HEAD;
                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
                };
            }

            @Override
            protected @NotNull ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, BiterEntity animatable, HumanoidModel<?> baseModel) {
                return switch (bone.getName()) {
                    case "armorhead" -> baseModel.head;
                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                };
            }

            private ItemStack armorStackForBone(BiterEntity entity, EquipmentSlot slot) {
                return entity.getItemBySlot(slot);
            }
        });
    }

    @Override
    public RenderType getRenderType(BiterEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}