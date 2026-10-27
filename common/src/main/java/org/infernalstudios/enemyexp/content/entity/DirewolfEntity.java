package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

public class DirewolfEntity extends Monster implements GeoEntity {
    private static final int LEAP_COOLDOWN_TICKS = 80;
    private static final int LEAP_BUFF_TICKS = 50;
    private static final double LEAP_MIN_HORIZONTAL = 1.25D;
    private static final double LEAP_MAX_HORIZONTAL = 2.0D;
    private static final double LEAP_MIN_VERTICAL = 0.3D;
    private static final double LEAP_MAX_VERTICAL = 0.6D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int leapCooldown;

    public DirewolfEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D).add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.ARMOR, 0.0D).add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DirewolfMeleeAttackGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.5F));

        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Skeleton.class, false, false));
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive() && this.onGround() && this.leapCooldown <= 0) {
            this.leapCooldown = LEAP_COOLDOWN_TICKS;
            this.triggerAnim("procedure", "leap");
            this.playSound(SoundEvents.WOLF_GROWL, 0.7F, 1.0F);

            Vec3 look = this.getLookAngle();
            this.setDeltaMovement(look.x * randomLeapDouble(LEAP_MIN_HORIZONTAL, LEAP_MAX_HORIZONTAL),
                    randomLeapDouble(LEAP_MIN_VERTICAL, LEAP_MAX_VERTICAL),
                    look.z * randomLeapDouble(LEAP_MIN_HORIZONTAL, LEAP_MAX_HORIZONTAL));
            this.hasImpulse = true;

            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, LEAP_BUFF_TICKS, 0, false, true));
            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, LEAP_BUFF_TICKS, 0, false, true));
        }
        return hurt;
    }

    private double randomLeapDouble(double min, double max) {
        return Mth.nextDouble(this.random, min, max);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.leapCooldown > 0) {
            this.leapCooldown--;
        }
    }

    @Override
    public void awardKillScore(@NotNull Entity killed, int score, @NotNull DamageSource source) {
        super.awardKillScore(killed, score, source);
        if (!this.level().isClientSide) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 4, this::movementPredicate));
        data.add(new AnimationController<>(this, "procedure", state -> PlayState.STOP).triggerableAnim("leap", EEAnimations.DIREWOLF_LEAP));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        return AnimUtils.idleWalkAnimation(event, EEAnimations.IDLE, EEAnimations.DIREWOLF_RUN);
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.WOLF_GROWL;
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.WOLF_HURT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    static class DirewolfMeleeAttackGoal extends MeleeAttackGoal {
        public DirewolfMeleeAttackGoal(DirewolfEntity wolf) {
            super(wolf, 1.2D, false);
        }

        @Override
        protected double getAttackReachSqr(@NotNull LivingEntity attackTarget) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() + attackTarget.getBbWidth();
        }
    }
}
