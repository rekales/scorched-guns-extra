package net.zincstudios.scgextra.entity.common.gun;

import net.minecraft.util.RandomSource;

public class IntervalTriggerSampler implements TriggerStateSampler {

    private final int minOffLength;
    private final int maxOffLength;
    private final int minOnLength;
    private final int maxOnLength;

    private boolean state = false;
    private int currentLength = 0;
    private int flipLength = 0;

    public IntervalTriggerSampler(int minOffLength, int maxOffLength, int minOnLength, int maxOnLength) {
        this.minOffLength = minOffLength;
        this.maxOffLength = maxOffLength;
        this.minOnLength = minOnLength;
        this.maxOnLength = maxOnLength;
        this.setState(true);
    }

    @Override
    public boolean next(RandomSource random) {
        if (this.currentLength >= this.flipLength) {
            if (this.state) {
                this.flipLength = random.nextIntBetweenInclusive(this.minOffLength, this.maxOffLength);
            } else {
                this.flipLength = random.nextIntBetweenInclusive(this.minOnLength, this.maxOnLength);
            }

            this.currentLength = 0;
            this.state = !this.state;
        }

        this.currentLength++;
        return this.state;
    }

    @Override
    public void setState(boolean state) {
        this.state = state;
        this.flipLength = (this.state ? this.minOnLength+this.maxOnLength : this.minOffLength+this.maxOffLength)/2;
    }

    @Override
    public boolean getState() {
        return this.state;
    }
}
