package org.infernalstudios.enemyexp.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.infernalstudios.enemyexp.Constants;
import org.infernalstudios.enemyexp.client.entity.model.armor.HeadBiterArmorModel;
import org.infernalstudios.enemyexp.client.entity.model.armor.HorseHeadArmorModel;
import org.infernalstudios.enemyexp.content.item.HorseHeadArmor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("all")
@Mixin(HorseHeadArmor.class)
public abstract class HorseHeadMixin extends ArmorItem {
    public HorseHeadMixin(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(), Map.of("head", (new HorseHeadArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(HorseHeadArmorModel.LAYER_LOCATION))).all, "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                armorModel.crouching = livingEntity.isShiftKeyDown();
                armorModel.riding = original.riding;
                armorModel.young = livingEntity.isBaby();
                armorModel.setAllVisible(equipmentSlot == EquipmentSlot.HEAD);
                return armorModel;
            }
        });
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return Constants.MOD_ID + ":textures/armor/horsehead.png";
    }
}