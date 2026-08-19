package net.zincstudios.scgextra.entity.cog;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.gun.ScorchedSimGun;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.item.GunItem;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MountedGunGoal<T extends Mob> extends Goal {

    protected final T mob;
    protected final GunItem gunItem;
    protected final ItemStack gunStack;
    protected double maxRange = 12;
    protected int attackInterval = 20;
    protected int burstAmount = 3;
    protected int burstInterval = 4;
    protected Vec3 spawnOffset;
    protected boolean constantFire = false;
    protected float accuracyModifier = 2.0F;
    protected float spreadDegrees = 0F;

    protected int attackCooldown = 0;
    protected int burstCooldown = 0;
    protected int burstLeft = 0;
    protected int seeTime = 0;

    public MountedGunGoal(T mob, GunItem gunItem) {
        this.mob = mob;
        this.gunItem = gunItem;
        this.gunStack = new ItemStack(gunItem);
        this.spawnOffset = this.mob.getEyePosition();
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null
                && !this.mob.getTarget().isDeadOrDying();
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

        if (this.canShoot(target)) {
            if (this.attackCooldown <= 0) {
//                this.setGoalState(FIRING_STATE);
                boolean continueAttack = handleAttack(target);
                if (!continueAttack) {
                    resetAttackCooldown();
//                    this.setGoalState(AIMING_STATE);
                }
//            } else {
//                this.setGoalState(AIMING_STATE);
            }
        }
    }

    public boolean canShoot(LivingEntity target) {
        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean lineOfSight = this.mob.getSensing().hasLineOfSight(target);

        if (lineOfSight) {
            this.seeTime += this.seeTime < 40 ? 1 : 0;
        } else {
            this.seeTime -= this.seeTime > 0 ? 1 : 0;
        }

        return this.seeTime >= 20 && distSqr <= (this.maxRange*this.maxRange) * (this.burstLeft > 0 ? 1.3 : 1)
                && this.mob.position().closerThan(target.position(), this.maxRange);
    }

    protected boolean handleAttack(LivingEntity target) {
        if (this.burstLeft == 0) {
            this.burstLeft = this.burstAmount;
        }

        this.burstCooldown--;
        if (this.burstLeft > 0 && this.burstCooldown <= 0) {
            this.burstCooldown = this.burstInterval;
            this.burstLeft--;
//            this.mob.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            fireGun(target);
        }

        return this.burstLeft > 0;
    }

    protected void fireGun(LivingEntity target) {
        Gun gun = this.gunItem.getModifiedGun(this.gunStack);
        Vec3 spawnPosOffset = this.getSpawnOffset();
        if (this.spreadDegrees > 0F) {
            MobUtil.performGunAttack(this.mob, target, this.gunStack, gun,
                    () -> this.computeFireDirection(target, gun, spawnPosOffset), spawnPosOffset);
        } else {
            MobUtil.performGunAttack(this.mob, target, this.gunStack, gun, this.getAccuracyModifier(), spawnPosOffset);
        }

        ResourceLocation fireSound = gun.getSounds().getFire();
        if (fireSound != null) {
            double posX = this.mob.getX() + spawnPosOffset.x;
            double posY = this.mob.getY() + spawnPosOffset.y;
            double posZ = this.mob.getZ() + spawnPosOffset.z;
            float volume = (float) Config.COMMON.gameplay.mobGunfireVolume.get();
            float pitch = 0.9F + this.mob.level().random.nextFloat() * 0.2F;
            this.mob.level().playSound(null, posX, posY, posZ, SoundEvent.createVariableRangeEvent(fireSound), SoundSource.HOSTILE, volume - 0.5F, pitch);
        }
    }

    protected float getAccuracyModifier() {
        return this.accuracyModifier;
    }

    protected Vec3 computeFireDirection(LivingEntity target, Gun gun, Vec3 spawnPosOffset) {
        Vec3 muzzle = this.mob.position().add(spawnPosOffset);
        Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D);
        double speed = Math.max(gun.getProjectile().getSpeed(), 0.1D);
        double timeToHit = targetPos.distanceTo(muzzle) / speed;
        Vec3 predicted = targetPos.add(target.getDeltaMovement().scale(timeToHit));
        Vec3 dir = predicted.subtract(muzzle).normalize();
        return ScorchedSimGun.addWeaponSpread(this.mob, dir, this.spreadDegrees);
    }

    protected Vec3 getSpawnOffset() {
        return this.spawnOffset.yRot(-this.mob.yBodyRot * Mth.DEG_TO_RAD);
    }

    protected void resetAttackCooldown() {
        int randRange = this.attackInterval/4;
        this.attackCooldown = this.mob.getRandom().nextIntBetweenInclusive(this.attackInterval - randRange, this.attackInterval + randRange);
    }

    // Factory methods
    public MountedGunGoal<T> maxRange(double maxRange) {
        this.maxRange = maxRange;
        return this;
    }

    public MountedGunGoal<T> attackInterval(int intervalTicks) {
        this.attackInterval = intervalTicks;
        return this;
    }

    public MountedGunGoal<T> burstAmount(int burstAmount) {
        this.burstAmount = burstAmount;
        return this;
    }

    public MountedGunGoal<T> burstIntervalTicks(int burstIntervalTicks) {
        this.burstInterval = Math.max(burstIntervalTicks, 1);
        return this;
    }

    public MountedGunGoal<T> constantFire() {
        this.constantFire = true;
        return this;
    }

    public MountedGunGoal<T> spawnOffset(Vec3 spawnOffset) {
        this.spawnOffset = spawnOffset;
        return this;
    }

    public MountedGunGoal<T> accuracyModifier(float accuracyModifier) {
        this.accuracyModifier = accuracyModifier;
        return this;
    }

    public MountedGunGoal<T> spread(float spreadDegrees) {
        this.spreadDegrees = spreadDegrees;
        return this;
    }
}
