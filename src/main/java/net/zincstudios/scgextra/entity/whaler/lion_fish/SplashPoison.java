package net.zincstudios.scgextra.entity.whaler.lion_fish;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;

public class SplashPoison extends Behavior<LivingEntity>{

    protected final float range;
    private final int cooldown;
    private final int duration;

    public SplashPoison(float range, int duration) {
        this(range, 0, duration);
    }

    public SplashPoison(float range, int cooldown, int duration) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.POISON_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT,
                ModBrainMemories.ATTACK_RUN_FOR.get(), MemoryStatus.REGISTERED
        ), duration);
        this.range = range;
        this.cooldown = cooldown;
        this.duration = duration;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity mob, long gameTime) {
        return mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && mob.getBrain().hasMemoryValue(ModBrainMemories.ATTACK_RUN_FOR.get());
    }

    @Override
    protected void start(ServerLevel level, LivingEntity mob, long gameTime) {
        mob.getBrain().setMemoryWithExpiry(
                ModBrainMemories.ATTACK_RUN_FOR.get(),
                gameTime,
                this.duration
        );
    }

    @Override
    protected void stop(ServerLevel level, LivingEntity mob, long gameTime) {
        mob.getBrain().setMemoryWithExpiry(ModBrainMemories.POISON_COOLDOWN.get(), true, this.cooldown);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity mob, long gameTime) {
        AABB aabb = mob.getBoundingBox().inflate(5);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, aabb);
        for(LivingEntity entity : list){
            if(!Faction.isFriendlies(mob, entity)){
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 160));
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160));
            }
        }
    }
}
