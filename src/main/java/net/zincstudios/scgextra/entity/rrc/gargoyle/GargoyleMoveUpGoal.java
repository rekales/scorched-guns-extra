package net.zincstudios.scgextra.entity.rrc.gargoyle;

import net.minecraft.world.entity.ai.goal.Goal;

public class GargoyleMoveUpGoal extends Goal{

    private GargoyleEntity gargoyle;

    public GargoyleMoveUpGoal(GargoyleEntity entity){
        this.gargoyle = entity;
    }

    @Override
    public boolean canUse() {
        return this.gargoyle.getTarget()!=null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        super.start();
        this.gargoyle.setAttacking(true);
    }

    @Override
    public void tick() {
        super.tick();
        this.gargoyle.getLookControl().setLookAt(this.gargoyle.getTarget());
        if(!this.gargoyle.level().getBlockState(this.gargoyle.blockPosition().below(5)).isAir()){
            if(this.gargoyle.distanceToSqr(this.gargoyle.getTarget())>100){
                this.gargoyle.getNavigation().moveTo(this.gargoyle.getTarget().getX(), this.gargoyle.getY()+5, this.gargoyle.getTarget().getZ(), 1.7);
            }else{
                this.gargoyle.getNavigation().moveTo(this.gargoyle.getX(), this.gargoyle.getY()+5, this.gargoyle.getZ(), 1.7);
            }
        }else{
            if(this.gargoyle.distanceToSqr(this.gargoyle.getTarget())>100){
                this.gargoyle.getNavigation().moveTo(this.gargoyle.getTarget().getX(), this.gargoyle.getY(), this.gargoyle.getTarget().getZ(), 1.7);
            }
        }
    }
    @Override
    public void stop() {
        super.stop();
        this.gargoyle.setAttacking(false);
    }
}
