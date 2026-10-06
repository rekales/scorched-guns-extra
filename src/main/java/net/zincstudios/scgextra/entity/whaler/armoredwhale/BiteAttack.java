package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.ModBrainMemories;

import java.util.UUID;

//Mostly copied from DelayedMeleeAttack
@SuppressWarnings("unused")
public class BiteAttack extends Behavior<Mob> {

    private final int damageDelay;
    private final int meleeDuration;
    protected final float range;
    private final int meleeCooldown;

    private UUID oldTargetId = UUID.randomUUID();
    private float oldDist = -1;
    private float accDistDelta = 0;  // accumulated distance delta

    public BiteAttack(int damageDelay, int meleeDuration, float range) {
        this(damageDelay, meleeDuration, range, 0);
    }

    public BiteAttack(int damageDelay, int meleeDuration, float range, int meleeCooldown) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT,
                ModBrainMemories.DELAYED_MELEE.get(), MemoryStatus.REGISTERED
        ));
        this.damageDelay = damageDelay;
        this.meleeDuration = meleeDuration;
        this.range = range;
        this.meleeCooldown = meleeCooldown;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on hasRequiredMemories
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        ArmoredWhaleEntity owner = (ArmoredWhaleEntity) mob;
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();

        if (this.oldDist < 0 || this.oldTargetId != target.getUUID()) {
            this.oldTargetId = target.getUUID();
            this.oldDist = owner.headFrontPart.distanceTo(target);
            this.accDistDelta = 0;
        }

        float dist = owner.headFrontPart.distanceTo(target);
        this.accDistDelta += dist - this.oldDist;
        this.oldDist = dist;
        if (this.accDistDelta > 0) {
            this.accDistDelta = Math.max(0, this.accDistDelta-0.06f);
        } else {
            this.accDistDelta = Math.min(0, this.accDistDelta+0.06f);
        }

        return owner.headFrontPart.position().closerThan(target.position(), owner.headFrontPart.getBbWidth()/2 + this.range - this.accDistDelta);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        return mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && mob.getBrain().hasMemoryValue(ModBrainMemories.DELAYED_MELEE.get());
    }

    @Override
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        this.oldDist = -1;
        mob.getBrain().setMemoryWithExpiry(
                ModBrainMemories.DELAYED_MELEE.get(),
                this.damageDelay + gameTime,
                this.meleeDuration
        );
    }

    @Override
    protected void stop(ServerLevel level, Mob mob, long gameTime) {
        mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, this.meleeCooldown);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        ArmoredWhaleEntity owner = (ArmoredWhaleEntity) mob;
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if (owner.getBrain().getMemory(ModBrainMemories.DELAYED_MELEE.get()).get() == gameTime
                && owner.headFrontPart.position().closerThan(target.position(), (owner.headFrontPart.getBbWidth()/2 + this.range) * 1.2)) {
            owner.doHurtTarget(target);
        }
    }
}
