package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.Vec3;
import org.infernalstudios.enemyexp.core.mixin.RandomLookAroundGoalAccessor;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class SluggerEntity extends Zombie implements GeoEntity {
    private static final EntityDataAccessor<Integer> CHARGE_TIME = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CHARGE_DIR_X = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CHARGE_DIR_Z = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.STRING);
    private static final String NORMAL_TEXTURE = "slugger";
    private static final String STAGGERED_CHARGE = "slugger_charge";
    private static final String DASHING_TEXTURE = "slugger_dashing";
    private static final int CHARGE_DURATION = 17;
    private static final int CHARGE_WINDUP = 10;
    private static final float CHARGE_SPEED = 0.7F;
    private static final float CHARGE_DAMAGE = 6.0F;
    private static final float CHARGE_KNOCKBACK = 1.5F;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SluggerEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D).add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.ARMOR, 16.0D).add(Attributes.KNOCKBACK_RESISTANCE, 3.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.3D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SluggerChargeGoal(this));
        this.goalSelector.addGoal(2, new SluggerAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0F, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(8, new SluggerLookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new SluggerRandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARGE_TIME, 0);
        this.entityData.define(CHARGE_DIR_X, 0F);
        this.entityData.define(CHARGE_DIR_Z, 0F);
        this.entityData.define(TEXTURE, NORMAL_TEXTURE);
    }

    private void lockRotationDuringCharge() {
        this.setYRot(this.yRotO);
        this.yHeadRot = this.yRotO;
        this.yBodyRot = this.yRotO;
        this.setXRot(this.xRotO);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isCharging()) lockRotationDuringCharge();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (isCharging()) lockRotationDuringCharge();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && !player.isCreative() && !isCharging() && !this.level().isClientSide) {
            Vec3 toPlayer = player.position().subtract(this.position()).normalize();
            this.entityData.set(CHARGE_DIR_X, (float) toPlayer.x);
            this.entityData.set(CHARGE_DIR_Z, (float) toPlayer.z);
            setTexture(STAGGERED_CHARGE);
            setChargeTime(CHARGE_DURATION + CHARGE_WINDUP);
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
        }
        return super.hurt(source, amount);
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
        return AnimUtils.idleWalkAnimation(event, "idle", "walk");
    }

    public int getChargeTime() {
        return this.entityData.get(CHARGE_TIME);
    }

    public void setChargeTime(int time) {
        this.entityData.set(CHARGE_TIME, time);
    }

    public boolean isCharging() {
        return getChargeTime() > 0 && getChargeTime() <= CHARGE_DURATION;
    }

    public String getTexture() {
        return this.entityData.get(TEXTURE);
    }

    public void setTexture(String texture) {
        this.entityData.set(TEXTURE, texture);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean childZombie) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static class SluggerChargeGoal extends Goal {
        private final SluggerEntity mob;
        private float chargeDirectionX;
        private float chargeDirectionZ;

        public SluggerChargeGoal(SluggerEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.mob.getChargeTime() > 0;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            this.chargeDirectionX = this.mob.entityData.get(CHARGE_DIR_X);
            this.chargeDirectionZ = this.mob.entityData.get(CHARGE_DIR_Z);
        }

        @Override
        public void tick() {
            int chargeTime = this.mob.getChargeTime();
            if (chargeTime <= 0) return;

            if (chargeTime > CHARGE_DURATION) {
                this.mob.setDeltaMovement(0, this.mob.getDeltaMovement().y, 0);
            } else {
                performCharge();
            }

            if (chargeTime == 1) {
                this.mob.setTexture(NORMAL_TEXTURE);
            }

            this.mob.setChargeTime(chargeTime - 1);
            this.mob.hurtMarked = true;
        }

        private void performCharge() {
            this.mob.setTexture(DASHING_TEXTURE);
            this.mob.setDeltaMovement(
                    this.chargeDirectionX * CHARGE_SPEED,
                    this.mob.getDeltaMovement().y,
                    this.chargeDirectionZ * CHARGE_SPEED
            );

            if (!this.mob.level().isClientSide) {
                damageCollidingEntities();
            }
        }

        private void damageCollidingEntities() {
            List<LivingEntity> collided = this.mob.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.mob.getBoundingBox().inflate(0.5)
            );
            collided.remove(this.mob);

            for (LivingEntity entity : collided) {
                entity.hurt(this.mob.damageSources().mobAttack(this.mob), CHARGE_DAMAGE);
                Vec3 knockbackDir = entity.position().subtract(this.mob.position()).normalize();
                entity.push(knockbackDir.x * CHARGE_KNOCKBACK, 0.3, knockbackDir.z * CHARGE_KNOCKBACK);
            }
        }

        @Override
        public void stop() {
            this.mob.setTexture(NORMAL_TEXTURE);
            this.mob.setChargeTime(0);
        }
    }

    public static class SluggerLookAtPlayerGoal extends LookAtPlayerGoal {
        public SluggerLookAtPlayerGoal(SluggerEntity mob, Class<? extends LivingEntity> lookAtType, float lookDistance) {
            super(mob, lookAtType, lookDistance);
        }

        @Override
        public boolean canUse() {
            return ((SluggerEntity) this.mob).getChargeTime() <= 0 && super.canUse();
        }
    }

    public static class SluggerRandomLookAroundGoal extends RandomLookAroundGoal {
        public SluggerRandomLookAroundGoal(SluggerEntity mob) {
            super(mob);
        }

        @Override
        public boolean canUse() {
            RandomLookAroundGoalAccessor accessor = (RandomLookAroundGoalAccessor) this;
            return ((SluggerEntity) accessor.getMob()).getChargeTime() <= 0 && super.canUse();
        }
    }

    public static class SluggerAttackGoal extends MeleeAttackGoal {
        public SluggerAttackGoal(SluggerEntity mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            return ((SluggerEntity) this.mob).getChargeTime() <= 0 && super.canUse();
        }
    }
}
