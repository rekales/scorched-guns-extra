package net.zincstudios.scgextra.entity.fac.lion;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.ModBrainMemories;

import javax.annotation.ParametersAreNonnullByDefault;

// Because sensor types don't allow parameters
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WeaponRangeOverride extends Behavior<Mob> {

    private final float idealRange;
    private final float maxRange;

    public WeaponRangeOverride(float idealRange, float maxRange) {
        super(ImmutableMap.of(
                ModBrainMemories.SIMULATED_GUN.get(), MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.WEAPON_IDEAL_RANGE.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.WEAPON_MAX_RANGE.get(), MemoryStatus.REGISTERED
        ), 20);
        this.idealRange = idealRange;
        this.maxRange = maxRange;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        Brain<?> brain = mob.getBrain();
        if (!brain.isMemoryValue(ModBrainMemories.WEAPON_IDEAL_RANGE.get(), this.idealRange)) {
            brain.setMemory(ModBrainMemories.WEAPON_IDEAL_RANGE.get(), this.idealRange);
        }
        if (!brain.isMemoryValue(ModBrainMemories.WEAPON_MAX_RANGE.get(), this.maxRange)) {
            brain.setMemory(ModBrainMemories.WEAPON_MAX_RANGE.get(), this.maxRange);
        }

        return false;
    }
}
