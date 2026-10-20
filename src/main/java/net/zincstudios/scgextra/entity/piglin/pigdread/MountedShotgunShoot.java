package net.zincstudios.scgextra.entity.piglin.pigdread;

import com.google.common.collect.ImmutableMap;

import java.util.function.BiFunction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.AccuracyBuildup;

public class MountedShotgunShoot extends Behavior<Mob>{

    private int ticks = 0;
    private int cooldown = 0;
    private final int cooldownDuration;
    private final int startAnimDuration;
    private final int shootEvery;
    private int numberOfShots = 0;
    private int timer = 0;

    private final BiFunction<LivingEntity, Boolean, Float> accuracyFunc = new AccuracyBuildup(0.8f, 2.4f, 0.05f);

    public MountedShotgunShoot(int cooldownDuration, int startAnimDuration, int shootEvery) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ), 60);
        this.cooldownDuration = cooldownDuration;
        this.startAnimDuration = startAnimDuration;
        this.shootEvery = shootEvery;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob entity) {
        if(this.cooldown>0)this.cooldown--;
        return this.cooldown <= 0 && !entity.getBrain().hasMemoryValue(ModBrainMemories.DELAYED_MELEE.get()) && entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && !entity.getBrain().hasMemoryValue(ModBrainMemories.ABILITY_STATE.get());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && !entity.getBrain().hasMemoryValue(ModBrainMemories.DELAYED_MELEE.get()) && !entity.getBrain().hasMemoryValue(ModBrainMemories.ABILITY_STATE.get());
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        this.cooldown = this.cooldownDuration;
        if(entity instanceof PigdreadEntity pe)pe.triggerAnim("behavior", "shoot");
        this.ticks = 0;
        this.timer = 0;
        this.numberOfShots = 0;
    }

    @Override
    protected void tick(ServerLevel level, Mob entity, long gameTime) {
        this.ticks++;
        if(this.numberOfShots>=2)return;
        if(entity instanceof PigdreadEntity owner){
            LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
            double dX = target.getX() - owner.getX();
            double dY = target.getEyeY() - owner.getEyeY();
            double dZ = target.getZ() - owner.getZ();
            double dist = Math.sqrt(dX * dX + dZ * dZ);
            float yaw = (float) (Math.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
            float pitch = (float) -(Math.atan2(dY, dist) * (180D / Math.PI));
            owner.setYRot(yaw);
            owner.setXRot(pitch);
            owner.setYHeadRot(yaw);
            if(this.ticks>this.startAnimDuration){
                if(this.timer>0)this.timer--;
                if(this.timer <= 0){
                    this.numberOfShots++;
                    this.timer = this.shootEvery;
                    owner.mountedShotGun.tick(target, this.accuracyFunc.apply(entity, true));
                }
            }
        }
    }
}
