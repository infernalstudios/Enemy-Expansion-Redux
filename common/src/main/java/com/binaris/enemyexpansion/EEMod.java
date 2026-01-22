package com.binaris.enemyexpansion;

import com.binaris.enemyexpansion.core.ServerLevelRuns;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class EEMod {
    public static void init() {

    }

    public static ResourceLocation location(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }

    public static void scheduleTask(ServerLevel level, int time, Runnable runnable) {
        ((ServerLevelRuns) level).addServerLevelRun(time, runnable);
    }
}