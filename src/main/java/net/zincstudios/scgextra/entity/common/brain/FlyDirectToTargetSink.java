package net.zincstudios.scgextra.entity.common.brain;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.ModBrainMemories;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FlyDirectToTargetSink extends Behavior<Mob> {

    public FlyDirectToTargetSink() {
        this(150, 250);
    }

    public FlyDirectToTargetSink(int minDuration, int maxDuration) {
        super(ImmutableMap.of(
                ModBrainMemories.FLY_DIRECT_TARGET.get(), MemoryStatus.VALUE_PRESENT
        ), minDuration, maxDuration);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return entity.getBrain().hasMemoryValue(ModBrainMemories.FLY_DIRECT_TARGET.get());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")  // handled on canStillUse
    @Override
    protected void tick(ServerLevel level, Mob owner, long gameTime) {
        FlyDirectTarget target = owner.getBrain().getMemory(ModBrainMemories.FLY_DIRECT_TARGET.get()).get();
        Vec3 targetPos = target.getTarget().currentPosition();
        Vec3 currentPos = owner.position();
        Vec3 dir = targetPos.subtract(currentPos);

        if (dir.lengthSqr() > target.getMaxDist() * target.getMaxDist()) {
            Vec3 movePos = dir.reverse().normalize().scale(target.getMaxDist()).add(targetPos);
            owner.getMoveControl().setWantedPosition(movePos.x, movePos.y, movePos.z, target.getSpeedModifier());
        } else if (dir.lengthSqr() < target.getMinDist() * target.getMinDist()) {
            Vec3 movePos = dir.reverse().normalize().scale(target.getMinDist()).add(targetPos);
            owner.getMoveControl().setWantedPosition(movePos.x, movePos.y, movePos.z, target.getSpeedModifier());
        }
    }

    @Override
    protected void stop(ServerLevel level, Mob entity, long gameTime) {
        entity.getBrain().eraseMemory(ModBrainMemories.FLY_DIRECT_TARGET.get());
    }
}
