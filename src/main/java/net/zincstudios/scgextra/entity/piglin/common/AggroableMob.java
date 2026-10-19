package net.zincstudios.scgextra.entity.piglin.common;

//interface to keep track of mobs
public interface AggroableMob {
    public int getAggroTicks();
    public boolean isAggro();
    public void updateAggroTicks();
    public void setAggro(int ticks);
}
