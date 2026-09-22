package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import top.ribs.scguns.entity.throwable.ThrowableGrenadeEntity;
import top.ribs.scguns.init.ModEntities;

public class DeployMines <T extends ArmoredWhaleEntity> extends Behavior<T> {
    private int cooldown = 0;
    public DeployMines() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
    }
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T owner) {
        if(this.cooldown>0)this.cooldown--;
        return this.cooldown <= 0;
    }
    @Override
    protected boolean canStillUse(ServerLevel level, T entity, long gameTime) {
        return false;
    }
    @Override
    protected void start(ServerLevel level, T entity, long gameTime) {
        this.cooldown = 600;
        for(int i = 0; i < 10; i++){
            ThrowableGrenadeEntity ge = new ThrowableGrenadeEntity(ModEntities.THROWABLE_GRENADE.get(), level);
            ge.moveTo(entity.blockPosition().above(6), 0, 0);
            ge.shoot(entity.getRandom().nextInt(-8, 8), 20, entity.getRandom().nextInt(-8, 8), 1, 10);
            level.addFreshEntity(ge);
        }
    }
}
