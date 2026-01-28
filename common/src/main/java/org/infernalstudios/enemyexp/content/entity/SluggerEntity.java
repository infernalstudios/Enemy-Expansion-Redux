package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.damagesource.DamageSource;
import org.infernalstudios.enemyexp.core.mixin.RandomLookAroundGoalAccessor;
import org.infernalstudios.enemyexp.core.mixin.SpawnPlacementsAccessor;
import org.infernalstudios.enemyexp.setup.EEntities;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class SluggerEntity extends Zombie implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Integer> CHARGE_TIME = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CHARGE_DIR_X = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CHARGE_DIR_Z = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.FLOAT);

    private static final int CHARGE_DURATION = 20;
    private static final int CHARGE_WINDUP = 20;
    private static final float CHARGE_SPEED = 0.7F;
    private static final float CHARGE_DAMAGE = 6.0F;
    private static final float CHARGE_KNOCKBACK = 1.5F;

    public SluggerEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0F, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.goalSelector.addGoal(8, new SluggerLookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new SluggerRandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new SluggerAttackGoal(this, 1.0D, false));
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D).add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.ARMOR, 16.0D).add(Attributes.KNOCKBACK_RESISTANCE, 3.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.3D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARGE_TIME, 0);
        this.entityData.define(CHARGE_DIR_X, 0F);
        this.entityData.define(CHARGE_DIR_Z, 0F);
    }

    public static void spawn() {
        SpawnPlacementsAccessor.callRegister(EEntities.SLUGGER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EEntities::checkHostileRules);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && getChargeTime() <= 0 && !this.level().isClientSide) {
            Vec3 toPlayer = player.position().subtract(this.position()).normalize();
            this.entityData.set(CHARGE_DIR_X, (float) toPlayer.x);
            this.entityData.set(CHARGE_DIR_Z, (float) toPlayer.z);
            setChargeTime(CHARGE_DURATION + CHARGE_WINDUP);

            this.getLookControl().setLookAt(player, 30.0F, 30.0F);

        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        int chargeTime = getChargeTime();
        if (chargeTime <= 0) return;

        float dirX = this.entityData.get(CHARGE_DIR_X);
        float dirZ = this.entityData.get(CHARGE_DIR_Z);

        if (chargeTime > CHARGE_DURATION) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        } else {
            this.setDeltaMovement(dirX * CHARGE_SPEED, this.getDeltaMovement().y, dirZ * CHARGE_SPEED);

            if (!this.level().isClientSide) {
                List<LivingEntity> collided = this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        this.getBoundingBox().inflate(0.5)
                );
                collided.remove(this);

                for (LivingEntity entity : collided) {
                    entity.hurt(this.damageSources().mobAttack(this), CHARGE_DAMAGE);
                    Vec3 knockbackDir = entity.position().subtract(this.position()).normalize();
                    entity.push(knockbackDir.x * CHARGE_KNOCKBACK, 0.3, knockbackDir.z * CHARGE_KNOCKBACK);
                }
            }
        }

        setChargeTime(chargeTime - 1);
        this.hurtMarked = true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        int chargeTime = getChargeTime();
        if (chargeTime > CHARGE_DURATION) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("charge"));
        } else if (chargeTime > 0) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("dash"));
        }
        return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F
                ? event.setAndContinue(RawAnimation.begin().thenLoop("idle"))
                : event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
    }

    public int getChargeTime() {
        return this.entityData.get(CHARGE_TIME);
    }

    public void setChargeTime(int time) {
        this.entityData.set(CHARGE_TIME, time);
    }

    @Override
    public void setBaby(boolean childZombie) {}

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static class SluggerLookAtPlayerGoal extends LookAtPlayerGoal {
        public SluggerLookAtPlayerGoal(SluggerEntity mob, Class<? extends LivingEntity> lookAtType, float lookDistance) {
            super(mob, lookAtType, lookDistance);
        }

        @Override
        public boolean canUse() {
            if (((SluggerEntity) this.mob).getChargeTime() > 0) return false;
            return super.canUse();
        }
    }

    public static class SluggerRandomLookAroundGoal extends RandomLookAroundGoal {
        public SluggerRandomLookAroundGoal(SluggerEntity mob) {
            super(mob);
        }

        @Override
        public boolean canUse() {
            RandomLookAroundGoalAccessor accessor = (RandomLookAroundGoalAccessor) this;
            if (((SluggerEntity) accessor.getMob()).getChargeTime() > 0) return false;
            return super.canUse();
        }
    }

    public static class SluggerAttackGoal extends MeleeAttackGoal {
        public SluggerAttackGoal(SluggerEntity mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            if (((SluggerEntity) this.mob).getChargeTime() > 0) return false;
            return super.canUse();
        }
    }
}
