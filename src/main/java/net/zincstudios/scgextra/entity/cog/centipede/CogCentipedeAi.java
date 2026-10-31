package net.zincstudios.scgextra.entity.cog.centipede;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainActivities;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.*;
import net.zincstudios.scgextra.entity.common.gun.IdentityTriggerSampler;

public class CogCentipedeAi {

    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super CogCentipedeEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.LONG_RANGE_PLAYER.get(),
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
            MemoryModuleType.ATTACK_COOLING_DOWN,
            ModBrainMemories.AIM_TICKS.get(),
            ModBrainMemories.WEAPON_IDEAL_RANGE.get(),
            ModBrainMemories.WEAPON_MAX_RANGE.get(),
            ModBrainMemories.STUNNED.get(),
            ModBrainMemories.STUNNED_COOLING_DOWN.get(),
            ModBrainMemories.HEADSHOT_COUNT.get(),
            ModBrainMemories.DELAYED_MELEE.get()
    );

    protected static Brain<?> makeBrain(CogCentipedeEntity mob, Brain<CogCentipedeEntity> brain) {
        BrainCommons.initCoreWithStunActivity(brain, CogCentipedeEntity.STUN_DURATION);
        BrainCommons.initIdleActivity(brain);
        initFightActivity(mob, brain);
        BrainCommons.initStunnedActivity(brain);

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initFightActivity(CogCentipedeEntity mob, Brain<CogCentipedeEntity> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.FIGHT, BrainUtils.createPriorityPairs(10,
                ImmutableList.of(
                        StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                        AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                        new SlamAttack(CogCentipedeEntity.SLAM_DAMAGE_DELAY, CogCentipedeEntity.SLAM_DURATION, 4, 80),
                        new ApproachTargetIfCannotAim(1.0F),
                        new AimWhenNotWalking(),
                        new ShootTarget(20,
                                (entity, firing) -> 3.2F,
                                entity -> !entity.getBrain().hasMemoryValue(ModBrainMemories.DELAYED_MELEE.get()),
                                new IdentityTriggerSampler())
                )), ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                ), ImmutableSet.of(
                        ModBrainMemories.AIM_TICKS.get()
                )
        );
    }

    public static void updateActivity(CogCentipedeEntity mob) {
        Brain<CogCentipedeEntity> brain = mob.getBrain();
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
