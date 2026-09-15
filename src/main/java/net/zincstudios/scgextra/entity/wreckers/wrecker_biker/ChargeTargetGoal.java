package net.zincstudios.scgextra.entity.wreckers.wrecker_biker;

import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import top.ribs.scguns.entity.throwable.ThrowableMolotovCocktailEntity;

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
        if(this.biker.level().isClientSide())return;
        LivingEntity target = this.biker.getTarget();
        if(this.points.isEmpty()){
            this.biker.getNavigation().moveTo(target, 2);
            if(this.biker.distanceToSqr(target)<=5){
                ThrowableMolotovCocktailEntity cocktail = new ThrowableMolotovCocktailEntity(this.biker.level(), this.biker, 20);
                cocktail.shoot(target.getX(), target.getY()+3, target.getZ(), 0.6f, 0f);
                this.biker.level().addFreshEntity(cocktail);
                this.charged = true;
            }
        }else{
            BlockPos pos = this.points.get(0);
            if(this.biker.distanceToSqr(pos.getX(), pos.getY(), pos.getZ())>5){
                this.biker.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.2);
            }else{
                this.points.remove(0);
            }
        }
    }
}
