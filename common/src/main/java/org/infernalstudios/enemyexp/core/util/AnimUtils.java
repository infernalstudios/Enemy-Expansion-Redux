package org.infernalstudios.enemyexp.core.util;

import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public final class AnimUtils {

    /**
     * A utility method to handle idle and walk animations based on movement.
     *
     * @param event    The animation state event.
     * @param idleAnim The name of the idle animation.
     * @param walkAnim The name of the walk animation.
     * @return The appropriate PlayState for the animation.
     */
    public static PlayState idleWalkAnimation(AnimationState<?> event, String idleAnim, String walkAnim) {
        return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F
                ? event.setAndContinue(RawAnimation.begin().thenLoop(idleAnim))
                : event.setAndContinue(RawAnimation.begin().thenLoop(walkAnim));
    }

    private AnimUtils() {
    }
}
