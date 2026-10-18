package net.zincstudios.scgextra.entity.piglin.pigdread;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class Summon extends Behavior<Mob>{

    private final List<EntityType<?>> ENTITES;
    private int cooldown = 0;
    private final int cooldownDuration;

    public Summon(int cooldown, List<EntityType<?>> entities) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ));
        this.ENTITES = entities;
        this.cooldownDuration = cooldown;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
        if(this.cooldown>0)this.cooldown--;
        return this.cooldown <= 0;
    }
    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        this.cooldown = this.cooldownDuration;
        for(EntityType<?> en : ENTITES){
            en.spawn(level, entity.blockPosition(), MobSpawnType.MOB_SUMMONED);
        }
    }
}
