package org.infernalstudios.enemyexp.core.mixin;

import net.minecraft.server.level.ServerLevel;
import org.infernalstudios.enemyexp.core.ServerLevelRuns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelRuns {
    @Unique
    private static final Map<Integer, Runnable> enemyexp$scheduledTasks = new HashMap<>();

    @Inject(method = "tick", at = @At("TAIL"))
    public void enemyExpansion$tickTask(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        int currentTick = self.getServer().getTickCount();

        for (Integer tick : enemyexp$scheduledTasks.keySet().toArray(new Integer[0])) {
            if (tick <= currentTick) {
                Runnable task = enemyexp$scheduledTasks.remove(tick);
                if (task != null) task.run();
            }
        }
    }

    @Unique
    @Override
    public void enemyExpansion$addServerLevelRun(int tickLimit, Runnable run) {
        ServerLevel self = (ServerLevel) (Object) this;
        int executeAtTick = self.getServer().getTickCount() + tickLimit;
        enemyexp$scheduledTasks.put(executeAtTick, run);
    }
}
