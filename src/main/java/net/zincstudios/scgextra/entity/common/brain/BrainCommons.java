package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainActivities;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;

import java.util.Optional;

public final class BrainCommons {

    public static void initCoreActivity(Brain<? extends Mob> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink()
        ));
    }

    public static void initCoreWithStunActivity(Brain<? extends Mob> brain, int stunDuration) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new CheckHeadshotStun(5, stunDuration, 200)
        ));
    }

    // Not for flying and swimming mobs
    public static void initIdleActivity(Brain<? extends PathfinderMob> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                StartAttacking.create(BrainUtils::getHurtByNonFriendlies),
                StartAttacking.create(BrainUtils::findNearestVisibleAttackablePlayer),
                StartAttacking.create(BrainUtils::findNearestAttackableFactionEnemy),
                new RunOne<>(ImmutableList.of(
                        Pair.of(RandomStroll.stroll(1.0F), 2),
                        Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
                        Pair.of(new DoNothing(30, 60), 1)
                ))
        ));
    }

    public static void initStunnedActivity(Brain<? extends Mob> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(ModBrainActivities.STUNNED.get(), 10, ImmutableList.of(
                        new HandleStunnedVisuals()
                ), ModBrainMemories.STUNNED.get()
        );
    }

    public static void initAvoidActivity(Brain<? extends PathfinderMob> brain, float fleeDistance) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(
                        new SetToSprint(),
                        SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1F, (int) fleeDistance, true)
                ),
                MemoryModuleType.AVOID_TARGET
        );
    }

    public static void initAlertActivity(Brain<? extends Mob> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(ModBrainActivities.ALERT.get(), 10, ImmutableList.of(
                        new AlertNearbyFactionMobs()
                ), ModBrainMemories.TO_ALERT.get()
        );
    }

    public static void updateActivity(Mob mob) {
        mob.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }

    public static void updateAimingAggressive(Mob mob) {
        boolean aggressive = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()
                && mob.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent();
        if (mob.isAggressive() != aggressive) {
            mob.setAggressive(aggressive);
        }
    }

    public static void updateHasTargetAggressive(Mob mob) {
        boolean aggressive = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent();
        if (mob.isAggressive() != aggressive) {
            mob.setAggressive(aggressive);
        }
    }

    public static void updateMaxRangeAggressive(Mob mob) {
        boolean aggressive;
        Optional<LivingEntity> target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
        //noinspection OptionalIsPresent
        if (target.isPresent()) {
            aggressive = mob.closerThan(target.get(), mob.getBrain().getMemory(ModBrainMemories.WEAPON_MAX_RANGE.get()).orElse(16F));
        } else {
            aggressive = false;
        }

        if (mob.isAggressive() != aggressive) {
            mob.setAggressive(aggressive);
        }
    }

    public static class BasicGunner {
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
                ModBrainMemories.AIM_TICKS.get(),
                ModBrainMemories.SIMULATED_GUN.get(),
                ModBrainMemories.WEAPON_IDEAL_RANGE.get(),
                ModBrainMemories.WEAPON_MAX_RANGE.get()
        );

        public static <T extends PathfinderMob> Brain<?> makeBrain(T mob, Brain<T> brain) {
            BrainCommons.initCoreActivity(brain);
            BrainCommons.initIdleActivity(brain);
            BrainCommons.BasicGunner.initFightActivity(mob, brain);
            brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
            brain.setDefaultActivity(Activity.IDLE);
            brain.useDefaultActivity();
            return brain;
        }

        public static <T extends PathfinderMob> Brain.Provider<T> brainProvider() {
            return Brain.provider(BrainCommons.BasicGunner.MEMORY_TYPES, BrainCommons.BasicGunner.SENSOR_TYPES);
        }

        public static <T extends PathfinderMob> void initFightActivity(T mob, Brain<T> brain) {
            brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                    StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                    AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                    new ApproachTargetIfCannotAim(1.0F),
                    new AimWhenNotWalking(),
                    new ShootTarget(20, 1.6F)
            ), MemoryModuleType.ATTACK_TARGET);
        }
    }
}
