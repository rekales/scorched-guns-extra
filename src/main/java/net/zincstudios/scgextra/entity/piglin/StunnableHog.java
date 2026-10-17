package net.zincstudios.scgextra.entity.piglin;

//MAYBE MAKE BETTER?
/**
 * Utility class to handle stun logic(used in animation) for the piglin faction
 */
public class StunnableHog{

    private final int stunStartDuration;
    private final int stunMiddleDuration;
    private final int stunEndDuration;
    private final int stunDuration;

    public StunnableHog(int stunStartDuration, int stunMiddleDuration, int stunEndDuration){
        this.stunStartDuration = stunStartDuration;
        this.stunMiddleDuration = stunMiddleDuration;
        this.stunEndDuration = stunEndDuration;
        this.stunDuration = this.stunStartDuration+this.stunMiddleDuration+this.stunEndDuration;
    }

    public boolean isStart(long ticksRemaining){
        return (this.stunDuration - ticksRemaining) < this.stunStartDuration;
    }

    public boolean isMiddle(long ticksRemaining){
        return (this.stunDuration - ticksRemaining) < (this.stunStartDuration + this.stunMiddleDuration);
    }

    public boolean isEnd(long ticksRemaining){
        return (this.stunDuration - ticksRemaining) < (this.stunStartDuration + this.stunMiddleDuration + this.stunEndDuration);
    }

    public int getStunDuration(){
        return this.stunDuration;
    }
}