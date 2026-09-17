package net.zincstudios.scgextra.entity.wreckers.wrecker_biker;

import java.util.ArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.zincstudios.scgextra.sounds.WreckersSounds;
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
        this.biker.setCircling(true);
    }

    @Override
    public void tick() {
        super.tick();
        if(this.biker.level().isClientSide())return;
        LivingEntity target = this.biker.getTarget();
        if(this.points.isEmpty()){
            this.biker.getNavigation().moveTo(target, 1.5);
            if(this.biker.distanceToSqr(target)<=5){
                ThrowableMolotovCocktailEntity cocktail = new ThrowableMolotovCocktailEntity(this.biker.level(), this.biker, 20);
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
                this.biker.playSound(WreckersSounds.WRECKER_BIKER_HIT.get());
                this.biker.level().addFreshEntity(cocktail);
                this.biker.doHurtTarget(target);
                //move away to not get hit
                AABB box = this.biker.getBoundingBox().inflate(20);
                this.biker.getNavigation().moveTo(
                    this.biker.getRandom().nextInt((int)box.minX, (int)box.maxX), 
                    this.biker.getY(),
                    this.biker.getRandom().nextInt((int)box.minZ, (int)box.maxZ), 
                    1.1
                );
                this.charged = true;
            }
        }else{
            BlockPos pos = this.points.get(0);
            if(this.biker.distanceToSqr(pos.getX(), this.biker.getY(), pos.getZ())>5){
                this.biker.getNavigation().moveTo(pos.getX(), 300/* Can't get stuck if sky's the limit */, pos.getZ(), 1.05);
            }else{
                this.points.remove(0);
            }
        }
    }
    @Override
    public void stop() {
        super.stop();
        this.biker.setCircling(false);
    }
}
