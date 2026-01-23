package org.infernalstudios.enemyexpansion.datagen.provider;

import org.infernalstudios.enemyexpansion.Constants;
import org.infernalstudios.enemyexpansion.core.DeferredObject;
import org.infernalstudios.enemyexpansion.setup.EEDataGenProcessor;
import org.infernalstudios.enemyexpansion.setup.EEItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class EEItemModelProvider extends ItemModelProvider {
    public EEItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        EEDataGenProcessor.getDefaultItems().forEach((name, item) -> simpleItem(name));

        spawnEgg(EEItems.SPRINTER_SPAWN_EGG);
        spawnEgg(EEItems.HAUL_SPAWN_EGG);
    }

    private void spawnEgg(DeferredObject<Item> item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.get());
        withExistingParent(id.getPath(), mcLoc("item/template_spawn_egg"));
    }

    private void simpleItem(String name) {
        withExistingParent(name,
                new ResourceLocation("item/generated")).texture(
                "layer0",
                new ResourceLocation(Constants.MOD_ID, "item/" + name)
        );
    }
}
