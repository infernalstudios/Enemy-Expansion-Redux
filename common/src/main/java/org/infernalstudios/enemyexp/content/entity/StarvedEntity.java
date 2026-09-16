package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.EEAnimations;
import org.infernalstudios.enemyexp.content.menu.StarvedGUIMenu;
import org.infernalstudios.enemyexp.core.platform.Services;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

public class StarvedEntity extends Monster implements GeoEntity, OwnableEntity {
    public static final int TAME_FOOD_AMOUNT = 5;

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(StarvedEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> TAME_PROGRESS = SynchedEntityData.defineId(StarvedEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ORDERED_TO_SIT = SynchedEntityData.defineId(StarvedEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MOUTH_OPEN = SynchedEntityData.defineId(StarvedEntity.class, EntityDataSerializers.BOOLEAN);

    private static final float HARD_MODE_DAMAGE = 10.0F;
    private static final double WAKE_CHASE_RANGE_SQR = 144.0D;

    public static final SoundEvent STARVED_CHOMP = SoundEvent.createVariableRangeEvent(EEMod.location("starved.chomp"));

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** The single inventory slot inside the starved's mouth, holding up to a full stack. */
    private final SimpleContainer stomach = new SimpleContainer(1);

    private boolean takenFromMouth = false;

    public StarvedEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.36D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new StarvedMeleeAttackGoal(this, () -> !isOrderedToSit()));
        this.goalSelector.addGoal(3, new StarvedFollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(4, new StarvedSitGoal(this));

        this.targetSelector.addGoal(1, new StarvedHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new StarvedOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new StarvedOwnerHurtTargetGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(TAME_PROGRESS, 0);
        this.entityData.define(ORDERED_TO_SIT, false);
        this.entityData.define(MOUTH_OPEN, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Stomach", this.stomach.createTag());
        if (getOwnerUUID() != null) {
            tag.putUUID("Owner", getOwnerUUID());
        }
        tag.putInt("TameProgress", getTameProgress());
        tag.putBoolean("Sitting", isOrderedToSit());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Stomach")) {
            this.stomach.fromTag(tag.getList("Stomach", Tag.TAG_COMPOUND));
        }
        if (tag.hasUUID("Owner")) {
            setOwnerUUID(tag.getUUID("Owner"));
        }
        setTameProgress(tag.getInt("TameProgress"));
        setOrderedToSit(tag.getBoolean("Sitting"));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData ret = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (difficulty.getDifficulty() == Difficulty.HARD) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(HARD_MODE_DAMAGE);
        }
        fillStomach();
        return ret;
    }

    private void fillStomach() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        LootTable table = serverLevel.getServer().getLootData().getLootTable(EEMod.location("starved_treasure"));
        LootParams params = new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY);
        List<ItemStack> items = table.getRandomItems(params, this.random.nextLong());
        if (!items.isEmpty()) {
            this.stomach.setItem(0, items.get(0));
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof AbstractArrow) {
            return false;
        }
        if (source == this.damageSources().fall() || source == this.damageSources().starve()) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        // Waking up: the jaw snaps shut when the fossil is attacked.
        if (hurt && !this.level().isClientSide && !isTamed() && !isMouthOpen()) {
            triggerAnim("mouth", "openclose");
            playChompSound(1.0F);
        }
        return hurt;
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.COOKED_RABBIT) && !isTamed()) {
            if (!this.level().isClientSide) {
                int progress = getTameProgress() + 1;
                if (progress >= TAME_FOOD_AMOUNT) {
                    tame(player);
                } else {
                    setTameProgress(progress);
                }
                stack.shrink(1);
            }
            this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (isTamed() && player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                setOrderedToSit(!isOrderedToSit());
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            openMouthMenu(serverPlayer);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private void openMouthMenu(ServerPlayer player) {
        Services.PLATFORM.openMenu(player, new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Component.translatable("container.enemyexp.starved");
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
                return new StarvedGUIMenu(id, inventory, StarvedEntity.this);
            }
        }, buf -> buf.writeInt(StarvedEntity.this.getId()));

        this.entityData.set(MOUTH_OPEN, true);
        this.takenFromMouth = false;
        triggerAnim("mouth", "open");
        playChompSound(2.0F);
    }

    public void onItemTaken(Player player) {
        if (this.level().isClientSide) return;
        this.takenFromMouth = true;
        grantAdvancement(player, EEMod.location("starved_steal"));
        playChompSound(1.0F);
    }

    public void onMenuClosed(Player player, boolean taken) {
        if (this.level().isClientSide) return;
        this.entityData.set(MOUTH_OPEN, false);
        if (!isTamed() && taken) {
            wakeUp(player);
        } else {
            triggerAnim("mouth", "close");
            playChompSound(1.0F);
        }
    }

    public void wakeUp(Player player) {
        if (this.level().isClientSide) return;
        triggerAnim("mouth", "close");
//        triggerAnim("mouth", "openclose");
        playChompSound(1.0F);
        this.setTarget(player);
    }

    private void tame(Player player) {
        setOwnerUUID(player.getUUID());
        setTameProgress(0);
        setTarget(null);
        for (int i = 0; i < 4; ++i) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
        }
        grantAdvancement(player, new ResourceLocation("minecraft:husbandry/tame_an_animal"));
    }

    private void grantAdvancement(Player player, ResourceLocation location) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(location);
        if (advancement == null) return;
        AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) return;
        for (String s : progress.getRemainingCriteria()) {
            serverPlayer.getAdvancements().award(advancement, s);
        }
    }

    private void playChompSound(float pitch) {
        this.level().playSound(null, this.blockPosition(), STARVED_CHOMP, SoundSource.HOSTILE, 1.0F, pitch);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        if (isOrderedToSit()) {
            this.getNavigation().stop();
            this.setTarget(null);
        }

        if (getTarget() instanceof Player player && player.getUUID().equals(getOwnerUUID())) {
            this.setTarget(null);
        }

        if (!isTamed() && getTarget() instanceof Player player && this.distanceToSqr(player) > WAKE_CHASE_RANGE_SQR) {
            this.setTarget(null);
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (!this.stomach.isEmpty()) {
            this.spawnAtLocation(this.stomach.removeItemNoUpdate(0));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "movement", 2, this::movementPredicate));
        data.add(new AnimationController<>(this, "mouth", state -> PlayState.STOP)
                .triggerableAnim("open", EEAnimations.STARVED_OPEN)
                .triggerableAnim("close", EEAnimations.STARVED_CLOSE)
                .triggerableAnim("openclose", EEAnimations.STARVED_OPEN_CLOSE));
    }

    private PlayState movementPredicate(AnimationState<?> event) {
        if (isMouthOpen()) return PlayState.STOP;
        return AnimUtils.idleWalkAnimation(event);
    }

    @Override
    public @NotNull MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull net.minecraft.world.level.block.state.BlockState state) {
        this.playSound(STARVED_CHOMP, 0.15F, 1.0F);
    }

    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
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

    public boolean isTamed() {
        return getOwnerUUID() != null;
    }

    public int getTameProgress() {
        return this.entityData.get(TAME_PROGRESS);
    }

    public void setTameProgress(int progress) {
        this.entityData.set(TAME_PROGRESS, progress);
    }

    public boolean isOrderedToSit() {
        return this.entityData.get(ORDERED_TO_SIT);
    }

    public void setOrderedToSit(boolean sitting) {
        this.entityData.set(ORDERED_TO_SIT, sitting);
    }

    public boolean isMouthOpen() {
        return this.entityData.get(MOUTH_OPEN);
    }

    public SimpleContainer getStomach() {
        return this.stomach;
    }

    public boolean isTakenFromMouth() {
        return this.takenFromMouth;
    }

    private boolean wantsToAttack(@Nullable LivingEntity target, @Nullable LivingEntity owner) {
        if (target == null || target == this || owner == null) return false;
        if (target instanceof Player player && player.getUUID().equals(getOwnerUUID())) return false;
        return !target.isAlliedTo(owner);
    }

    static class StarvedMeleeAttackGoal extends MeleeAttackGoal {
        private final BooleanSupplier shouldExecutePredicate;

        public StarvedMeleeAttackGoal(StarvedEntity starved, BooleanSupplier shouldExecutePredicate) {
            super(starved, 1.2D, true);
            this.shouldExecutePredicate = shouldExecutePredicate;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.shouldExecutePredicate.getAsBoolean();
        }

        @Override
        protected double getAttackReachSqr(@NotNull LivingEntity attackTarget) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() + attackTarget.getBbWidth();
        }
    }

    static class StarvedHurtByTargetGoal extends HurtByTargetGoal {
        public StarvedHurtByTargetGoal(StarvedEntity starved) {
            super(starved, Player.class);
        }

        @Override
        public boolean canUse() {
            return !((StarvedEntity) this.mob).isOrderedToSit() && super.canUse();
        }
    }

    static class StarvedSitGoal extends Goal {
        private final StarvedEntity starved;

        public StarvedSitGoal(StarvedEntity starved) {
            this.starved = starved;
        }

        @Override
        public boolean canUse() {
            return this.starved.isTamed() && this.starved.isOrderedToSit() && !this.starved.isPassenger();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            this.starved.getNavigation().stop();
            this.starved.setTarget(null);
        }
    }

    static class StarvedFollowOwnerGoal extends Goal {
        private final StarvedEntity starved;
        private final double speedModifier;
        private final float startDistance;
        private final float stopDistance;
        private int timeToRecalcPath;

        public StarvedFollowOwnerGoal(StarvedEntity starved, double speedModifier, float startDistance, float stopDistance) {
            this.starved = starved;
            this.speedModifier = speedModifier;
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
        }

        @Override
        public boolean canUse() {
            if (!this.starved.isTamed() || this.starved.isOrderedToSit()) return false;
            LivingEntity owner = this.starved.getOwner();
            if (owner == null) return false;
            if (owner.isSpectator()) return false;
            return this.starved.distanceToSqr(owner) >= (double) (this.startDistance * this.startDistance);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.starved.isOrderedToSit()) return false;
            LivingEntity owner = this.starved.getOwner();
            if (owner == null) return false;
            return this.starved.distanceToSqr(owner) > (double) (this.stopDistance * this.stopDistance);
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            this.starved.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity owner = this.starved.getOwner();
            if (owner == null) return;
            this.starved.getLookControl().setLookAt(owner, 10.0F, this.starved.getMaxHeadXRot());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (this.starved.distanceToSqr(owner) >= 144.0D) {
                    this.teleportToOwner(owner);
                } else {
                    this.starved.getNavigation().moveTo(owner, this.speedModifier);
                }
            }
        }

        private void teleportToOwner(LivingEntity owner) {
            BlockPos blockpos = owner.blockPosition();
            for (int i = 0; i < 10; ++i) {
                int dx = this.randomIntInclusive(-3, 3);
                int dy = this.randomIntInclusive(-1, 1);
                int dz = this.randomIntInclusive(-3, 3);
                if (this.teleportTo(blockpos.getX() + dx, blockpos.getY() + dy, blockpos.getZ() + dz)) {
                    return;
                }
            }
        }

        private boolean teleportTo(int x, int y, int z) {
            if (Math.abs((double) x - this.starved.getX()) < 2.0D && Math.abs((double) z - this.starved.getZ()) < 2.0D) {
                return false;
            }
            if (!this.starved.level().hasChunkAt(new BlockPos(x, y, z))) {
                return false;
            }
            this.starved.teleportTo(x + 0.5D, y, z + 0.5D);
            this.starved.getNavigation().stop();
            return true;
        }

        private int randomIntInclusive(int min, int max) {
            return this.starved.getRandom().nextInt(max - min + 1) + min;
        }
    }

    static class StarvedOwnerHurtByTargetGoal extends Goal {
        private final StarvedEntity starved;

        public StarvedOwnerHurtByTargetGoal(StarvedEntity starved) {
            this.starved = starved;
        }

        @Override
        public boolean canUse() {
            if (!this.starved.isTamed() || this.starved.isOrderedToSit()) return false;
            LivingEntity owner = this.starved.getOwner();
            if (owner == null) return false;
            LivingEntity attacker = owner.getLastHurtByMob();
            if (attacker == null) return false;
            return this.starved.wantsToAttack(attacker, owner);
        }

        @Override
        public void start() {
            LivingEntity owner = this.starved.getOwner();
            if (owner != null) {
                this.starved.setTarget(owner.getLastHurtByMob());
            }
            super.start();
        }
    }

    static class StarvedOwnerHurtTargetGoal extends Goal {
        private final StarvedEntity starved;

        public StarvedOwnerHurtTargetGoal(StarvedEntity starved) {
            this.starved = starved;
        }

        @Override
        public boolean canUse() {
            if (!this.starved.isTamed() || this.starved.isOrderedToSit()) return false;
            LivingEntity owner = this.starved.getOwner();
            if (owner == null) return false;
            LivingEntity hurtMob = owner.getLastHurtMob();
            if (hurtMob == null) return false;
            return this.starved.wantsToAttack(hurtMob, owner);
        }

        @Override
        public void start() {
            LivingEntity owner = this.starved.getOwner();
            if (owner != null) {
                this.starved.setTarget(owner.getLastHurtMob());
            }
            super.start();
        }
    }
}