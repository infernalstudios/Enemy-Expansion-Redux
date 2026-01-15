package com.binaris.enemyexpansion.client.model;

import com.binaris.enemyexpansion.EEMod;
import com.binaris.enemyexpansion.content.entity.SprinterEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SprinterModel extends DefaultedEntityGeoModel<SprinterEntity> {
    public SprinterModel() {
        super(EEMod.location("sprinter"), true);
    }

    @Override
    public ResourceLocation getTextureResource(SprinterEntity animatable) {
        return EEMod.location("textures/entity/" + animatable.getTexture() + ".png");
    }
}
