package net.zincstudios.scgextra.entity.whaler;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.SCGExtra;

public class SwimWalkMoveControl extends MoveControl {

    private final float swimSpeedMult;
    private final boolean applyGravity;

    // TODO: use forge swim speed attrib
    public SwimWalkMoveControl(Mob mob, float swimSpeedMult, boolean applyGravity) {
        super(mob);
        this.swimSpeedMult = swimSpeedMult;
        this.applyGravity = applyGravity;
    }

    // TODO: when close to target, match y to walk target instead of wanted
    public void tick() {
        if (this.mob.isInWater()) {
            SCGExtra.LOGGER.debug(this.operation + "");
            if (this.operation == Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
                if (!this.applyGravity) {
                    this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0.005, 0));
                }
                Vec3 delta = this.mob.getDeltaMovement();
                float speed = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * this.swimSpeedMult;

                // NOTE: does yya even fucking work?
                // TODO: figure out what nullifies yya values
                double yDiff = this.wantedY - this.mob.position().y;
                if (Math.abs(yDiff) > 0.1) {
                    float ySpeed = (float) delta.y;
                    if (yDiff > 0) {
                        ySpeed += speed/5;
                    } else {
                        ySpeed += -speed/5;
                    }

                    ySpeed = Mth.clamp(ySpeed, -speed/2, speed/2);
                    if (yDiff < 0.5) {
                        ySpeed = ySpeed * (float)(Math.abs(yDiff) + 0.25);
                    }

                    this.mob.setDeltaMovement(new Vec3(delta.x, ySpeed, delta.z));
                }

                double d0 = this.wantedX - this.mob.getX();
                double d1 = this.wantedZ - this.mob.getZ();
                float targetRot = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;

                float rotDiff = Math.abs(Mth.wrapDegrees(targetRot - this.mob.getYRot()));

                float rotSpeedAdj;
                if (rotDiff < 90) {
                    rotSpeedAdj = (90 - rotDiff) / 90;
                } else {
                    rotSpeedAdj = 0.001f;
                }

                // 6x speed to compensate for LivingEntity#travel slowing it down a lot
                this.mob.setSpeed(speed * rotSpeedAdj * 6);

                if (!this.mob.position().closerThan(new Vec3(this.wantedX, this.wantedY, this.wantedZ), 0.2)) {
                    this.mob.setYRot(this.rotlerp(this.mob.getYRot(), targetRot, 7.5F));
                }
            }
        } else {
            super.tick();
        }
    }
}
