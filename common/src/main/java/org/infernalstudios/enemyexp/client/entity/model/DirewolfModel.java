package org.infernalstudios.enemyexp.client.entity.model;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.DirewolfEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class DirewolfModel extends DefaultedEntityGeoModel<DirewolfEntity> {
    public DirewolfModel() {
        super(EEMod.location("direwolf"), true);
    }

    @Override
    public ResourceLocation getTextureResource(DirewolfEntity animatable) {
        return EEMod.location("textures/entity/direwolf.png");
    }
}