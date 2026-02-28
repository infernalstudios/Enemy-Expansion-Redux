package org.infernalstudios.enemyexp.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.infernalstudios.enemyexp.content.entity.MeatureEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// We can't really use {Entity#setRecordPlayingNearby} because it only happens on client, if we want to know when the
// meature is dancing so we can have special checks on goals, we need to know on the server when the jukebox starts and
// stops playing. So we inject into the jukebox block entity to set the meature's animation to dance when it starts
// playing and set it back to undefined when it stops playing.
@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin extends BlockEntity {

    public JukeboxBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "startPlaying", at = @At("HEAD"))
    public void EnemyExpansion$startPlaying(CallbackInfo ci) {
        if (this.level == null) return;
        double x = this.worldPosition.getX() + 0.5;
        double y = this.worldPosition.getY() + 0.5;
        double z = this.worldPosition.getZ() + 0.5;
        List<MeatureEntity> meatures = EE$getEntitiesWithinRadius(16, x, y, z, this.level, MeatureEntity.class);
        meatures.stream().filter(m -> m.getTarget() == null).forEach(MeatureEntity::setDancingRule);
    }

    @Inject(method = "stopPlaying", at = @At("HEAD"))
    public void EnemyExpansion$stopPlaying(CallbackInfo ci) {
        if (this.level == null) return;
        double x = this.worldPosition.getX() + 0.5;
        double y = this.worldPosition.getY() + 0.5;
        double z = this.worldPosition.getZ() + 0.5;
        List<MeatureEntity> meatures = EE$getEntitiesWithinRadius(16, x, y, z, this.level, MeatureEntity.class);
        meatures.stream().filter(MeatureEntity::isDancing).forEach(MeatureEntity::setIdleRule);
    }

    @Unique
    private static <T extends Entity> List<T> EE$getEntitiesWithinRadius(double radius, double x, double y, double z, Level world, Class<T> entityType) {
        AABB box = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        List<T> entityList = world.getEntitiesOfClass(entityType, box);
        double radiusSq = radius * radius;
        entityList.removeIf(entity -> entity.distanceToSqr(x, y, z) > radiusSq);
        return entityList;
    }
}
