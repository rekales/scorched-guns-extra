package net.zincstudios.scgextra.entity.piglin.pigdread;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.sounds.PiglinSounds;

public class SpeedBuff extends Behavior<Mob>{

    private final int cooldownDuration;
    private final int speedDuration;
    private int cooldown = 0;

    public SpeedBuff(int cooldownDuration, int speedDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ));
        this.cooldownDuration = cooldownDuration;
        this.speedDuration = speedDuration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
        if(this.cooldown>0)this.cooldown--;
        return this.cooldown <= 0;
    }
    
    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        super.start(level, entity, gameTime);
        this.cooldown = this.cooldownDuration;
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, speedDuration, 1, false, false));
        entity.playSound(PiglinSounds.PIGDREAD_SPEED.get());
    }
}
