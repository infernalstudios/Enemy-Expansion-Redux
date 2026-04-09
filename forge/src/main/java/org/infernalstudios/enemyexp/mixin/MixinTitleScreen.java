package org.infernalstudios.enemyexp.mixin;

import net.minecraft.client.gui.screens.TitleScreen;
import org.infernalstudios.enemyexp.Constants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {

        Constants.LOG.info("Hello from " + Constants.MOD_NAME + "!");
    }
}