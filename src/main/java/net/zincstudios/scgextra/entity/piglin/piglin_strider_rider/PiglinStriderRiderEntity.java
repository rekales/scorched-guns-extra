package net.zincstudios.scgextra.entity.piglin.piglin_strider_rider;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.sounds.PiglinSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModEffects;

public class PiglinStriderRiderEntity extends EquippedEntity implements GeoEntity{

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run_no_jump");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> SHOULD_RUN = SynchedEntityData.defineId(PiglinStriderRiderEntity.class, EntityDataSerializers.BOOLEAN);

    static final int MELEE_DAMAGE_DELAY = 10;
    static final int MELEE_DURATION = 19;

    public PiglinStriderRiderEntity(EntityType<? extends Monster> entity, Level level) {
        super(entity, level);
        this.moveControl = new LavaWalkMoveControl(this, 2f, false);
        this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 128.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.32F)
        .add(Attributes.ATTACK_DAMAGE, 10.0D)
        .add(Attributes.ARMOR, 0.0D)
        .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 6.0D)
        .add(Attributes.MAX_HEALTH, 60.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 3, state -> {
            if (state.isMoving()) {
                state.setAndContinue(state.getAnimatable().isRunning() ? RUN : WALK);
            } else {
                state.setAndContinue(IDLE);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "attack", 0, state -> {
            return PlayState.CONTINUE;
        })
        .triggerableAnim("bite", ATTACK));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinStriderRiderAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PiglinStriderRiderEntity> getBrain() {
        return (Brain<PiglinStriderRiderEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PiglinStriderRiderEntity> brainProvider() {
        return PiglinStriderRiderAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinStriderRiderBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if(hurt && entity instanceof LivingEntity le && le.isAlive())le.addEffect(new MobEffectInstance(ModEffects.SULFUR_POISONING.get(), 200));
        return hurt;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide){
            if(this.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)){
                this.setRunning(true);
            }else{
                this.setRunning(false);
            }
            if (brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
                this.playSound(PiglinSounds.PIGLIN_STRIDER_RIDER_ATTACK.get());
                this.triggerAnim("attack", "bite");
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOULD_RUN, false);
    }

    public boolean isRunning(){
        return this.entityData.get(SHOULD_RUN);
    }
    public void setRunning(boolean bool){
        this.entityData.set(SHOULD_RUN, bool);
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return PiglinSounds.PIGLIN_STRIDER_RIDER_IDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PiglinSounds.PIGLIN_STRIDER_RIDER_IDLE.get();
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STRIDER_HURT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.STRIDER_STEP, this.getSoundVolume() * 0.15F, this.getVoicePitch());
    }
    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 9);
    }
    @Override
    public void die(DamageSource damageSource) {
        this.stopTriggeredAnimation("attack", null);
        super.die(damageSource);
    }
}