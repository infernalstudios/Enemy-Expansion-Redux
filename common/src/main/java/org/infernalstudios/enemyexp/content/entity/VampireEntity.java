package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.infernalstudios.enemyexp.setup.EEntities;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VampireEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Boolean> AWAKE = SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> AERIAL = SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(VampireEntity.class, EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int ticksSinceInteraction = 0;
    private int dodgeTicks = 0;
    private int alertTicks = 0;

    private final PathNavigation groundNavigation;
    private final PathNavigation flyingNavigation;
    private final MoveControl groundMoveControl;
    private final MoveControl flyingMoveControl;

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
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AWAKE, false);
        this.entityData.define(AERIAL, false);
        this.entityData.define(ANGRY, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, IronGolem.class, 8.0F, 1.2D, 1.5D));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));

        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return VampireEntity.this.isAerial() && super.canUse();
            }
        });

        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return !VampireEntity.this.isAerial() && super.canUse();
            }
        });

        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, this::canTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true, this::canTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true, this::canTarget));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Skeleton.class, true, this::canTarget));
    }

    private boolean canTarget(LivingEntity target) {
        double range = isAwake() ? 64.0D : 8.0D;
        return this.distanceToSqr(target) <= range * range;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.getTarget() != null) {
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

            if (dodgeTicks > 0) dodgeTicks--;

            if (!isAerial() && this.getTarget() != null && this.tickCount % 20 == 0) {
                if (this.getTarget().getY() > this.getY() + 2.0D && this.getNavigation().isDone()) {
                    setAerial(true);
                }
            }
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getEntity() != null) {
            if (!isAwake()) {
                setAwake(true);
                alertTicks = 20;
                setAngry(true);
            }
            ticksSinceInteraction = 0;

            if (this.random.nextFloat() < 0.25f) {
                setAerial(!isAerial());
            }

            Vec3 knockbackDir = this.position().subtract(source.getEntity().position()).normalize();
            this.setDeltaMovement(knockbackDir.x * 1.2, 0.4, knockbackDir.z * 1.2);
            dodgeTicks = 10;
            triggerAnim("dodge", "dodge_back");
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if (hurt && entity instanceof LivingEntity livingTarget) {
            this.heal(6.0F);

            if (livingTarget instanceof AbstractVillager || livingTarget instanceof AbstractIllager) {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
            }
        }
        return hurt;
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        super.die(cause);
        if (!this.level().isClientSide && this.onGround() && this.random.nextBoolean()) {
            BiterEntity biter = EEntities.BITER.get().create(this.level());
            if (biter != null) {
                biter.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                this.level().addFreshEntity(biter);
                triggerAnim("death", "biter_spawn");
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        data.add(new AnimationController<>(this, "alert", 0, state -> PlayState.STOP)
                .triggerableAnim("alert", EEAnimations.VAMPIRE_ALERT));
        data.add(new AnimationController<>(this, "dodge", 0, state -> PlayState.STOP)
                .triggerableAnim("dodge_back", EEAnimations.VAMPIRE_DODGE));
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
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        if (isAerial()) {
            return event.setAndContinue(EEAnimations.VAMPIRE_FLYING);
        }
        if (isAwake() && event.isMoving()) {
            return event.setAndContinue(EEAnimations.VAMPIRE_CHASE);
        }
        return AnimUtils.idleWalkAnimation(event, EEAnimations.IDLE, EEAnimations.WALK);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}