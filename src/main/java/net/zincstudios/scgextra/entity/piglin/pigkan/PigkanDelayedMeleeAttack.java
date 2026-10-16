package net.zincstudios.scgextra.entity.piglin.pigkan;

import java.util.UUID;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.sounds.PiglinSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;

public class PigkanDelayedMeleeAttack extends Behavior<Mob>{

    private int damageDelay;
    private int meleeDuration;
    protected final float range;
    private final int meleeCooldown;

    private UUID oldTargetId = UUID.randomUUID();
    private float oldDist = -1;
    private float accDistDelta = 0;  // accumulated distance delta

    public PigkanDelayedMeleeAttack(float range) {
        this(range, 0);
    }

    public PigkanDelayedMeleeAttack(float range, int meleeCooldown) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT,
                ModBrainMemories.DELAYED_MELEE.get(), MemoryStatus.REGISTERED
        ));
        this.range = range;
        this.meleeCooldown = meleeCooldown;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on hasRequiredMemories
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();

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
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        return mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && mob.getBrain().hasMemoryValue(ModBrainMemories.DELAYED_MELEE.get());
    }

    @Override
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        this.oldDist = -1;
        float rand = mob.getRandom().nextFloat();
        if(rand<0.5f){
            this.damageDelay = PigkanEntity.CLAW_DELAY;
            this.meleeDuration = PigkanEntity.CLAW_DURATION;
            mob.getBrain().setMemory(ModBrainMemories.ABILITY_STATE.get(), new AbilityState("claw", gameTime, this.meleeDuration));
        }else{
            this.damageDelay = PigkanEntity.SAW_DELAY;
            this.meleeDuration = PigkanEntity.SAW_DURATION;
            mob.getBrain().setMemory(ModBrainMemories.ABILITY_STATE.get(), new AbilityState("saw", gameTime, this.meleeDuration));
        }
        mob.getBrain().setMemoryWithExpiry(
                ModBrainMemories.DELAYED_MELEE.get(),
                this.damageDelay + gameTime,
                this.meleeDuration
        );
    }

    @Override
    protected void stop(ServerLevel level, Mob mob, long gameTime) {
        mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, this.meleeCooldown);
        mob.getBrain().eraseMemory(ModBrainMemories.ABILITY_STATE.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if (mob.getBrain().getMemory(ModBrainMemories.DELAYED_MELEE.get()).get() == gameTime
                && mob.position().closerThan(target.position(), (mob.getBbWidth()/2 + this.range) * 1.2)) {
            mob.doHurtTarget(target);
            String name = mob.getBrain().getMemory(ModBrainMemories.ABILITY_STATE.get()).get().abilityId();
            if(name.equals("claw")){
                mob.playSound(PiglinSounds.GENERAL_CLAW.get());
            }
            if(name.equals("saw")){
                mob.playSound(PiglinSounds.GENERAL_SAW.get());
            }
        }
    }
}
