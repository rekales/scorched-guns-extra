package net.zincstudios.scgextra.entity.rrc.flaminghead;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.CommonConfig;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.common.HeadShotHandler;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.Stunnable;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.entity.common.goal.StunnedWithVisualGoal;
import net.zincstudios.scgextra.particle.ModParticleTypes;
import net.zincstudios.scgextra.sounds.RRCSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlamingHeadEntity extends Monster implements GeoEntity, Stunnable, HeadShotHandler {

    public enum BehaviorState {
        NONE, RAMMING, SPINNING, STUNNED
    }

    public static final RawAnimation EFFECTS_BASE = RawAnimation.begin().then("effect.none", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static final RawAnimation EYE_FLASH = RawAnimation.begin().then("effect.eye_flash", Animation.LoopType.PLAY_ONCE);

    // For forcing the entity to look to a certain direction because the look control is unresponsive
    private static final EntityDataAccessor<Float> RAM_YAW =
            SynchedEntityData.defineId(FlamingHeadEntity.class, EntityDataSerializers.FLOAT);
    // Ramming has 20 tick charge time to turn to the target direction
    private static final EntityDataAccessor<Boolean> ANIMATE_RAM =
            SynchedEntityData.defineId(FlamingHeadEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BEHAVIOR_STATE =
            SynchedEntityData.defineId(FlamingHeadEntity.class, EntityDataSerializers.INT);
//    private static final Vec3 FLAMER_NO_ROT_OFFSET = new Vec3(0, 2, 2);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    // Server-side only for stunnable handling
    private int headshotCounter = 0;
    private boolean stunCooldown = false;

    // Client-side only for flame particle rendering
    private Vec3[] flamethrowerPos = {Vec3.ZERO,Vec3.ZERO,Vec3.ZERO,Vec3.ZERO};
    private Vec3[] flamethrowerDir = {Vec3.ZERO,Vec3.ZERO,Vec3.ZERO,Vec3.ZERO};  // Radians
    private long spinStart = 0;  // timestamp
    private BehaviorState lastState = BehaviorState.NONE;

    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    public FlamingHeadEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(1.1F);
    }

    public void setFlamethrowerPos(Vec3[] pos) {
        this.flamethrowerPos = pos;
    }

    public void setFlamethrowerDir(Vec3[] radians) {
        this.flamethrowerDir = radians;
    }

    @Override
    public int shouldStun() {
        if (!CommonConfig.enableAbilityWeakness) return 0;

        if (this.headshotCounter >= CommonConfig.abilityWeaknessHeadshots) {
//            return CommonConfig.abilityWeaknessDuration;
            return 100;
        }

        return 0;
    }

    @Override
    public boolean headshot(DamageSource source, float amount) {
        if (this.headshotCounter < CommonConfig.abilityWeaknessHeadshots-1 || !this.stunCooldown) {
            this.headshotCounter++;
        }
        return false;
    }

    @Override
    public void setStunned(boolean stunned) {
        if (stunned) {
            this.setBehaviorState(FlamingHeadEntity.BehaviorState.STUNNED);
        } else {
            if (this.getBehaviorState() == FlamingHeadEntity.BehaviorState.STUNNED) {
                this.setBehaviorState(FlamingHeadEntity.BehaviorState.NONE);
            }
            this.headshotCounter = 0;
        }
    }

    @Override
    public boolean isStunned() {
        return this.getBehaviorState() == BehaviorState.STUNNED;
    }

    public BehaviorState getBehaviorState() {
        return BehaviorState.values()[this.entityData.get(BEHAVIOR_STATE)];
    }

    public void setBehaviorState(BehaviorState state) {
        this.entityData.set(BEHAVIOR_STATE, state.ordinal());
    }

    public void setRamYaw(float yaw) {
        this.entityData.set(RAM_YAW, yaw);
    }

    public float getRamYaw() {
        return this.entityData.get(RAM_YAW);
    }

    public boolean hasRamYaw() {
        return this.entityData.get(RAM_YAW) != -1000;
    }

    public void resetRamYaw() {
        this.entityData.set(RAM_YAW, -1000F);
    }

    public void setAnimateRamming(boolean animateRam) {
        this.entityData.set(ANIMATE_RAM, animateRam);
    }

    public boolean isAnimateRamming() {
        return this.entityData.get(ANIMATE_RAM);
    }

    @Override
    public void setStunCooldown(boolean cooldown) {
        this.stunCooldown = cooldown;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BEHAVIOR_STATE, BehaviorState.NONE.ordinal());
        this.entityData.define(RAM_YAW, -1000F);
        this.entityData.define(ANIMATE_RAM, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300D)
                .add(Attributes.ARMOR, 3D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0.35F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4,
                state -> {
                    if (state.getAnimatable().getBehaviorState() == BehaviorState.STUNNED) {
                        state.setAnimation(RawAnimation.begin().thenLoop("stun"));
                    } else if (state.getAnimatable().isAnimateRamming()) {
                        state.setAnimation(RawAnimation.begin().thenPlay("ramming_attack"));
                    } else if (state.isMoving()) {
                        state.setAnimation(RawAnimation.begin().thenLoop("move"));
                    } else {
                        state.setAnimation(RawAnimation.begin().thenLoop("idle"));
                    }

                    return PlayState.CONTINUE;
                }).triggerableAnim("spin", RawAnimation.begin().thenPlay("fire_attack"))
        );

        controllers.add(new AnimationController<>(this, "death", 4,
                state -> {
                    if (state.getAnimatable().isDeadOrDying()) {
                        return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
                    }
                    return PlayState.STOP;
                }
        ));

        controllers.add(new AnimationController<>(this, "effects", 0,
                state -> state.setAndContinue(EFFECTS_BASE))
                .triggerableAnim("eye_flash", EYE_FLASH)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StunnedWithVisualGoal<>(this));
        this.goalSelector.addGoal(2, new RammingAttackGoal(this, 600, 50, 3));
        this.goalSelector.addGoal(3, new FireSpinAttackGoal(this, 200, 30, 8F, 10));
//
        this.goalSelector.addGoal(5, new MoveTowardsTargetGoal(this, 1, 40));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 20));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(2, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity) || entity.getMobType().equals(MobType.UNDEAD)));
    }

    @Override
    public void tick() {
        super.tick();

        if(!this.level().isClientSide()){
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }

        if (this.level().isClientSide()) {
            Level level = this.level();
            if (this.lastState == BehaviorState.NONE && this.getBehaviorState() == BehaviorState.SPINNING) {
                this.spinStart = this.level().getGameTime();
                this.triggerAnim("main", "spin");
            }
            this.lastState = this.getBehaviorState();

            // Random particles around to not make it look bare
//            for (int i = 0; i < 1; i++) {
            if (this.tickCount%1==0) {

                double posRand = 0.2;
                double dirRand = 0.15;
                Vec3 offset = new Vec3(0, 1.7, 1.5);
                Vec3 pos = offset.yRot(-(this.getRandom().nextFloat() * 360) * Mth.DEG_TO_RAD).add(this.position());

                level.addParticle(
                        ModParticleTypes.COPPER_FIRE_BALL.get(),
                        pos.x + (this.getRandom().nextDouble() - 0.5) * posRand,
                        pos.y + (this.getRandom().nextDouble() - 0.5) * posRand,
                        pos.z + (this.getRandom().nextDouble() - 0.5) * posRand,
                        (this.getRandom().nextDouble() - 0.5) * dirRand,
                        (this.getRandom().nextDouble() - 0.5) * dirRand,
                        (this.getRandom().nextDouble() - 0.5) * dirRand
                );
            }

            for (int i = 0; i < 2; i++) {
                double posRand = 0.2;
                double dirRand = 0.15;
                Vec3 offset = new Vec3(0, 1.7, 1.6);
                Vec3 pos = offset.yRot(-(this.getRandom().nextFloat() * 360) * Mth.DEG_TO_RAD).add(this.position());

                level.addParticle(
                        ParticleTypes.SMOKE,
                        pos.x + (this.getRandom().nextDouble()-0.5) * posRand,
                        pos.y + (this.getRandom().nextDouble()-0.5) * posRand,
                        pos.z + (this.getRandom().nextDouble()-0.5) * posRand,
                        (this.getRandom().nextDouble()-0.5) * dirRand,
                        (this.getRandom().nextDouble()-0.5) * dirRand,
                        (this.getRandom().nextDouble()-0.5) * dirRand
                );
            }

            if (this.tickCount%2==0) {
                double posRand = 0.2;
                double dirRand = 0.15;
                Vec3 offset = new Vec3(0, 1.7, 1.6);
                Vec3 pos = offset.yRot(-(this.getRandom().nextFloat() * 360) * Mth.DEG_TO_RAD).add(this.position());

                level.addParticle(
                        ModParticleTypes.COPPER_FLAME.get(),
                        pos.x + (this.getRandom().nextDouble()-0.5) * posRand,
                        pos.y + (this.getRandom().nextDouble()-0.5) * posRand,
                        pos.z + (this.getRandom().nextDouble()-0.5) * posRand,
                        (this.getRandom().nextDouble()-0.5) * dirRand,
                        (this.getRandom().nextDouble()-0.5) * dirRand,
                        (this.getRandom().nextDouble()-0.5) * dirRand
                );
            }

            // Flamethrower particles
            for (int i = 0; i < 4; i++) {
                boolean turbo = (this.level().getGameTime() - this.spinStart) > 20
                        && (this.level().getGameTime() - this.spinStart) < 50
                        && this.getBehaviorState() == BehaviorState.SPINNING;

                double posRand = 0.2;
                double dirRand = 0.12;
                double velocity = turbo ? 0.75 : 0.15;
                Vec3 delta = flamethrowerDir[i].scale(velocity);

                if(this.tickCount%4==0 || turbo){
                    for (int j = 0; j < 2; j++) {
                        level.addParticle(
                                ModParticleTypes.COPPER_FLAME.get(),
                                this.flamethrowerPos[i].x + (this.getRandom().nextDouble()-0.5) * posRand,
                                this.flamethrowerPos[i].y + (this.getRandom().nextDouble()-0.5) * posRand,
                                this.flamethrowerPos[i].z + (this.getRandom().nextDouble()-0.5) * posRand,
                                delta.x + (this.getRandom().nextDouble()-0.5) * dirRand,
                                delta.y + (turbo ? 0.1 : 0) + (this.getRandom().nextDouble()-0.5) * dirRand,
                                delta.z + (this.getRandom().nextDouble()-0.5) * dirRand
                        );
                        if (!turbo) break;
                    }
                }

                delta = flamethrowerDir[i].scale(velocity * (turbo ? 1.5 : 1));

                if(this.tickCount%3==0 || turbo){
                    for (int j = 0; j < 2; j++) {
                        level.addParticle(
                                ModParticleTypes.COPPER_FIRE_BALL.get(),
                                this.flamethrowerPos[i].x + (this.getRandom().nextDouble()-0.5) * posRand,
                                this.flamethrowerPos[i].y + (this.getRandom().nextDouble()-0.5) * posRand,
                                this.flamethrowerPos[i].z + (this.getRandom().nextDouble()-0.5) * posRand,
                                delta.x + (this.getRandom().nextDouble()-0.5) * dirRand,
                                delta.y + (turbo ? 0.1 : 0) + (this.getRandom().nextDouble()-0.5) * dirRand,
                                delta.z + (this.getRandom().nextDouble()-0.5) * dirRand
                        );
                        if (!turbo) break;
                    }
                }

                if(this.tickCount%3==0){
                    for (int j = 0; j < 3; j++) {
                        level.addParticle(
                                ParticleTypes.SMOKE,
                                this.flamethrowerPos[i].x + (this.getRandom().nextDouble()-0.5) * posRand,
                                this.flamethrowerPos[i].y + (this.getRandom().nextDouble()-0.5) * posRand,
                                this.flamethrowerPos[i].z + (this.getRandom().nextDouble()-0.5) * posRand,
                                delta.x + (this.getRandom().nextDouble()-0.5) * dirRand,
                                delta.y + (turbo ? 0.1 : 0) + (this.getRandom().nextDouble()-0.5) * dirRand,
                                delta.z + (this.getRandom().nextDouble()-0.5) * dirRand
                        );
                        if (!turbo) break;
                    }
                }
            }

            if (this.hasRamYaw()) {
                MobUtil.turnEntityToYaw(this, this.getRamYaw(), 10F);
            }
        }else{
            AABB range = this.getBoundingBox();
            List<LivingEntity> lEn = level().getEntitiesOfClass(LivingEntity.class, range);
            for(LivingEntity en : lEn){
                if(en.is(this)){
                    continue;
                }
                BlockPos pos = en.getOnPos().above();
                if (range.contains(pos.getX(), pos.getY(), pos.getZ())) {
                    en.hurt(this.damageSources().mobAttack(this), 4);
                }
            }
        }

    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.tickCount%5==1) {
            this.burnNearby(3);
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(pSource.is(DamageTypes.IN_FIRE) || pSource.is(DamageTypes.ON_FIRE)){
            return false;
        }

        if(this.isStunned()){
            return super.hurt(pSource, pAmount*2);
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 20);
    }
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RRCSounds.RRC_FLAMING_HEAD_DEAD.get();
    }

    protected SoundEvent getAmbientSound() {
        return RRCSounds.RRC_FLAMING_HEAD_IDLE.get();
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    protected SoundEvent getDeathSound() {
        return RRCSounds.RRC_FLAMING_HEAD_DEAD.get();
    }

    protected float getSoundVolume() {
        return 2F;
    }

    protected void playStepSound(net.minecraft.core.BlockPos pPos, net.minecraft.world.level.block.state.BlockState pState) {
      this.playSound(this.getStepSound(), this.getSoundVolume() * 0.15F, 1F);
    }

    public void burnNearby(float radius) {
        AABB boundingBox = this.getBoundingBox().inflate(radius);

        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                boundingBox,
                entity -> entity instanceof LivingEntity && entity.distanceTo(this) <= radius
        );

        for (LivingEntity entity : nearbyEntities) {
            entity.setSecondsOnFire(5);
            entity.hurt(this.damageSources().onFire(), 2.0F);
        }
    }
    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }
}
