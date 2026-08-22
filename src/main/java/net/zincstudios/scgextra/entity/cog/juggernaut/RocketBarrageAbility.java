package net.zincstudios.scgextra.entity.cog.juggernaut;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.PatchedEntityTracker;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RocketBarrageAbility extends Behavior<CogJuggernautEntity> {

    public static final int DEFAULT_COOLDOWN_DURATION_TICKS = 160;
    public static final String ABILITY_ID = "cog_juggernaut_rocket_barrage";

    private final int cooldownDuration;
    private final SimulatedGun gun;

    private long startTime = 0;  // gameTime timestamp
    private long cooldownEnd = 0;  // gameTime timestamp

    public RocketBarrageAbility() {
        this(DEFAULT_COOLDOWN_DURATION_TICKS);
    }

    public RocketBarrageAbility(int cooldownDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.AIM_TICKS.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED
        ), 80);
        this.cooldownDuration = cooldownDuration;
        this.gun = new CustomScorchedSimGun.Builder(ModItems.ROCKET_RIFLE.get().getGun())
                .projectileFactory(RocketBarrageProjectileEntity::new)
                .velocityModifier(vec -> vec.scale(1/3f).add(0, 0.2F, 0))
                .fireRate(5)
                .reloadTime(10000)
                .ammoCapacity(5)
                .reloads()
                .noGunFlash()
                .build();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CogJuggernautEntity mob) {
        int aimTicks = mob.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).orElse(0);
        return aimTicks >= 40 && this.cooldownEnd < level.getGameTime();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CogJuggernautEntity mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && brain.hasMemoryValue(ModBrainMemories.ABILITY_STATE.get())
                && this.gun.getAmmoCount() > 0;
    }

    @Override
    protected void start(ServerLevel level, CogJuggernautEntity mob, long gameTime) {
        this.startTime = gameTime;
        this.gun.reloadAmmo();
        mob.getBrain().setMemoryWithExpiry(
                ModBrainMemories.ABILITY_STATE.get(),
                new AbilityState(ABILITY_ID, gameTime, gameTime + Behavior.DEFAULT_DURATION),
                Behavior.DEFAULT_DURATION
        );
    }

    @Override
    protected void stop(ServerLevel level, CogJuggernautEntity mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        this.cooldownEnd = gameTime + this.cooldownDuration;
        brain.eraseMemory(ModBrainMemories.ABILITY_STATE.get());
        brain.setMemory(ModBrainMemories.AIM_TICKS.get(), 0);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, CogJuggernautEntity mob, long gameTime) {
        if (gameTime-this.startTime < 30) return;

        Brain<?> brain = mob.getBrain();
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();

        if (!brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
            brain.setMemory(MemoryModuleType.LOOK_TARGET, new PatchedEntityTracker(target, true));
        }

        this.gun.tickFire(mob, SimulatedGun.getCenterMassPos(target), 2F, true);
    }
}
