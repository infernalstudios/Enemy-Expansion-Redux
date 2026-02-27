package org.infernalstudios.enemyexp.content;

import software.bernie.geckolib.core.animation.RawAnimation;

public final class EEAnimations {

    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    public static final RawAnimation SPRINT = RawAnimation.begin().thenLoop("sprint");

    // Sprinter
    public static final RawAnimation STAGGERED_USED = RawAnimation.begin().thenPlay("staggered_used");

    // Slugger
    public static final RawAnimation CHARGE = RawAnimation.begin().thenPlayAndHold("charge");
    public static final RawAnimation DASH = RawAnimation.begin().thenLoop("dash");

    // Meature
    public static final RawAnimation DANCE = RawAnimation.begin().thenLoop("dance");
    public static final RawAnimation HAPPY = RawAnimation.begin().thenPlay("happy");

    private EEAnimations() {
    }
}
