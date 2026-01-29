package org.infernalstudios.enemyexp.datagen.provider;

import org.infernalstudios.enemyexp.Constants;
import org.infernalstudios.enemyexp.core.DeferredObject;
import org.infernalstudios.enemyexp.setup.EECreativeTabs;
import org.infernalstudios.enemyexp.setup.EEDataGenProcessor;
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

        EECreativeTabs.getSpawnEggs().forEach(item -> spawnEgg((DeferredObject<Item>) item));
    }

    private void spawnEgg(DeferredObject<Item> item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item.get());
        withExistingParent(id.getPath(), mcLoc("item/template_spawn_egg"));
    }

    private void simpleItem(String name) {
        withExistingParent(name,
                ResourceLocation.withDefaultNamespace("item/generated")).texture(
                "layer0",
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/" + name)
        );
    }
}
