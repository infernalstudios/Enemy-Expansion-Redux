package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.content.entity.goal.*;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class EquestrianEntity extends Zombie implements GeoEntity, IChargeable {
    // State values
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PANIC = 1;
    public static final int STATE_KITING = 2;
    public static final int STATE_CHARGING_GALLOP = 3;
    public static final int STATE_SITTING = 4;

    public static final int PANIC_TIME_TICKS = 60;
    public static final int CHARGE_WINDUP = 20;
    public static final int CHARGE_DURATION = 13;
    public static final float CHARGE_SPEED = 1.0F;
    public static final float CHARGE_DAMAGE = 10.0F;
    public static final float CHARGE_KNOCKBACK = 1.7F;

    private static final UUID PANIC_KNOCKBACK_MODIFIER_UUID = UUID.fromString("6d7f9b8a-3e2c-4f1a-9d5b-8e7c6d5f4a3b");
    private static final AttributeModifier PANIC_KNOCKBACK_MODIFIER = new AttributeModifier(PANIC_KNOCKBACK_MODIFIER_UUID, "Panic knockback resistance", 1.0D, AttributeModifier.Operation.ADDITION);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(EquestrianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHARGE_TIME = SynchedEntityData.defineId(EquestrianEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CHARGE_DIR_X = SynchedEntityData.defineId(EquestrianEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CHARGE_DIR_Z = SynchedEntityData.defineId(EquestrianEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EquestrianEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, STATE_NORMAL);
        this.entityData.define(CHARGE_TIME, 0);
        this.entityData.define(CHARGE_DIR_X, 0F);
        this.entityData.define(CHARGE_DIR_Z, 0F);
    }

    @Override
    public float maxUpStep() {
        return 1F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ControlPanicGoal(this, 1.4F, () -> this.getState() == STATE_PANIC && this.getState() != STATE_SITTING));
        this.goalSelector.addGoal(2, new EquestrianChargeGoal(this));
        this.goalSelector.addGoal(3, new EquestrianRangedKitingGoal(this));
        this.goalSelector.addGoal(4, new HardLookAtTargetGoal(this, 10.0F, 10.0F));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Zombie.class));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    @Override
    protected void handleAttributes(float difficulty) {
        // This makes it have the same values as the Leader Zombies
        this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * (double) 0.25F + (double) 0.5F, AttributeModifier.Operation.ADDITION));
        this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * (double) 3.0F + (double) 1.0F, AttributeModifier.Operation.MULTIPLY_TOTAL));
        this.setCanBreakDoors(this.supportsBreakDoorGoal());
    }

    private void lockRotationDuringCharge() {
        this.setYRot(this.yRotO);
        this.yHeadRot = this.yRotO;
        this.yBodyRot = this.yRotO;
        this.setXRot(this.xRotO);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getVehicle() != null) {
            this.setState(STATE_SITTING);
        } else {
            if (getState() == STATE_SITTING) {
                this.setState(STATE_NORMAL);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isDashing()) lockRotationDuringCharge();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (isDashing()) lockRotationDuringCharge();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        AttributeInstance attribute = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);

        if (source.getEntity() instanceof Player player && !player.getAbilities().instabuild && !this.level().isClientSide) {
            if (getState() != STATE_PANIC && getState() != STATE_CHARGING_GALLOP) {
                setState(STATE_PANIC);
                if (attribute != null) attribute.addTransientModifier(PANIC_KNOCKBACK_MODIFIER);
                EEMod.scheduleTask((ServerLevel) this.level(), PANIC_TIME_TICKS, () -> {
                    if (this.isDeadOrDying()) return;
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1, this.getZ(), 5, 1.0D, 1.0D, 1.0D, 0.6);
                    setState(STATE_NORMAL);
                    if (attribute != null) attribute.removeModifier(PANIC_KNOCKBACK_MODIFIER_UUID);
                });
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        data.add(new AnimationController<>(this, "special", 2, this::specialPredicate));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        int chargeTime = getChargeTime();
        // Dashing
        if (chargeTime > 0 && chargeTime <= CHARGE_DURATION) {
            event.getController().setAnimation(EEAnimations.GALLOP);
            return PlayState.CONTINUE;
        }
        // Windup
        if (chargeTime > CHARGE_DURATION) {
            event.getController().setAnimation(EEAnimations.PREPARE);
            return PlayState.CONTINUE;
        }
        // Kiting
        if (getState() == STATE_KITING) {
            event.getController().setAnimation(EEAnimations.TROT);
            return PlayState.CONTINUE;
        }

        if (getState() == STATE_SITTING) return event.setAndContinue(EEAnimations.SIT);

        if (getState() != STATE_NORMAL) return PlayState.STOP;
        return AnimUtils.idleWalkAnimation(event);
    }

    private PlayState specialPredicate(AnimationState<?> event) {
        if (getState() == STATE_PANIC && !AnimUtils.isNotMoving(event)) {
            event.getController().setAnimation(EEAnimations.PANICKED);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    public void setState(int state) {
        this.entityData.set(STATE, state);
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    public boolean isInSpecialState() {
        return getState() != STATE_NORMAL;
    }

    /** True only during the actual dash (not windup). */
    public boolean isDashing() {
        int t = getChargeTime();
        return t > 0 && t <= CHARGE_DURATION;
    }

    @Override
    public int getChargeTime() {
        return this.entityData.get(CHARGE_TIME);
    }

    @Override
    public void setChargeTime(int time) {
        this.entityData.set(CHARGE_TIME, time);
    }

    @Override
    public float getChargeDirX() {
        return this.entityData.get(CHARGE_DIR_X);
    }

    @Override
    public void setChargeDirX(float x) {
        this.entityData.set(CHARGE_DIR_X, x);
    }

    @Override
    public float getChargeDirZ() {
        return this.entityData.get(CHARGE_DIR_Z);
    }

    @Override
    public void setChargeDirZ(float z) {
        this.entityData.set(CHARGE_DIR_Z, z);
    }

    @Override
    protected @NotNull SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static class EquestrianRangedKitingGoal extends RangedKitingGoal {
        private final EquestrianEntity equestrian;

        public EquestrianRangedKitingGoal(EquestrianEntity mob) {
            super(mob, 0.9D);
            this.equestrian = mob;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !equestrian.isInSpecialState();
        }
    }
}