package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.zincstudios.scgextra.entity.common.brain.ShootTargetAnimated;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainActivities;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.ModBrainSensors;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.brain.AimWhenTargetVisible;
import net.zincstudios.scgextra.entity.common.brain.ApproachTargetIfCannotAim;
import net.zincstudios.scgextra.entity.common.brain.AttackLastHurtIfNear;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.brain.BrainUtils;
import net.zincstudios.scgextra.entity.common.brain.DelayedMeleeAttack;
import net.zincstudios.scgextra.entity.common.brain.ShootTarget;
import net.zincstudios.scgextra.entity.common.gun.IntervalTriggerSampler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import static net.zincstudios.scgextra.entity.ModBrainMemories.MAX_AIM_TICKS;
import static net.zincstudios.scgextra.entity.ModBrainMemories.MIN_AIM_TICKS;

public class ArmoredWhaleAi {
    private static final ImmutableList<? extends SensorType<? extends Sensor<? super PathfinderMob>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            ModBrainSensors.MEDIUM_RANGE_PLAYER.get(),
            SensorType.HURT_BY,
            ModBrainSensors.CUSTOM_GUN.get()
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
            ModBrainMemories.SIMULATED_GUN.get(),
            ModBrainMemories.WEAPON_IDEAL_RANGE.get(),
            ModBrainMemories.WEAPON_MAX_RANGE.get(),
            ModBrainMemories.HEADSHOT_COUNT.get(),
            ModBrainMemories.DELAYED_MELEE.get(),
            ModBrainMemories.ABILITY_STATE.get()
    );

    public static <T extends ArmoredWhaleEntity> Brain<?> makeBrain(T mob, Brain<T> brain) {
        BrainCommons.initCoreWithStunActivity(brain, MobUtil.DEFAULT_STUN_DURATION);
        BrainCommons.initIdleActivity(brain);
        initFightActivity(mob, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static <T extends PathfinderMob> Brain.Provider<T> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static <T extends ArmoredWhaleEntity> void initFightActivity(T mob, Brain<T> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.FIGHT, BrainUtils.createPriorityPairs(10,
            ImmutableList.of(
                StopAttackingIfTargetInvalid.create(target -> !BrainUtils.isTargetStillValidNonFriendlies(mob, target, false)),
                AttackLastHurtIfNear.create((self, target) -> !Faction.isFriendlies(self, target), false),
                new DelayedMeleeAttack(ArmoredWhaleEntity.MELEE_DAMAGE_DELAY, ArmoredWhaleEntity.MELEE_DURATION, 3, 60),
                new ApproachTargetIfCannotAim(1F){
                    @Override
                    protected void tick(ServerLevel level, Mob mob, long gameTime) {
                        Brain<?> brain = mob.getBrain();
                        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
                        float approachDist = brain.getMemory(ModBrainMemories.WEAPON_IDEAL_RANGE.get()).get();
                        float weaponRange = brain.getMemory(ModBrainMemories.WEAPON_MAX_RANGE.get()).get();
                        int aimTicks = brain.getMemory(ModBrainMemories.AIM_TICKS.get()).orElse(0);

                        if (mob.closerThan(target, approachDist) && mob.getSensing().hasLineOfSight(target)) {
                            if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
                                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                            }
                        }
                    }
                },
                new AimWhenTargetVisible(){
                    @Override
                    protected void tick(ServerLevel level, Mob mob, long gameTime) {
                        Brain<?> brain = mob.getBrain();
                        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();

                        int aimTicks = brain.getMemory(ModBrainMemories.AIM_TICKS.get()).orElse(0);
                        aimTicks += mob.hasLineOfSight(target) ? 1 : -1;
                        aimTicks = Mth.clamp(aimTicks, MIN_AIM_TICKS, MAX_AIM_TICKS);

                        brain.setMemory(ModBrainMemories.AIM_TICKS.get(), aimTicks);
                    }
                },
                new ShootTargetAnimated(20,
                        (entity, firing) -> 1.0F,
                        entity -> !entity.getBrain().hasMemoryValue(ModBrainMemories.DELAYED_MELEE.get()),
                        "attack", "fire", 0)
            )), ImmutableSet.of(
            Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
            ), ImmutableSet.of(
                    ModBrainMemories.AIM_TICKS.get()
            )
        );
    }

    public static void updateActivity(ArmoredWhaleEntity mob) {
        Brain<ArmoredWhaleEntity> brain = mob.getBrain();
        brain.setActiveActivityToFirstValid(ImmutableList.of(
                Activity.FIGHT,
                Activity.IDLE
        ));
    }
}
