package org.infernalstudios.enemyexp.content.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.npc.AbstractVillager;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.BiterEntity;
import org.infernalstudios.enemyexp.setup.EEntities;

public class BittenEffect extends MobEffect {
    public BittenEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A0303);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;

        if (entity.level().isDay() && entity.level().canSeeSky(entity.blockPosition())) {
            if (!entity.hasEffect(MobEffects.POISON)) {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            }
        }

        MobEffectInstance instance = entity.getEffect(this);
        if (instance != null && instance.getDuration() <= 1) {
            if (entity instanceof AbstractVillager || entity instanceof AbstractIllager) {
                EEMod.scheduleTask((ServerLevel) entity.level(), 1, () -> transformIntoBiter(entity));
            }
        }
    }

    private void transformIntoBiter(LivingEntity entity) {
        if (entity.isRemoved()) return;

        BiterEntity biter = EEntities.BITER.get().create(entity.level());
        if (biter != null) {
            biter.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
            biter.setHealth(biter.getMaxHealth());
            entity.level().addFreshEntity(biter);
            entity.discard();
        }
    }
}