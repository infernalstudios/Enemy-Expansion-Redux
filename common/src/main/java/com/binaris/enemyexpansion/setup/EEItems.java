package com.binaris.enemyexpansion.setup;

import com.binaris.enemyexpansion.EEMod;
import com.binaris.enemyexpansion.core.DeferredObject;
import com.binaris.enemyexpansion.core.RegisterFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class EEItems {
    static final Map<String, DeferredObject<? extends Item>> items = new HashMap<>(); // For register function

    public static final DeferredObject<Item> SPRINTER_SPAWN_EGG = spawnEgg("sprinter_spawn_egg", EEntities.SPRINTER.get(), -14269861, -9273797);
    public static final DeferredObject<Item> HAUL_SPAWN_EGG = spawnEgg("haul_spawn_egg", EEntities.SPRINTER.get(), 0x005a4539, 0x00deb289);


    public static void register(RegisterFunction<Item> function) {
        items.forEach(((id, item) ->
                function.register(BuiltInRegistries.ITEM, EEMod.location(id), item.get())));
    }

    /**
     * Creates and saves a spawn egg item for the given entity type, util method that calls the generic item method
     * with default model and tab addition set to true.
     *
     * @param name          The registry name of the item used in registration
     * @param entityType    The entity type this spawn egg will spawn
     * @param primaryColor  The primary color of the spawn egg
     * @param secondaryColor The secondary color of the spawn egg
     * @return The deferred object that holds the spawn egg item instance
     */
    static DeferredObject<Item> spawnEgg(String name, EntityType<? extends Mob> entityType, int primaryColor, int secondaryColor) {
        return item(name, () -> new SpawnEggItem(entityType, primaryColor, secondaryColor, new Item.Properties()
        ), false, true);
    }

    /**
     * Save the item for registration on the different loaders, also giving the option to add the item to the mod's creative
     * tab and data gen the model in case is a simple item (item with just a png as texture/model)
     *
     * @param name         The registry name of the item used in registration
     * @param itemSupplier The supplier that provides the item instance when requested
     * @param defaultModel Whether to data gen a simple item model for this item
     * @param defaultTab   Whether to add this item to the mod's creative tab by default
     * @return The deferred object that holds the item instance
     */
    static <T extends Item> DeferredObject<T> item(String name, Supplier<T> itemSupplier, boolean defaultModel, boolean defaultTab) {
        var ret = new DeferredObject<T>(itemSupplier);
        items.put(name, ret);
        if (defaultTab) EECreativeTabs.addItem(ret);
        if (defaultModel) EEDataGenProcessor.addDefaultItem(name, ret);
        return ret;
    }

    private EEItems() {
        // Prevent instantiation, why would you even want to? :p
    }
}
