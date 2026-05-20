package org.infernalstudios.enemyexp.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.client.entity.model.armor.HeadBiterArmorModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HeadBiterRenderer implements ArmorRenderer {
    private static final Map<Item, HumanoidModel<?>> models = new HashMap<>();

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<LivingEntity> contextModel) {
        HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(), Map.of("head", (new HeadBiterArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(HeadBiterArmorModel.LAYER_LOCATION))).all2, "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
        armorModel.setAllVisible(slot == EquipmentSlot.HEAD);

        armorModel = (HumanoidModel) models.computeIfAbsent(stack.getItem(), (Item item) -> new HumanoidModel(new ModelPart(Collections.emptyList(), Map.of("head", (new HeadBiterArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(HeadBiterArmorModel.LAYER_LOCATION))).all2, "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap())))));

        contextModel.copyPropertiesTo(armorModel);

        ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, armorModel, EEMod.location("textures/armor/headbiter.png"));
    }
}
