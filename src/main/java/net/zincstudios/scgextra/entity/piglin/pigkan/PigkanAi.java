package net.zincstudios.scgextra.entity.piglin.pigkan;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.zincstudios.scgextra.entity.ModBrainActivities;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.AttackLastHurtIfNear;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.brain.BrainUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class PigkanAi {
    static final ImmutableList<? extends SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.EXTREMELY_LONG_RANGE_PLAYER.get(),
            SensorType.HURT_BY
    );

    static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            ModBrainMemories.STUNNED.get(),
            ModBrainMemories.STUNNED_COOLING_DOWN.get(),
            ModBrainMemories.HEADSHOT_COUNT.get(),
            ModBrainMemories.DELAYED_MELEE.get(),
            ModBrainMemories.ABILITY_STATE.get(),
            ModBrainMemories.WEAPON_MAX_RANGE.get()
    );

    public static <T extends PathfinderMob> Brain<?> makeBrain(T mob, Brain<T> brain) {
        BrainCommons.initCoreWithStunActivity(brain, PigkanEntity.instance.getStunDuration());
        BrainCommons.initIdleActivity(brain);
        initFightActivity(mob, brain);
        BrainCommons.initStunnedActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static <T extends PathfinderMob> Brain.Provider<T> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static <T extends PathfinderMob> void initFightActivity(T mob, Brain<T> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                new PigkanDelayedMeleeAttack(4f, 10)
        ), MemoryModuleType.ATTACK_TARGET);
    }

    public static void updateActivity(PigkanEntity mob) {
        Brain<PigkanEntity> brain = mob.getBrain();
        Activity oldActivity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(
                ModBrainActivities.STUNNED.get(),
                Activity.FIGHT,
                Activity.IDLE
        ));

        Activity newActivity = brain.getActiveNonCoreActivity().orElse(null);
        if (oldActivity != newActivity && newActivity == ModBrainActivities.STUNNED.get()) {
            brain.stopAll((ServerLevel) mob.level(), mob);
            mob.getNavigation().stop();
        }
    }
}
