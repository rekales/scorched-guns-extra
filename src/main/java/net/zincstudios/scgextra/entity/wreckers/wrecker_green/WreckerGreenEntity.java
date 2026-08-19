package net.zincstudios.scgextra.entity.wreckers.wrecker_green;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.item.ItemStack;
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

import javax.annotation.ParametersAreNonnullByDefault;

public class WreckerGreenEntity extends GunnerEntity implements GeoEntity, InterruptibleVoice {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_AIM = RawAnimation.begin().thenLoop("idle_aim");
    private static final RawAnimation WALK_AIM = RawAnimation.begin().thenLoop("walk_aim");
    private static final RawAnimation IDLE_SHOOT = RawAnimation.begin().thenLoop("idle_shoot");
    private static final RawAnimation WALK_SHOOT = RawAnimation.begin().thenLoop("walk_shoot");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private static final EntityDataAccessor<Integer> SHOOT_TICKS =
            SynchedEntityData.defineId(WreckerGreenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ROCKET_HIDDEN_TICKS =
            SynchedEntityData.defineId(WreckerGreenEntity.class, EntityDataSerializers.INT);

    private static final int ROCKET_HIDDEN_DURATION = 70;
    private static final float EXPLOSION_RADIUS = 2.0F;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private boolean exploded = false;

    public WreckerGreenEntity(EntityType<? extends GunnerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22F)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.MAX_HEALTH, 20.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOOT_TICKS, 0);
        this.entityData.define(ROCKET_HIDDEN_TICKS, 0);
    }

    public boolean isRocketHidden() {
        return this.entityData.get(ROCKET_HIDDEN_TICKS) > 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 1, true, false,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 1, true, false,
                entity -> Faction.isEnemies(this, entity)));
    }

    @Override
    public void onGunAttack(LivingEntity target, ItemStack itemStack) {
        this.entityData.set(SHOOT_TICKS, 5);
        this.entityData.set(ROCKET_HIDDEN_TICKS, ROCKET_HIDDEN_DURATION);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            int shootTicks = this.entityData.get(SHOOT_TICKS);
            if (shootTicks > 0) {
                this.entityData.set(SHOOT_TICKS, shootTicks - 1);
            }
            int rocketHiddenTicks = this.entityData.get(ROCKET_HIDDEN_TICKS);
            if (rocketHiddenTicks > 0) {
                this.entityData.set(ROCKET_HIDDEN_TICKS, rocketHiddenTicks - 1);
            }
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide() && !this.exploded) {
            this.exploded = true;

            this.level().explode(this, this.getX(), this.getY() + 0.5D, this.getZ(),
                    EXPLOSION_RADIUS, Level.ExplosionInteraction.NONE);
        }
        super.die(cause);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
            boolean moving = state.isMoving() || this.getNavigation().isInProgress();
            if (this.entityData.get(SHOOT_TICKS) > 0) {
                return state.setAndContinue(moving ? WALK_SHOOT : IDLE_SHOOT);
            }
            if (this.isAiming()) {
                return state.setAndContinue(moving ? WALK_AIM : IDLE_AIM);
            }
            return state.setAndContinue(moving ? WALK : IDLE);
        }).setAnimationSpeed(1.0));

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
        return WreckersSounds.GANG_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WreckersSounds.GANG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WreckersSounds.GANG_DEATH.get();
    }

    @Override
    public List<SoundEvent> voiceLinesToSilenceOnDeath() {
        return WreckersSounds.gangVoiceLines();
    }
}
