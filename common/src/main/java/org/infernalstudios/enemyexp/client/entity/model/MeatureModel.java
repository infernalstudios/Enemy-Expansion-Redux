package org.infernalstudios.enemyexp.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.MeatureEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;


public class MeatureModel extends DefaultedEntityGeoModel<MeatureEntity> {
    public MeatureModel() {
        super(EEMod.location("meature"), true);
    }

    @Override
    public ResourceLocation getTextureResource(MeatureEntity animatable) {
        String texture = animatable.getOwnerUUID() == null ? "meature" : "meature_tamed";
        return EEMod.location("textures/entity/" + texture + ".png");
    }
}
