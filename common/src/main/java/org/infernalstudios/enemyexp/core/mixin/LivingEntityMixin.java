package org.infernalstudios.enemyexp.core.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.infernalstudios.enemyexp.content.item.HeadBiterArmor;
import org.infernalstudios.enemyexp.content.item.HorseHeadArmor;
import org.infernalstudios.enemyexp.content.item.MeatheadArmor;
import org.infernalstudios.enemyexp.setup.EEMobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    LivingEntity self = (LivingEntity) (Object) this;

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/WalkAnimationState;setSpeed(F)V"))
    public void enemyExpansion$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity killer = source.getEntity();
        if (!(killer instanceof LivingEntity livingKiller)) return;

        HeadBiterArmor.onHurtEntity(livingKiller, source, self);
    }

    @Inject(method = "die", at = @At("HEAD"))
    public void enemyExpansion$onKilled(DamageSource damageSource, CallbackInfo ci) {
        Entity killer = damageSource.getEntity();
        if (!(killer instanceof LivingEntity livingKiller)) return;

        if (killer instanceof Player playerKiller) {
            MeatheadArmor.onKillEntity(playerKiller);
            HorseHeadArmor.onKillEntity(playerKiller);
            HeadBiterArmor.onKillEntity(playerKiller);
        }

        if (!livingKiller.level().isClientSide) {
            MobEffectInstance bloodBoost = livingKiller.getEffect(EEMobEffects.BLOOD_BOOST.get());
            if (bloodBoost == null) return;
            int currentAmp = bloodBoost.getAmplifier();
            int newAmp = Math.min(currentAmp + 1, 9);

            livingKiller.addEffect(new MobEffectInstance(EEMobEffects.BLOOD_BOOST.get(), 200, newAmp));

            float targetAbsorption = (newAmp + 1) * 2.0F;
            livingKiller.setAbsorptionAmount(Math.max(livingKiller.getAbsorptionAmount(), targetAbsorption));
        }
    }
}