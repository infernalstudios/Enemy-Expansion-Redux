package org.infernalstudios.enemyexp.client.entity.model;

import org.infernalstudios.enemyexp.EEMod;
import org.infernalstudios.enemyexp.content.entity.FrigidEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class FrigidModel extends DefaultedEntityGeoModel<FrigidEntity> {
    public FrigidModel() {
        super(EEMod.location("frigid"), true);
    }
}
