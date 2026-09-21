package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.SCGExtra;

public class Slam <T extends ArmoredWhaleEntity> extends Behavior<T>{

    private final int slamUpTicks;
    private final int airTicks;
    private final int slamDownTicks;
    private final int cooldownDuration;
    private int cooldown = 0;
    private long runsUntil = 0;

    public Slam(int slamUpTicks, int airTicks, int slamDownTicks, int cooldownDuration) {
        super(ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ), slamUpTicks+airTicks+slamDownTicks);
        this.slamUpTicks = slamUpTicks;
        this.airTicks = airTicks;
        this.slamDownTicks = slamDownTicks;
        this.cooldownDuration = cooldownDuration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T owner) {
        if(this.cooldown > 0)this.cooldown--;
        return this.cooldown <= 0 && owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent() && owner.distanceToSqr(owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get()) <= 100;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, T entity, long gameTime) {
        return gameTime < this.runsUntil;
    }

    @Override
    protected void start(ServerLevel level, T entity, long gameTime) {
        this.cooldown = this.cooldownDuration;
        this.runsUntil = gameTime+this.slamUpTicks+this.airTicks+this.slamDownTicks;
    }
    
    @Override
    protected void tick(ServerLevel level, T owner, long gameTime) {
        long hasRanFor = gameTime-(runsUntil-(this.slamUpTicks+this.airTicks+this.slamDownTicks));
        if(hasRanFor == 0){
            owner.triggerAnim("attack", "slam_up");
        }
        if(hasRanFor == this.slamUpTicks){
            owner.setSlamming(true);
            int seconds = airTicks/20;
            int blocks = (78*seconds)/2;
            int l = calculateLevel(blocks);
            owner.addEffect(new MobEffectInstance(MobEffects.JUMP, this.airTicks, l));
            owner.getJumpControl().jump();
        }
        if(hasRanFor == this.slamUpTicks+this.airTicks){
            owner.setSlamming(false);
            owner.triggerAnim("attack", "slam_down");
        }
        if(hasRanFor == (this.slamUpTicks+this.airTicks+this.slamDownTicks)-1){
            List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(20, 0, 20), (e)->!e.is(owner));
            for(LivingEntity entity : list){
                if(owner.distanceToSqr(entity) <= 25){
                    entity.hurt(owner.damageSources().genericKill(), 10);
                }
                if(owner.distanceToSqr(entity) <= 100){
                    entity.hurt(owner.damageSources().genericKill(), 10);
                }
            }
        }
    }

    private int calculateLevel(int blocks){
        int level = 0;
        for(int i = 0; i < 255; i++){
            //got the formula from minecraft.wiki/w/Jump_Boost
            int blo = (int)Math.ceil(3.92 * Math.log( 5 * i / 196+31 / 28) / Math.log(0.98) + 5 * i + 21);
            if(blocks < blo){
                level = i;
                break;
            }
        }
        return level;
    }
}
