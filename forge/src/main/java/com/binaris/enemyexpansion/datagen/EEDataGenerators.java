package com.binaris.enemyexpansion.datagen;

import com.binaris.enemyexpansion.Constants;
import com.binaris.enemyexpansion.datagen.provider.EEItemModelProvider;
import com.binaris.enemyexpansion.datagen.provider.EELootTableProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EEDataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), EELootTableProvider.create(packOutput));
        generator.addProvider(event.includeClient(), new EEItemModelProvider(packOutput, existingFileHelper));
    }
}
