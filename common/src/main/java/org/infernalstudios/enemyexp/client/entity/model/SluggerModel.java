package org.infernalstudios.enemyexp.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.SluggerEntity;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SluggerModel extends DefaultedEntityGeoModel<SluggerEntity> {
    public SluggerModel() {
        super(EEMod.location("slugger"), true);
    }

    @Override
    public ResourceLocation getTextureResource(SluggerEntity animatable) {
        String texture;
        if (animatable.getState() == SluggerEntity.STATE_CHARGING) {
            texture = "slugger_charge";
        } else if (animatable.getState() == SluggerEntity.STATE_DASHING) {
            texture = "slugger_dashing";
        } else {
            texture = "slugger";
        }

        return EEMod.location("textures/entity/" + texture + ".png");
    }

    @Override
    public void setCustomAnimations(SluggerEntity animatable, long instanceId, AnimationState<SluggerEntity> animationState) {
        if (!animatable.isCharging()) {
            super.setCustomAnimations(animatable, instanceId, animationState);
        }
    }
}