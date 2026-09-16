package org.infernalstudios.enemyexp.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.menu.StarvedGUIMenu;
import org.infernalstudios.enemyexp.core.DeferredObject;
import org.infernalstudios.enemyexp.core.RegisterFunction;
import org.infernalstudios.enemyexp.core.platform.Services;
import org.infernalstudios.enemyexp.core.platform.services.IPlatformHelper;

import java.util.HashMap;
import java.util.Map;

public final class EEMenus {
    static final Map<String, DeferredObject<? extends MenuType<?>>> menus = new HashMap<>();

    public static final DeferredObject<MenuType<StarvedGUIMenu>> STARVED_GUI = menu("starved_gui", StarvedGUIMenu::new);

    private EEMenus() {
    }

    public static void register(RegisterFunction<MenuType<?>> function) {
        menus.forEach((id, menu) -> function.register(BuiltInRegistries.MENU, EEMod.location(id), menu.get()));
    }

    static <T extends AbstractContainerMenu> DeferredObject<MenuType<T>> menu(String name, IPlatformHelper.MenuFactory<T> factory) {
        DeferredObject<MenuType<T>> ret = new DeferredObject<>(() -> Services.PLATFORM.createMenuType(factory));
        menus.put(name, (DeferredObject<? extends MenuType<?>>) (Object) ret);
        return ret;
    }
}