package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;

import java.util.List;

// Separated checking and alerting because this can be either on an isolated activity or run concurrently with other behaviors
public class AlertNearbyFactionMobs extends Behavior<Mob> {

    public static final String ABILITY_ID = "alert_faction_mobs";

    public AlertNearbyFactionMobs() {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.VALUE_ABSENT,
                ModBrainMemories.TO_ALERT.get(), MemoryStatus.VALUE_PRESENT
        ), 600);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        return mob.getBrain().hasMemoryValue(ModBrainMemories.TO_ALERT.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // already handled on hasRequiredMemories
    @Override
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        List<LivingEntity> entities = brain.getMemory(ModBrainMemories.TO_ALERT.get()).get();
        for (LivingEntity other : entities) {

            LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
            BrainUtils.setTarget(other, target);
        }

        long duration = brain.getTimeUntilExpiry(ModBrainMemories.TO_ALERT.get());
        brain.setMemoryWithExpiry(ModBrainMemories.ABILITY_STATE.get(),
                new AbilityState(ABILITY_ID, gameTime, gameTime + duration), duration);
    }

    @Override
    protected void stop(ServerLevel level, Mob mob, long gameTime) {
        mob.getBrain().eraseMemory(ModBrainMemories.TO_ALERT.get());
        mob.getBrain().eraseMemory(ModBrainMemories.ABILITY_STATE.get());
    }
}

//brain.setMemory(ModBrainMemories.TO_ALERT.get(), new ArrayList<>())
//brain.getMemory(ModBrainMemories.TO_ALERT.get())