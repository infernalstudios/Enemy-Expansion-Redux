package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FrigidEntity extends Zombie implements GeoEntity {
    private static final int MAX_FREEZE_TIME_TICKS = 300;
    private static final int FREEZE_TIME_TICKS = 50;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FrigidEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        boolean flag = super.doHurtTarget(entity);
        if (flag && entity instanceof LivingEntity livingEntity) {
            float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            if (!livingEntity.canFreeze() || livingEntity.getTicksFrozen() >= MAX_FREEZE_TIME_TICKS) return flag;
            livingEntity.setTicksFrozen((int) Math.min(livingEntity.getTicksFrozen() + (FREEZE_TIME_TICKS * f), MAX_FREEZE_TIME_TICKS));
        }

        return flag;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        if (!AnimUtils.isNotMoving(event) && this.getTarget() != null) {
            return event.setAndContinue(EEAnimations.SPRINT);
        }
        return AnimUtils.idleWalkAnimation(event);
    }

    @Override
    protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions size) {
        return 0.8F;
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean childZombie) {
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
