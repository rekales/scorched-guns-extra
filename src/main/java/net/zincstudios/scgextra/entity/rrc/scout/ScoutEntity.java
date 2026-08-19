package net.zincstudios.scgextra.entity.rrc.scout;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
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
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.sounds.RRCSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ScoutEntity extends GunnerEntity implements GeoEntity{
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation AIMING = RawAnimation.begin().thenPlayAndHold("idle_aim");
    public ScoutEntity(EntityType<? extends GunnerEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk/idle/aim", 2, state -> {
            if (state.getAnimatable().isAiming()) {
                return state.setAndContinue(AIMING);
            } else {
                RawAnimation anim = RawAnimation.begin();
                if (state.isCurrentAnimation(AIMING)) {
                    anim = anim.thenPlay("aim_idle");
                }
                if (state.isMoving()) {
                    return state.setAndContinue(anim.thenLoop("walk"));
                } else {
                    return state.setAndContinue(RawAnimation.begin()
                        .thenPlayXTimes("idle", state.getAnimatable().random.nextIntBetweenInclusive(2,4))
                        .thenLoop("idle_2"));
                }
            }
        }).setAnimationSpeed(1.3));
        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
            } else {
                return PlayState.STOP;
            }
        }));
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 20.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.23F)
        .add(Attributes.ATTACK_DAMAGE, 2.0D)
        .add(Attributes.ARMOR, 3.0D)
        .add(Attributes.MAX_HEALTH, 20.0D);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity) || entity.getMobType().equals(MobType.UNDEAD)));
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 20);
    }
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RRCSounds.RRC_SCOUT_HURT.get();
    };
    protected SoundEvent getAmbientSound() {
        return RRCSounds.RRC_SCOUT_IDLE.get();
    };
    protected SoundEvent getStepSound() {
        return RRCSounds.RRC_SCOUT_WALK.get();
    };
    protected SoundEvent getDeathSound() {
        return RRCSounds.RRC_SCOUT_DEATH.get();
    };
    protected float getSoundVolume() {
        return 2F;
    };
}