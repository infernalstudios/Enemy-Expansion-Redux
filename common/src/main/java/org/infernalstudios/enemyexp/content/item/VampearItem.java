package org.infernalstudios.enemyexp.content.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.infernalstudios.enemyexp.setup.EEMobEffects;
import org.jetbrains.annotations.NotNull;

public class VampearItem extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder()
            .nutrition(2).saturationMod(0.3F).meat()
            .effect(new MobEffectInstance(EEMobEffects.BLOOD_BOOST.get(), 200, 0), 1.0F).build();

    public VampearItem(Properties properties) {
        super(properties.food(FOOD));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entityLiving) {
        ItemStack result = super.finishUsingItem(stack, level, entityLiving);
        if (!level.isClientSide) {
            entityLiving.setAbsorptionAmount(Math.max(entityLiving.getAbsorptionAmount(), 2.0F));
        }
        return result;
    }
}