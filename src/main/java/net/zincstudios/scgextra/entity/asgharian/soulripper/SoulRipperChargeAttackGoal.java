package net.zincstudios.scgextra.entity.asgharian.soulripper;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.asgharian.GoalState;

public class SoulRipperChargeAttackGoal extends Goal {

    public static final int HURT_DELAY_TICKS = 13;
    public static final int DURATION_TICKS = 30;
    public static final GoalState APPROACH = new GoalState("soul_ripper_melee_approach");
    public static final GoalState MELEE = new GoalState("soul_ripper_melee_attack");
    public static final GoalState COOLDOWN = new GoalState("soul_ripper_melee_cooldown");

    protected final SoulRipperEntity mob;
    protected final int cooldownDuration;
    protected int cooldownEnd = 0;  // tickCount timestamp
    protected int activeTimer = 0;
    protected GoalState state = COOLDOWN;

    public SoulRipperChargeAttackGoal(SoulRipperEntity mob, int cooldownDuration) {
        this.mob = mob;
        this.cooldownDuration = cooldownDuration;
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (this.mob.tickCount > this.cooldownEnd
                && livingentity != null && livingentity.isAlive()
                && this.mob.tickCount > this.cooldownEnd
                && this.mob.canMelee()
                && this.mob.getRandom().nextInt(reducedTickDelay(7)) == 0) {
            return this.mob.distanceToSqr(livingentity) > 4.0D;
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.state == MELEE) return true;
        return this.mob.getMoveControl().hasWanted()
                && this.mob.tickCount > this.cooldownEnd
                && this.mob.getTarget() != null
                && this.mob.getTarget().isAlive();
    }

    @Override
    public void start() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null) {
            Vec3 vec3 = livingentity.getEyePosition();
            this.mob.getMoveControl().setWantedPosition(vec3.x, vec3.y, vec3.z, 1.4D);
        }
        this.activeTimer = -1;
        this.setGoalState(APPROACH);
    }

    @Override
    public void stop() {
        this.resetCooldown();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            if (this.activeTimer >= 0) {
                if (this.activeTimer == 0) {
                    this.resetCooldown();
                    this.setGoalState(COOLDOWN);
                } else if (this.activeTimer == DURATION_TICKS - HURT_DELAY_TICKS + 6) {
                    this.pushClosetToTarget(target);
                } else if (this.activeTimer == DURATION_TICKS - HURT_DELAY_TICKS) {
                    if (this.mob.getBoundingBox().inflate(2).intersects(target.getBoundingBox())) {
                        this.mob.doHurtTarget(target);
                    }
                }
                this.activeTimer--;
            } else if (this.mob.getBoundingBox().inflate(4).intersects(target.getBoundingBox())) {
                this.activeTimer = DURATION_TICKS;
                this.setGoalState(MELEE);
            } else {
                double d0 = this.mob.distanceToSqr(target);
                if (d0 < 9.0D) {
                    Vec3 vec3 = target.getEyePosition();
                    this.mob.getMoveControl().setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0D);
                }
            }
        }
    }

    protected void pushClosetToTarget(LivingEntity target) {
        Vec3 vec = this.mob.position().subtract(target.position()).normalize().scale(-0.75);
        this.mob.setDeltaMovement(this.mob.getDeltaMovement().scale(0.5).add(vec));
    }

    protected void resetCooldown() {
        this.cooldownEnd = this.mob.tickCount + this.cooldownDuration;
    }

    protected void setGoalState(GoalState state) {
        GoalState oldState = this.state;
        this.state = state;
        if (oldState != state) {
            this.mob.onGoalStateChanged(this, state);
        }
    }
}
