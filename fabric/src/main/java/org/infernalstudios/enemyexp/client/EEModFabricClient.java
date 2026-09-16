package org.infernalstudios.enemyexp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import org.infernalstudios.enemyexp.client.gui.StarvedGUIScreen;
import org.infernalstudios.enemyexp.client.render.HeadBiterRenderer;
import org.infernalstudios.enemyexp.client.render.HorseHeadRenderer;
import org.infernalstudios.enemyexp.client.render.MeatHeadRenderer;
import org.infernalstudios.enemyexp.setup.EEItems;
import org.infernalstudios.enemyexp.setup.EEMenus;
import org.infernalstudios.enemyexp.setup.client.EERenderers;

public class EEModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EERenderers.registerRenderers();
        EERenderers.getRenderers().forEach((entity, renderer) -> EntityRendererRegistry.register(entity.get(), (EntityRendererProvider<Entity>) renderer));
        EERenderers.registerEntityLayers((layer, supplier) -> EntityModelLayerRegistry.registerModelLayer(layer, supplier::get));
        ScreenRegistry.register(EEMenus.STARVED_GUI.get(), StarvedGUIScreen::new);

        ArmorRenderer.register(new MeatHeadRenderer(), EEItems.MEATHEAD.get());
        ArmorRenderer.register(new HorseHeadRenderer(), EEItems.HORSEHEAD.get());
        ArmorRenderer.register(new HeadBiterRenderer(), EEItems.HEADBITER.get());
    }
}