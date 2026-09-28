package net.zincstudios.scgextra.entity.whaler.lion_fish;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SplashPoison extends Behavior<LivingEntity>{

    protected final float range;
    private final int cooldown;
    private final int duration;
    private long cooldownEnd = 0;  // gameTime timestamp

    public SplashPoison(float range, int duration) {
        this(range, 0, duration);
    }

    public SplashPoison(float range, int cooldown, int duration) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.ATTACK_RUN_FOR.get(), MemoryStatus.REGISTERED
        ), duration);
        this.range = range;
        this.cooldown = cooldown;
        this.duration = duration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity owner) {
        return this.cooldownEnd < level.getGameTime() && owner.getRandom().nextFloat() <= 0.3F;
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
        this.cooldownEnd = gameTime + this.cooldown;
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
        float f = (float)mob.getY();
        Vec3 vec3 = mob.getDeltaMovement();
        for(int j = 0; j < 10; ++j) {
            float f1 = (mob.getRandom().nextFloat() * 2.0F - 1.0F) * mob.getBbWidth() * 0.8F;
            float f2 = (mob.getRandom().nextFloat() * 2.0F - 1.0F) * mob.getBbWidth() * 0.8F;
            level.sendParticles(
                ParticleTypes.SPLASH, 
                mob.getX() + (double)f1, 
                (double)(f + 1F), 
                mob.getZ() + (double)f2, 
                5, 
                vec3.x, 
                vec3.y, 
                vec3.z,
                0.2
            );
        }
    }
}
