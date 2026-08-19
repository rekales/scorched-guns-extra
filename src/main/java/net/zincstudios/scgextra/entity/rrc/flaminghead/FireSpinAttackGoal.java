package net.zincstudios.scgextra.entity.rrc.flaminghead;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.zincstudios.scgextra.sounds.RRCSounds;

import java.util.EnumSet;

public class FireSpinAttackGoal extends Goal{
    protected final FlamingHeadEntity mob;
    private final int cooldownDuration;
    private final int chargeDuration;
    private final int maxDuration;
    private final float range;
    private long cooldownEnd = 0;  // level timestamp
    private int duration;
    private boolean hadTarget = false;

    public FireSpinAttackGoal(FlamingHeadEntity mob, int cooldownDuration, int maxDuration, float range, int chargeDuration) {
        this.mob = mob;
        this.cooldownDuration = cooldownDuration;
        this.chargeDuration = chargeDuration;
        this.maxDuration = maxDuration;
        this.range = range;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null || !livingentity.isAlive()) {
            this.hadTarget = false;
            return false;
        }

        if (!this.hadTarget) {
            this.hadTarget = true;
            this.cooldownEnd = this.mob.level().getGameTime() + this.cooldownDuration/2;  // Half cooldown at start
        }

        return (this.mob.getBehaviorState() == FlamingHeadEntity.BehaviorState.NONE)
                && this.mob.level().getGameTime() > this.cooldownEnd;
    }

    @Override
    public boolean canContinueToUse() {
        return this.duration < this.maxDuration;
    }

    @Override
    public void start() {
        this.mob.setBehaviorState(FlamingHeadEntity.BehaviorState.SPINNING);
        assert this.mob.getTarget() != null;
        this.duration = 0;
    }

    @Override
    public void stop() {
        if (this.mob.getBehaviorState() == FlamingHeadEntity.BehaviorState.SPINNING) {
            this.mob.setBehaviorState(FlamingHeadEntity.BehaviorState.NONE);
        }
        this.cooldownEnd = this.mob.level().getGameTime() + this.cooldownDuration;
    }

    @Override
    public void tick() {
        this.duration++;

        if (this.duration < this.chargeDuration) return;

        if (this.duration == this.chargeDuration) {
            this.mob.playSound(RRCSounds.RRC_FLAMING_HEAD_SPIN.get(), this.mob.getSoundVolume(), 1F);
        } else if (this.duration%5 == 0) {
            this.mob.burnNearby(range);
        }
    }
}