package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.setup.EEntities;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BiterEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BiterEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FLYING_SPEED, 0.6D);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.5D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0D));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if (hurt && entity instanceof LivingEntity && !this.level().isClientSide) {
            VampireEntity vampire = EEntities.VAMPIRE.get().create(this.level());
            if (vampire != null) {
                vampire.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                vampire.setAwake(true);
                this.level().addFreshEntity(vampire);
                this.discard();
            }
        }
        return hurt;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));

        data.add(new AnimationController<>(this, "wings", 2, state -> state.setAndContinue(EEAnimations.BITER_WINGS_FLAPPING)));

        data.add(new AnimationController<>(this, "death", 0, state -> PlayState.STOP)
                .triggerableAnim("die", EEAnimations.BITER_DIE));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        if (event.isMoving()) {
            return event.setAndContinue(EEAnimations.BITER_FORWARD);
        }
        return event.setAndContinue(EEAnimations.BITER_WOBBLING);
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        super.die(cause);
        if (!this.level().isClientSide) {
            triggerAnim("death", "die");
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}