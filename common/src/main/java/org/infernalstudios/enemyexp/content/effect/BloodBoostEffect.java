package org.infernalstudios.enemyexp.content.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.infernalstudios.enemyexp.EEMod;

public class BloodBoostEffect extends MobEffect {
    public BloodBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8A0303);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;

        MobEffectInstance instance = entity.getEffect(this);
        if (instance != null && instance.getDuration() <= 1) {
            if (amplifier > 0) {
                EEMod.scheduleTask((ServerLevel) entity.level(), 1, () -> {
                    entity.addEffect(new MobEffectInstance(this, 200, amplifier - 1));
                });
            }
        }
    }
}