package org.infernalstudios.enemyexp;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infernalstudios.enemyexp.core.ServerLevelRuns;

public class EEMod {
    public static final Logger logger = LogManager.getLogger();
    public static void init() {

    }

    public static ResourceLocation location(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }

    public static void scheduleTask(ServerLevel level, int time, Runnable runnable) {
        ((ServerLevelRuns) level).enemyExpansion$addServerLevelRun(time, runnable);
    }
}