package net.zincstudios.scgextra.entity.common;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.SCGExtra;

public class FlyDirectMoveControl extends MoveControl {

    private final double lerp;
    private final double backwardMult;

    @SuppressWarnings("unused")
    public FlyDirectMoveControl(Mob mob, double lerp) {
        this(mob, lerp, 1);
    }

    public FlyDirectMoveControl(Mob mob, double lerp, double backwardMult) {
        super(mob);
        this.lerp = lerp;
        this.backwardMult = backwardMult;
    }

    @Override
    public void tick() {
        Vec3 desiredVelocity = Vec3.ZERO;

        if (this.operation == Operation.MOVE_TO) {
            Vec3 wanted = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 current = this.mob.position();
            Vec3 dir = wanted.subtract(current);
            double speed = this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);

            // Reduced speed when moving backward
            float targetYaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
            float diff = Mth.wrapDegrees(mob.getYRot() - targetYaw);
            speed *= Mth.map(Math.abs(diff), 0f, 180f, 1f, this.backwardMult);
            SCGExtra.LOGGER.debug(speed + "");

            desiredVelocity = dir.normalize().scale(speed);

            // Repel from ground
            BlockHitResult hitResult = this.mob.level().clip(new ClipContext(
                    this.mob.position(),
                    this.mob.position().add(0, -16, 0),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY,
                    this.mob
            ));
            if (hitResult.getType() != HitResult.Type.MISS) {
                double airHeight = this.mob.getY() - hitResult.getLocation().y;
                double heightThresh = 1.5;
                if (airHeight < heightThresh) {
                    double mult = (heightThresh - airHeight) / heightThresh;
                    desiredVelocity = new Vec3(
                            desiredVelocity.x,
                            Math.min(desiredVelocity.y + speed*mult, speed),
                            desiredVelocity.z);
                }
            }

            ((ServerLevel)this.mob.level()).sendParticles(ParticleTypes.FLAME,
                    wanted.x, wanted.y, wanted.z, 1,
                    0, 0, 0, 0);

            this.operation = Operation.WAIT;
        }

        this.mob.setDeltaMovement(this.mob.getDeltaMovement().lerp(desiredVelocity, this.lerp));
    }
}
