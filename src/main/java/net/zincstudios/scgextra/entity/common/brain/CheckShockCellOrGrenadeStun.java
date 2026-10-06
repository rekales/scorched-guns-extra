package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import top.ribs.scguns.init.ModEffects;

public class CheckShockCellOrGrenadeStun extends Behavior<Mob> {

    private final int shockCellThreshold;
    private final int stunDuration;
    private final int stunCooldown;

    private int shockCellCount = 0;
    private int lastShockCellCount = 0;

    public CheckShockCellOrGrenadeStun(int shockCellThreshold, int headshotsDuration, int stunCooldown) {
        super(ImmutableMap.of(
                ModBrainMemories.SHOCK_CELL_COUNT.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.STUNNED.get(), MemoryStatus.VALUE_ABSENT,
                ModBrainMemories.STUNNED_COOLING_DOWN.get(), MemoryStatus.REGISTERED
        ));
        this.shockCellThreshold = shockCellThreshold;
        this.stunDuration = headshotsDuration;
        this.stunCooldown = stunCooldown;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        Brain<?> brain = mob.getBrain();
        int currentShockCellCount = brain.getMemory(ModBrainMemories.SHOCK_CELL_COUNT.get()).orElse(0);
        if (currentShockCellCount != this.lastShockCellCount) {
            this.shockCellCount += Math.max(0, currentShockCellCount - this.lastShockCellCount);
            this.lastShockCellCount = currentShockCellCount;
            if (!brain.hasMemoryValue(ModBrainMemories.STUNNED_COOLING_DOWN.get())
                    && this.shockCellCount >= this.shockCellThreshold) {
                return true; // Only triggered when entity gets shocked
            }
        }

        return mob.hasEffect(ModEffects.BLINDED.get()) || mob.hasEffect(ModEffects.DEAFENED.get());
    }

    @Override
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        brain.setMemoryWithExpiry(ModBrainMemories.STUNNED.get(), true, this.stunDuration);
        brain.setMemoryWithExpiry(ModBrainMemories.STUNNED_COOLING_DOWN.get(), true,
                this.stunDuration + this.stunCooldown);
        this.shockCellCount = 0;
        mob.removeEffect(ModEffects.BLINDED.get());
        mob.removeEffect(ModEffects.DEAFENED.get());
    }
}
