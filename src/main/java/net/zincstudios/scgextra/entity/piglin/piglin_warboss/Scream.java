package net.zincstudios.scgextra.entity.piglin.piglin_warboss;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.sounds.PiglinSounds;

public class Scream extends Behavior<Mob>{

    private final int cooldownDuration;
    private int cooldown = 0;
    private int ticks = 0;

    public Scream(int cooldownDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ), 50);
        this.cooldownDuration = cooldownDuration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
        if(this.cooldown>0)this.cooldown--;
        return this.cooldown <= 0;
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        this.cooldown = this.cooldownDuration;
        if(entity instanceof PiglinWarbossEntity pe)pe.triggerAnim("behavior", "scream");
        this.ticks = 0;
        if(entity instanceof PiglinWarbossEntity pe){pe.isScreaming = true;};
    }

    @Override
    protected void stop(ServerLevel level, Mob entity, long gameTime) {
        if(entity instanceof PiglinWarbossEntity pe){pe.isScreaming = false;};
    }

    @Override
    protected void tick(ServerLevel level, Mob owner, long gameTime) {
        this.ticks++;
        owner.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        owner.getNavigation().stop();
        owner.setDeltaMovement(Vec3.ZERO);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(8));
        for(LivingEntity en : list){
            en.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 2));
            en.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            en.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
        }
        if(this.ticks == 25){
            owner.playSound(PiglinSounds.PIGLIN_WARBOSS_SCREAM.get());
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return true;
    }
    
}
