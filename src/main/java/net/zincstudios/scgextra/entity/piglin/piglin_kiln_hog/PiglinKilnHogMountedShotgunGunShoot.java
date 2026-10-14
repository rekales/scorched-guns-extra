package net.zincstudios.scgextra.entity.piglin.piglin_kiln_hog;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class PiglinKilnHogMountedShotgunGunShoot extends Behavior<Mob>{

    private int ticks = 0;
    private int cooldown = 0;
    private final int cooldownDuration;
    private final int startAnimDuration;
    private final int shootEvery;
    private int numberOfShots = 0;
    private int timer = 0;

    public PiglinKilnHogMountedShotgunGunShoot(int cooldownDuration, int startAnimDuration, int shootEvery) {
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
        return this.cooldown <= 0;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        this.cooldown = this.cooldownDuration;
        if(entity instanceof PiglinKilnHogEntity pe)pe.triggerAnim("behavior", "small_canon");
        this.ticks = 0;
        this.timer = 0;
        this.numberOfShots = 0;
    }

    @Override
    protected void tick(ServerLevel level, Mob entity, long gameTime) {
        this.ticks++;
        if(this.numberOfShots>=4)return;
        if(entity instanceof PiglinKilnHogEntity owner){
            if(this.ticks>this.startAnimDuration){
                if(this.timer>0)this.timer--;
                if(this.timer <= 0){
                    LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
                    this.numberOfShots++;
                    this.timer = this.shootEvery;
                    owner.mountedShotGunLeft.tick(target, 1f);
                    owner.mountedShotGunRight.tick(target, 1f);
                }
            }
        }
    }
}
