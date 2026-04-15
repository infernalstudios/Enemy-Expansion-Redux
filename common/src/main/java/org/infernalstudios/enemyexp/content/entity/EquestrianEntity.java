package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.content.entity.goal.HardLookAtTargetGoal;
import org.infernalstudios.enemyexp.content.entity.goal.RangedKitingGoal;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EquestrianEntity extends Zombie implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final int PANIC_TIME_TICKS = 60;

    /**
     * 0 - normal
     * 1 - panic
     * 2 - kiting
     */
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(EquestrianEntity.class, EntityDataSerializers.INT);

    public EquestrianEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new EquestrianPanicGoal(this, 1.4F));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new EquestrianRangedKitingGoal(this));
        this.goalSelector.addGoal(3, new HardLookAtTargetGoal(this, 10.0F, 10.0F));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void handleAttributes(float difficulty) {
        // This makes it have the same values as the Leader Zombies
        this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * (double) 0.25F + (double) 0.5F, AttributeModifier.Operation.ADDITION));
        this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * (double) 3.0F + (double) 1.0F, AttributeModifier.Operation.MULTIPLY_TOTAL));
        this.setCanBreakDoors(this.supportsBreakDoorGoal());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && !player.getAbilities().instabuild && !this.level().isClientSide && this.getState() != 1) {
            setState(1);
            EEMod.scheduleTask((ServerLevel) this.level(), PANIC_TIME_TICKS, () -> {
                if (this.isDeadOrDying()) return;
                ServerLevel serverLevel = (ServerLevel) this.level();
                setState(0);
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 1, this.getZ(), 5, 1.0D, 1.0D, 1.0D, 0.6);
            });
        }
        return super.hurt(source, amount);
    }

    public void setState(int state) {
        this.entityData.set(STATE, state);
    }

    public int getState() {
        return this.entityData.get(STATE);
    }

    public boolean isInSpecialState() {
        return this.entityData.get(STATE) != 0;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        data.add(new AnimationController<>(this, "special", 2, this::specialPredicate));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        if (getState() == 2) {
            event.getController().setAnimation(EEAnimations.TROT);
            return PlayState.CONTINUE;
        }
        if (getState() != 0) return PlayState.STOP;
        return AnimUtils.idleWalkAnimation(event);
    }

    private PlayState specialPredicate(AnimationState<?> event) {
        if (getState() == 1 && !AnimUtils.isNotMoving(event)) {
            event.getController().setAnimation(EEAnimations.PANICKED);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static class EquestrianPanicGoal extends PanicGoal {
        public EquestrianPanicGoal(EquestrianEntity mob, double speedModifier) {
            super(mob, speedModifier);
        }

        @Override
        protected boolean shouldPanic() {
            if (mob instanceof EquestrianEntity equestrian) {
                return equestrian.getState() == 1;
            }
            return false;
        }
    }

    public static class EquestrianRangedKitingGoal extends RangedKitingGoal {
        EquestrianEntity equestrian;

        public EquestrianRangedKitingGoal(EquestrianEntity mob) {
            super(mob, 0.6D);
            this.equestrian = mob;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !equestrian.isInSpecialState();
        }
    }
}
