package net.zincstudios.scgextra.entity.asgharian.surgeon;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.asgharian.*;
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
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AsgharSurgeonEntity extends GunnerEntity implements GeoEntity, GoalStateHandler, BulletSpawnOffset {

    // TODO: maybe make custom EntityDataSerializer
    private static final EntityDataAccessor<String> GUN_ATTACK_GOAL_STATE =
            SynchedEntityData.defineId(AsgharSurgeonEntity.class, EntityDataSerializers.STRING);

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);

    public AsgharSurgeonEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new AsgharSurgeonAttackGoal<>(this));
        this.goalSelector.addGoal(3, new ConstantSummonGoal(this, 140, AsgharianEntities.FAILED_ONE.get()));
        this.goalSelector.addGoal(4, new HealNearbyGoal<>(this, 8, LivingEntity.class,
                entity -> Faction.isFriendlies(entity, this)));
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
                .add(Attributes.MOVEMENT_SPEED, 0.2F)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 7.0D)
                .add(Attributes.MAX_HEALTH, 200.0D);
    }

    // TODO: gun attack animation
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
            if (state.isMoving()) {
                state.setAnimation(RawAnimation.begin().thenLoop("walk"));
            } else {
                state.setAnimation(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "behaviour", 2, state -> PlayState.STOP)
                .triggerableAnim("melee", RawAnimation.begin().thenPlay("saw"))
                .setAnimationSpeed(1.2f)
        );

        controllers.add(new AnimationController<>(this, "gun", 2, state -> {
            if (this.getGunAttackGoalState().equals(SimpleGunAttackGoal.FIRING_STATE)) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("gun"));
            } else {
                return PlayState.STOP;
            }
        }));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
            }
            return PlayState.STOP;
        }).setAnimationSpeed(1.3f));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 30);
    }

    @Override
    public void onGoalStateChanged(Goal goal, GoalState state) {
        if (state.equals(AsgharSurgeonAttackGoal.MELEE_STATE) && !Objects.equals(this.getGunAttackGoalState(), state)) {
            this.triggerAnim("behaviour", "melee");
            this.playSound(this.getAttackSound());
        } else if (state.equals(AsgharSurgeonAttackGoal.FIRING_STATE) && !Objects.equals(this.getGunAttackGoalState(), state)) {
            if (this.random.nextBoolean()) {
                this.playSound(this.getAttackSound());
            }
        }
        SCGExtra.LOGGER.debug(state.id());
        this.setGunAttackGoalState(state);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(GUN_ATTACK_GOAL_STATE, SimpleGunAttackGoal.IDLE_STATE.id());
    }

    public void setGunAttackGoalState(GoalState state) {
        if (!this.entityData.get(GUN_ATTACK_GOAL_STATE).equals(state.id())) {
            this.entityData.set(GUN_ATTACK_GOAL_STATE, state.id());
        }
    }

    public GoalState getGunAttackGoalState() {
        GoalState state = GoalState.get(this.entityData.get(GUN_ATTACK_GOAL_STATE));
        if (state == null) {
            if (this.level().isClientSide) {
                // in case there's no goal state because checks on render side are too early
                return SimpleGunAttackGoal.IDLE_STATE;
            }
            throw new IllegalStateException("no goal state found");
        }
        return state;
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return new Vec3(1.25,2,0.75).yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
    }

    protected SoundEvent getAmbientSound() {
        return AsgharianSounds.ASGHAR_SURGEON_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AsgharianSounds.ASGHAR_SURGEON_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return AsgharianSounds.ASGHAR_SURGEON_DEATH.get();
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    protected SoundEvent getAttackSound() {
        return AsgharianSounds.ASGHAR_SURGEON_ATTACK.get();
    }

    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }
}
