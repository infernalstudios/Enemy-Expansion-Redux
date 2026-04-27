package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.content.entity.goal.ChargeAttackGoal;
import org.infernalstudios.enemyexp.content.entity.goal.ControlAttackGoal;
import org.infernalstudios.enemyexp.content.entity.goal.ControlLookAtPlayerGoal;
import org.infernalstudios.enemyexp.content.entity.goal.ControlRandomLookAroundGoal;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SluggerEntity extends Zombie implements GeoEntity, IChargeable {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_CHARGING = 1;
    public static final int STATE_DASHING = 2;
    public static final int STATE_SPRINTING = 3;
    public static final int STATE_SITTING = 4;

    private static final EntityDataAccessor<Integer> CHARGE_TIME = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> CHARGE_DIR_X = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CHARGE_DIR_Z = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(SluggerEntity.class, EntityDataSerializers.INT);

    private static final int CHARGE_DURATION = 8;
    private static final int CHARGE_WINDUP = 10;
    private static final float CHARGE_SPEED = 1.0F;
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
        this.goalSelector.addGoal(1, new ChargeAttackGoal<>(this, new SluggerChargeCallbacks(),
                CHARGE_WINDUP, CHARGE_DURATION, CHARGE_SPEED, CHARGE_DAMAGE, CHARGE_KNOCKBACK
        ));
        this.goalSelector.addGoal(2, new ControlAttackGoal(this, 1.0D, false,
                () -> this.getChargeTime() <= 0 && this.getState() != STATE_SITTING) {
            @Override
            public void start() {
                super.start();
                setState(STATE_SPRINTING);
            }

            @Override
            public void stop() {
                super.stop();
                if (getState() == STATE_SPRINTING) {
                    setState(STATE_NORMAL);
                }
            }
        });
        this.goalSelector.addGoal(8, new ControlLookAtPlayerGoal(this, Player.class, 8.0F,
                () -> this.getChargeTime() <= 0));
        this.goalSelector.addGoal(9, new ControlRandomLookAroundGoal(this,
                () -> this.getChargeTime() <= 0));

        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0F, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0F));
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
        this.entityData.define(STATE, STATE_NORMAL);
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
        if (isCharging()) lockRotationDuringCharge();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (isCharging()) lockRotationDuringCharge();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && !player.isCreative() && !isCharging() && !this.level().isClientSide && getState() != STATE_SITTING) {
            Vec3 toPlayer = player.position().subtract(this.position()).normalize();
            this.entityData.set(CHARGE_DIR_X, (float) toPlayer.x);
            this.entityData.set(CHARGE_DIR_Z, (float) toPlayer.z);
            setState(STATE_CHARGING);
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
            return event.setAndContinue(EEAnimations.CHARGE);
        } else if (chargeTime > 0) {
            return event.setAndContinue(EEAnimations.DASH);
        }
        if (getState() == STATE_SPRINTING && !AnimUtils.isNotMoving(event)) {
            return event.setAndContinue(EEAnimations.SPRINT);
        }

        if (getState() == STATE_SITTING) return event.setAndContinue(EEAnimations.SIT);

        return AnimUtils.idleWalkAnimation(event);
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

    public boolean isCharging() {
        int t = getChargeTime();
        return t > 0 && t <= CHARGE_DURATION;
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    public void setState(int state) {
        this.entityData.set(STATE, state);
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

    private class SluggerChargeCallbacks implements ChargeAttackGoal.ChargeAttackCallbacks {
        @Override
        public void onWindupStart() {
            // Texture is set in hurt() just before chargeTime is assigned,
            // so nothing extra needed here.
        }

        @Override
        public void onChargeTick() {
            setState(STATE_DASHING);
        }

        @Override
        public void onChargeEnd() {
            setState(STATE_NORMAL);
        }

        @Override
        public void onStop() {
            setState(STATE_NORMAL);
        }

        @Override
        public boolean canBeHurtNormally(LivingEntity entity) {
            return true;
        }
    }
}