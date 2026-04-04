package org.infernalstudios.enemyexp.content;

import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class EEAnimations {

    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    public static final RawAnimation SPRINT = RawAnimation.begin().thenLoop("sprint");

    // Sprinter
    public static final RawAnimation STAGGERED_USED = RawAnimation.begin().then("staggered_used", Animation.LoopType.PLAY_ONCE);

    // Slugger
    public static final RawAnimation CHARGE = RawAnimation.begin().thenPlayAndHold("charge");
    public static final RawAnimation DASH = RawAnimation.begin().thenLoop("dash");

    // Meature
    public static final RawAnimation DANCE = RawAnimation.begin().thenLoop("dance");
    public static final RawAnimation HAPPY = RawAnimation.begin().thenPlay("happy");
    public static final RawAnimation LEAP = RawAnimation.begin().thenPlay("leap");

    // Vampire
    public static final RawAnimation VAMPIRE_ALERT = RawAnimation.begin().then("alert", Animation.LoopType.PLAY_ONCE);
    public static final RawAnimation VAMPIRE_CHASE = RawAnimation.begin().thenLoop("chase");
    public static final RawAnimation VAMPIRE_DODGE = RawAnimation.begin().then("dodge_back", Animation.LoopType.PLAY_ONCE);
    public static final RawAnimation VAMPIRE_FLYING = RawAnimation.begin().thenLoop("flying");

    // Biter
    public static final RawAnimation BITER_FORWARD = RawAnimation.begin().thenLoop("forward");

    private EEAnimations() {
    }
}
