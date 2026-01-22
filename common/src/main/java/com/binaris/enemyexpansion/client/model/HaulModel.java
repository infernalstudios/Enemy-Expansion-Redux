package com.binaris.enemyexpansion.client.model;

import com.binaris.enemyexpansion.EEMod;
import com.binaris.enemyexpansion.content.entity.HaulEntity;
import com.binaris.enemyexpansion.content.entity.SprinterEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class HaulModel extends DefaultedEntityGeoModel<HaulEntity> {
    public HaulModel() {
        super(EEMod.location("haul"), true);
    }

    @Override
    public ResourceLocation getTextureResource(HaulEntity animatable) {
        return EEMod.location("textures/entity/" + animatable.getTexture() + ".png");
    }
}
