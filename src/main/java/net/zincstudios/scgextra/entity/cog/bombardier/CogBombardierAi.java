package net.zincstudios.scgextra.entity.cog.bombardier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainActivities;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.brain.*;
import net.zincstudios.scgextra.entity.common.gun.IdentityTriggerSampler;

public class CogBombardierAi {

    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super CogBombardierEntity>>> SENSOR_TYPES = ImmutableList.of(
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
            ModBrainMemories.WEAPON_MAX_RANGE.get(),
            ModBrainMemories.STUNNED.get(),
            ModBrainMemories.STUNNED_COOLING_DOWN.get(),
            ModBrainMemories.HEADSHOT_COUNT.get(),
            ModBrainMemories.ABILITY_STATE.get(),
            ModBrainMemories.TO_ALERT.get()
    );

    protected static Brain<?> makeBrain(CogBombardierEntity mob, Brain<CogBombardierEntity> brain) {
        BrainCommons.initCoreWithStunActivity(brain, MobUtil.DEFAULT_STUN_DURATION);
        BrainCommons.initIdleActivity(brain);
        initFightActivity(mob, brain);
        BrainCommons.initAlertActivity(brain);
        BrainCommons.initAvoidActivity(brain, 14);
        BrainCommons.initStunnedActivity(brain);

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initFightActivity(CogBombardierEntity mob, Brain<CogBombardierEntity> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.FIGHT, BrainUtils.createPriorityPairs(10, ImmutableList.of(
                        StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                        AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                        new RunOneOrdered<>(ImmutableList.of(
                                new AvoidTargetIfClose(10),
                                new CheckShouldAlert(CogBombardierEntity.ALERT_ANIM_TICKS)
                        )),
                        new WalkUpToIdealRange(1.0F),
                        new AimWhenNotWalking(),
                        new ShootTarget(30, (simGun, entity) -> 2.4F,
                                entity -> true, new IdentityTriggerSampler())
                )), ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                ), ImmutableSet.of(
                        ModBrainMemories.AIM_TICKS.get()
                )
        );
    }

    public static void updateActivity(CogBombardierEntity mob) {
        Brain<CogBombardierEntity> brain = mob.getBrain();
        Activity oldActivity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(
                ModBrainActivities.STUNNED.get(),
                Activity.AVOID,
                ModBrainActivities.ALERT.get(),
                Activity.FIGHT,
                Activity.IDLE
        ));

        Activity newActivity = brain.getActiveNonCoreActivity().orElse(null);
        if (oldActivity != newActivity
                && (newActivity == ModBrainActivities.STUNNED.get()
                || newActivity == ModBrainActivities.ALERT.get())) {
            brain.stopAll((ServerLevel) mob.level(), mob);
            mob.getNavigation().stop();
        }
    }
}
