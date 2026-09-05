package net.zincstudios.scgextra.entity.whaler.lion_fish;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.DelayedMeleeAttack;

public class BiteAttack extends DelayedMeleeAttack{

    public BiteAttack(int damageDelay, int meleeDuration, float range, int meleeCooldown) {
        super(damageDelay, meleeDuration, range, meleeCooldown);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        if(mob.getBrain().hasMemoryValue(ModBrainMemories.ATTACK_RUN_FOR.get()))return false;
        return super.checkExtraStartConditions(level, mob);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        LivingEntity target = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        if (mob.getBrain().getMemory(ModBrainMemories.DELAYED_MELEE.get()).get() == gameTime
                && mob.position().closerThan(target.position(), (mob.getBbWidth()/2 + this.range) * 1.2)) {
            mob.doHurtTarget(target);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 160));
        }
    }
}
