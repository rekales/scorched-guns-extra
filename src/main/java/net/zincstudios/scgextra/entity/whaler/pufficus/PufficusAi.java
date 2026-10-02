package net.zincstudios.scgextra.entity.whaler.pufficus;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.*;

public class PufficusAi {
    static final ImmutableList<? extends SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.MEDIUM_RANGE_PLAYER.get(),
            SensorType.HURT_BY,
            ModBrainSensors.HELD_GUN.get()
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
            ModBrainMemories.DELAYED_MELEE.get(),
            ModBrainMemories.NET_TICKS.get(),
            ModBrainMemories.ABILITY_STATE.get()
    );

    public static <T extends PathfinderMob> Brain<?> makeBrain(T mob, Brain<T> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(mob, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static <T extends PathfinderMob> void initFightActivity(T mob, Brain<T> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                new DelayedMeleeAttackWithEffects(
                    PufficusEntity.MELEE_DAMAGE_DELAY, PufficusEntity.MELEE_DURATION, 
                    3f, 
                    10,
                    List.of(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        MobEffects.POISON
                    )
                ),
                new ThrowNet(
                    PufficusEntity.NET_DELAY, 
                    PufficusEntity.NET_DURATION, 
                    6f,
                    200
                )
        ), MemoryModuleType.ATTACK_TARGET);
    }
    public static void initCoreActivity(Brain<? extends Mob> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink()
        ));
    }
    public static void initIdleActivity(Brain<? extends PathfinderMob> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                StartAttacking.create(BrainUtils::getHurtByNonFriendlies),
                StartAttacking.create(BrainUtils::findNearestVisibleAttackablePlayer),
                StartAttacking.create(BrainUtils::findNearestAttackableFactionEnemy),
                new RunOne<>(ImmutableList.of(
                        Pair.of(RandomStroll.stroll(1.0F, true), 2),
                        Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
                        Pair.of(new DoNothing(30, 60), 1)
                ))
        ));
    }
}
