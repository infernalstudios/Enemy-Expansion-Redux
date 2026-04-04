package org.infernalstudios.enemyexp.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.BiterEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class BiterModel extends DefaultedEntityGeoModel<BiterEntity> {
    public BiterModel() {
        super(EEMod.location("biter"), true);
    }

    @Override
    public ResourceLocation getTextureResource(BiterEntity animatable) {
        return EEMod.location("textures/entity/vampire_angry.png");
    }
}