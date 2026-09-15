package net.zincstudios.scgextra.entity.wreckers.wrecker_biker;

import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class ChargeTargetGoal extends Goal{

    private final WreckerBikerEntity biker;
    private boolean charged = false;
    private ArrayList<BlockPos> points = new ArrayList<>();

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
        LivingEntity target = this.biker.getTarget();
        this.points.clear();

        this.points.add(new BlockPos((int)target.getX(), (int)target.getY(), (int)(target.getZ()+8)));
        this.points.add(new BlockPos((int)target.getX(), (int)target.getY(), (int)(target.getZ()-8)));
        this.points.add(new BlockPos((int)(target.getX()+8), (int)target.getY(), (int)target.getZ()));
        this.points.add(new BlockPos((int)(target.getX()-8), (int)target.getY(), (int)target.getZ()));
        this.biker.getNavigation().stop();
    }

    @Override
    public void tick() {
        super.tick();
        if(this.points.isEmpty()){
            this.biker.getNavigation().moveTo(this.biker.getTarget(), 1);
            this.charged = true;
        }else{
            BlockPos pos = this.points.get(0);
            if(this.biker.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())>5){
                this.biker.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1);
            }else{
                this.biker.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1);
                this.points.remove(0);
            }
        }
    }
}
