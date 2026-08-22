package net.zincstudios.scgextra.entity.fac.tank;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.PatchedEntityTracker;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TankCannonFire extends Behavior<FacTankEntity> {

    public static final int DEFAULT_COOLDOWN_DURATION_TICKS = 200;
    public static final String ABILITY_ID = "fac_tank_cannon_fire";

    private final int cooldownDuration;

    private long startTime = 0;  // gameTime timestamp
    private long cooldownEnd = 0;  // gameTime timestamp

    public TankCannonFire() {
        this(DEFAULT_COOLDOWN_DURATION_TICKS);
    }

    public TankCannonFire(int cooldownDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.AIM_TICKS.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED
        ), 40);
        this.cooldownDuration = cooldownDuration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, FacTankEntity mob) {
        int aimTicks = mob.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).orElse(0);
        return aimTicks >= 40 && this.cooldownEnd < level.getGameTime();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, FacTankEntity mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.hasMemoryValue(ModBrainMemories.ABILITY_STATE.get());
    }

    @Override
    protected void start(ServerLevel level, FacTankEntity mob, long gameTime) {
        this.startTime = gameTime;
        mob.getBrain().setMemoryWithExpiry(
                ModBrainMemories.ABILITY_STATE.get(),
                new AbilityState(ABILITY_ID, gameTime, gameTime + Behavior.DEFAULT_DURATION),
                Behavior.DEFAULT_DURATION
        );
    }

    @Override
    protected void stop(ServerLevel level, FacTankEntity mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        this.cooldownEnd = gameTime + this.cooldownDuration;
        brain.eraseMemory(ModBrainMemories.ABILITY_STATE.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, FacTankEntity mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();

        if (!brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new PatchedEntityTracker(target, true));
        }

        if (gameTime-this.startTime < 30) return;

        mob.getMainCannon().tickFire(mob, SimulatedGun.getCenterMassPos(target), 2F, true);
    }

}
