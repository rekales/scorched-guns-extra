package net.zincstudios.scgextra.entity.common.brain;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
public class FlyDirectTarget {

    private final PositionTracker target;
    private final float speedModifier;
    private final float minDist;
    private final float maxDist;

    public FlyDirectTarget(BlockPos pos, float speedModifier, float minDist, float maxDist) {
        this(new BlockPosTracker(pos), speedModifier, minDist, maxDist);
    }

    public FlyDirectTarget(Vec3 vectorPos, float speedModifier, float minDist, float maxDist) {
        this(new BlockPosTracker(BlockPos.containing(vectorPos)), speedModifier, minDist, maxDist);
    }

    public FlyDirectTarget(Entity targetEntity, float speedModifier, float minDist, float maxDist) {
        this(new EntityTracker(targetEntity, false), speedModifier, minDist, maxDist);
    }

    public FlyDirectTarget(PositionTracker target, float speedModifier, float minDist, float maxDist) {
        this.target = target;
        this.speedModifier = speedModifier;
        this.minDist = minDist;
        this.maxDist = maxDist;
    }

    public PositionTracker getTarget() {
        return this.target;
    }

    public float getSpeedModifier() {
        return this.speedModifier;
    }

    public float getMinDist() {
        return minDist;
    }

    public float getMaxDist() {
        return maxDist;
    }
}
