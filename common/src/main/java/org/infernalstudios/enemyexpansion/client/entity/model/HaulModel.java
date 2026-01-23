package org.infernalstudios.enemyexpansion.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexpansion.EEMod;
import org.infernalstudios.enemyexpansion.content.entity.HaulEntity;
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
