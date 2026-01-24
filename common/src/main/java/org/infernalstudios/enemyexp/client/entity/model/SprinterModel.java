package org.infernalstudios.enemyexp.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.SprinterEntity;
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
