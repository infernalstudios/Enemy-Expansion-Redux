package com.binaris.enemyexpansion;

import net.minecraft.resources.ResourceLocation;

public class EEMod {
    public static void init() {

    }

    public static ResourceLocation location(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }
}