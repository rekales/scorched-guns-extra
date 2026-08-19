package net.zincstudios.scgextra.entity.wreckers.wrecker_blue;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.common.GunnerEntity;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.sounds.InterruptibleVoice;
import net.zincstudios.scgextra.sounds.WreckersSounds;
import software.bernie.geckolib.animatable.GeoEntity;

import java.util.List;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WreckerBlueEntity extends GunnerEntity implements GeoEntity, InterruptibleVoice {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private LivingEntity delayedHitTarget;
    private int delayedHitTicks = -1;

    public WreckerBlueEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34F)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.MAX_HEALTH, 22.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new WreckerBlueMeleeGoal(this, 1.35D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity)));
    }

    public void scheduleDelayedHit(LivingEntity target, int delayTicks) {
        if (target == null || !target.isAlive()) {
            this.clearDelayedHit();
            return;
        }
        this.delayedHitTarget = target;
        this.delayedHitTicks = Math.max(0, delayTicks);
    }

    private void clearDelayedHit() {
        this.delayedHitTarget = null;
        this.delayedHitTicks = -1;
    }

    private void processDelayedHit() {
        if (this.level().isClientSide() || this.delayedHitTicks < 0) {
            return;
        }
        if (this.delayedHitTicks > 0) {
            this.delayedHitTicks--;
            return;
        }
        LivingEntity target = this.delayedHitTarget;
        this.clearDelayedHit();
        if (target == null || !target.isAlive()) {
            return;
        }
        double reachSqr = this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + target.getBbWidth();
        if (this.distanceToSqr(target) <= reachSqr && this.hasLineOfSight(target)) {
            this.doHurtTarget(target);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.processDelayedHit();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {

            this.playSound(WreckersSounds.BLUE_ATTACK.get());
        }
        return hit;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
            boolean moving = state.isMoving();
            if (this.isAggressive()) {
                state.getController().setAnimationSpeed(1.3);
                return state.setAndContinue(moving ? RUN : IDLE);
            }
            state.getController().setAnimationSpeed(1.0);
            return state.setAndContinue(moving ? WALK : IDLE);
        }));

        controllers.add(new AnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK)
                .setAnimationSpeed(1.4));

        controllers.add(new AnimationController<>(this, "death", 0, state -> {
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected float getSoundVolume() {
        return 0.85F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WreckersSounds.BLUE_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WreckersSounds.BLUE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WreckersSounds.BLUE_DEATH.get();
    }

    @Override
    public List<SoundEvent> voiceLinesToSilenceOnDeath() {
        return WreckersSounds.blueVoiceLines();
    }
}
