package net.zincstudios.scgextra.entity.asgharian.candlefiend;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.entity.common.client.ExpandedAnimationController;
import net.zincstudios.scgextra.sounds.AsgharianSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CandleFiendEntity extends Monster implements GeoEntity, Leaping {

    public enum BehaviorState {
        NONE, ENRAGED, SLASH, SLAM, DYING, REVIVE
    }

    public static final AttributeModifier UNMASKED_SPEED_MODIFIER = new AttributeModifier("unmasked_speed", 0.4,
            AttributeModifier.Operation.MULTIPLY_TOTAL);

    private static final EntityDataAccessor<Boolean> MASKED =
            SynchedEntityData.defineId(CandleFiendEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation IDLE_UNMASKED = RawAnimation.begin().thenLoop("idle_mask_off");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation WALK_UNMASKED = RawAnimation.begin().thenLoop("walk_mask_off");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation RUN_UNMASKED = RawAnimation.begin().thenLoop("run_mask_off");
    private static final RawAnimation ENRAGED = RawAnimation.begin().thenPlay("enraged");
    private static final RawAnimation ENRAGED_UNMASKED = RawAnimation.begin().thenPlay("enraged_mask_off");
    private static final RawAnimation SLASH = RawAnimation.begin().thenPlay("double_slash");
    private static final RawAnimation SLASH_UNMASKED = RawAnimation.begin().thenPlay("double_slash_mask_off");
    private static final RawAnimation SLAM = RawAnimation.begin().thenPlay("slam");
    private static final RawAnimation SLAM_UNMASKED = RawAnimation.begin().thenPlay("slam_mask_off");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("death_1");
    private static final RawAnimation DEATH_UNMASKED = RawAnimation.begin().thenPlay("death_2");
    private static final RawAnimation REVIVE = RawAnimation.begin().thenPlay("revive");
    private static final RawAnimation EFFECTS_BASE = RawAnimation.begin().thenPlayAndHold("effect.none");
    private static final RawAnimation EYE_FLASH = RawAnimation.begin().thenPlay("effect.eye_flash");

    public static final int WARNING_FLASH_DURATION = 10;
    private static final int ENRAGE_DURATION_TICKS = 70;  // Match with animation

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);

    // Serverside only
    private BehaviorState behaviorState = BehaviorState.NONE;
    private LivingEntity lastTarget = null;
    private int enragedTicks = 0;
    private int slamDelay = 0;

    public CandleFiendEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            ClientLevel clientLevel = (ClientLevel) level();  // Dedicated Server doesn't like doing instanceof ClientLevel
            if (this.isMasked() && !this.isDeadOrDying() && this.tickCount % 3 == 0) {
                Vec3 pos = this.isSprinting() ? new Vec3(0,3.4,1) : new Vec3(0,3.6,0.3);
                pos = pos.add(
                        (this.getRandom().nextDouble() - 0.5) * 2,
                        (this.getRandom().nextDouble() - 0.5) * 0.6,
                        (this.getRandom().nextDouble() - 0.5) * 0.6
                        ).yRot(-this.yBodyRot * Mth.DEG_TO_RAD)
                        .add(this.position());

                clientLevel.addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        pos.x, pos.y, pos.z,
                        0, (this.getRandom().nextDouble()) * 0.02, 0
                );

                clientLevel.addParticle(
                        ParticleTypes.SMOKE,
                        pos.x, pos.y, pos.z,
                        (this.getRandom().nextDouble() - 0.5) * 0.03,
                        0,
                        (this.getRandom().nextDouble() - 0.5) * 0.03
                );
            }


        } else {
            if (this.tickCount % 20 == 0) {
                if (this.isMasked()) {
                    if (!this.hasEffect(MobEffects.REGENERATION)) {
                        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 1));
                    }

                } else {
                    if (this.hasEffect(MobEffects.REGENERATION)) {
                        this.removeEffect(MobEffects.REGENERATION);
                    }
                    AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speedAttr != null && !speedAttr.hasModifier(UNMASKED_SPEED_MODIFIER)) {
                        speedAttr.addTransientModifier(UNMASKED_SPEED_MODIFIER);
                    }
                }
            }

            if (this.getBehaviourState() == BehaviorState.SLAM) {
                this.slamDelay--;
                if (this.slamDelay == 0) {
                    this.triggerAnim("behaviour", "slam");
                }
            }

            if (this.enragedTicks > 0) {
                this.enragedTicks--;
                this.getNavigation().stop();
                this.setBehaviorState(BehaviorState.ENRAGED);
            } else if (this.getBehaviourState() == BehaviorState.ENRAGED) {
                this.setBehaviorState(BehaviorState.NONE);
            }

            if (this.getTarget() != null) {
                if (this.lastTarget == null) {
                    this.enragedTicks = ENRAGE_DURATION_TICKS;
                }
                this.setSprinting(true);
            } else {
                this.setSprinting(false);
            }
            this.lastTarget = this.getTarget();
        }
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.isMasked()) {
            if (this.deathTime >= 160) {
                if (!this.level().isClientSide) this.setBehaviorState(BehaviorState.NONE);
                this.deathTime = 0;
                this.setMasked(false);
                this.setHealth(this.getMaxHealth());
                this.dead = false;
            } else if (this.deathTime >= 100) {
                if (!this.level().isClientSide) this.setBehaviorState(BehaviorState.REVIVE);
            } else {
                if (!this.level().isClientSide) this.setBehaviorState(BehaviorState.DYING);
            }
        } else {
            this.setBehaviorState(BehaviorState.DYING);
            if (this.deathTime >= 65 && !this.level().isClientSide() && !this.isRemoved()) {
                this.level().broadcastEntityEvent(this, (byte)60);
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return !this.isMasked();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new CandleFiendAttackGoal(this, 20, true));
        this.goalSelector.addGoal(1, new LeapGoal<>(this, 160, 3));
        this.goalSelector.addGoal(2, new BreakBlocksToTargetGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // Bosses should prioritize players
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(3, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 7.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.MAX_HEALTH, 700.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, state -> {
            if (state.getAnimatable().isMasked()) {
                if (state.isMoving()) {
                    return state.setAndContinue(state.getAnimatable().isSprinting() ? RUN : WALK);
                }
                else {
                    return state.setAndContinue(IDLE);
                }
            } else {
                if (state.isMoving()) {
                    return state.setAndContinue(state.getAnimatable().isSprinting() ? RUN_UNMASKED : WALK_UNMASKED);
                }
                else {
                    return state.setAndContinue(IDLE_UNMASKED);
                }
            }
        }));

        controllers.add(new ExpandedAnimationController<>(this, "behaviour", 2, state -> PlayState.STOP)
                .triggerableAnim("enraged", (ctr) -> this.isMasked() ? ENRAGED : ENRAGED_UNMASKED)
                .triggerableAnim("slash", (ctr) -> this.isMasked() ? SLASH : SLASH_UNMASKED)
                .triggerableAnim("slam", (ctr) -> this.isMasked() ? SLAM : SLAM_UNMASKED)
        );

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isMasked()) {
                if (state.getAnimatable().isDeadOrDying() || state.isCurrentAnimation(DEATH)) {
                    return state.setAndContinue(DEATH);
                }
            } else {
                if (state.getAnimatable().isDeadOrDying()) {
                    return state.setAndContinue(DEATH_UNMASKED);
                }
            }
            return PlayState.STOP;
        }).triggerableAnim("revive", REVIVE));

        controllers.add(new AnimationController<>(this, "revive", 2, state -> PlayState.STOP)
                .triggerableAnim("revive", REVIVE));

        controllers.add(new AnimationController<>(this, "effects", 0,
                state -> state.setAndContinue(EFFECTS_BASE))
                .triggerableAnim("eye_flash", EYE_FLASH)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MASKED, true);
    }

    public boolean isMasked() {
        return this.entityData.get(MASKED);
    }

    private void setMasked(boolean masked) {
        this.entityData.set(MASKED, masked);
    }

    public CandleFiendEntity.BehaviorState getBehaviourState() {
        return this.behaviorState;
    }

    public void setBehaviorState(CandleFiendEntity.BehaviorState behaviorState) {
        if (this.behaviorState != BehaviorState.ENRAGED && behaviorState == BehaviorState.ENRAGED) {
            this.triggerAnim("behaviour", "enraged");
            this.playSound(AsgharianSounds.CANDLE_FIEND_SCREAM.get());
        } else if (this.behaviorState != BehaviorState.SLASH && behaviorState == BehaviorState.SLASH) {
            this.triggerAnim("behaviour", "slash");
            SCGExtra.LOGGER.debug("trig sound: " + this.tickCount);
            this.playSound(AsgharianSounds.CANDLE_FIEND_SLASH.get());
        } else if (this.behaviorState != BehaviorState.SLAM && behaviorState == BehaviorState.SLAM) {
            this.slamDelay = WARNING_FLASH_DURATION;
            this.triggerAnim("effects", "eye_flash");
            this.playSound(AsgharianSounds.CANDLE_FIEND_WARNING.get());
        } else if (this.behaviorState != BehaviorState.REVIVE && behaviorState == BehaviorState.REVIVE) {
            this.triggerAnim("revive", "revive");
            this.playSound(AsgharianSounds.CANDLE_FIEND_REVIVE.get());
        }

        this.behaviorState = behaviorState;
    }

    // TODO: improve slam visual impact
    // TODO: fast lunging advancing behaviour?

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Masked", this.isMasked());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Masked")) {
            this.setMasked(tag.getBoolean("Masked"));
        }
    }

    @Override
    public boolean canLeap() {
        return this.getBehaviourState() == BehaviorState.NONE;
    }

    protected SoundEvent getAmbientSound() {
        return AsgharianSounds.CANDLE_FIEND_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AsgharianSounds.CANDLE_FIEND_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return AsgharianSounds.CANDLE_FIEND_DEATH.get();
    }

    protected SoundEvent getStepSound() {
        if (this.isSprinting()) {
            return AsgharianSounds.CANDLE_FIEND_RUN.get();
        } else {
            return AsgharianSounds.CANDLE_FIEND_WALK.get();
        }
    }

    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(this.getStepSound(), this.isMasked() ? 0.25F : 0.35F, 1.0F);
    }
}
