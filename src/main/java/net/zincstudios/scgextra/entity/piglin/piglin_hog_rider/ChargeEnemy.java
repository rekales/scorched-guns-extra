package net.zincstudios.scgextra.entity.piglin.piglin_hog_rider;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.sounds.PiglinSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

//TODO: REDO
public class ChargeEnemy <T extends Mob> extends Behavior<T>{

    private final int cooldown;
    private long endTime = 0;
    private final int startAnimTime;
    private long chargeEndedAt = 0;

    public ChargeEnemy(int cooldown, int startAnimTime) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.STUNNED.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED
        ), 800);
        this.cooldown = cooldown;
        this.startAnimTime = startAnimTime;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T owner) {
        return level.getGameTime() > endTime+cooldown && !owner.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get()) && owner.distanceToSqr(owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get()) <= 36;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, T owner, long gameTime) {
        return !owner.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get()) && owner.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && ((owner instanceof PiglinHogRiderEntity pe) ? (pe.getChargePhase()!=-1) : true);
    }

    @Override
    protected void start(ServerLevel level, T owner, long gameTime) {
        owner.getBrain().setMemoryWithExpiry(ModBrainMemories.ABILITY_STATE.get(), new AbilityState("chargeTicks", gameTime, gameTime+startAnimTime), this.startAnimTime);
        this.chargeEndedAt = 0;
        if(owner instanceof PiglinHogRiderEntity pe)pe.setChargePhase(1);
        owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get(), 3.0f, 0));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void tick(ServerLevel level, T owner, long gameTime) {
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        owner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target, 3.0f, 0));
        Brain<PiglinHogRiderEntity> brain = (Brain<PiglinHogRiderEntity>)owner.getBrain();
        if(!(owner.distanceToSqr(target)<=16)){
            if(
                !brain.hasMemoryValue(ModBrainMemories.ABILITY_STATE.get())
            ){
                if(owner instanceof PiglinHogRiderEntity pe)pe.setChargePhase(3);
            }
        }else{
            if(this.chargeEndedAt == 0){
                if(owner instanceof PiglinHogRiderEntity pe)pe.setChargePhase(2);
                this.chargeEndedAt = gameTime;
            }else if(!brain.hasMemoryValue(ModBrainMemories.ABILITY_STATE.get())){
                owner.doHurtTarget(target);
                Vec3 ramDirection = target.position().subtract(owner.position()).normalize();
                target.knockback(3, ramDirection.x, ramDirection.z);
                brain.setMemoryWithExpiry(ModBrainMemories.STUNNED.get(), true, PiglinHogRiderEntity.STUN_DURATION);
                if(owner instanceof PiglinHogRiderEntity pe)pe.setChargePhase(-1);
                this.endTime = gameTime;
                owner.playSound(PiglinSounds.PIGLIN_HOG_RIDER_STUN.get());
            }
        }
    }
}