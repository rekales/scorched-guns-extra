package net.zincstudios.scgextra.entity.common.brain;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.ModBrainMemories;

import java.util.Optional;

public class FlightDirectStroll {

    public static OneShot<Mob> create(float range, float speedModifier) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(ModBrainMemories.FLY_DIRECT_TARGET.get()), instance.registered(MemoryModuleType.LOOK_TARGET))
                    .apply(instance, (walkAcc, lookAcc) -> (level, mob, gameTime) -> {
                        Optional<Vec3> optional = Optional.of(getStrollPos(mob, range));
                        walkAcc.setOrErase(optional.map((pos) -> new FlyDirectTarget(pos, speedModifier, 0, 1)));
                        lookAcc.setOrErase(optional.map((pos) -> new BlockPosTracker(BlockPos.containing(pos))));
                        return true;
            }));
    }

    public static Vec3 getStrollPos(Mob mob, float range) {
        RandomSource rand = mob.getRandom();
        rand.nextFloat();

        float yaw = rand.nextFloat() * (float)(2 * Math.PI);
        float pitch = (float) ((rand.triangle(0.0, 0.5)) * Math.PI);
        float dist = (rand.nextFloat() * 0.66f + 0.33f) * range;

        double cosP = Math.cos(pitch);
        Vec3 dir = new Vec3(Math.sin(yaw) * cosP, Math.sin(pitch), Math.cos(yaw) * cosP);

        Vec3 start = mob.getEyePosition();
        Vec3 end = start.add(dir.scale(dist));

        BlockHitResult hit = mob.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob));
        return hit.getType() != HitResult.Type.MISS ? hit.getLocation() : end;
    }

}
