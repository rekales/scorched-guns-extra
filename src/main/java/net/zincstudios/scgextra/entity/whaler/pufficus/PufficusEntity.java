package net.zincstudios.scgextra.entity.whaler.pufficus;

import net.zincstudios.scgextra.entity.common.EquippedEntity;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.AmphibiousMoveControl;
import net.zincstudios.scgextra.entity.common.client.ExpandedAnimationController;
import net.zincstudios.scgextra.sounds.WhalerSounds;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.fluids.FluidType;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PufficusEntity extends EquippedEntity implements GeoEntity {
    static final int MELEE_DAMAGE_DELAY = 16;
    static final int MELEE_DURATION = 33;
    static final int NET_DELAY = 14;
    static final int NET_DURATION = 25;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation ATTACK_WALKING = RawAnimation.begin().thenPlay("attack_walking");
    private static final RawAnimation NET_THROW = RawAnimation.begin().thenPlay("throw");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PufficusEntity(EntityType<? extends EquippedEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.moveControl = new AmphibiousMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.MAX_HEALTH, 240.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PufficusAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<PufficusEntity> getBrain() {
        return (Brain<PufficusEntity>) super.getBrain();
    }

    protected Brain.Provider<PufficusEntity> brainProvider() {
        return Brain.provider(PufficusAi.MEMORY_TYPES, PufficusAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("cogTrenchGoblinBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.triggerAnim("attack", "melee");
        }

        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.NET_TICKS.get()) == NET_DURATION) {
            this.triggerAnim("throw", "throw_net");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
                    if (state.isMoving()) {
                        state.setControllerSpeed(1.5f);
                        return state.setAndContinue(WALK);
                    } else {
                        state.setControllerSpeed(0.8f);
                        return state.setAndContinue(IDLE);
                    }
                }
        ));

        controllers.add(new ExpandedAnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                .triggerableAnim("melee", (ctr) -> {
                    PufficusEntity entity = (PufficusEntity) ctr.getAnimatable();
                    AnimationController<?> mainCtr = entity.getAnimatableInstanceCache().getManagerForId(entity.getId()).getAnimationControllers().get("main");
                    return mainCtr.getCurrentRawAnimation() == WALK ? ATTACK_WALKING : ATTACK;
                })
                .setAnimationSpeed(1.4f)
        );

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "throw", 0, state -> PlayState.STOP).triggerableAnim("throw_net", NET_THROW));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(source.getEntity() instanceof LivingEntity le){
            le.addEffect(new MobEffectInstance(MobEffects.POISON, 40));
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }
    public boolean isPushedByFluid() {
        return false;
    }

    public boolean canBreatheUnderwater() {
        return true;
    }
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return WhalerSounds.PUFFICUS_IDLE.get();
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WhalerSounds.PUFFICUS_HURT.get();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return WhalerSounds.PUFFICUS_DEATH.get();
    }
    @Override
    protected float getSoundVolume() {
        return super.getSoundVolume() * 2f;
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.DROWNED_STEP, this.getSoundVolume() * 0.15F, this.getVoicePitch());
    }
}
