package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.ModBrainMemories;

public class CheckHeadshotStun extends Behavior<LivingEntity> {

    private final int headshotThreshold;
    private final int stunDuration;
    private final int stunCooldown;

    private int headshots = 0;
    private int lastHeadshotCount = 0;

    public CheckHeadshotStun(int headshotThreshold, int headshotsDuration, int stunCooldown) {
        super(ImmutableMap.of(
                ModBrainMemories.HEADSHOT_COUNT.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.STUNNED.get(), MemoryStatus.VALUE_ABSENT,
                ModBrainMemories.STUNNED_COOLING_DOWN.get(), MemoryStatus.REGISTERED
        ));
        this.headshotThreshold = headshotThreshold;
        this.stunDuration = headshotsDuration;
        this.stunCooldown = stunCooldown;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        int currentHeadshotCount = brain.getMemory(ModBrainMemories.HEADSHOT_COUNT.get()).orElse(0);
        if (currentHeadshotCount != this.lastHeadshotCount) {
            this.headshots += Math.max(0, currentHeadshotCount - this.lastHeadshotCount);
            this.lastHeadshotCount = currentHeadshotCount;
            return !brain.hasMemoryValue(ModBrainMemories.STUNNED_COOLING_DOWN.get())
                    && this.headshots >= this.headshotThreshold; // Only triggered when entity gets headshotted
        }

        return false;
    }

    @Override
    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        brain.setMemoryWithExpiry(ModBrainMemories.STUNNED.get(), true, this.stunDuration);
        brain.setMemoryWithExpiry(ModBrainMemories.STUNNED_COOLING_DOWN.get(), true,
                this.stunDuration + this.stunCooldown);
        this.headshots = 0;
    }
}
