package net.zincstudios.scgextra.entity.asgharian.flamer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.asgharian.GoalState;
import net.zincstudios.scgextra.entity.asgharian.GoalStateHandler;
import net.zincstudios.scgextra.entity.asgharian.SimpleBurstGunAttackGoal;
import net.zincstudios.scgextra.entity.common.GunnerEntity;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
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
public class AsgharFlamerEntity extends GunnerEntity implements GeoEntity, GoalStateHandler {

    private static final int ATTACK_SOUND_INTERVAL = 100;

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);

    // Serverside only
    private int lastAttackSound = 0;  // tickCount timestamp

    public AsgharFlamerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isLeftHanded() {
        return true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SimpleBurstGunAttackGoal<>(this)
                .burstAmount(3)
                .burstIntervalTicks(4)
                .runAndGun()
                .approachDist(4)
                .attackInterval(60)
        );
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.ARMOR, 7.0D)
                .add(Attributes.MAX_HEALTH, 70.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (state.getAnimatable().isAggressive()) {
                if (state.isMoving()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("aim_walk"));
                } else {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("aim"));
                }
            } else {
                if (state.isMoving()) {
                    state.setAnimation(RawAnimation.begin().thenLoop("walk"));
                } else {
                    state.setAnimation(RawAnimation.begin().thenLoop("idle"));
                }
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
            }
            return PlayState.STOP;
        }).setAnimationSpeed(1.1f));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 55);
    }

    @Override
    public void onGoalStateChanged(Goal goal, GoalState state) {
        if (state.equals(SimpleBurstGunAttackGoal.FIRING_STATE) && this.tickCount - this.lastAttackSound > ATTACK_SOUND_INTERVAL) {
            this.playSound(this.getAttackSound());
            this.lastAttackSound = this.tickCount;
        }
    }

    protected SoundEvent getAmbientSound() {
        return AsgharianSounds.ASGHAR_FLAMER_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AsgharianSounds.ASGHAR_FLAMER_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return AsgharianSounds.ASGHAR_FLAMER_DEATH.get();
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    protected SoundEvent getAttackSound() {
        return AsgharianSounds.ASGHAR_FLAMER_ATTACK.get();
    }

    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }
    @Override
    public boolean fireImmune() {
        return true;
    }
}
