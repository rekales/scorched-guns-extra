package net.zincstudios.scgextra.entity.cog.trenchbleeder;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class CogTrenchbleederShootTarget  extends Behavior<Mob> {

    public static final float DEFAULT_ACCURACY = 1.6F;
    private final int aimThreshold;
    private final BiFunction<Mob, Boolean, Float> accuracyFunc;
    private final Predicate<Mob> canShoot;

    public CogTrenchbleederShootTarget(int aimThreshold) {
        this(aimThreshold, DEFAULT_ACCURACY);
    }

    public CogTrenchbleederShootTarget(int aimThreshold, float accuracy) {
        this(
                aimThreshold,
                (entity, firing) -> accuracy,
                entity -> true
        );
    }

    public CogTrenchbleederShootTarget(int aimThreshold, BiFunction<Mob, Boolean, Float> accuracyFunc, Predicate<Mob> canShoot) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.AIM_TICKS.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.SIMULATED_GUN.get(), MemoryStatus.VALUE_PRESENT
        ), 48);
        this.aimThreshold = aimThreshold;
        this.accuracyFunc = accuracyFunc;
        this.canShoot = canShoot;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.hasMemoryValue(ModBrainMemories.SIMULATED_GUN.get());
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent"}) // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, Mob entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        if (!canShoot.test(entity)) return;

        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
        int aimTicks = brain.getMemory(ModBrainMemories.AIM_TICKS.get()).orElse(0);
        SimulatedGun simGun = brain.getMemory(ModBrainMemories.SIMULATED_GUN.get()).get();
        Vec3 targetPos = SimulatedGun.getCenterMassPos(target);

        if (aimTicks >= this.aimThreshold) {
            simGun.tickFire(entity, targetPos, this.accuracyFunc.apply(entity, true), true);
        } else {
            simGun.tickFire(entity, targetPos, this.accuracyFunc.apply(entity, false), false);
        }
    }
}