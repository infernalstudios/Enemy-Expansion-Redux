package org.infernalstudios.enemyexp.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HorseHeadArmor extends ArmorItem {
    public HorseHeadArmor() {
        super(ArmorMaterials.IRON, Type.HELMET, new Properties());
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof LivingEntity living && !(living instanceof Player)) {
            MobEffectInstance effect = living.getEffect(MobEffects.MOVEMENT_SPEED);
            if (effect == null || effect.getDuration() <= 20) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, false));
            }
        }
    }

    public static void onKillEntity(Player player) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) return;
        if (!(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HorseHeadArmor)) return;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip.enemyexp.horsehead_armor").withStyle(ChatFormatting.GRAY));
    }
}