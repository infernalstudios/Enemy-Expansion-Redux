package org.infernalstudios.enemyexp.core.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.infernalstudios.enemyexp.setup.EEMobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "die", at = @At("HEAD"))
    public void enemyExpansion$onKilled(DamageSource damageSource, CallbackInfo ci) {
        Entity killer = damageSource.getEntity();
        if (killer instanceof LivingEntity livingKiller && !livingKiller.level().isClientSide()) {
            MobEffectInstance bloodBoost = livingKiller.getEffect(EEMobEffects.BLOOD_BOOST.get());
            if (bloodBoost != null) {
                int currentAmp = bloodBoost.getAmplifier();
                int newAmp = Math.min(currentAmp + 1, 9);

                livingKiller.addEffect(new MobEffectInstance(EEMobEffects.BLOOD_BOOST.get(), 200, newAmp));

                float targetAbsorption = (newAmp + 1) * 2.0F;
                livingKiller.setAbsorptionAmount(Math.max(livingKiller.getAbsorptionAmount(), targetAbsorption));
            }
        }
    }
}