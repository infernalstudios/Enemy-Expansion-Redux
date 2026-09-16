package org.infernalstudios.enemyexp.datagen.provider;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.infernalstudios.enemyexp.EEMod;

import java.util.function.BiConsumer;

public class StarvedTreasureLootTables implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
        consumer.accept(EEMod.location("starved_treasure"),
                LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(20))
                        .add(LootItem.lootTableItem(Items.SAND).setWeight(20))
                        .add(LootItem.lootTableItem(Items.BONE).setWeight(15))
                        .add(LootItem.lootTableItem(Items.BONE_MEAL).setWeight(15))
                        .add(LootItem.lootTableItem(Items.SANDSTONE).setWeight(12))
                        .add(LootItem.lootTableItem(Items.CACTUS).setWeight(10))
                        .add(LootItem.lootTableItem(Items.RAW_COPPER).setWeight(8))
                        .add(LootItem.lootTableItem(Items.RAW_IRON).setWeight(8))
                        .add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(6))
                        .add(LootItem.lootTableItem(Items.RAW_GOLD).setWeight(4))
                        .add(LootItem.lootTableItem(Items.SPYGLASS).setWeight(3))
                        .add(LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(2))
                        .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(2))));
    }
}