package net.zincstudios.scgextra.entity.piglin.piglin_strider_rider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.AttackLastHurtIfNear;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.brain.BrainUtils;
import net.zincstudios.scgextra.entity.common.brain.DelayedMeleeAttack;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class PiglinStriderRiderAi {
    private static final ImmutableList<? extends SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.MEDIUM_RANGE_PLAYER.get(),
            SensorType.HURT_BY,
            ModBrainSensors.HELD_GUN.get()
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
            ModBrainMemories.DELAYED_MELEE.get()
    );

    public static <T extends PathfinderMob> Brain<?> makeBrain(T mob, Brain<T> brain) {
        BrainCommons.initCoreActivity(brain);
        BrainCommons.initIdleActivity(brain);
        PiglinStriderRiderAi.initFightActivity(mob, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static <T extends PathfinderMob> Brain.Provider<T> brainProvider() {
        return Brain.provider(PiglinStriderRiderAi.MEMORY_TYPES, PiglinStriderRiderAi.SENSOR_TYPES);
    }

    public static <T extends PathfinderMob> void initFightActivity(T mob, Brain<T> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2F),
                new DelayedMeleeAttack(PiglinStriderRiderEntity.MELEE_DAMAGE_DELAY, PiglinStriderRiderEntity.MELEE_DURATION, 2f, 20)
        ), MemoryModuleType.ATTACK_TARGET);
    }
}
