package org.infernalstudios.enemyexp.core.platform.services;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Consumer;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
     * Creates a {@link MenuType} for the given menu factory, bridging the
     * loader-specific way of constructing menu types that receive extra data.
     *
     * @param factory The factory that creates the menu from the container id,
     *                the player inventory and the extra data buffer.
     * @param <T>     The menu type.
     * @return The created menu type.
     */
    <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory);

    /**
     * Opens a menu for the given player, sending the extra data written by
     * {@code extraDataWriter} alongside the open screen packet.
     *
     * @param player           The player to open the menu for.
     * @param provider         The menu provider.
     * @param extraDataWriter  Writes the extra data the menu needs into the buffer.
     */
    void openMenu(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraDataWriter);

    /**
     * Creates an {@link AbstractContainerMenu} from the container id, the player
     * inventory and the extra data buffer sent when opening the menu.
     */
    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int id, Inventory inventory, FriendlyByteBuf extraData);
    }
}