package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiPredicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BreakBlocksToTarget extends Behavior<Mob> {

    public static final int POS_CHECK_INTERVAL = 10;
    public static final double LAST_POS_DISTANCE_THRESHOLD = 0.5;
    public static final double TARGET_DISTANCE_THRESHOLD = 12;
    public static final double HEIGHT_DIFF_THRESHOLD = 2;

    private final BiPredicate<Level, BlockPos> canBreakBlock;
    private final int frustrationThresh;

    private Vec3 lastPos = Vec3.ZERO;
    private long lastPosCheck = 0;  // gameTime timestamp
    private int frustration = 0;  // TODO: maybe turn into a memory module

    public BreakBlocksToTarget(BiPredicate<Level, BlockPos> canBreakBlock, int frustrationThresh) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ), 200);
        this.canBreakBlock = canBreakBlock;
        this.frustrationThresh = frustrationThresh;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null) return false;

        if (level.getGameTime()-this.lastPosCheck > POS_CHECK_INTERVAL) {
            if (this.lastPos.closerThan(mob.position(), LAST_POS_DISTANCE_THRESHOLD)
                    && target.position().closerThan(mob.position(), TARGET_DISTANCE_THRESHOLD)){
                this.frustration += 5;
            } else {
                this.frustration -= 1;
            }
            this.frustration = Mth.clamp(this.frustration, 0, 60);

            this.lastPos = mob.position();
            this.lastPosCheck = mob.tickCount;
        }

        return this.frustration > this.frustrationThresh;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // already handled on canStillUse
    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();

        if (level.getGameTime() % 10 == 0) {
            AABB breakBB = this.getBreakBB(mob, target);
            this.breakBlocks(level, mob, breakBB);
        }
    }

    public void breakBlocks(ServerLevel level, LivingEntity entity, AABB aabb) {
        BlockPos min = new BlockPos(Mth.floor(aabb.minX), Mth.floor(aabb.minY), Mth.floor(aabb.minZ));
        BlockPos max = new BlockPos(Mth.floor(aabb.maxX), Mth.floor(aabb.maxY), Mth.floor(aabb.maxZ));

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (this.canBreakBlock.test(level, pos)) {
                level.destroyBlock(pos, true, entity);
            }
        }
    }

    protected AABB getBreakBB(LivingEntity entity, LivingEntity target) {
        Vec3 direction = target.position().subtract(entity.position()).normalize();
        Vec3 xzOffset = new Vec3(direction.x, 0, direction.z).normalize();

        AABB breakBB = entity.getBoundingBox();
        breakBB = breakBB.move(xzOffset);

        double pitchDegrees = Math.toDegrees(Math.asin(direction.y));
        if (target.position().y-entity.position().y >= HEIGHT_DIFF_THRESHOLD) {
            breakBB = breakBB.move(0,1.1,0);
        } else if (pitchDegrees < -45.0) {
            breakBB = breakBB.move(0,-0.9,0);
        }

        return breakBB;
    }
}
