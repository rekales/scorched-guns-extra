package net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.AimWhenNotWalking;
import net.zincstudios.scgextra.entity.common.brain.ApproachTargetIfCannotAim;
import net.zincstudios.scgextra.entity.common.brain.AttackLastHurtIfNear;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.brain.BrainUtils;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class PiglinBlazeKopterAi {
    private static final ImmutableList<? extends SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.EXTREMELY_LONG_RANGE_PLAYER.get(),
            SensorType.HURT_BY
    );

    private static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            ModBrainMemories.AIM_TICKS.get(),
            ModBrainMemories.WEAPON_IDEAL_RANGE.get(),
            ModBrainMemories.WEAPON_MAX_RANGE.get()
    );

    public static <T extends PathfinderMob> Brain<?> makeBrain(T mob, Brain<T> brain) {
        BrainCommons.initCoreActivity(brain);
        BrainCommons.initIdleActivity(brain);
        initFightActivity(mob, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static <T extends PathfinderMob> Brain.Provider<T> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static <T extends PathfinderMob> void initFightActivity(T mob, Brain<T> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.FIGHT, BrainUtils.createPriorityPairs(10,
                ImmutableList.of(
                        StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                        AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                        new ApproachTargetIfCannotAim(1F),
                        new AimWhenNotWalking()
                )), ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                ), ImmutableSet.of(
                        ModBrainMemories.AIM_TICKS.get()
                )
        );
    }
}