package org.infernalstudios.enemyexp.content.entity;

import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;

public class VampireMoveControl extends MoveControl {
    private final FlyingMoveControl flyingMoveControl;

    public VampireMoveControl(VampireEntity mob) {
        super(mob);
        this.flyingMoveControl = new FlyingMoveControl(mob, 20, true);
    }

    @Override
    public void tick() {
        if (((VampireEntity) this.mob).isAerial()) {
            this.flyingMoveControl.setWantedPosition(this.getWantedX(), this.getWantedY(), this.getWantedZ(), this.getSpeedModifier());
            this.flyingMoveControl.tick();
        } else {
            super.tick();
        }
    }

    public void switchMode() {
        this.setWantedPosition(this.mob.getX(), this.mob.getY(), this.mob.getZ(), this.getSpeedModifier());
    }
}
