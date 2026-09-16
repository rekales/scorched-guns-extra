package net.zincstudios.scgextra.entity.common;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

// TODO: redo
//mostly copied from turtle
public class AmphibiousMoveControl extends MoveControl {
    private final Mob mob;

    public AmphibiousMoveControl(Mob mob) {
        super(mob);
        this.mob = mob;
    }

    public void tick() {
        if (this.mob.isInWater()) {
            this.speedModifier = 2.0;
        }
        if (this.operation == Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
            double d0 = this.wantedX - this.mob.getX();
            double d1 = this.wantedY - this.mob.getY();
            double d2 = this.wantedZ - this.mob.getZ();
            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            if (d3 < (double) 1.0E-5F) {
                this.mob.setSpeed(0.0F);
            } else {
                d1 /= d3;
                float f = (float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, 90.0F));
                this.mob.yBodyRot = this.mob.getYRot();
                float f1 = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                this.mob.setSpeed(f1);
                this.mob.setDeltaMovement(this.mob.getDeltaMovement().add( 0.0F,
                        (double) this.mob.getSpeed() * d1 * 0.1, 0.0F));
            }
        } else {
            this.mob.setSpeed(0.0F);
        }
    }
}
