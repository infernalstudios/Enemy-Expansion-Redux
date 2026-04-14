package org.infernalstudios.enemyexp.client.entity.model;

import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.EquestrianEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class EquestrianModel extends DefaultedEntityGeoModel<EquestrianEntity> {
    public EquestrianModel() {
        super(EEMod.location("equestrian"), true);
    }
}
