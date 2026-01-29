package org.infernalstudios.enemyexp.setup;

import net.minecraft.world.item.Item;
import org.infernalstudios.enemyexp.core.DeferredObject;

import java.util.HashSet;
import java.util.Set;

public final class EECreativeTabs {
    private static final Set<DeferredObject<? extends Item>> GENERAL_ITEMS = new HashSet<>();

    private static final Set<DeferredObject<? extends Item>> SPAWN_EGGS = new HashSet<>();

    private EECreativeTabs() {
    }

    public static void addItem(DeferredObject<? extends Item> item) {
        GENERAL_ITEMS.add(item);
    }

    public static void addSpawnEgg(DeferredObject<? extends Item> item) {
        SPAWN_EGGS.add(item);
    }

    public static Set<DeferredObject<? extends Item>> getGeneralItems() {
        return GENERAL_ITEMS;
    }

    public static Set<DeferredObject<? extends Item>> getSpawnEggs() {
        return SPAWN_EGGS;
    }
}
