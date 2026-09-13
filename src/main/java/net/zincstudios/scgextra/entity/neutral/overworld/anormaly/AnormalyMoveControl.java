package net.zincstudios.scgextra.entity.neutral.overworld.anormaly;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class AnormalyMoveControl extends MoveControl{
    private final AnormalyEntity anormaly;
    public AnormalyMoveControl(AnormalyEntity entity){
        super(entity);
        this.anormaly = entity;
    }
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            Vec3 vec3 = new Vec3(this.wantedX - anormaly.getX(),
                    this.wantedY - anormaly.getY(), this.wantedZ - anormaly.getZ());
            double d0 = vec3.length();
            if (d0 < anormaly.getBoundingBox().getSize()) {
                this.operation = Operation.WAIT;
                anormaly.setDeltaMovement(anormaly.getDeltaMovement().scale((double) 0.5F));
            } else {
                anormaly.setDeltaMovement(
                        anormaly.getDeltaMovement().add(vec3.scale(this.speedModifier * 0.05 / d0)));
                if (anormaly.getTarget() == null) {
                    Vec3 vec31 = anormaly.getDeltaMovement();
                    anormaly.setYRot(-((float) Mth.atan2(vec31.x, vec31.z)) * (180F / (float) Math.PI));
                    anormaly.yBodyRot = anormaly.getYRot();
                } else {
                    double d2 = anormaly.getTarget().getX() - anormaly.getX();
                    double d1 = anormaly.getTarget().getZ() - anormaly.getZ();
                    anormaly.setYRot(-((float) Mth.atan2(d2, d1)) * (180F / (float) Math.PI));
                    anormaly.yBodyRot = anormaly.getYRot();
                }
            }
        }
    }
}
