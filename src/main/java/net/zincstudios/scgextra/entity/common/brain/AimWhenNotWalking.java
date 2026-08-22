package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.ModBrainMemories;

import java.util.function.Predicate;

import static net.zincstudios.scgextra.entity.ModBrainMemories.MAX_AIM_TICKS;
import static net.zincstudios.scgextra.entity.ModBrainMemories.MIN_AIM_TICKS;

public class AimWhenNotWalking extends Behavior<LivingEntity> {

    private final Predicate<LivingEntity> canAim;

    public AimWhenNotWalking() {
        this(entity -> true);
    }

    public AimWhenNotWalking(Predicate<LivingEntity> canAim) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                ModBrainMemories.AIM_TICKS.get(), MemoryStatus.REGISTERED
        ));
        this.canAim = canAim;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
        return this.canAim.test(entity);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        return this.canAim.test(entity)
                && entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void stop(ServerLevel level, LivingEntity entity, long gameTime) {
        if (!this.timedOut(gameTime)) {
            entity.getBrain().setMemory(ModBrainMemories.AIM_TICKS.get(), 0);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();

        if (!brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new PatchedEntityTracker(target, true));
        }

        int aimTicks = brain.getMemory(ModBrainMemories.AIM_TICKS.get()).orElse(0);
        if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
            aimTicks = 0;
        } else {
            boolean lineOfSight = entity instanceof Mob mob
                    ? mob.getSensing().hasLineOfSight(target) : entity.hasLineOfSight(target);
            aimTicks += lineOfSight ? 1 : -1;
            aimTicks = Mth.clamp(aimTicks, MIN_AIM_TICKS, MAX_AIM_TICKS);
        }

        brain.setMemory(ModBrainMemories.AIM_TICKS.get(), aimTicks);
    }
}
