package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.zincstudios.scgextra.entity.ModBrainMemories;

import java.util.function.Function;

public class WalkUpToIdealRange extends Behavior<LivingEntity> {

    private final Function<LivingEntity, Float> speedModifier;

    public WalkUpToIdealRange(Function<LivingEntity, Float> speedModifier) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.WEAPON_IDEAL_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.WEAPON_MAX_RANGE.get(), MemoryStatus.VALUE_PRESENT
        ), 20);
        this.speedModifier = speedModifier;
    }

    public WalkUpToIdealRange(float speedModifier) {
        this(entity -> speedModifier);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.hasMemoryValue(ModBrainMemories.WEAPON_IDEAL_RANGE.get())
                && brain.hasMemoryValue(ModBrainMemories.WEAPON_MAX_RANGE.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
        float approachDist = brain.getMemory(ModBrainMemories.WEAPON_IDEAL_RANGE.get()).get();
        float weaponRange = brain.getMemory(ModBrainMemories.WEAPON_MAX_RANGE.get()).get();

        if (!entity.closerThan(target, weaponRange)) {
            if (!brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
                brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(
                        new EntityTracker(target, false), this.speedModifier.apply(entity), (int) approachDist
                ));
            }
            return;
        }

        if (entity.closerThan(target, approachDist)) {
            if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            }
        }
    }

}
