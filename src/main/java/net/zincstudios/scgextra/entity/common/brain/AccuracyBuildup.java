package net.zincstudios.scgextra.entity.common.brain;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiFunction;

// NOTE: temporary implementation, will be changed at some point
public final class AccuracyBuildup implements BiFunction<LivingEntity, Boolean, Float> {

    public static final int AIM_RESET_TIME = 30;

    private final float minAcc;
    private final float maxAcc;
    private final float accPerTick;

    private float currentAcc = 0;
    private long lastFire = 0;  // gameTime timestamp

    public AccuracyBuildup(float minAcc, float maxAcc, float accPerTick) {
        this.minAcc = minAcc;
        this.maxAcc = maxAcc;
        this.accPerTick = accPerTick;
    }

    @Override
    public Float apply(LivingEntity entity, Boolean firing) {
        if (!firing) return this.minAcc;

        if (entity.level().getGameTime() - this.lastFire > AIM_RESET_TIME) {
            this.currentAcc = this.minAcc;
        } else {
            this.currentAcc = Math.min(this.currentAcc+this.accPerTick, this.maxAcc);
        }
        this.lastFire = entity.level().getGameTime();
        return this.currentAcc;
    }
}
