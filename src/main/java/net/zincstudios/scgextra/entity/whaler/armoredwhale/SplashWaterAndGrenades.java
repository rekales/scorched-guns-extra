package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Monster;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.sounds.WhalerSounds;
import top.ribs.scguns.entity.throwable.ThrowableGrenadeEntity;
import top.ribs.scguns.init.ModEntities;

public class SplashWaterAndGrenades <T extends ArmoredWhaleEntity> extends Behavior<T>{

    private int cooldown = 0;

    public SplashWaterAndGrenades() {
        super(ImmutableMap.of(
            ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, T owner) {
        if(this.cooldown>0)this.cooldown--;
        return this.cooldown <= 0 && !owner.STARTED_SLAM;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, T entity, long gameTime) {
        return entity.getBrain().getMemory(ModBrainMemories.ABILITY_STATE.get()).isPresent();
    }

    @Override
    protected void start(ServerLevel level, T entity, long gameTime) {
        this.cooldown = 600;
        entity.getBrain().setMemoryWithExpiry(ModBrainMemories.ABILITY_STATE.get(), new AbilityState("splash_water", gameTime, gameTime+100), 100);
        entity.triggerAnim("attack", "splash");
        if(entity.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)){
            for(int i = 0; i < 10; i++){
                ThrowableGrenadeEntity ge = new ThrowableGrenadeEntity(ModEntities.THROWABLE_GRENADE.get(), level);
                ge.moveTo(entity.blockPosition().above(6), 0, 0);
                ge.shoot(entity.getRandom().nextInt(-8, 8), 20, entity.getRandom().nextInt(-8, 8), 1, 10);
                level.addFreshEntity(ge);
            }
        }
        entity.playSound(WhalerSounds.ARMORED_WHALE_SPLASH.get());
    }

    @Override
    protected void tick(ServerLevel level, T owner, long gameTime) {
        List<Monster> list = level.getEntitiesOfClass(Monster.class, owner.getBoundingBox().inflate(10), (e)->Faction.isFriendlies(owner, e));
        for(Monster m : list){
            m.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
        }
    }
}
