package net.zincstudios.scgextra.entity.piglin;

//interface to keep track of mobs
public interface AggroableMob {
    public int getAggroTicks();
    public boolean isAggro();
    public void updateAggroTicks();
    public void setAggro(int ticks);
}
