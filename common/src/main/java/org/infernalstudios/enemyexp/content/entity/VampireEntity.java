package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.content.entity.goal.ControlAttackGoal;
import org.infernalstudios.enemyexp.content.entity.goal.HardLookAtTargetGoal;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.infernalstudios.enemyexp.setup.EEMobEffects;
import org.infernalstudios.enemyexp.setup.EEntities;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class VampireEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> AWAKE = SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AERIAL = SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITING = SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final UUID FLYING_SPEED_MODIFIER = UUID.fromString("bac26f76-fa57-4c72-b0e2-a26c8de7e2a4");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final PathNavigation groundNavigation;
    private final PathNavigation flyingNavigation;
    private final MoveControl groundMoveControl;
    private final MoveControl flyingMoveControl;
    private int ticksSinceInteraction = 0;
    private int alertTicks = 0;
    private boolean spawningBiter = false;

    public VampireEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.groundNavigation = new GroundPathNavigation(this, level);
        this.flyingNavigation = new FlyingPathNavigation(this, level);
        this.groundMoveControl = new MoveControl(this);
        this.flyingMoveControl = new FlyingMoveControl(this, 20, true);

        this.navigation = this.groundNavigation;
        this.moveControl = this.groundMoveControl;
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.FLYING_SPEED, 0.7D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AWAKE, false);
        this.entityData.define(AERIAL, false);
        this.entityData.define(ANGRY, false);
        this.entityData.define(SITING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, IronGolem.class, 8.0F, 1.2D, 1.5D));
        this.goalSelector.addGoal(2, new ControlAttackGoal(this, 1.0D, false, () -> this.alertTicks <= 0){
            @Override
            protected double getAttackReachSqr(@NotNull LivingEntity attackTarget) {
                // 50% reach
                return super.getAttackReachSqr(attackTarget) * 0.5;
            }
        });
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return VampireEntity.this.isAerial() && VampireEntity.this.alertTicks <= 0 && super.canUse();
            }
        });

        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return !VampireEntity.this.isAerial() && VampireEntity.this.alertTicks <= 0 && super.canUse();
            }
        });

        this.goalSelector.addGoal(3, new HardLookAtTargetGoal(this, 10.0F, 10.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, this::canTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true, this::canTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true, this::canTarget));
    }

    private boolean canTarget(LivingEntity target) {
        if (target.hasEffect(EEMobEffects.BITTEN.get())) return false;
        double range = isAwake() ? 64.0D : 8.0D;
        return this.distanceToSqr(target) <= range * range;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && !this.level().isClientSide) {
            if (this.level().isDay() && this.level().canSeeSky(this.blockPosition())) {
                this.setSecondsOnFire(8);
            }
        }
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        if (getVehicle() != null && !isSitting()) setSitting(true);
        else if (getVehicle() == null && isSitting()) setSitting(false);

        AttributeModifier flyingSpeed = new AttributeModifier(FLYING_SPEED_MODIFIER, "Vampire Flying Speed", 0.2, AttributeModifier.Operation.ADDITION);

        if (isAerial()) {
            if (!this.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(flyingSpeed)) this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(flyingSpeed);
            if (!this.getAttribute(Attributes.FLYING_SPEED).hasModifier(flyingSpeed)) this.getAttribute(Attributes.FLYING_SPEED).addTransientModifier(flyingSpeed);
        } else {
            if (this.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(flyingSpeed)) this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(FLYING_SPEED_MODIFIER);
            if (this.getAttribute(Attributes.FLYING_SPEED).hasModifier(flyingSpeed)) this.getAttribute(Attributes.FLYING_SPEED).removeModifier(FLYING_SPEED_MODIFIER);
        }

        if (this.tickCount % 20 == 0 && this.isInWater()) {
            if (!this.isAerial()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.5, 0));
                setAerial(true);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.1, 0));
            }
        }


        if (this.getTarget() != null) {
            // in case the target is 3 blocks of distance on y, the vampire will fly. This checks every 2 seconds
            if (this.getTarget().getY() > this.getY() + 3.0D && !isAerial() && this.level().getGameTime() % 40 == 0) {
                setAerial(true);
            }

            if (!isAwake()) {
                setAwake(true);
                setAngry(true);
                alertTicks = 20;
                triggerAnim("alert", "alert");
                this.playSound(SoundEvents.WARDEN_ROAR, 1.0F, 1.0F);
            }
            ticksSinceInteraction = 0;
        } else if (isAwake()) {
            ticksSinceInteraction++;
            if (ticksSinceInteraction >= 6000) {
                setAwake(false);
                setAngry(false);
                ticksSinceInteraction = 0;
            }
        }

        if (alertTicks > 0) {
            alertTicks--;
        } else if (isAngry() && !this.isAggressive()) {
            setAngry(false);
        }

        if (!isAerial() && this.getTarget() != null && this.tickCount % 20 == 0) {
            boolean targetHigher = this.getTarget().getY() > this.getY() + 2.0D;
            boolean noPath = !this.getNavigation().isInProgress() || this.getNavigation().isStuck();

            if (targetHigher && noPath) {
                setAerial(true);
            }
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);

        if (this.level().isClientSide || !(source.getEntity() instanceof LivingEntity)) return hurt;
        if (!hurt) return false;

        if (!isAwake()) {
            setAwake(true);
            alertTicks = 20;
            setAngry(true);
        }
        ticksSinceInteraction = 0;

        if (this.random.nextFloat() < 0.25f && !this.isDeadOrDying()) {
            setAerial(!isAerial());
        }

        if (this.getHealth() > 0) triggerAnim("hurt", "hurt_flying");
        return true;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if (hurt && entity instanceof LivingEntity livingTarget) {
            this.heal(6.0F);

            if (livingTarget instanceof AbstractVillager || livingTarget instanceof AbstractIllager) {
                livingTarget.addEffect(new MobEffectInstance(EEMobEffects.BITTEN.get(), 600, 0));
            }
        }
        return hurt;
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        super.die(cause);
        if (!this.level().isClientSide) {
            if (!isAerial() && this.random.nextBoolean()) {
                this.spawningBiter = true;
                triggerAnim("death", "biter_spawn");
            } else {
                if (isAerial()) triggerAnim("death", "die_air");
                else triggerAnim("death", "die_ground");
            }
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        int maxDeathTime = this.spawningBiter ? 30 : 20;

        if (this.deathTime >= maxDeathTime && !this.level().isClientSide) {
            this.remove(Entity.RemovalReason.KILLED);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.8, this.getZ(), 10, 0.3, 0.5, 0.3, 0.05);
            }

            if (this.spawningBiter) {
                BiterEntity biter = EEntities.BITER.get().create(this.level());
                if (biter != null) {
                    double yaw = Math.toRadians(this.getYRot());
                    double spawnX = this.getX() - Math.sin(yaw);
                    double spawnY = this.getY() + 1.5;
                    double spawnZ = this.getZ() + Math.cos(yaw);

                    biter.moveTo(spawnX, spawnY, spawnZ, this.getYRot(), this.getXRot());
                    this.level().addFreshEntity(biter);
                }
            }
        }
    }

    public boolean isAwake() {
        return this.entityData.get(AWAKE);
    }

    public void setAwake(boolean awake) {
        this.entityData.set(AWAKE, awake);
    }

    public boolean isAerial() {
        return this.entityData.get(AERIAL);
    }

    public void setAerial(boolean aerial) {
        this.entityData.set(AERIAL, aerial);
    }

    public boolean isAngry() {
        return this.entityData.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        this.entityData.set(ANGRY, angry);
    }

    public boolean isSitting() {
        return this.entityData.get(SITING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(SITING, sitting);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        data.add(new AnimationController<>(this, "wings", 2, this::wingsPredicate));
        data.add(new AnimationController<>(this, "falling", 2, this::fallingPredicate));

        data.add(new AnimationController<>(this, "alert", 0, state -> PlayState.STOP)
                .triggerableAnim("alert", EEAnimations.VAMPIRE_ALERT));

        data.add(new AnimationController<>(this, "dodge", 0, state -> PlayState.STOP)
                .triggerableAnim("dodge_back", EEAnimations.VAMPIRE_DODGE_BACK)
                .triggerableAnim("dodge_forward", EEAnimations.VAMPIRE_DODGE_FORWARD));

        data.add(new AnimationController<>(this, "hurt", 0, state -> PlayState.STOP)
                .triggerableAnim("hurt_standing", EEAnimations.VAMPIRE_HURT_STANDING)
                .triggerableAnim("hurt_flying", EEAnimations.VAMPIRE_HURT_FLYING));

        data.add(new AnimationController<>(this, "death", 0, state -> PlayState.STOP)
                .triggerableAnim("die_ground", EEAnimations.VAMPIRE_DIE_GROUND)
                .triggerableAnim("die_air", EEAnimations.VAMPIRE_DIE_AIR)
                .triggerableAnim("biter_spawn", EEAnimations.VAMPIRE_BITER_SPAWN));

        data.add(new AnimationController<>(this, "landing", 0, state -> PlayState.STOP)
                .triggerableAnim("falling_end", EEAnimations.VAMPIRE_FALLING_END));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        if (isSitting()) return event.setAndContinue(EEAnimations.SIT);

        if (isAerial()) {
            if (event.isMoving()) return event.setAndContinue(EEAnimations.VAMPIRE_FLYING);
            return event.setAndContinue(EEAnimations.VAMPIRE_HOVER);
        }
        if (isAwake() && event.isMoving()) {
            return event.setAndContinue(EEAnimations.VAMPIRE_CHASE);
        }
        return AnimUtils.idleWalkAnimation(event, EEAnimations.IDLE, EEAnimations.WALK);
    }

    private PlayState wingsPredicate(AnimationState<?> event) {
        if (isAerial()) {
            return event.setAndContinue(EEAnimations.VAMPIRE_WINGS_FLAPPING);
        }
        return PlayState.STOP;
    }

    private PlayState fallingPredicate(AnimationState<?> event) {
        if (!isAerial() && !this.onGround() && this.getDeltaMovement().y < -0.4) {
            return event.setAndContinue(EEAnimations.VAMPIRE_FALLING);
        }
        return PlayState.STOP;
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (AERIAL.equals(key)) {
            boolean aerial = isAerial();
            this.setNoGravity(aerial);

            if (!this.level().isClientSide) {
                if (aerial) {
                    this.navigation = this.flyingNavigation;
                    this.moveControl = this.flyingMoveControl;
                } else {
                    this.navigation = this.groundNavigation;
                    this.moveControl = this.groundMoveControl;
                }
            }
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        if (isAerial()) return false;

        if (!this.level().isClientSide && fallDistance > 1.0F) {
            triggerAnim("landing", "falling_end");
        }

        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Awake", this.isAwake());
        compound.putBoolean("Aerial", this.isAerial());
        compound.putBoolean("Angry", this.isAngry());
        compound.putInt("AlertTicks", this.alertTicks);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Awake")) this.setAwake(compound.getBoolean("Awake"));
        if (compound.contains("Aerial")) this.setAerial(compound.getBoolean("Aerial"));
        if (compound.contains("Angry")) this.setAngry(compound.getBoolean("Angry"));
        if (compound.contains("AlertTicks")) this.alertTicks = compound.getInt("AlertTicks");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}