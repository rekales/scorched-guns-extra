package net.zincstudios.scgextra.entity.cog.juggernaut;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.MobUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JetBootsAbility extends Behavior<CogJuggernautEntity> {

    public static final int DEFAULT_COOLDOWN_DURATION_TICKS = 120;
    public static final String ABILITY_ID = "cog_juggernaut_jet_boots";

    private final int cooldownDuration;
    private final int jetStartTicks;

    private float targetRot = 0;
    private long startTime = 0;
    private int recoveryTicks = 0;

    public JetBootsAbility() {
        this(DEFAULT_COOLDOWN_DURATION_TICKS);
    }

    public JetBootsAbility(int cooldownDuration) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.REGISTERED,
                ModBrainMemories.RELOCATE_TARGET.get(), MemoryStatus.VALUE_PRESENT
        ), 120);
        this.cooldownDuration = cooldownDuration;
        this.jetStartTicks = 20;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CogJuggernautEntity mob, long gameTime) {
        if (gameTime-this.startTime > this.jetStartTicks+10 && mob.onGround()) {
            mob.setJetActive(false);
            if (this.recoveryTicks-- <= 0) return false;
        }

        Brain<?> brain = mob.getBrain();
        return brain.hasMemoryValue(ModBrainMemories.RELOCATE_TARGET.get())
                && !brain.hasMemoryValue(ModBrainMemories.JET_BOOTS_COOLING_DOWN.get())
                && brain.hasMemoryValue(ModBrainMemories.ABILITY_STATE.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // because already handled on hasRequiredMemories
    @Override
    protected void start(ServerLevel level, CogJuggernautEntity mob, long gameTime) {
        mob.setJetActive(true);
        Brain<?> brain = mob.getBrain();
        PositionTracker target = brain.getMemory(ModBrainMemories.RELOCATE_TARGET.get()).get();
        this.targetRot = MobUtil.rotFromVec(target.currentPosition().subtract(mob.position()));
        this.startTime = gameTime;
        this.recoveryTicks = 30;

        brain.setMemoryWithExpiry(
                ModBrainMemories.ABILITY_STATE.get(),
                new AbilityState(ABILITY_ID, gameTime, gameTime + 120),
                120
        );
    }

    @Override
    protected void stop(ServerLevel level, CogJuggernautEntity mob, long gameTime) {
        mob.setJetActive(false);
        Brain<?> brain = mob.getBrain();
        brain.setMemoryWithExpiry(ModBrainMemories.JET_BOOTS_COOLING_DOWN.get(), true, this.cooldownDuration);
        brain.eraseMemory(ModBrainMemories.ABILITY_STATE.get());
        brain.eraseMemory(ModBrainMemories.RELOCATE_TARGET.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // because already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, CogJuggernautEntity mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        PositionTracker target = brain.getMemory(ModBrainMemories.RELOCATE_TARGET.get()).get();

        if (brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);  // Because look control isn't working great when flying
        }
        MobUtil.turnEntityToYaw(mob, this.targetRot, 10f);

        long durationTicks = gameTime-this.startTime;
        if (durationTicks < this.jetStartTicks) return;

        double horizontalDist = target.currentPosition().subtract(mob.position()).horizontalDistance();
        double verticalAccel = 0.04F + 0.06F
                * ((16 - getDistanceToGround(mob, 12)) / 16)
                * Math.min(horizontalDist / 4f, 1);
        double horizontalAccel = 0.04F
                * Mth.clamp((durationTicks - this.jetStartTicks) / 20f, 0, 1)
                * Math.min(horizontalDist / 5f, 1);

        Vec3 delta = target.currentPosition().subtract(mob.position());
        delta = new Vec3(delta.x, 0, delta.z).normalize()
                .scale(horizontalAccel)
                .add(0, verticalAccel, 0);
        mob.addDeltaMovement(delta);
    }

    @SuppressWarnings("SameParameterValue")
    private static double getDistanceToGround(LivingEntity entity, double maxDist) {
        Vec3 start = entity.position();
        Vec3 end = start.add(0, -maxDist, 0);

        BlockHitResult result = entity.level().clip(new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                entity
        ));

        if (result.getType() == HitResult.Type.MISS) {
            return maxDist;
        }
        return start.y - result.getLocation().y;
    }
}
