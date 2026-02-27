package org.infernalstudios.enemyexp.core.util;

import org.infernalstudios.enemyexp.content.EEAnimations;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public final class AnimUtils {

    public static PlayState idleWalkAnimation(AnimationState<?> event) {
        return idleWalkAnimation(event, EEAnimations.IDLE, EEAnimations.WALK);
    }

    /**
     * A utility method to handle idle and walk animations based on movement.
     *
     * @param event    The animation state event.
     * @param idleAnim The name of the idle animation.
     * @param walkAnim The name of the walk animation.
     * @return The appropriate PlayState for the animation.
     */
    public static PlayState idleWalkAnimation(AnimationState<?> event, RawAnimation idleAnim, RawAnimation walkAnim) {
        return isNotMoving(event)
                ? event.setAndContinue(idleAnim)
                : event.setAndContinue(walkAnim);
    }

    public static boolean isNotMoving(AnimationState<?> event) {
        return !event.isMoving() && event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F;
    }

    private AnimUtils() {
    }
}
