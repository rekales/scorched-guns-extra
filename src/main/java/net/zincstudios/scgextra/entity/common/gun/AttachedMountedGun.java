package net.zincstudios.scgextra.entity.common.gun;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class AttachedMountedGun extends MountedGun{

    public final SimulatedGun gun;

    public AttachedMountedGun(LivingEntity parent, SimulatedGun gun, Vec3 gunPosition, Predicate<LivingEntity> canShoot) {
        super(parent, gun, gunPosition, canShoot);
        this.gun = gun;
    }

    @Override
    public boolean tick(LivingEntity target, float accuracyModifier) {
        if (!isActive.test(this.parent)) return false;

        Vec3 targetPos = SimulatedGun.getCenterMassPos(target);
        this.pointToTarget(targetPos);

        return this.gun.tickFire(this.parent,
                targetPos,
                accuracyModifier,
                this.shouldShoot(targetPos)
        );
    }

    @Override
    protected void pointToTarget(Vec3 targetPos) {
        this.yaw = this.parent.getYHeadRot();
        this.pitch = this.parent.getXRot();
    }

    @Override
    protected Vec3 getGunPos() {
        return this.parent.position().add(this.spawnOffset.yRot(-this.parent.yHeadRot * Mth.DEG_TO_RAD));
    }

    @Override
    protected boolean shouldShoot(Vec3 targetPos) {
        if (!this.getGunPos().closerThan(targetPos, this.gun.getMaxRange())) return false;

        Vec3 delta = targetPos.subtract(this.getGunPos());
        float targetYaw = -((float)(Mth.atan2(delta.x, delta.z) * 180.0F / Math.PI));
        float targetPitch = -((float)(Mth.atan2(delta.y, delta.horizontalDistance()) * 180.0F / Math.PI));

        float yawDiff = Mth.wrapDegrees(targetYaw - this.yaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - this.pitch);

        return (Math.abs(yawDiff) < 50.0f && Math.abs(pitchDiff) < 50.0f);
    }
}
