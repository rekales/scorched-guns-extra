package net.zincstudios.scgextra.entity.piglin.piglin_strider_rider;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

// Copied from SwimWalkMoveControl
public class LavaWalkMoveControl extends MoveControl {

    private final float swimSpeedMult;
    private final boolean applyGravity;
    private int ticks = 12;

    public LavaWalkMoveControl(PiglinStriderRiderEntity mob, float swimSpeedMult, boolean applyGravity) {
        super(mob);
        this.swimSpeedMult = swimSpeedMult;
        this.applyGravity = applyGravity;
    }

    public void tick() {
        if (this.mob.isInLava()) {
            if (this.operation == Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
                if (!this.applyGravity) {
                    this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0.3, 0));
                }
                Vec3 delta = this.mob.getDeltaMovement();
                float speed = (float)(this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)
                        * this.mob.getAttributeValue(ForgeMod.SWIM_SPEED.get())
                        *(this.swimSpeedMult + (this.mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) ? 0.5f : 0.0f)));

                // does yya even fucking work?
                // NOTE: figure out what nullifies yya values
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

                        // add boost at -water- lava border
                        PathNavigation nav = this.mob.getNavigation();
                        Path path = nav.getPath();
                        if (path != null) {
                            BlockPos pos = path.getNextNodePos();
                            if (nav.getNodeEvaluator().getBlockPathType(this.mob.level(), pos.getX(), pos.getY(), pos.getZ()) == BlockPathTypes.WALKABLE) {
                                ySpeed += 0.15f;
                            }
                        }
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
            }else{
                if (!this.applyGravity) {
                    this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0.1, 0));
                }
            }
        } else {
            super.tick();
            if(this.mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)){
                this.mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1, 2, true, false));
                this.ticks++;
                if(this.ticks>=12 && this.mob.onGround()){
                    this.ticks = 0;
                    Vec3 delta = this.mob.getDeltaMovement();
                    this.mob.setDeltaMovement(new Vec3(delta.x, delta.y+0.6, delta.z));
                }
            }
        }
    }
}
