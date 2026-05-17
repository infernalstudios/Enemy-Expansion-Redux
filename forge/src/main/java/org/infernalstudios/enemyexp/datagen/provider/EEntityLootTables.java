package org.infernalstudios.enemyexp.datagen.provider;

import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.infernalstudios.enemyexp.core.DeferredObject;
import org.infernalstudios.enemyexp.setup.EEntities;
import org.infernalstudios.enemyexp.setup.EEItems;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class EEntityLootTables extends EntityLootSubProvider {
    public EEntityLootTables() {
        super(FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    public void generate() {
        addDefaultZombieLootTable(EEntities.SPRINTER.get());
        addDefaultZombieLootTable(EEntities.HAUL.get());
        addDefaultZombieLootTable(EEntities.SLUGGER.get());
        addDefaultZombieLootTable(EEntities.FRIGID.get());
        this.add(EEntities.MEATURE.get(), defaultZombieLootTable().withPool(headDropPool(EEItems.MEATHEAD)));
        this.add(EEntities.EQUESTRIAN.get(), defaultZombieLootTable().withPool(headDropPool(EEItems.HORSEHEAD)));
        this.add(EEntities.VAMPIRE.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.GUNPOWDER).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))).apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemKilledByPlayerCondition.killedByPlayer()).when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F))));
        this.add(EEntities.BITER.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.GUNPOWDER).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))).apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemKilledByPlayerCondition.killedByPlayer()).when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F))).withPool(headDropPool(EEItems.HEADBITER)));
        this.add(EEntities.GOBLIN_THIEF.get(), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))).apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))));
    }

    @Override
    protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
        return EEntities.getEntityTypes().values().stream().map(DeferredObject::get);
    }

    private void addDefaultZombieLootTable(EntityType<?> entityType) {
        this.add(entityType, defaultZombieLootTable());
    }

    private LootTable.Builder defaultZombieLootTable() {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.ROTTEN_FLESH).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))).apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.IRON_INGOT)).add(LootItem.lootTableItem(Items.CARROT)).add(LootItem.lootTableItem(Items.POTATO).apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))).when(LootItemKilledByPlayerCondition.killedByPlayer()).when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.025F, 0.01F)));
    }

    private LootPool.Builder headDropPool(DeferredObject<Item> headItem) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(headItem.get())).when(LootItemKilledByPlayerCondition.killedByPlayer()).when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.01F, 0.04F));
    }
}
