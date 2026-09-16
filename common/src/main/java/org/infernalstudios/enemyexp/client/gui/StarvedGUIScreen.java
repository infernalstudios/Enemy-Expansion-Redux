package org.infernalstudios.enemyexp.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.menu.StarvedGUIMenu;

public class StarvedGUIScreen extends AbstractContainerScreen<StarvedGUIMenu> {
    private static final ResourceLocation TEXTURE = EEMod.location("textures/gui/starved_menu.png");

    public StarvedGUIScreen(StarvedGUIMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // The custom background already depicts the mouth; no text is needed.
    }
}