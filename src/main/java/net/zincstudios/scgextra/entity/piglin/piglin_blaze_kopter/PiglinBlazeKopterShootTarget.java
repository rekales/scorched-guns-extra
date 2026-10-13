package net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.entity.common.brain.AccuracyBuildup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.gun.MarkovTriggerSampler;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import net.zincstudios.scgextra.entity.common.gun.TriggerStateSampler;
import net.zincstudios.scgextra.sounds.PiglinSounds;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PiglinBlazeKopterShootTarget extends Behavior<Mob> {

    public static final float DEFAULT_ACCURACY = 1.6F;
    private final int aimThreshold;
    private final BiFunction<LivingEntity, Boolean, Float> accuracyFunc;
    private final Predicate<Mob> canShoot;
    private final TriggerStateSampler triggerSampler;
    private boolean played = false;

    public PiglinBlazeKopterShootTarget(int aimThreshold) {
        this(aimThreshold, DEFAULT_ACCURACY);
    }

    public PiglinBlazeKopterShootTarget(int aimThreshold, float accuracy) {
        this(
                aimThreshold,
                new AccuracyBuildup(0.0f, 2.4f, 0.05f),
                entity -> true,
                new MarkovTriggerSampler(0.93f, 0.94f, 15, 60)
        );
    }

    public PiglinBlazeKopterShootTarget(int aimThreshold, BiFunction<LivingEntity, Boolean, Float> accuracyFunc, Predicate<Mob> canShoot, TriggerStateSampler triggerSampler) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.AIM_TICKS.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.SIMULATED_GUN.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.aimThreshold = aimThreshold;
        this.accuracyFunc = accuracyFunc;
        this.canShoot = canShoot;
        this.triggerSampler = triggerSampler;
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

        if (aimTicks >= this.aimThreshold && this.triggerSampler.next(entity.getRandom())) {
            if(!this.played){
                entity.playSound(PiglinSounds.PIGLIN_BLAZE_KOPTER_GUN.get());
                this.played = true;
            }
            simGun.tickFire(entity, targetPos, this.accuracyFunc.apply(entity, true), true);
            if(entity instanceof PiglinBlazeKopterEntity pbke)pbke.setShooting(true);
        } else {
            this.played = false;
            simGun.tickFire(entity, targetPos, this.accuracyFunc.apply(entity, false), false);
            if(entity instanceof PiglinBlazeKopterEntity pbke)pbke.setShooting(false);
        }
    }
}
