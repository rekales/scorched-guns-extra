package net.zincstudios.scgextra.entity.common;

import net.zincstudios.scgextra.entity.common.goal.StunnedGoal;

/**
 * Interface to be paired with the use of StunnedGoal.
 *
 * @see StunnedGoal
 *
 * @deprecated as of the transition to brain behaviors
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Stunnable {

    /**
     * Will be checked by the StunnedGoal every tick while it's not stunned.
     * @return amount of ticks the entity should be stunned, return 0 or less to not stun.
     */
    int shouldStun();

    /**
     * To be primarily be invoked by the StunnedGoal
     */
    void setStunned(boolean stunned);

    /**
     * Will be invoked by the StunnedGoal while the entity is being stunned.
     * @return true to interrupt the stun
     */
    default boolean tickStunned(int ticksLeft) {
        return false;
    }

    /**
     * Mostly just for state checks from other sources, ignore if not needed.
     */
    default boolean isStunned() {
        return false;
    }

    default void setStunCooldown(boolean cooldown) {
    }
}
