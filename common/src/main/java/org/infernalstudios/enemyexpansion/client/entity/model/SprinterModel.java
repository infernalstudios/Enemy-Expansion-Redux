package org.infernalstudios.enemyexpansion.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexpansion.EEMod;
import org.infernalstudios.enemyexpansion.content.entity.SprinterEntity;
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
