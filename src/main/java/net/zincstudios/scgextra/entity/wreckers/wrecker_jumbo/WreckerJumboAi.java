package net.zincstudios.scgextra.entity.wreckers.wrecker_jumbo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.AttackLastHurtIfNear;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.brain.BrainUtils;
import net.zincstudios.scgextra.entity.common.brain.DelayedMeleeAttack;
import net.zincstudios.scgextra.entity.common.brain.BreakBlocksToTarget;

public class WreckerJumboAi {
    static final ImmutableList<? extends SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.MEDIUM_RANGE_PLAYER.get(),
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
            ModBrainMemories.DELAYED_MELEE.get()
    );

    public static <T extends PathfinderMob> Brain<?> makeBrain(T mob, Brain<T> brain) {
        BrainCommons.initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(mob, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    // Only difference in common is stroll speed and chance to do nothing
    public static void initIdleActivity(Brain<? extends PathfinderMob> brain) {
        brain.addActivity(Activity.IDLE, 10, ImmutableList.of(
                StartAttacking.create(BrainUtils::getHurtByNonFriendlies),
                StartAttacking.create(BrainUtils::findNearestVisibleAttackablePlayer),
                StartAttacking.create(BrainUtils::findNearestAttackableFactionEnemy),
                new RunOne<>(ImmutableList.of(
                        Pair.of(RandomStroll.stroll(0.8F), 2),
                        Pair.of(SetWalkTargetFromLookTarget.create(0.8F, 3), 2),
                        Pair.of(new DoNothing(30, 60), 5)
                ))
        ));
    }

    public static <T extends PathfinderMob> void initFightActivity(T mob, Brain<T> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                new BreakBlocksToTarget(WreckerJumboAi::canBreakWithHammer, 20),
                SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F),
                new DelayedMeleeAttack(WreckerJumboEntity.MELEE_DAMAGE_DELAY, WreckerJumboEntity.MELEE_DURATION, 4.0f, 10)
        ), MemoryModuleType.ATTACK_TARGET);
    }

    private static boolean canBreakWithHammer(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;

        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0) return false;  // Unbreakable
        if (hardness >= 5) return false;  // Ex. Iron Block: 5, Stone: 1.5

        return (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.MINEABLE_WITH_HOE) || state.is(BlockTags.LEAVES) || state.is(BlockTags.WOOL))
                && !state.is(BlockTags.NEEDS_DIAMOND_TOOL);
    }
}
