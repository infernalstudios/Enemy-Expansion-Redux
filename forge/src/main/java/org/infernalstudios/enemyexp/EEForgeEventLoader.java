package org.infernalstudios.enemyexp;

import org.infernalstudios.enemyexp.core.RegisterFunction;
import org.infernalstudios.enemyexp.setup.EEItems;
import org.infernalstudios.enemyexp.setup.EEntities;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Consumer;

public class EEForgeEventLoader {

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {

    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            if (event.getRegistryKey() == Registries.ENTITY_TYPE)
                register(event, EEntities::register);
            else if (event.getRegistryKey() == Registries.ITEM)
                register(event, EEItems::register);

        }

        private static <T> void register(RegisterEvent event, Consumer<RegisterFunction<T>> consumer) {
            consumer.accept((registry, id, value) -> event.register(registry.key(), id, () -> value));
        }

        @SubscribeEvent
        public static void createEntityAttributes(EntityAttributeCreationEvent event) {
            EEntities.registerAttributes(event::put);
        }

    }
}
