package net.zincstudios.scgextra.entity.wreckers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.security.InvalidParameterException;
import java.util.EnumSet;

public class ApproachTargetGoal extends Goal {

    protected final PathfinderMob mob;
    protected final double maxRange;
    protected final double approachDist;
    protected final double speedModifier;

    public ApproachTargetGoal(PathfinderMob mob, double maxRange, double approachDist, double speedModifier) {
        this.mob = mob;
        this.maxRange = maxRange;
        this.approachDist = approachDist;
        this.speedModifier = speedModifier;
        if (approachDist > this.maxRange)
            throw new InvalidParameterException("approachDist must be smaller or equal to maxRange");
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && !this.mob.getTarget().isDeadOrDying();
    }

    public void start() {
        this.mob.setAggressive(true);
    }

    public void stop() {
        this.mob.setAggressive(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean lineOfSight = this.mob.getSensing().hasLineOfSight(target);

        if (distSqr > this.maxRange*this.maxRange || !lineOfSight) {
            // NOTE: maybe cache pathfinding if necessary
            this.mob.getNavigation().moveTo(target, this.speedModifier);
//            this.setGoalState(APPROACH_STATE);
        }  if (distSqr <= this.approachDist*this.approachDist) {
            this.mob.getNavigation().stop();
        }
    }
}
