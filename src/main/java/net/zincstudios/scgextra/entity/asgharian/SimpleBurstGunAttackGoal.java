package net.zincstudios.scgextra.entity.asgharian;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * @see SimpleGunAttackGoal for docs.
 */
public class SimpleBurstGunAttackGoal<T extends Mob> extends SimpleGunAttackGoal<T> {

    protected int burstAmount = 3;
    protected int burstInterval = 4;
    protected int burstCooldown = 0;
    protected int burstLeft = 0;

    public SimpleBurstGunAttackGoal(T mob) {
        super(mob);
    }

    @Override
    public void start() {
        super.start();
        burstLeft = 0;
        burstCooldown = 0;
    }

    protected boolean handleAttack(LivingEntity target) {
        if (this.burstLeft == 0) {
            this.burstLeft = this.burstAmount;
        }

        this.burstCooldown--;
        if (this.burstLeft > 0 && this.burstCooldown <= 0) {
            this.burstCooldown = this.burstInterval;
            this.burstLeft--;
            this.mob.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            fireGun(target);
        }

        return this.burstLeft > 0;
    }

    // Factory methods
    public SimpleBurstGunAttackGoal<T> burstAmount(int burstAmount) {
        this.burstAmount = burstAmount;

        return this;
    }

    public SimpleBurstGunAttackGoal<T> burstIntervalTicks(int burstIntervalTicks) {
        this.burstInterval = Math.max(burstIntervalTicks, 1);

        return this;
    }
}
