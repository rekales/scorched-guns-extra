package net.zincstudios.scgextra.entity.whaler.tentacliator;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import software.bernie.geckolib.animatable.GeoEntity;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class TentacliatorShootTarget extends Behavior<LivingEntity> {
    
    public static final float DEFAULT_ACCURACY = 1.6F;
    public static final String DEFAULT_CONTROLLER = "controller";
    public static final String DEFAULT_ANIM = "shoot";
    public static final int DEFAULT_COOLDOWN = 12;
    private final int aimThreshold;
    private final String animControllerName;
    private final String animName;
    private final int cooldown;
    private final BiFunction<LivingEntity, Boolean, Float> accuracyFunc;
    private final Predicate<LivingEntity> canShoot;

    public TentacliatorShootTarget(int aimThreshold) {
        this(aimThreshold, DEFAULT_ACCURACY, DEFAULT_CONTROLLER, DEFAULT_ANIM, DEFAULT_COOLDOWN);
    }

    public TentacliatorShootTarget(int aimThreshold, float accuracy, String animControllerName, String animName, int cooldown) {
        this(
                aimThreshold,
                (entity, firing) -> accuracy,
                entity -> true,
                animControllerName,
                animName,
                cooldown
        );
    }

    public TentacliatorShootTarget(int aimThreshold, BiFunction<LivingEntity, Boolean, Float> accuracyFunc, Predicate<LivingEntity> canShoot, String animControllerName, String animName, int cooldown) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.AIM_TICKS.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.SIMULATED_GUN.get(), MemoryStatus.VALUE_PRESENT
        ));
        this.aimThreshold = aimThreshold;
        this.accuracyFunc = accuracyFunc;
        this.canShoot = canShoot;
        this.animControllerName = animControllerName;
        this.animName = animName;
        this.cooldown = cooldown;
    }
    
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity owner) {
        return super.checkExtraStartConditions(level, owner) && !owner.getBrain().getMemory(MemoryModuleType.ATTACK_COOLING_DOWN).orElse(false);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.hasMemoryValue(ModBrainMemories.SIMULATED_GUN.get());
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent"}) // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        int aimTicks = brain.getMemory(ModBrainMemories.AIM_TICKS.get()).orElse(-12);
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if(brain.getMemory(MemoryModuleType.ATTACK_COOLING_DOWN).orElse(false))return;
        if (!canShoot.test(entity)) return;
        
        SimulatedGun simGun = brain.getMemory(ModBrainMemories.SIMULATED_GUN.get()).get();
        Vec3 targetPos = SimulatedGun.getCenterMassPos(target);
        if(aimTicks == 21 || aimTicks == 22)if(entity instanceof GeoEntity ge)ge.triggerAnim(this.animControllerName, this.animName);
        
        if (aimTicks >= this.aimThreshold) {
            simGun.tickFire(entity, targetPos, this.accuracyFunc.apply(entity, true), true);
            brain.setMemory(ModBrainMemories.AIM_TICKS.get(), -22);
            brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, this.cooldown);
        } else {
            simGun.tickFire(entity, targetPos, this.accuracyFunc.apply(entity, false), false);
        }
    }
}
