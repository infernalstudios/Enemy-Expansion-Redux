package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VampirePathNavigation extends GroundPathNavigation {
    private final FlyingPathNavigation flyingNavigation;

    public VampirePathNavigation(VampireEntity mob, Level level) {
        super(mob, level);
        this.flyingNavigation = new FlyingPathNavigation(mob, level);
    }

    private boolean isFlying() {
        return ((VampireEntity) this.mob).isAerial();
    }

    public void switchMode() {
        this.stop();
        this.flyingNavigation.stop();
    }

    @Override
    public @NotNull Path createPath(BlockPos pos, int accuracy) {
        return isFlying() ? this.flyingNavigation.createPath(pos, accuracy) : super.createPath(pos, accuracy);
    }

    @Override
    public @NotNull Path createPath(Entity entity, int accuracy) {
        return isFlying() ? this.flyingNavigation.createPath(entity, accuracy) : super.createPath(entity, accuracy);
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        return isFlying() ? this.flyingNavigation.moveTo(x, y, z, speed) : super.moveTo(x, y, z, speed);
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        return isFlying() ? this.flyingNavigation.moveTo(entity, speed) : super.moveTo(entity, speed);
    }

    @Override
    public boolean moveTo(@Nullable Path path, double speed) {
        return isFlying() ? this.flyingNavigation.moveTo(path, speed) : super.moveTo(path, speed);
    }

    @Override
    public void tick() {
        if (isFlying()) {
            this.flyingNavigation.tick();
        } else {
            super.tick();
        }
    }

    @Override
    public void stop() {
        super.stop();
        this.flyingNavigation.stop();
    }

    @Override
    public void setSpeedModifier(double speed) {
        super.setSpeedModifier(speed);
        this.flyingNavigation.setSpeedModifier(speed);
    }

    @Override
    public boolean isDone() {
        return isFlying() ? this.flyingNavigation.isDone() : super.isDone();
    }

    @Override
    public boolean isInProgress() {
        return isFlying() ? this.flyingNavigation.isInProgress() : super.isInProgress();
    }

    @Nullable
    @Override
    public Path getPath() {
        return isFlying() ? this.flyingNavigation.getPath() : super.getPath();
    }

    @Override
    public boolean isStuck() {
        return isFlying() ? this.flyingNavigation.isStuck() : super.isStuck();
    }
}
