package org.infernalstudios.enemyexp.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeadBiterArmor extends ArmorItem {
    public HeadBiterArmor() {
        super(ArmorMaterials.IRON, Type.HELMET, new Properties());
    }

    public static void onKillEntity(Player player) {
        if (player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) return;
        if (!(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HeadBiterArmor)) return;
        player.heal(2.0F);
    }

    public static void onHurtEntity(LivingEntity sourceEntity, DamageSource source, LivingEntity affected) {
        if (sourceEntity instanceof Player) return;
        if (sourceEntity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) return;
        if (!(sourceEntity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HeadBiterArmor)) return;
        sourceEntity.heal(4.0F);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("tooltip.enemyexp.headbiter_armor").withStyle(ChatFormatting.GRAY));
    }
}