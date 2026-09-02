package net.zincstudios.scgextra.entity.whaler.tentacliator;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.effects.ModEffects;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import software.bernie.geckolib.animatable.GeoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class InkAttack extends Behavior<LivingEntity> {

    private final int cooldown;
    private final String animController;
    private final String animName;
    private final int aimThreshold;

    public InkAttack(int cooldown, String animController, String animName, int aimThreshold) {
        super(ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET, 
            MemoryStatus.VALUE_PRESENT,
            ModBrainMemories.INK_ATTACK_TICKS.get(),
            MemoryStatus.REGISTERED,
            ModBrainMemories.IS_ON_INK_COOLDOWN.get(),
            MemoryStatus.REGISTERED
        ));
        this.cooldown = cooldown;
        this.animController = animController;
        this.animName = animName;
        this.aimThreshold = aimThreshold;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent"}) // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        if(brain.getMemory(ModBrainMemories.IS_ON_INK_COOLDOWN.get()).orElse(false))return;

        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();

        if(entity.distanceToSqr(target) > 256)return;

        int aimTicks = brain.getMemory(ModBrainMemories.INK_ATTACK_TICKS.get()).orElse(-10);
        
        if(aimTicks == 1)if(entity instanceof GeoEntity ge)ge.triggerAnim(this.animController, this.animName);

        if(aimTicks >= 1){
            Vec3 vec = entity.position().add(new Vec3(-5.5D/16D,20D/16D,1.5D/16D).yRot(-entity.yBodyRot * Mth.DEG_TO_RAD));
            BlockPos end = target.blockPosition();
            double startX = vec.x;
            double startY = vec.y;
            double startZ = vec.z;
        
            double endX = end.getX() + 0.5;
            double endY = end.getY() + 0.5;
            double endZ = end.getZ() + 0.5;
        
            double dx = endX - startX;
            double dy = endY - startY;
            double dz = endZ - startZ;
        
            double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
            double stepX = dx / distance;
            double stepY = dy / distance;
            double stepZ = dz / distance;
            for (double i = 0; i < distance; i += 0.1) {
                double px = startX + stepX * i;
                double py = startY + stepY * i;
                double pz = startZ + stepZ * i;
                if (entity.level() instanceof ServerLevel serverLevel)serverLevel.sendParticles(ParticleTypes.FALLING_WATER, px, py, pz,1,0,0,0,0);    
            }
        }

        if (aimTicks >= this.aimThreshold) {
            if(entity instanceof TentacliatorEntity te){
                if(te.isGlowing()){
                    target.addEffect(new MobEffectInstance(ModEffects.GLOWING_INK_EFFECT.get(), 100));
                }else{
                    target.addEffect(new MobEffectInstance(ModEffects.INK_EFFECT.get(), 100));
                }
            }
            brain.setMemoryWithExpiry(ModBrainMemories.IS_ON_INK_COOLDOWN.get(), true, this.cooldown);
            brain.setMemory(ModBrainMemories.INK_ATTACK_TICKS.get(), -10);
        }else{
            brain.setMemory(ModBrainMemories.INK_ATTACK_TICKS.get(), brain.getMemory(ModBrainMemories.INK_ATTACK_TICKS.get()).orElse(-10)+1);
        }
    }
}
