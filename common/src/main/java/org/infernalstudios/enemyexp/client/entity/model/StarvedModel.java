package org.infernalstudios.enemyexp.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.StarvedEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class StarvedModel extends DefaultedEntityGeoModel<StarvedEntity> {
    public StarvedModel() {
        super(EEMod.location("starved"), true);
    }

    @Override
    public ResourceLocation getTextureResource(StarvedEntity animatable) {
        return EEMod.location("textures/entity/starved.png");
    }
}