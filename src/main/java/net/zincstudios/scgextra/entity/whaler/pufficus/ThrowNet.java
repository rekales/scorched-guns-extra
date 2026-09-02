package net.zincstudios.scgextra.entity.whaler.pufficus;

import com.google.common.collect.ImmutableMap;

import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.projectile.net.NetEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class ThrowNet extends Behavior<LivingEntity>{

    private final int cooldown;

    public ThrowNet(int cooldown) {
        super(ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET, 
            MemoryStatus.VALUE_PRESENT,
            ModBrainMemories.IS_ON_NET_COOLDOWN.get(),
            MemoryStatus.REGISTERED
        ));
        this.cooldown = cooldown;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
    }

    @SuppressWarnings({"OptionalGetWithoutIsPresent"}) // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, LivingEntity entity, long gameTime) {
        Brain<?> brain = entity.getBrain();
        if(brain.getMemory(ModBrainMemories.IS_ON_NET_COOLDOWN.get()).orElse(false))return;

        if(entity.getRandom().nextFloat() > 0.6f)return;

        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
        
        if(entity instanceof GeoEntity ge)ge.triggerAnim("attack", "throw_net");

        NetEntity net = new NetEntity(entity, level);
        net.shoot(target.getX(), target.getY(), target.getZ(), 1f, 0f);
        
        level.addFreshEntity(net);

        brain.setMemoryWithExpiry(ModBrainMemories.IS_ON_NET_COOLDOWN.get(), true, this.cooldown);
    }
    
}
