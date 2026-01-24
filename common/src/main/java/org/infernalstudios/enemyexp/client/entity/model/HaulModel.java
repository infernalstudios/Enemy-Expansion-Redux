package org.infernalstudios.enemyexp.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.HaulEntity;
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
