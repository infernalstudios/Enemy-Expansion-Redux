package org.infernalstudios.enemyexp.datagen.provider;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

public final class EELootTableProvider {
    private EELootTableProvider() {
    }

    public static LootTableProvider create(PackOutput output) {
        return new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(EEntityLootTables::new, LootContextParamSets.ENTITY)

                ));
    }
}
