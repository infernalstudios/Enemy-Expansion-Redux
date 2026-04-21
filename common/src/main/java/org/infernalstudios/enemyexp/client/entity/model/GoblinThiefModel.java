package org.infernalstudios.enemyexp.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.GoblinThiefEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class GoblinThiefModel extends DefaultedEntityGeoModel<GoblinThiefEntity> {
    public GoblinThiefModel() {
        super(EEMod.location("goblin_thief"), true);
    }

    @Override
    public ResourceLocation getTextureResource(GoblinThiefEntity animatable) {
        if (animatable.getState() == GoblinThiefEntity.STATE_PANIC) {
            return EEMod.location("textures/entity/goblin_thief_shocked.png");
        }

        return super.getTextureResource(animatable);
    }
}