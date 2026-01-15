package com.binaris.enemyexpansion.setup;

import com.binaris.enemyexpansion.core.DeferredObject;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

public final class EECreativeTabs {
    static final Set<DeferredObject<? extends Item>> GENERAL_ITEMS = new HashSet<>();

    public static void addItem(DeferredObject<? extends Item> item) {
        GENERAL_ITEMS.add(item);
    }

    private EECreativeTabs() {
    }
}
