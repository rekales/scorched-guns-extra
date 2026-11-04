package net.zincstudios.scgextra.entity.asgharian;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.Path;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.entity.ai.AIGunEvent;
import top.ribs.scguns.item.GunItem;

import javax.annotation.Nullable;
import java.security.InvalidParameterException;
import java.util.EnumSet;

// Relying heavily on the builder pattern because overloads fucking suck for multiple optionals
// TODO: another overhaul to use a simulated gun object to handle weapon firing logic and some
//   aspects like burst fire and reloads.

/**
 * A much more simplified and dumbed down version of the GunAttackGoal. No reloading, going into cover, or panicking.
 *
 * @see net.zincstudios.scgextra.mixin.GunnerMobSpawnerMixin for mixin that disables dynamic addition of GunAttackGoal.
 */
public class SimpleGunAttackGoal<T extends Mob> extends Goal {

    public static final GoalState FIRING_STATE = new GoalState("simple_gun_firing_state");
    public static final GoalState AIMING_STATE = new GoalState("simple_gun_aiming_state");
    public static final GoalState IDLE_STATE = new GoalState("simple_gun_idle_state");
    public static final GoalState APPROACH_STATE = new GoalState("simple_gun_approach_state");
    public static final int PATH_REEVALUATE_TICKS = 20;

    protected final T mob;
    protected double speedModifier = 1;
    protected int attackInterval = 20;
    protected double approachDist = 6;
    protected double maxRange = 12;
    protected boolean runAndGun = false;
    protected float accuracyOverride = -1;

    protected int attackCooldown = 0;
    protected int seeTime = 0;
    protected GoalState goalState = IDLE_STATE;
    protected @Nullable Path path = null;
    protected int pathTimeout = 0;

    public SimpleGunAttackGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null
                && this.isHoldingGun()
                && !this.mob.getTarget().isDeadOrDying();
    }

    public void start() {
        this.mob.setAggressive(true);
        this.seeTime = 0;
        resetAttackCooldown();
    }

    public void stop() {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
        this.path = null;
        this.setGoalState(IDLE_STATE);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.attackCooldown -= this.attackCooldown > 0 ? 1 : 0;

        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        double dist = this.mob.distanceTo(target);
        boolean lineOfSight = this.mob.getSensing().hasLineOfSight(target);

        if (lineOfSight) {
            this.seeTime += seeTime < 40 ? 1 : 0;
        } else {
            this.seeTime -= seeTime > 0 ? 1 : 0;
        }

        this.tickMovement(target, dist);
        this.tickAttack(target, dist);
    }

    protected void tickMovement(LivingEntity target, double dist) {
        PathNavigation nav = this.mob.getNavigation();
        if (dist > this.maxRange || this.seeTime < 10) {
            if (this.path == null || (this.pathTimeout-- <= 0 && !this.path.getTarget().equals(target.blockPosition()))) {
                this.pathTimeout = PATH_REEVALUATE_TICKS;
                this.path = nav.createPath(target, 1);
                nav.moveTo(this.path, this.speedModifier);
            }
            this.setGoalState(APPROACH_STATE);
        } else if (dist <= this.approachDist) {
            nav.stop();
            this.path = null;
        }
    }

    protected void tickAttack(LivingEntity target, double dist) {
        if (this.seeTime >= 10 && dist <= this.maxRange) {
            if (!this.runAndGun) {
                this.mob.getNavigation().stop();
                this.path = null;
            }

            if (this.attackCooldown <= 0) {
                this.setGoalState(FIRING_STATE);
                boolean continueAttack = handleAttack(target);
                if (!continueAttack) {
                    resetAttackCooldown();
                    this.setGoalState(AIMING_STATE);
                }
            } else {
                this.setGoalState(AIMING_STATE);
            }
        }
    }

    /**
     * @return false to stop the attack and reset the cooldown
     */
    protected boolean handleAttack(LivingEntity target) {
        this.mob.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        fireGun(target);
        return false;
    }

    protected void fireGun(LivingEntity target) {
        ItemStack itemStack = this.mob.getMainHandItem();
        if (itemStack.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(itemStack);
            AIGunEvent.performGunAttack(this.mob, target, itemStack, gun, this.getAccuracyModifier());

            ResourceLocation fireSound = gun.getSounds().getFire();
            if (fireSound != null) {
                double posX = this.mob.getX();
                double posY = this.mob.getY() + (double)this.mob.getEyeHeight();
                double posZ = this.mob.getZ();
                float volume = (float) Config.COMMON.gameplay.mobGunfireVolume.get();
                float pitch = 0.9F + this.mob.level().random.nextFloat() * 0.2F;
                this.mob.level().playSound(null, posX, posY, posZ, SoundEvent.createVariableRangeEvent(fireSound), SoundSource.HOSTILE, volume - 0.5F, pitch);
            }
        }
    }

    protected float getAccuracyModifier() {
        if (this.accuracyOverride > 0) {
            return this.accuracyOverride;
        }
        return runAndGun ? 2f : 3.2f;
    }

    protected boolean isHoldingGun() {
        return this.mob.isHolding((itemStack) -> itemStack.getItem() instanceof GunItem);
    }

    protected void resetAttackCooldown() {
        int randRange = this.attackInterval/4;
        this.attackCooldown = this.mob.getRandom().nextIntBetweenInclusive(this.attackInterval - randRange, this.attackInterval + randRange);
    }

    protected void setGoalState(GoalState goalState) {
        this.goalState = goalState;
        if (this.mob instanceof GoalStateHandler goalStateHandler) {
            goalStateHandler.onGoalStateChanged(this, goalState);
        }
    }

    public GoalState getGoalState() {
        return this.goalState;
    }

    // Factory methods
    public SimpleGunAttackGoal<T> speedModifier(double speedModifier) {
        this.speedModifier = speedModifier;
        return this;
    }

    public SimpleGunAttackGoal<T> attackInterval(int intervalTicks) {
        this.attackInterval = intervalTicks;
        return this;
    }

    /**
     * The mob will try to pathfind to the target until it reaches the approachDist
     */
    public SimpleGunAttackGoal<T> approachDist(double approachDist) {
        if (approachDist > this.maxRange)
            throw new InvalidParameterException("approachDist must be smaller or equal to maxRange");

        this.approachDist = approachDist;
        return this;
    }

    /**
     * The mob will try to get closer when farther than the maxRange
     */
    public SimpleGunAttackGoal<T> maxRange(double maxRange) {
        this.maxRange = maxRange;
        return this;
    }

    public SimpleGunAttackGoal<T> runAndGun() {
        this.runAndGun = true;
        return this;
    }

    public SimpleGunAttackGoal<T> accuracy(float accuracy) {
        this.accuracyOverride = accuracy;
        return this;
    }
}
