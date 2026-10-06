package net.zincstudios.scgextra.entity.whaler.guardian_statue;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class SlideToTargetGoal extends Goal {

    private final GuardianStatueEntity mob;
    private final float speed;

    public SlideToTargetGoal(GuardianStatueEntity mob, float speed) {
        this.mob = mob;
        this.speed = speed;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null
                && target.isAlive()
                && !this.mob.hasLineOfSight(target)
                && this.mob.getGuardianLaserAttackTimer() == 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        Vec3 dir = target.position().subtract(mob.position()).normalize();
        this.mob.setDeltaMovement(dir.x * this.speed, this.mob.getDeltaMovement().y, dir.z * this.speed);
    }
}
