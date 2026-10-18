package net.zincstudios.scgextra.entity.piglin.piglin_warboss;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;

public class ChargeAtTarget extends Behavior<Mob>{

    private final int cooldownDuration;
    private final int maxDistance;
    private int cooldown = 0;
    private Vec3 ramDirection = Vec3.ZERO;
    private boolean charged = false;

    public ChargeAtTarget(int cooldownDuration, int maxDistance) {
        super(ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
            ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED
        ), 500);
        this.cooldownDuration = cooldownDuration;
        this.maxDistance = maxDistance;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
        if(this.cooldown>0)this.cooldown--;
        if(owner.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)){
            return owner.distanceToSqr(owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get()) < this.maxDistance && this.cooldown<=0;
        }
        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && !this.charged;
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        this.cooldown = this.cooldownDuration;
        this.charged = false;
    }
    
    @Override
    protected void tick(ServerLevel level, Mob owner, long gameTime) {
        if(!owner.getBrain().hasMemoryValue(ModBrainMemories.ABILITY_STATE.get())){
            owner.getBrain().setMemoryWithExpiry(ModBrainMemories.ABILITY_STATE.get(), new AbilityState("charge_state", gameTime, gameTime+400), 400);
        }
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        owner.getLookControl().setLookAt(target, 30.0F, 30.0F);
        Vec3 direction = target.position().subtract(owner.position());
        this.ramDirection = new Vec3(direction.x, 0.0D, direction.z).normalize();
        owner.setDeltaMovement(
            this.ramDirection.x * 0.3f,
            owner.getDeltaMovement().y,
            this.ramDirection.z * 0.3f
        );
        if(owner.distanceToSqr(target)<=6){
            target.hurt(owner.damageSources().mobAttack(owner), 20);
            this.charged = true;
        }
    }
    @Override
    protected void stop(ServerLevel level, Mob entity, long gameTime) {
        entity.getBrain().setMemoryWithExpiry(ModBrainMemories.ABILITY_STATE.get(), new AbilityState("charge_state", gameTime, gameTime+PiglinWarbossEntity.CHARGE_END_DURATION), PiglinWarbossEntity.CHARGE_END_DURATION);
    }
}
