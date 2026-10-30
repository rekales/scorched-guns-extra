package net.zincstudios.scgextra.entity.common.brain;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.SCGExtra;

public class FlyDirectMoveControl extends MoveControl {

    private final double lerp;

    public FlyDirectMoveControl(Mob mob, double lerp) {
        super(mob);
        this.lerp = lerp;
    }

    @Override
    public void tick() {
        Vec3 desiredVelocity = Vec3.ZERO;

        if (this.operation == Operation.MOVE_TO) {
            Vec3 wanted = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 current = this.mob.position();
            Vec3 dir = wanted.subtract(current);
            double speed = this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);

            desiredVelocity = dir.normalize().scale(speed);
            // TODO: reduced speed when moving backward

            SCGExtra.LOGGER.debug("wanted: " + wanted);
            ((ServerLevel)this.mob.level()).sendParticles(ParticleTypes.FLAME,
                    wanted.x, wanted.y, wanted.z, 1,
                    0, 0, 0, 0);

//            double dx = wanted.x - current.x;
//            double dz = wanted.z - current.z;
//            float targetYaw = (float)(Math.atan2(dz, dx) * (180D / Math.PI) - (double)90.0F);
//            float currentYaw = this.mob.getYRot();
//            float yawDifference = Mth.wrapDegrees(targetYaw - currentYaw);
//            float maxTurnSpeed = 9.0F;
//            float turnAmount = Mth.clamp(yawDifference, -maxTurnSpeed, maxTurnSpeed);
//            currentYaw = Mth.wrapDegrees(currentYaw + turnAmount);
//            this.mob.setYRot(currentYaw);
//            this.mob.yBodyRot = currentYaw;
//            this.mob.yHeadRot = currentYaw;

            this.operation = Operation.WAIT;
        }

        this.mob.setDeltaMovement(this.mob.getDeltaMovement().lerp(desiredVelocity, this.lerp));
    }
}
