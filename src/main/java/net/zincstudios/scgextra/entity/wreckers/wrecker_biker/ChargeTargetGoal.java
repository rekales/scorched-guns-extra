package net.zincstudios.scgextra.entity.wreckers.wrecker_biker;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class ChargeTargetGoal extends Goal{

    private final WreckerBikerEntity biker;
    private boolean charged = false;
    private float angle;
    private float distance;
    private float height;
    private float clockwise;

    // private ArrayList<BlockPos> points = new ArrayList<>();

    public ChargeTargetGoal(WreckerBikerEntity entity){
        this.biker = entity;
    }

    @Override
    public boolean canUse() {
        return this.biker.getTarget()!=null && this.biker.distanceToSqr(this.biker.getTarget()) <= 64;
    }
    @Override
    public boolean canContinueToUse() {
        return this.biker.getTarget()!=null && !this.charged;
    }
    @Override
    public void start() {
        super.start();
        this.charged = false;
        this.biker.setCircling(true);
        this.distance = 5.0F + biker.getRandom().nextFloat() * 10.0F;
        this.height = -4.0F + biker.getRandom().nextFloat() * 9.0F;
        this.clockwise = biker.getRandom().nextBoolean() ? 1.0F : -1.0F;
        this.selectNext();
    }

    @Override
    public void tick() {
        super.tick();
        if(this.biker.level().isClientSide())return;
        if (biker.getRandom().nextInt(this.adjustedTickDelay(350)) == 0) {
            this.height = -4.0F + biker.getRandom().nextFloat() * 9.0F;
        }
        
        if (biker.getRandom().nextInt(this.adjustedTickDelay(250)) == 0) {
            ++this.distance;
            if (this.distance > 15.0F) {
                this.distance = 5.0F;
            }
        }
        
        if (biker.getRandom().nextInt(this.adjustedTickDelay(450)) == 0) {
            this.angle = biker.getRandom().nextFloat() * 2.0F * (float)Math.PI;
            this.selectNext();
        }
        
        if (this.touchingTarget()) {
            this.selectNext();
        }
        
        if (biker.moveTargetPoint.y < biker.getY() && !biker.level().isEmptyBlock(biker.blockPosition().below(1))) {
           this.height = Math.max(1.0F, this.height);
           this.selectNext();
        }

        if (biker.moveTargetPoint.y > biker.getY() && !biker.level().isEmptyBlock(biker.blockPosition().above(1))) {
           this.height = Math.min(-1.0F, this.height);
           this.selectNext();
        }
        this.biker.getNavigation().moveTo(this.biker.moveTargetPoint.x, this.biker.moveTargetPoint.y, this.biker.moveTargetPoint.z, 1.2);

    }
    @Override
    public void stop() {
        super.stop();
        this.biker.setCircling(false);
    }

    private void selectNext() {
       if (BlockPos.ZERO.equals(biker.anchorPoint)) {
          biker.anchorPoint = biker.blockPosition();
       }
       this.angle += this.clockwise * 15.0F * ((float)Math.PI / 180F);
       biker.moveTargetPoint = Vec3.atLowerCornerOf(biker.anchorPoint).add((double)(this.distance * Mth.cos(this.angle)), 0, (double)(this.distance * Mth.sin(this.angle)));
    }
    public boolean touchingTarget(){
        return biker.moveTargetPoint.distanceToSqr(biker.getX(), biker.getY(), biker.getZ()) < (double)8.0F;
    }
}
