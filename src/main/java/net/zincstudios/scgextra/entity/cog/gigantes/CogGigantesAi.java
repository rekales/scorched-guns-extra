package net.zincstudios.scgextra.entity.cog.gigantes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.*;

public class CogGigantesAi {

    private static final ImmutableList<? extends SensorType<? extends Sensor<? super Mob>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.MEDIUM_RANGE_PLAYER.get(),
            SensorType.HURT_BY,
            ModBrainSensors.CUSTOM_GUN.get()
    );

    private static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.LOOK_TARGET,
            ModBrainMemories.FLY_DIRECT_TARGET.get(),
            ModBrainMemories.AIM_TICKS.get(),
            ModBrainMemories.SIMULATED_GUN.get(),
            ModBrainMemories.WEAPON_IDEAL_RANGE.get(),
            ModBrainMemories.WEAPON_MAX_RANGE.get(),
            ModBrainMemories.STUNNED.get(),
            ModBrainMemories.STUNNED_COOLING_DOWN.get(),
            ModBrainMemories.HEADSHOT_COUNT.get()
    );

    public static <T extends Mob> Brain<?> makeBrain(T mob, Brain<T> brain) {
        initCoreActivity(brain, CogGigantesEntity.STUN_DURATION);
        initIdleActivity(brain);
//        initFightActivity(mob, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static <T extends Mob> Brain.Provider<T> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static void initCoreActivity(Brain<? extends Mob> brain, int stunDuration) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new FlyDirectToTargetSink(),
                new CheckHeadshotStun(5, stunDuration, 200)
        ));
    }

    public static void initIdleActivity(Brain<? extends Mob> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                StartAttacking.create(BrainUtils::getHurtByNonFriendlies),
                StartAttacking.create(BrainUtils::findNearestVisibleAttackablePlayer),
                StartAttacking.create(BrainUtils::findNearestAttackableFactionEnemy),
                new RunOne<>(ImmutableList.of(
                        Pair.of(FlightDirectStroll.create(12, 1), 2),
                        Pair.of(new DoNothing(30, 60), 3)
                ))
        ));
    }

//    public static <T extends Mob> void initFightActivity(T mob, Brain<T> brain) {
//        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
//                StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
//                AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
//                GetCloseToTarget.create(5.0F, 1),
//                new AimWhenTargetVisible(),
//                new ShootTarget(20, (simGun, entity) -> 1.1F, entity -> true,
//                        new IntervalTriggerSampler(30, 50, 5, 30))
//        ), MemoryModuleType.ATTACK_TARGET);
//    }

}
