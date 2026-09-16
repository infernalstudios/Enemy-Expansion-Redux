package org.infernalstudios.enemyexp.content.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.enemyexp.content.entity.StarvedEntity;
import org.infernalstudios.enemyexp.setup.EEMenus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StarvedGUIMenu extends AbstractContainerMenu {
    private static final int STARVED_SLOT_X = 78;
    private static final int STARVED_SLOT_Y = 35;

    @Nullable
    private final StarvedEntity starved;

    public StarvedGUIMenu(int id, Inventory inventory, FriendlyByteBuf extraData) {
        this(id, inventory, resolve(inventory, extraData.readInt()));
    }

    public StarvedGUIMenu(int id, Inventory inventory, @Nullable StarvedEntity starved) {
        super(EEMenus.STARVED_GUI.get(), id);
        this.starved = starved;

        Container stomach = starved != null ? starved.getStomach() : new SimpleContainer(1);
        this.addSlot(new Slot(stomach, 0, STARVED_SLOT_X, STARVED_SLOT_Y) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return StarvedGUIMenu.this.starved != null && StarvedGUIMenu.this.starved.isTamed();
            }

            @Override
            public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                super.onTake(player, stack);
                if (StarvedGUIMenu.this.starved != null) {
                    StarvedGUIMenu.this.starved.onItemTaken(player);
                }
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    @Nullable
    private static StarvedEntity resolve(Inventory inventory, int entityId) {
        Entity entity = inventory.player.level().getEntity(entityId);
        if (entity instanceof StarvedEntity starved) {
            return starved;
        }
        return null;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.starved != null && this.starved.isAlive() && this.starved.distanceToSqr(player) < 64.0D;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (this.starved != null && !player.level().isClientSide) {
            this.starved.onMenuClosed(player, this.starved.isTakenFromMouth());
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack = slot.getItem();
            ret = itemstack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack, 0, 1, false)) {
                if (index < 28) {
                    if (!this.moveItemStackTo(itemstack, 28, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (itemstack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack.getCount() == ret.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack);
        }
        return ret;
    }
}