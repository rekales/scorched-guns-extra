package net.zincstudios.scgextra.entity.cog.juggernaut;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.ModBrainMemories;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JetBootsCheckRelocate extends Behavior<CogJuggernautEntity> {

    public static final float TOO_CLOSE_DIST = 4;
    public static final float TOO_FAR_DIST = 25;

    private float oldDist = -1;
    private float accDistDelta = 0;  // accumulated distance delta

    public JetBootsCheckRelocate() {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.ABILITY_STATE.get(), MemoryStatus.VALUE_ABSENT,
                ModBrainMemories.WEAPON_IDEAL_RANGE.get(), MemoryStatus.VALUE_PRESENT,
                ModBrainMemories.RELOCATE_TARGET.get(), MemoryStatus.VALUE_ABSENT,
                ModBrainMemories.JET_BOOTS_COOLING_DOWN.get(), MemoryStatus.VALUE_ABSENT
        ));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // because already handled on hasRequiredMemories
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CogJuggernautEntity mob) {
        Brain<?> brain = mob.getBrain();
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
        float idealRange = brain.getMemory(ModBrainMemories.WEAPON_IDEAL_RANGE.get()).get();

        if (this.oldDist < 0) {
            this.oldDist = mob.distanceTo(target);
            this.accDistDelta = 0;
        }

        float dist = mob.distanceTo(target);
        this.accDistDelta += dist - this.oldDist;
        this.oldDist = dist;
        if (this.accDistDelta > 0) {
            this.accDistDelta = Math.max(0, this.accDistDelta-0.02f);
        } else {
            this.accDistDelta = Math.min(0, this.accDistDelta+0.05f);
        }

        float effectiveDist = dist + (isLookingAwayFrom(target, mob.position()) ? 4 : 0);
        effectiveDist += this.accDistDelta * (target.isSprinting() ? 1.3f : 1);

        if (effectiveDist < TOO_CLOSE_DIST) {
            Vec3 targetPos = mob.getEyePosition()
                    .subtract(target.getEyePosition())
                    .normalize()
                    .scale(16);
            targetPos = targetPos
                    .add(
                            (level.getRandom().nextDouble() - 0.5) * 2,
                            0,
                            (level.getRandom().nextDouble() - 0.5) * 2
                    ).add(mob.position());
            BlockHitResult result = level.clip(new ClipContext(
                    targetPos, targetPos.add(0, -4, 0),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY,
                    null
            ));

            if (result.getType() == HitResult.Type.BLOCK) {
                brain.setMemory(ModBrainMemories.RELOCATE_TARGET.get(), new BlockPosTracker(result.getBlockPos().above()));
                return true;
            }
        } else if (effectiveDist > idealRange + 6 || dist > TOO_FAR_DIST) {
            Vec3 targetPos = target.getEyePosition()
                    .subtract(mob.getEyePosition())
                    .normalize()
                    .scale(effectiveDist - 4)
                    .add(mob.position());

            BlockHitResult result = level.clip(new ClipContext(
                    targetPos, targetPos.add(0, -4, 0),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY,
                    null
            ));

            if (result.getType() == HitResult.Type.BLOCK) {
                brain.setMemory(ModBrainMemories.RELOCATE_TARGET.get(), new BlockPosTracker(result.getBlockPos().above()));
                return true;
            }
        }

        return false;
    }

    @Override
    protected void start(ServerLevel level, CogJuggernautEntity entity, long gameTime) {
        this.oldDist = -1;
    }

    private static boolean isLookingAwayFrom(LivingEntity entity, Vec3 targetPos) {
        Vec3 lookVec = entity.getLookAngle();
        Vec3 toTarget = targetPos.subtract(entity.getEyePosition()).normalize();
        return lookVec.dot(toTarget) < -0.5;
    }
}
