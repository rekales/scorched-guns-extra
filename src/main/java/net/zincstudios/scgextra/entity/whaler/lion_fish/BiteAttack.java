package net.zincstudios.scgextra.entity.whaler.lion_fish;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
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
    
}
