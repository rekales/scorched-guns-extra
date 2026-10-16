package net.zincstudios.scgextra.entity.piglin.pigdread;

import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.DelayedMeleeAttack;
import net.zincstudios.scgextra.sounds.PiglinSounds;

import java.util.Collections;
import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

// TODO: replace with adding effects via mob#doHurtTarget
public class DelayedMeleeAttackWithEffects extends DelayedMeleeAttack {

    private final List<MobEffect> EFFECTS;
    private final List<Integer> TIMERS;

    public DelayedMeleeAttackWithEffects(int damageDelay, int meleeDuration, float range, int meleeCooldown, List<MobEffect> effects) {
        super(damageDelay, meleeDuration, range, meleeCooldown);
        this.EFFECTS = effects;
        this.TIMERS = Collections.nCopies(effects.size(), 100);
    }

    public DelayedMeleeAttackWithEffects(int damageDelay, int meleeDuration, float range, int meleeCooldown, List<MobEffect> effects, List<Integer> timers) {
        super(damageDelay, meleeDuration, range, meleeCooldown);
        this.EFFECTS = effects;
        this.TIMERS = timers;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if (mob.getBrain().getMemory(ModBrainMemories.DELAYED_MELEE.get()).get() == gameTime
                && mob.position().closerThan(target.position(), (mob.getBbWidth()/2 + this.range) * 1.2)) {
            mob.doHurtTarget(target);
            for(int i = 0; i < this.EFFECTS.size(); i++){
                target.addEffect(new MobEffectInstance(this.EFFECTS.get(i), this.TIMERS.get(i)));
            }
            mob.playSound(PiglinSounds.GENERAL_CLAW.get());
        }
    }
}
