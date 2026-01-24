package org.infernalstudios.enemyexp.setup;

import net.minecraft.world.item.Item;
import org.infernalstudios.enemyexp.core.DeferredObject;

import java.util.HashSet;
import java.util.Set;

public final class EECreativeTabs {
    static final Set<DeferredObject<? extends Item>> GENERAL_ITEMS = new HashSet<>();

    private EECreativeTabs() {
    }

    public static void addItem(DeferredObject<? extends Item> item) {
        GENERAL_ITEMS.add(item);
    }
}
