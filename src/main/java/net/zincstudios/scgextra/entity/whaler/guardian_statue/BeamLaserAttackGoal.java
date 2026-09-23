package net.zincstudios.scgextra.entity.whaler.guardian_statue;

import net.zincstudios.scgextra.CommonConfig;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.sounds.WhalerSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

// NOTE: Designed to work around the shitass GuardianLaserAttackGoal from the Guardian entity
public class BeamLaserAttackGoal extends Goal {

    private final GuardianStatueEntity mob;
    protected final int maxInterval;
    protected final float range;
    protected int cooldown = 0;
    private Vec3 lastPos;

    public BeamLaserAttackGoal(GuardianStatueEntity mob, int maxInterval, float range) {
        this.mob = mob;
        this.maxInterval = maxInterval;
        this.range = range;
        this.lastPos = this.mob.position().add(this.mob.getLookAngle());
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        return livingentity != null && livingentity.isAlive();
    }

    @Override
    public void start() {
        super.start();
        this.cooldown = this.maxInterval;
        this.mob.startBeamActiveTimer(0);
        SCGExtra.LOGGER.debug("restarted");
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    // activeTicks 100-86: silence
    // activeTicks 85-61: eye starts glowing
    // activeTicks 60: eye flashes
    // activeTicks 40: beam renders
    // activeTicks 35: beam hits
    // activeTicks 15: beam starts fading
    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        int timer = this.mob.getBeamActiveTimer();
        if (timer > 0) {
            if (timer == 60 && CommonConfig.enableAbilityWarning) {
                this.mob.triggerAnim("effects", "eye_flash");
            } else if (timer == 35 && this.mob.hasLineOfSight(target) && target.isAlive()) {
                target.hurt(this.mob.damageSources().mobAttack(this.mob), 40F);
            }
        } else if (this.cooldown > 0 && this.mob.hasLineOfSight(target)) {
            this.cooldown--;
        } else if (this.cooldown == 0 && this.mob.getGuardianLaserAttackTimer() <= 0) {  // Don't start when guardian laser is active
            this.cooldown = maxInterval;
            this.mob.startBeamActiveTimer(100);
            timer = 100;
            this.mob.level().playSound(
                this.mob, 
                this.mob.blockPosition(), 
                WhalerSounds.GUARDIAN_STATUE_LASER.get(), 
                SoundSource.MASTER, 
                2.0F, 
                1.0F
            );
        }
        
        if (this.mob.hasLineOfSight(target) && !(0 < timer && timer < 25)) {
            this.lastPos = target.position().add(0,target.getBbHeight()/2,0);
        }
        this.mob.setBeamLookPos(this.lastPos);
    }
}
