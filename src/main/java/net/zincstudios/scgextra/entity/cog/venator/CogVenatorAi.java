package net.zincstudios.scgextra.entity.cog.venator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.*;

public class CogVenatorAi {

    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super CogVenatorEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.VERY_LONG_RANGE_PLAYER.get(),
            SensorType.HURT_BY,
            ModBrainSensors.CUSTOM_GUN.get()
    );

    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.AVOID_TARGET,
            ModBrainMemories.AIM_TICKS.get(),
            ModBrainMemories.SIMULATED_GUN.get(),
            ModBrainMemories.WEAPON_IDEAL_RANGE.get(),
            ModBrainMemories.WEAPON_MAX_RANGE.get()
    );

    protected static Brain<?> makeBrain(CogVenatorEntity mob, Brain<CogVenatorEntity> brain) {
        BrainCommons.initCoreActivity(brain);
        BrainCommons.initIdleActivity(brain);
        initFightActivity(mob, brain);
        initAvoidActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initFightActivity(CogVenatorEntity mob, Brain<CogVenatorEntity> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.FIGHT, BrainUtils.createPriorityPairs(10, ImmutableList.of(
                StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                new AvoidTargetIfClose(10),
                new WalkUpToIdealRange(1.0F),
                new AimWhenNotWalking(),
                new ShootTarget(30)
                )), ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                ), ImmutableSet.of(
                        ModBrainMemories.AIM_TICKS.get()
                )
        );

    }

    private static void initAvoidActivity(Brain<CogVenatorEntity> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(
                        new SetToSprint(),
                        SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1F, 15, true)
                ),
                MemoryModuleType.AVOID_TARGET
        );
    }

    public static void updateActivity(Mob mob) {
        mob.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
                Activity.AVOID,
                Activity.FIGHT,
                Activity.IDLE
        ));
    }

}
