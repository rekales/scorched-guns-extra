package net.zincstudios.scgextra.entity.neutral.overworld.inflicted_boar;

import net.zincstudios.scgextra.entity.common.MobUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class InflictedBoarEntity extends Monster implements GeoEntity{
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public InflictedBoarEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH, 40.0)
        .add(Attributes.MOVEMENT_SPEED, 0.5)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
        .add(Attributes.ATTACK_KNOCKBACK, 2)
        .add(Attributes.ATTACK_DAMAGE, 6)
        .add(Attributes.ARMOR, 2);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.goalSelector.addGoal(2, new InflictedBoarAttackGoal(this, 0.6, true));
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (state.isMoving()) {
                state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            } else {
                state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }).triggerableAnim("attack", RawAnimation.begin().thenPlay("attack")));
        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
            } else {
                return PlayState.STOP;
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.HOGLIN_HURT;
    };
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HOGLIN_AMBIENT;
    };
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOGLIN_DEATH;
    };
    protected float getSoundVolume() {
        return 0.8F;
    };
    protected void playStepSound(net.minecraft.core.BlockPos pPos, net.minecraft.world.level.block.state.BlockState pState) {
        this.playSound(SoundEvents.HOGLIN_STEP, this.getSoundVolume(), this.getVoicePitch());
    };
    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 30);
    }
    @Override
    public double getPerceivedTargetDistanceSquareForMeleeAttack(LivingEntity entity) {
        return distanceToSqr(entity);
    }
    public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(level, pos, random) && checkMobSpawnRules(type, level, spawnType, pos, random);
    }
}