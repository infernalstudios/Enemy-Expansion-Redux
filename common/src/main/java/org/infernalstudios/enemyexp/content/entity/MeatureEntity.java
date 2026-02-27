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
import org.infernalstudios.enemyexp.content.entity.goal.ControlLookAtPlayerGoal;
import org.infernalstudios.enemyexp.content.entity.goal.ControlRandomLookAroundGoal;
import org.infernalstudios.enemyexp.content.entity.goal.ControlWaterAvoidingRandomStrollGoal;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class MeatureEntity extends Zombie implements GeoEntity, OwnableEntity {
    /**
     * Helping to see if it's tamed, texture change, behavior change, etc. So instead of just having a field for UUID
     * and a lot of EntityData for each one, we can just have one EntityDataAccessor that holds an Optional<UUID>, if
     * it's empty then it's not tamed, if it has a value then it's tamed and the value is the owner's UUID
     */
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(MeatureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    /**
     * When the Meature kills a mob or feed it with rotten flesh, it will gain age, the age will determine the health
     * modifier, hitbox and size of the Meature
     */
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(MeatureEntity.class, EntityDataSerializers.INT);
    /**
     * (Idle / Walk) don't count in this entity data.
     * <p>
     * Used to sync the current animation being played by the Meature, normally it's "undefined" when no animation is
     * being played, "dance" when the meature is being pet and "leap" when the meature is leaping at a target
     */
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(MeatureEntity.class, EntityDataSerializers.STRING);
    private static final int MAX_AGE = 10;
    private static final int HEALTH_PER_AGE = 2;
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
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new MeatureAttackGoal(this));
        this.goalSelector.addGoal(5, new ControlWaterAvoidingRandomStrollGoal(this, 1.0F, () -> !isDancing()));
        this.goalSelector.addGoal(6, new ControlLookAtPlayerGoal(this, Player.class, 8.0F, () -> !isDancing()));
        this.goalSelector.addGoal(6, new ControlRandomLookAroundGoal(this, () -> !isDancing()));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Player.class));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, e -> !isTamed()));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Zombie.class, true, e -> e.getType().equals(EntityType.ZOMBIE) || e.getType().equals(EntityType.ZOMBIE_VILLAGER) || e.getType().equals(EntityType.HUSK)));
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.is(Items.ROTTEN_FLESH)) {
            if (getOwnerUUID() == null) {
                setOwnerUUID(player.getUUID());
            }
            // Grow the meature when fed
            if (!this.level().isClientSide) {
                grow();
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
                triggerAnim("happy", "happy");
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        // Avoid targeting the owner
        if (getOwnerUUID() != null && getTarget() != null) {
            LivingEntity target = getTarget();
            if (target instanceof Player player && player.getUUID().equals(getOwnerUUID())) {
                setTarget(null);
            }
        }

        // Stop dancing if the meature has a target, we don't want it to dance while trying to attack something
        if (isDancing() && getTarget() != null) {
            setDancing(false);
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
        this.entityData.define(ANIMATION, "undefined");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        data.add(new AnimationController<>(this, "procedure", 2, this::procedurePredicate));
        data.add(new AnimationController<>(this, "happy", state -> PlayState.STOP)
                .triggerableAnim("happy", RawAnimation.begin().thenPlay("happy")));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        if (!this.entityData.get(ANIMATION).equals("undefined")) return PlayState.STOP;
        return AnimUtils.idleWalkAnimation(event, "idle", "walk");
    }

    private PlayState procedurePredicate(AnimationState<?> event) {
        String animation = this.entityData.get(ANIMATION);
        if (!animation.equals("undefined") && event.getController().getAnimationState() == AnimationController.State.STOPPED) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay(animation));
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    private boolean isTamed() {
        return getOwnerUUID() != null;
    }

    public boolean isDancing() {
        return getAnimation().equals("dance");
    }

    public void setDancing(boolean dancing) {
        if (dancing) {
            setAnimation("dance");
        } else if (getAnimation().equals("dance")) {
            setAnimation("undefined");
        }
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

    public String getAnimation() {
        return this.entityData.get(ANIMATION);
    }

    public void setAnimation(String animation) {
        this.entityData.set(ANIMATION, animation);
    }

    static class MeatureAttackGoal extends MeleeAttackGoal {
        public MeatureAttackGoal(MeatureEntity meature) {
            super(meature, 1.0F, true);
        }

        public boolean canUse() {
            return super.canUse() && !this.mob.isVehicle();
        }

        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return 4.0F + attackTarget.getBbWidth();
        }
    }
}
