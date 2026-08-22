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

/**
 * For incrementing AIM_TICKS as long as the target is visible.
 * Unlike ApproachTargetAndAim, it can be used to make the mob shoot while moving
 */
public class AimWhenTargetVisible extends Behavior<Mob> {

    private final Predicate<Mob> canAim;

    public AimWhenTargetVisible() {
        this(mob -> true);
    }

    public AimWhenTargetVisible(Predicate<Mob> canAim) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.AIM_TICKS.get(), MemoryStatus.REGISTERED
        ));
        this.canAim = canAim;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        return this.canAim.test(mob);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        return this.canAim.test(mob)
                && mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void stop(ServerLevel level, Mob mob, long gameTime) {
        if (!this.timedOut(gameTime)) {
            mob.getBrain().setMemory(ModBrainMemories.AIM_TICKS.get(), 0);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();

        if (!brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new PatchedEntityTracker(target, true));
        }

        int aimTicks = brain.getMemory(ModBrainMemories.AIM_TICKS.get()).orElse(0);
        aimTicks += mob.hasLineOfSight(target) ? 1 : -1;
        aimTicks = Mth.clamp(aimTicks, MIN_AIM_TICKS, MAX_AIM_TICKS);

        brain.setMemory(ModBrainMemories.AIM_TICKS.get(), aimTicks);
    }
}
