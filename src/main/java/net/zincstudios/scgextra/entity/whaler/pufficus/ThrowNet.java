package net.zincstudios.scgextra.entity.whaler.pufficus;

import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.projectile.net.NetEntity;
import net.zincstudios.scgextra.sounds.WhalerSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class ThrowNet extends Behavior<LivingEntity>{

    private final int damageDelay;
    private final int duration;
    protected final float range;
    private final int cooldown;

    private UUID oldTargetId = UUID.randomUUID();
    private float oldDist = -1;
    private float accDistDelta = 0;  // accumulated distance delta
    private long cooldownEnd = 0;  // gameTime timestamp

    public ThrowNet(int damageDelay, int duration, float range) {
        this(damageDelay, duration, range, 0);
    }

    public ThrowNet(int damageDelay, int duration, float range, int cooldown) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED
        ));
        this.damageDelay = damageDelay;
        this.duration = duration;
        this.range = range;
        this.cooldown = cooldown;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on hasRequiredMemories
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity mob) {
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if(this.cooldownEnd < level.getGameTime()){
            if (this.oldDist < 0 || this.oldTargetId != target.getUUID()) {
                this.oldTargetId = target.getUUID();
                this.oldDist = mob.distanceTo(target);
                this.accDistDelta = 0;
            }
            
            float dist = mob.distanceTo(target);
            this.accDistDelta += dist - this.oldDist;
            this.oldDist = dist;
            if (this.accDistDelta > 0) {
                this.accDistDelta = Math.max(0, this.accDistDelta-0.06f);
            } else {
                this.accDistDelta = Math.min(0, this.accDistDelta+0.06f);
            }
            return mob.position().closerThan(target.position(), mob.getBbWidth()/2 + this.range - this.accDistDelta);
        }else{
            return false;
        }

    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity mob, long gameTime) {
        return mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && mob.getBrain().hasMemoryValue(ModBrainMemories.NET_TICKS.get());
    }

    @Override
    protected void start(ServerLevel level, LivingEntity mob, long gameTime) {
        this.oldDist = -1;
        mob.getBrain().setMemoryWithExpiry(
                ModBrainMemories.NET_TICKS.get(),
                this.damageDelay + gameTime,
                this.duration
        );
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity mob, long gameTime) {
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if (mob.getBrain().getMemory(ModBrainMemories.NET_TICKS.get()).get() == gameTime
                && mob.position().closerThan(target.position(), (mob.getBbWidth()/2 + this.range) * 1.2)) {            
            mob.playSound(WhalerSounds.PUFFICUS_THROW_NET.get());
            AbstractArrow net = new NetEntity(mob, level);
            double d0 = target.getX() - mob.getX();
            double d1 = target.getY(0.3333333333333333) - net.getY();
            double d2 = target.getZ() - mob.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            net.shoot(d0, d1 + d3 * (double)0.2F, d2, 1F, (float)(14 - level.getDifficulty().getId() * 4));
            level.addFreshEntity(net);
            this.cooldownEnd = gameTime + this.cooldown;
        }
    }
}
