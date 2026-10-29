package net.zincstudios.scgextra.entity.wreckers.wrecker_biker;

import java.util.EnumSet;

import net.zincstudios.scgextra.sounds.WreckersSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.entity.throwable.ThrowableMolotovCocktailEntity;

public class ChargeTargetGoal extends Goal{

    private final WreckerBikerEntity biker;
    private boolean charged = false;
    private float angle;
    private float distance;
    private float height;
    private long endTime = 0;

    // private ArrayList<BlockPos> points = new ArrayList<>();

    public ChargeTargetGoal(WreckerBikerEntity entity){
        this.biker = entity;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.biker.getTarget()!=null && this.biker.distanceToSqr(this.biker.getTarget()) <= 64 && this.biker.getNavigation().isDone();
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
        this.selectNext();
        this.endTime = this.biker.level().getGameTime()+(this.biker.getRandom().nextInt(100, 200));
    }

    @Override
    public void tick() {
        super.tick();
        if(this.biker.level().isClientSide())return;
        //tick was getting too big
        if(this.endTime>this.biker.level().getGameTime()){
            circleTarget();
        }else{
            chargeTarget();
        }
        if(this.endTime==this.biker.level().getGameTime())this.biker.playSound(WreckersSounds.WRECKER_BIKER_RIDER_CHARGE.get(), 1.5f, this.biker.getVoicePitch());
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
       this.angle += this.biker.clockwise * 15.0F * ((float)Math.PI / 180F);
       biker.moveTargetPoint = Vec3.atLowerCornerOf(biker.anchorPoint).add((double)(this.distance * Mth.cos(this.angle)), 0, (double)(this.distance * Mth.sin(this.angle)));
    }
    public boolean touchingTarget(){
        return biker.moveTargetPoint.distanceToSqr(biker.getX(), biker.getY(), biker.getZ()) < (double)8.0F;
    }
    private void circleTarget(){
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
        this.biker.getLookControl().setLookAt(this.biker.moveTargetPoint);
    }
    private void chargeTarget(){
        this.biker.getNavigation().moveTo(this.biker.getTarget().getX(), this.biker.getTarget().getY()+300, this.biker.getTarget().getZ(), 1.5);
        this.biker.getLookControl().setLookAt(this.biker.getTarget());
        if(this.biker.distanceToSqr(this.biker.getTarget())<=8){
            LivingEntity target = this.biker.getTarget();
            ThrowableMolotovCocktailEntity cocktail = new ThrowableMolotovCocktailEntity(this.biker.level(), this.biker, 20);
            cocktail.shoot(target.getX(), target.getY()+3, target.getZ(), 0.6f, 0f);
            double d0 = target.getX() - this.biker.getX();
            double d1 = target.getY(2) - biker.getY();
            double d2 = target.getZ() - this.biker.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            cocktail.shoot(
                d0, 
                d1 + d3 * (double)0.2F, 
                d2, 
                0.3F, 
                0.1F
            );
            this.biker.level().addFreshEntity(cocktail);
            this.biker.doHurtTarget(target);
            this.biker.playSound(WreckersSounds.WRECKER_BIKER_BIKE_HIT.get(), 1f, this.biker.getVoicePitch());
            this.biker.playSound(WreckersSounds.WRECKER_BIKER_RIDER_LAUGH.get(), 1.5f, this.biker.getVoicePitch());
            //move away to not get hit
            AABB box = this.biker.getBoundingBox().inflate(20);
            this.biker.getNavigation().moveTo(
                this.biker.getRandom().nextInt((int)box.minX, (int)box.maxX), 
                this.biker.getY(),
                this.biker.getRandom().nextInt((int)box.minZ, (int)box.maxZ), 
                1.2
            );
            this.charged = true;
        }
    }
}
