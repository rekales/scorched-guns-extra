package net.zincstudios.scgextra.entity.piglin.pigdread;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.zincstudios.scgextra.entity.common.brain.BreakBlocksToTarget;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainActivities;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.AimWhenNotWalking;
import net.zincstudios.scgextra.entity.common.brain.ApproachTargetIfCannotAim;
import net.zincstudios.scgextra.entity.common.brain.AttackLastHurtIfNear;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.brain.BrainUtils;
import net.zincstudios.scgextra.entity.common.brain.DelayedMeleeAttack;
import net.zincstudios.scgextra.entity.piglin.PiglinEntities;
import top.ribs.scguns.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PigdreadAi {
    private static final ImmutableList<? extends SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES,
        ModBrainSensors.VERY_LONG_RANGE_PLAYER.get(),
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
        ModBrainMemories.WEAPON_MAX_RANGE.get(),
        ModBrainMemories.STUNNED.get(),
        ModBrainMemories.STUNNED_COOLING_DOWN.get(),
        ModBrainMemories.HEADSHOT_COUNT.get(),
        ModBrainMemories.DELAYED_MELEE.get(),
        ModBrainMemories.ABILITY_STATE.get()
    );

    public static <T extends PathfinderMob> Brain<?> makeBrain(T mob, Brain<T> brain) {
        BrainCommons.initCoreWithStunActivity(brain, PigdreadEntity.STUN_DURATION);
        BrainCommons.initIdleActivity(brain);
        BrainCommons.initStunnedActivity(brain);
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
                new AimWhenNotWalking(),
                new DelayedMeleeAttack(PigdreadEntity.MELEE_DELAY, PigdreadEntity.MELEE_DURATION, 3f, 20),
                new DelayedMeleeAttackWithEffects(PigdreadEntity.MELEE_DELAY, PigdreadEntity.MELEE_DURATION, 4f, 20, List.of(
                    ModEffects.LACERATED.get(),
                    MobEffects.MOVEMENT_SLOWDOWN
                ),
                List.of(
                    200,
                    200
                )),
                new MountedShotgunShoot(120,13,8),
                new SpeedBuff(300,60),
                new Summon<>(800, List.of(
                    PiglinEntities.PIGKAN.get(),
                    PiglinEntities.PIGLIN_BLAZE_KOPTER.get()
                )),
                new BreakBlocksToTarget(PigdreadAi::canBreakWithShovel, 20)
            )), ImmutableSet.of(
                Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
            ), ImmutableSet.of(
                ModBrainMemories.AIM_TICKS.get()
            )
        );
    }

    public static void updateActivity(PigdreadEntity mob) {
        Brain<PigdreadEntity> brain = mob.getBrain();
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
    private static boolean canBreakWithShovel(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;

        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) return false;  // Unbreakable
        if (hardness >= 5) return false;  // Ex. Iron Block: 5, Stone: 1.5

        return (state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(BlockTags.MINEABLE_WITH_HOE) || state.is(BlockTags.LEAVES))
                && !state.is(BlockTags.NEEDS_DIAMOND_TOOL);
    }
}
