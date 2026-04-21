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

    // Equestrian
    public static final RawAnimation PANICKED = RawAnimation.begin().thenLoop("panicked");
    public static final RawAnimation TROT = RawAnimation.begin().thenLoop("trot");
    public static final RawAnimation PREPARE = RawAnimation.begin().thenPlayAndHold("prepare");
    public static final RawAnimation GALLOP = RawAnimation.begin().thenLoop("gallop");

    // Vampire
    public static final RawAnimation VAMPIRE_ALERT = RawAnimation.begin().then("alert", Animation.LoopType.PLAY_ONCE);
    public static final RawAnimation VAMPIRE_CHASE = RawAnimation.begin().thenLoop("chase");
    public static final RawAnimation VAMPIRE_FLYING = RawAnimation.begin().thenLoop("flying");
    public static final RawAnimation VAMPIRE_HOVER = RawAnimation.begin().thenLoop("hover");
    public static final RawAnimation VAMPIRE_WINGS_FLAPPING = RawAnimation.begin().thenLoop("wings_flapping");

    public static final RawAnimation VAMPIRE_DODGE_BACK = RawAnimation.begin().then("dodge_back", Animation.LoopType.PLAY_ONCE);
    public static final RawAnimation VAMPIRE_DODGE_FORWARD = RawAnimation.begin().then("dodge_forward", Animation.LoopType.PLAY_ONCE);

    public static final RawAnimation VAMPIRE_HURT_STANDING = RawAnimation.begin().then("hurt_standing", Animation.LoopType.PLAY_ONCE);
    public static final RawAnimation VAMPIRE_HURT_FLYING = RawAnimation.begin().then("hurt_flying", Animation.LoopType.PLAY_ONCE);

    public static final RawAnimation VAMPIRE_FALLING = RawAnimation.begin().then("falling_start", Animation.LoopType.PLAY_ONCE).thenLoop("falling_loop");
    public static final RawAnimation VAMPIRE_FALLING_END = RawAnimation.begin().then("falling_end", Animation.LoopType.PLAY_ONCE);

    public static final RawAnimation VAMPIRE_DIE_GROUND = RawAnimation.begin().then("die_ground", Animation.LoopType.PLAY_ONCE);
    public static final RawAnimation VAMPIRE_DIE_AIR = RawAnimation.begin().then("die_air", Animation.LoopType.PLAY_ONCE);
    public static final RawAnimation VAMPIRE_BITER_SPAWN = RawAnimation.begin().then("biter_spawn", Animation.LoopType.PLAY_ONCE);

    // Biter
    public static final RawAnimation BITER_FORWARD = RawAnimation.begin().thenLoop("forward");
    public static final RawAnimation BITER_WOBBLING = RawAnimation.begin().thenLoop("wobbling");
    public static final RawAnimation BITER_WINGS_FLAPPING = RawAnimation.begin().thenLoop("wings_flapping");
    public static final RawAnimation BITER_DIE = RawAnimation.begin().then("die", Animation.LoopType.PLAY_ONCE);

    private EEAnimations() {
    }
}