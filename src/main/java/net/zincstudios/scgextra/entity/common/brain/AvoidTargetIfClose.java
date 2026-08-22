package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class AvoidTargetIfClose extends Behavior<Mob> {

    public static final int AVOID_TARGET_TTL = 60;

    private final float avoidDist;

    public AvoidTargetIfClose(float avoidDist) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.AVOID_TARGET, MemoryStatus.VALUE_ABSENT
        ), 120);
        this.avoidDist = avoidDist;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // already handled on hasRequiredMemories
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        return mob.closerThan(target, this.avoidDist);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // already handled on hasRequiredMemories
    @Override
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        mob.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, AVOID_TARGET_TTL);
    }
}