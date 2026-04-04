package org.infernalstudios.enemyexp.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.VampireEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class VampireModel extends DefaultedEntityGeoModel<VampireEntity> {
    public VampireModel() {
        super(EEMod.location("vampire"), true);
    }

    @Override
    public ResourceLocation getTextureResource(VampireEntity animatable) {
        if (!animatable.isAwake()) {
            return EEMod.location("textures/entity/vampire_closed.png");
        } else if (animatable.isAngry() || animatable.isAggressive()) {
            return EEMod.location("textures/entity/vampire_angry.png");
        } else {
            return EEMod.location("textures/entity/vampire_open.png");
        }
    }
}