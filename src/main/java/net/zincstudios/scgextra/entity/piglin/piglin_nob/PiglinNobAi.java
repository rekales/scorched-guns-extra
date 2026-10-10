package net.zincstudios.scgextra.entity.piglin.piglin_nob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.brain.AimWhenNotWalking;
import net.zincstudios.scgextra.entity.common.brain.ApproachTargetIfCannotAim;
import net.zincstudios.scgextra.entity.common.brain.AttackLastHurtIfNear;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.brain.BrainUtils;
import net.zincstudios.scgextra.entity.common.brain.DelayedMeleeAttack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class PiglinNobAi {
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
            MemoryModuleType.ATTACK_COOLING_DOWN,
            ModBrainMemories.WEAPON_IDEAL_RANGE.get(),
            ModBrainMemories.WEAPON_MAX_RANGE.get(),
            ModBrainMemories.DELAYED_MELEE.get()
    );

    public static <T extends PathfinderMob> Brain<?> makeBrain(T mob, Brain<T> brain) {
        BrainCommons.initCoreActivity(brain);
        BrainCommons.initIdleActivity(brain);
        PiglinNobAi.initFightActivity(mob, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static <T extends PathfinderMob> Brain.Provider<T> brainProvider() {
        return Brain.provider(PiglinNobAi.MEMORY_TYPES, PiglinNobAi.SENSOR_TYPES);
    }

    public static <T extends PathfinderMob> void initFightActivity(T mob, Brain<T> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(
                StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                new ApproachTargetIfCannotAim(1.0F),
                new AimWhenNotWalking(),
                new PiglinNobShootTarget(37, 1.6F, "attack", "shoot", 22),
                new DelayedMeleeAttack(PiglinNobEntity.MELEE_DAMAGE_DELAY, PiglinNobEntity.MELEE_DURATION, 3f, 20){
                    protected void tick(ServerLevel level, Mob mob, long gameTime) {
                        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
                        if (mob.getBrain().getMemory(ModBrainMemories.DELAYED_MELEE.get()).get() == gameTime
                            && mob.position().closerThan(target.position(), (mob.getBbWidth()/2 + this.range) * 1.2)) {
                            mob.getOffhandItem().getItem().hurtEnemy(mob.getOffhandItem(), target, mob);
                        }
                    }
                }
        ), MemoryModuleType.ATTACK_TARGET);
    }
}
