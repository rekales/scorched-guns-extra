package net.zincstudios.scgextra.entity.piglin.common;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class Aggro extends Behavior<Mob>{
    private final int ticks;

    public Aggro(int ticks) {
        super(ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ), ticks);
        this.ticks = ticks;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
        return owner instanceof AggroableMob am ? !am.isAggro() : false;
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        if(entity instanceof AggroableMob am){
            am.setAggro(this.ticks);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return true;
    }

    @Override
    protected void tick(ServerLevel level, Mob owner, long gameTime) {
        owner.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        owner.getNavigation().stop();
    }
}
