package org.infernalstudios.enemyexp.setup;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.effect.BittenEffect;
import org.infernalstudios.enemyexp.content.effect.BloodBoostEffect;
import org.infernalstudios.enemyexp.core.DeferredObject;
import org.infernalstudios.enemyexp.core.RegisterFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class EEMobEffects {
    static final Map<String, DeferredObject<? extends MobEffect>> effects = new HashMap<>();

    public static final DeferredObject<MobEffect> BLOOD_BOOST = effect("blood_boost", BloodBoostEffect::new);
    public static final DeferredObject<MobEffect> BITTEN = effect("bitten", BittenEffect::new);

    private EEMobEffects() {
    }

    public static void register(RegisterFunction<MobEffect> function) {
        effects.forEach(((id, effect) ->
                function.register(BuiltInRegistries.MOB_EFFECT, EEMod.location(id), effect.get())));
    }

    static <T extends MobEffect> DeferredObject<T> effect(String name, Supplier<T> effectSupplier) {
        DeferredObject<T> ret = new DeferredObject<>(effectSupplier);
        effects.put(name, ret);
        return ret;
    }
}