package net.zincstudios.scgextra.entity.piglin.piglin_kiln_hog;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.projectile.piglin_kiln_hog_magma_ball.PiglinKilnHogMagmaBall;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;

public class BellyCannonShoot extends Behavior<Mob>{

    private int ticks = 0;
    private int cooldown = 0;
    private final int cooldownDuration;
    private final int startAnimDuration;

    public BellyCannonShoot(int cooldownDuration, int startAnimDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED
        ), 60);
        this.cooldownDuration = cooldownDuration;
        this.startAnimDuration = startAnimDuration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
        if(this.cooldown>0)this.cooldown--;
        return this.cooldown <= 0 && !owner.getBrain().hasMemoryValue(ModBrainMemories.DELAYED_MELEE.get()) && owner.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && ((owner instanceof PiglinKilnHogEntity pke) ? pke.aggroTicks <= 0 : true);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && ((entity instanceof PiglinKilnHogEntity pke) ? pke.aggroTicks <= 0 : true);
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        this.cooldown = this.cooldownDuration;
        this.ticks = 0;
        if(entity instanceof PiglinKilnHogEntity pe)pe.triggerAnim("behavior", "big_canon");
        entity.getBrain().setMemoryWithExpiry(ModBrainMemories.ABILITY_STATE.get(), new AbilityState("belly_cannon", gameTime, gameTime+60), 60);
    }

    @Override
    protected void tick(ServerLevel level, Mob owner, long gameTime) {
        this.ticks++;
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        owner.getLookControl().setLookAt(target);
        if(this.ticks>this.startAnimDuration && this.ticks<this.startAnimDuration+2){
            AbstractArrow ball = new PiglinKilnHogMagmaBall(owner, level);
            Vec3 vec = owner.position().add(new Vec3(0D/16D, 41D/16D, 26D/16D*-1f).yRot(-owner.yBodyRot * Mth.DEG_TO_RAD));
            ball.moveTo(
                vec.x,
                vec.y-1,
                vec.z
            );
            double d0 = target.getX() - vec.x;
            double d1 = target.getY(3.3333333333333333) - ball.getY();
            double d2 = target.getZ() - vec.z;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            ball.shoot(d0, d1 + d3 * (double)0.2F, d2, 1F, (float)(14 - level.getDifficulty().getId() * 4));
            level.addFreshEntity(ball);
        }
    }
}