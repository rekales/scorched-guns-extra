package net.zincstudios.scgextra.entity.piglin.piglin_nob;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import software.bernie.geckolib.animatable.GeoEntity;

import java.util.function.BiFunction;

public class PiglinNobShootTarget extends Behavior<LivingEntity> {
    
    public static final float DEFAULT_ACCURACY = 1.6F;
    public static final String DEFAULT_CONTROLLER = "controller";
    public static final String DEFAULT_ANIM = "shoot";
    public static final int DEFAULT_COOLDOWN = 12;
    private final int waitFor;
    private int ticks = 0;
    private final String animControllerName;
    private final String animName;
    private final int cooldownDuration;
    private int cooldown;
    private final BiFunction<LivingEntity, Boolean, Float> accuracyFunc;

    public PiglinNobShootTarget(int waitFor) {
        this(waitFor, DEFAULT_ACCURACY, DEFAULT_CONTROLLER, DEFAULT_ANIM, DEFAULT_COOLDOWN);
    }

    public PiglinNobShootTarget(int waitFor, float accuracy, String animControllerName, String animName, int cooldown) {
        this(
                waitFor,
                (a,b)->1.6f,
                animControllerName,
                animName,
                cooldown
        );
    }

    public PiglinNobShootTarget(int waitFor, BiFunction<LivingEntity, Boolean, Float> accuracyFunc, String animControllerName, String animName, int cooldown) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.AIM_TICKS.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.SIMULATED_GUN.get(), MemoryStatus.VALUE_PRESENT
        ), waitFor+10);
        this.waitFor = waitFor;
        this.accuracyFunc = accuracyFunc;
        this.animControllerName = animControllerName;
        this.animName = animName;
        this.cooldownDuration = cooldown;
    }
    
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity owner) {
        if(this.cooldown>0)this.cooldown--;
        return this.cooldown <= 0 && ((owner instanceof PiglinNobEntity pne) ? pne.aggroTicks <= 0 : true);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.hasMemoryValue(ModBrainMemories.SIMULATED_GUN.get()) && ((entity instanceof PiglinNobEntity pne) ? pne.aggroTicks <= 0 : true);
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent"}) // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        this.ticks++;
        if(this.ticks==this.waitFor){
            Brain<?> brain = entity.getBrain();
            LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
            SimulatedGun simGun = brain.getMemory(ModBrainMemories.SIMULATED_GUN.get()).get();
            //blank fires the first 2 shots for some reason
            simGun.tickFire(entity, target.position(), accuracyFunc.apply(target, true), true);
        }
    }

    @Override
    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        this.ticks = 0;
        if(entity instanceof GeoEntity ge)ge.triggerAnim(this.animControllerName, this.animName);
        entity.getBrain().setMemory(ModBrainMemories.ABILITY_STATE.get(), new AbilityState("isShooting", gameTime, gameTime+10));
        this.cooldown = this.cooldownDuration;
    }

    @Override
    protected void stop(ServerLevel level, LivingEntity entity, long gameTime) {
        entity.getBrain().eraseMemory(ModBrainMemories.ABILITY_STATE.get());
    }
}