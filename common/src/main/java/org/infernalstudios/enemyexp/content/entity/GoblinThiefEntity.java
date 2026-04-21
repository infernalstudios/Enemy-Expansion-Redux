package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
import org.infernalstudios.enemyexp.content.entity.goal.ControlPanicGoal;
import org.infernalstudios.enemyexp.core.util.AnimUtils;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GoblinThiefEntity extends Monster implements GeoEntity {
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PANIC = 1;

    private static final int PANIC_RECOVERY_TICKS = 45;

    private static final EntityDataAccessor<Boolean> IS_SNEAKING = SynchedEntityData.defineId(GoblinThiefEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(GoblinThiefEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GoblinThiefEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ControlPanicGoal(this, 1.4F, () -> this.getState() == STATE_PANIC));
        this.goalSelector.addGoal(2, new ControlAttackGoal(this, 0.8D, true, () -> true){
            @Override
            public void start() {
                super.start();
                setSneaking(true);
            }

            @Override
            public void stop() {
                super.stop();
                setSneaking(false);
            }
        });
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false, false));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STATE, STATE_NORMAL);
        this.entityData.define(IS_SNEAKING, false);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.39D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && !player.isCreative() && getState() != 1 && !this.level().isClientSide) {
            setState(STATE_PANIC);
            EEMod.scheduleTask((ServerLevel) this.level(), PANIC_RECOVERY_TICKS, () -> {
                if (this.isDeadOrDying()) return;
                setState(STATE_NORMAL);
            });
        }

        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        data.add(new AnimationController<>(this, "special", 2, this::specialPredicate));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        if (isSneaking()) {
            return AnimUtils.idleWalkAnimation(event, EEAnimations.SUSPICIOUS, EEAnimations.SNEAKY);
        }
        return AnimUtils.idleWalkAnimation(event);
    }

    private PlayState specialPredicate(AnimationState<?> event) {
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

    public void setSneaking(boolean sneaking) {
        this.entityData.set(IS_SNEAKING, sneaking);
    }

    public boolean isSneaking() {
        return this.entityData.get(IS_SNEAKING);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
