package net.zincstudios.scgextra.entity.neutral.overworld.anormaly;

import net.minecraft.world.entity.ai.goal.Goal;

public class AnormalyAttackGoal extends Goal{

    private final AnormalyEntity anormaly;
    private final int cooldownDuration;
    private int cooldown = 0;
    private final int delay;
    private final int total;
    private long start = 0;

    public AnormalyAttackGoal(AnormalyEntity entity, int damageDelay, int totalDuration, int cooldown){
        this.anormaly = entity;
        this.delay = damageDelay;
        this.total = totalDuration;
        this.cooldownDuration = cooldown;
    }

    @Override
    public boolean canUse() {
        if(this.cooldown > 0)this.cooldown--;
        return this.cooldown<= 0 && this.anormaly.getTarget()!=null && this.anormaly.distanceToSqr(this.anormaly.getTarget()) <= 9;
    }

    @Override
    public void start() {
        super.start();
        this.cooldown = this.cooldownDuration;
        this.start = this.anormaly.level().getGameTime();
        this.anormaly.triggerAnim("main", "attack");
    }

    @Override
    public boolean canContinueToUse(){
        return this.anormaly.getTarget()!=null && this.anormaly.level().getGameTime()-this.start < this.total;
    }

    @Override
    public void tick(){
        super.tick();
        if(this.anormaly.level().getGameTime()-this.start == this.delay && this.anormaly.getTarget()!=null){
            this.anormaly.doHurtTarget(this.anormaly.getTarget());
        }
    }
    
}
