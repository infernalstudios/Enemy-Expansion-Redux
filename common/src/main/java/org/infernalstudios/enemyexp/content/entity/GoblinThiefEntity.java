package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.content.entity.goal.ControlAttackGoal;
import org.infernalstudios.enemyexp.content.entity.goal.ControlAvoidEntityGoal;
import org.infernalstudios.enemyexp.content.entity.goal.EELeapAttackGoal;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class GoblinThiefEntity extends Monster implements GeoEntity {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PANIC = 1;
    public static final int STATE_LEAP = 2;
    public static final int STATE_SNEAK = 3;
    public static final int STATE_CONFIDENT = 4;
    public static final int STATE_SITTING = 5;

    private static final int PANIC_RECOVERY_TICKS = 45;
    private static final int CONFIDENT_TICKS = 50;
    private static final double MAX_TRIGGER_DISTANCE = 8.0D;
    private static final double MIN_TRIGGER_DISTANCE = 3.0D;
    private static final int COOLDOWN_TICKS = 105;
    private static final int WINDUP_ENDS = 5;
    private static final int ANIM_TOTAL = 42;

    private static final UUID PANIC_KNOCKBACK_MODIFIER_UUID = UUID.fromString("6d7f9b8a-3e2c-4f1a-9d5b-8e7c6d5f4a3b");
    private static final AttributeModifier PANIC_KNOCKBACK_MODIFIER = new AttributeModifier(PANIC_KNOCKBACK_MODIFIER_UUID, "Panic knockback resistance", 1.0D, AttributeModifier.Operation.ADDITION);

    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(GoblinThiefEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GoblinThiefEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ControlAvoidEntityGoal<>(this, Player.class, 16.0F, 1.2D, 1.3D, () -> this.getState() == STATE_CONFIDENT || this.getState() == STATE_PANIC));
        this.goalSelector.addGoal(1, new ControlAvoidEntityGoal<>(this, AbstractVillager.class, 16.0F, 1.2D, 1.3D, () -> this.getState() == STATE_CONFIDENT || this.getState() == STATE_PANIC));

        this.goalSelector.addGoal(3, new EELeapAttackGoal<>(this, new GoblinThiefLeapCallbacks(this), MIN_TRIGGER_DISTANCE, MAX_TRIGGER_DISTANCE, COOLDOWN_TICKS, WINDUP_ENDS, ANIM_TOTAL){
            @Override
            public boolean canUse() {
                return super.canUse() && getState() != STATE_LEAP && getState() != STATE_PANIC && getState() != STATE_CONFIDENT && getState() != STATE_SITTING;
            }
        });
        this.goalSelector.addGoal(4, new ControlAttackGoal(this, 1.0D, true, () -> getState() != STATE_LEAP && getState() != STATE_PANIC && getState() != STATE_CONFIDENT && getState() != STATE_SITTING){
            @Override
            public void start() {
                super.start();
                setState(STATE_SNEAK);
            }

            @Override
            public void stop() {
                super.stop();
                if (getState() == STATE_SNEAK) {
                    setState(STATE_NORMAL);
                }
            }
        });
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false, false));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, STATE_NORMAL);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
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
    public boolean doHurtTarget(@NotNull Entity entity) {
        boolean result = super.doHurtTarget(entity);

        if (getState() == STATE_SITTING) return result;

        if (entity instanceof Player player) {
            if (player.totalExperience >= 20) {
                xpReward += 20;
                player.giveExperiencePoints(-20);
            }
        }

        if (result && getState() != STATE_CONFIDENT) {
            if (entity instanceof LivingEntity living && living.isDeadOrDying()) return result;
            setState(STATE_CONFIDENT);
            EEMod.scheduleTask((ServerLevel) this.level(), CONFIDENT_TICKS, () -> {
                if (this.isDeadOrDying()) return;
                if (getState() == STATE_CONFIDENT) setState(STATE_NORMAL);
            });
        }

        return result;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        AttributeInstance attribute = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);

        if (source.getEntity() instanceof Player player && !player.isCreative() && getState() != STATE_PANIC && getState() != STATE_LEAP && getState() != STATE_SITTING && !this.level().isClientSide) {
            setState(STATE_PANIC);
            if (attribute != null) attribute.addTransientModifier(PANIC_KNOCKBACK_MODIFIER);
            EEMod.scheduleTask((ServerLevel) this.level(), PANIC_RECOVERY_TICKS, () -> {
                if (this.isDeadOrDying()) return;
                setState(STATE_NORMAL);
                if (attribute != null) attribute.removeModifier(PANIC_KNOCKBACK_MODIFIER_UUID);
            });
        }

        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        data.add(new AnimationController<>(this, "special", 2, this::specialPredicate));
        data.add(new AnimationController<>(this, "leap", state -> PlayState.STOP).triggerableAnim("leap", EEAnimations.POUNCE_LAND));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        if (getState() == STATE_SITTING) return event.setAndContinue(EEAnimations.SIT);

        if (getState() == STATE_SNEAK) {
            return AnimUtils.idleWalkAnimation(event, EEAnimations.SUSPICIOUS, EEAnimations.SNEAKY);
        }
        return AnimUtils.idleWalkAnimation(event);
    }

    private PlayState specialPredicate(AnimationState<?> event) {
        if (getState() == STATE_CONFIDENT) {
            event.getController().setAnimation(EEAnimations.SPRINT_CONFIDENT);
            return PlayState.CONTINUE;
        }

        if (getState() == STATE_PANIC && !AnimUtils.isNotMoving(event)) {
            event.getController().setAnimation(EEAnimations.SPRINT_PANIC);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    public void setState(int state) {
        this.entityData.set(STATE, state);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.WITCH_AMBIENT;
    }

    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.WITCH_DEATH;
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private record GoblinThiefLeapCallbacks(GoblinThiefEntity entity) implements EELeapAttackGoal.ILeapCallbacks {
        @Override
        public void onWindUpStart() {
            entity.setState(STATE_LEAP);
            entity.triggerAnim("leap", "leap");
        }

        @Override
        public void onWindUpEnd() {

        }

        @Override
        public void onLeapStart() {

        }

        @Override
        public void onLeapEnd() {

        }

        @Override
        public void onStop() {
            entity.setState(STATE_NORMAL);
        }
    }
}
