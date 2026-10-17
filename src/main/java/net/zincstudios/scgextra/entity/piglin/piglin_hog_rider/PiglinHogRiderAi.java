package net.zincstudios.scgextra.entity.piglin.piglin_hog_rider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.zincstudios.scgextra.entity.ModBrainActivities;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.AttackLastHurtIfNear;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.brain.BrainUtils;
import net.zincstudios.scgextra.entity.common.brain.DelayedMeleeAttack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class PiglinHogRiderAi {
        private static final ImmutableList<? extends SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.MEDIUM_RANGE_PLAYER.get(),
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
            ModBrainMemories.DELAYED_MELEE.get(),
            ModBrainMemories.STUNNED.get(),
            ModBrainMemories.ABILITY_STATE.get()
    );

    public static <T extends PathfinderMob> Brain<?> makeBrain(T mob, Brain<T> brain) {
        BrainCommons.initCoreWithStunActivity(brain, PiglinHogRiderEntity.instance.getStunDuration());
        BrainCommons.initIdleActivity(brain);
        BrainCommons.initStunnedActivity(brain);
        PiglinHogRiderAi.initFightActivity(mob, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static <T extends PathfinderMob> Brain.Provider<T> brainProvider() {
        return Brain.provider(PiglinHogRiderAi.MEMORY_TYPES, PiglinHogRiderAi.SENSOR_TYPES);
    }

    public static <T extends PathfinderMob> void initFightActivity(T mob, Brain<T> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2F),
                new DelayedMeleeAttack(PiglinHogRiderEntity.MELEE_DAMAGE_DELAY, PiglinHogRiderEntity.MELEE_DURATION, 4f, 20){
                    @Override
                    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
                        return super.checkExtraStartConditions(level, mob) && !mob.getBrain().hasMemoryValue(ModBrainMemories.ABILITY_STATE.get());
                    }
                    @Override
                    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
                        return super.canStillUse(level, mob, gameTime) && !mob.getBrain().hasMemoryValue(ModBrainMemories.ABILITY_STATE.get());
                    }
                },
                new ChargeEnemy<>(200, PiglinHogRiderEntity.CHARGE_ANIM_START)
        ), MemoryModuleType.ATTACK_TARGET);
    }

    public static void updateActivity(PiglinHogRiderEntity mob) {
        Brain<PiglinHogRiderEntity> brain = mob.getBrain();
        Activity oldActivity = brain.getActiveNonCoreActivity().orElse(null);
        brain.setActiveActivityToFirstValid(ImmutableList.of(
                ModBrainActivities.STUNNED.get(),
                Activity.AVOID,
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
