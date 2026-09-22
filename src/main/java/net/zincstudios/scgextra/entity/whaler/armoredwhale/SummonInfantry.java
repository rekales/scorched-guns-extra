package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.whaler.WhalerEntities;

public class SummonInfantry <T extends ArmoredWhaleEntity> extends Behavior<T>{

    private final int cooldownDuration;
    private final int reinforcementsN;
    private int cooldown = 0;
    
    public SummonInfantry(int cooldownDuration, int numberOfReinforcements) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.cooldownDuration = cooldownDuration;
        this.reinforcementsN = numberOfReinforcements;
    }
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T owner) {
        if(this.cooldown > 0)this.cooldown--;
        return this.cooldown <= 0;
    }
    @Override
    protected boolean canStillUse(ServerLevel level, T entity, long gameTime) {
        return false;
    }
    @Override
    protected void start(ServerLevel level, T entity, long gameTime) {
        this.cooldown = this.cooldownDuration;
        for(int i = 0; i < this.reinforcementsN; i++){
            WhalerEntities.FISH_FOLK.get().spawn(level, entity.blockPosition(), MobSpawnType.REINFORCEMENT);
        }
    }
}
