package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.content.entity.goal.*;
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

import java.util.Optional;
import java.util.UUID;

public class MeatureEntity extends Zombie implements GeoEntity, OwnableEntity {
    /** Owner UUID, saved for handling when the meature is tamed. Empty means untamed. */
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(MeatureEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    /** Age tracks growth, feeding rotten flesh increases it up to MAX_AGE, scaling max health and hitbox size. */
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(MeatureEntity.class, EntityDataSerializers.INT);

    /** Controls which animation/behavior override is active. Values: "undefined" (idle/walk), "dance", "happy", "leap" */
    private static final EntityDataAccessor<String> MOVE_RULE = SynchedEntityData.defineId(MeatureEntity.class, EntityDataSerializers.STRING);

    private static final int MAX_AGE = 10;
    private static final int HEALTH_PER_AGE = 2;
    private static final double MAX_TRIGGER_DISTANCE = 8.0D;
    private static final double MIN_TRIGGER_DISTANCE = 3.0D;
    private static final int COOLDOWN_TICKS = 5;
    private static final int WINDUP_ENDS = 2;
    private static final int ANIM_TOTAL_TICKS = 30;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MeatureEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new EELeapAttackGoal<>(this, new MeatureLeapCallbacks(this), MIN_TRIGGER_DISTANCE, MAX_TRIGGER_DISTANCE, COOLDOWN_TICKS, WINDUP_ENDS, ANIM_TOTAL_TICKS));
        this.goalSelector.addGoal(4, new MeatureAttackGoal(this));
        this.goalSelector.addGoal(5, new ControlWaterAvoidingRandomStrollGoal(this, 1.0F, () -> !isInSpecial()));
        this.goalSelector.addGoal(6, new ControlLookAtPlayerGoal(this, Player.class, 8.0F, () -> !isDancing() || !isHappy()));
        this.goalSelector.addGoal(6, new ControlRandomLookAroundGoal(this, () -> !isInSpecial()));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Player.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, e -> !isTamed()));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Zombie.class,
                true, e -> e.getType().equals(EntityType.ZOMBIE) || e.getType().equals(EntityType.ZOMBIE_VILLAGER) || e.getType().equals(EntityType.HUSK)));
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.is(Items.ROTTEN_FLESH)) {
            if (getOwnerUUID() == null) setOwnerUUID(player.getUUID());
            if (!this.level().isClientSide) {
                grow();
                if (!player.getAbilities().instabuild) itemstack.shrink(1);
                triggerAnim("happy", "happy");
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        // Avoid targeting the owner.
        if (getOwnerUUID() != null && getTarget() != null) {
            LivingEntity target = getTarget();
            if (target instanceof Player player && player.getUUID().equals(getOwnerUUID())) {
                setTarget(null);
            }
        }

        // Cancel dancing if a target is found.
        if (isDancing() && getTarget() != null) {
            setIdleRule();
        }

        // Cancel leaping state when back on the ground and the leap goal is no longer active.
        // (The goal itself calls setIdleRule on stop(), but this is a safety net for edge cases.)
        if (isLeaping() && onGround()) {
            setIdleRule();
        }
    }

    private void grow() {
        int currentAge = this.entityData.get(AGE);
        if (currentAge < MAX_AGE) {
            setAge(currentAge + 1);
            double newMaxHealth = 10.0D + (currentAge + 1) * HEALTH_PER_AGE;
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
            this.setHealth((float) newMaxHealth);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(AGE, 0);
        this.entityData.define(MOVE_RULE, "undefined");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        data.add(new AnimationController<>(this, "procedure", 2, this::dancePredicate));
        data.add(new AnimationController<>(this, "leap", state -> PlayState.STOP).triggerableAnim("leap", EEAnimations.LEAP));
        data.add(new AnimationController<>(this, "happy", state -> PlayState.STOP).triggerableAnim("happy", EEAnimations.HAPPY));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        // Suppress idle/walk while any special animation is playing.
        if (!this.entityData.get(MOVE_RULE).equals("undefined")) return PlayState.STOP;
        return AnimUtils.idleWalkAnimation(event);
    }

    private PlayState dancePredicate(AnimationState<?> event) {
        if (isDancing()) {
            event.getController().setAnimation(EEAnimations.DANCE);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private boolean isTamed() {
        return getOwnerUUID() != null;
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

    @Override
    public @Nullable UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public int getAge() {
        return this.entityData.get(AGE);
    }

    public void setAge(int age) {
        this.entityData.set(AGE, age);
    }

    public String getMoveRule() {
        return this.entityData.get(MOVE_RULE);
    }

    public void setMoveRule(String rule) {
        this.entityData.set(MOVE_RULE, rule);
    }

    public void setDancingRule() {
        setMoveRule("dance");
    }

    public void setIdleRule() {
        setMoveRule("undefined");
    }

    public void setHappyRule() {
        setMoveRule("happy");
    }

    public void setLeapRule() {
        setMoveRule("leap");
    }

    public boolean isInSpecial() {
        return isHappy() || isDancing() || isLeaping();
    }

    public boolean isHappy() {
        return getMoveRule().equals("happy");
    }

    public boolean isIdle() {
        return getMoveRule().equals("undefined");
    }

    public boolean isDancing() {
        return getMoveRule().equals("dance");
    }

    public boolean isLeaping() {
        return getMoveRule().equals("leap");
    }

    static class MeatureAttackGoal extends MeleeAttackGoal {
        public MeatureAttackGoal(MeatureEntity meature) {
            super(meature, 1.0F, true);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.mob.isVehicle() && !((MeatureEntity) this.mob).isLeaping();
        }

        @Override
        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return 4.0F + attackTarget.getBbWidth();
        }
    }

    private record MeatureLeapCallbacks(MeatureEntity meature) implements EELeapAttackGoal.ILeapCallbacks {

        @Override
        public void onWindUpStart() {
            meature.setLeapRule();
            meature.triggerAnim("leap", "leap");
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
            meature.setIdleRule();
        }
    }
}