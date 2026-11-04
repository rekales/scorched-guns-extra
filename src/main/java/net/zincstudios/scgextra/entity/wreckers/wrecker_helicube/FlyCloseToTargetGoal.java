package net.zincstudios.scgextra.entity.wreckers.wrecker_helicube;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

@SuppressWarnings("unused")
public class FlyCloseToTargetGoal extends Goal {

    protected final Mob mob;
    protected final float speedModifier;
    protected final float minDist;
    protected final float maxDist;

    public FlyCloseToTargetGoal(Mob mob, float speedModifier, float maxDist, float minDist) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.minDist = minDist;
        this.maxDist = maxDist;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Flag.LOOK));
    }

    public FlyCloseToTargetGoal(Mob mob, float speedModifier, float maxDist) {
        this(mob, speedModifier, maxDist, 0);
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null
                && !this.mob.getTarget().isDeadOrDying();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        Vec3 directionToTarget = target.position().subtract(this.mob.position());
        double dist = directionToTarget.length();

        this.mob.getLookControl().setLookAt(target);

        if (dist > this.maxDist) {
            Vec3 dir = directionToTarget.normalize().scale(-this.minDist);
            dir = dir.add(target.position());
            this.mob.getMoveControl().setWantedPosition(dir.x, dir.y+1, dir.z, this.speedModifier);
//            this.mob.getMoveControl().setWantedPosition(dir.getX(), dir.getY() + 2, target.getZ(), this.speedModifier);
        } else if (dist < this.minDist) {
            Vec3 dir = directionToTarget.normalize().scale(-this.minDist);
            dir = dir.add(target.position());
            double speed = this.speedModifier/2 * dist/this.minDist;
            this.mob.getMoveControl().setWantedPosition(dir.x, dir.y+2, dir.z, speed);
        }
    }
}
