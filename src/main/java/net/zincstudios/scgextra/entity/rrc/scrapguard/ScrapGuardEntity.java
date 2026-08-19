package net.zincstudios.scgextra.entity.rrc.scrapguard;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.block.state.BlockState;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.common.GunnerEntity;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.sounds.RRCSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.entity.ai.AIType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ScrapGuardEntity extends GunnerEntity implements GeoEntity {

    private static final RawAnimation AIMING = RawAnimation.begin().thenPlayAndHold("idle_aim");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public ScrapGuardEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        // TODO: GunAttackMeleeCombined goal
        this.goalSelector.addGoal(2, new ScrapGuardAttackGoal<>(this, this.getMainHandItem(), 1.0F, AIType.RECKLESS, 3, 10));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity) || entity.getMobType().equals(MobType.UNDEAD)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60D)
                .add(Attributes.ARMOR, 12)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.23F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk/idle/aim", 4,
                state -> {
                    if (state.getAnimatable().isAiming()) {
                        if(state.isMoving()){
                            return state.setAndContinue(RawAnimation.begin().thenLoop("walk_holding_aim"));
                        }else{
                            return state.setAndContinue(AIMING);
                        }
                    } else {
                        RawAnimation anim = RawAnimation.begin();
                        if (state.isCurrentAnimation(AIMING)) {
                            anim = anim.thenPlay("aim_idle");
                        }
                        if (state.isMoving()) {
                            return state.setAndContinue(anim.thenLoop("walk"));
                        } else {
                            return state.setAndContinue(anim.thenLoop("idle"));
                            // TODO: idle variation switching
                        }
                    }
                }
        ).setAnimationSpeed(1.3));

        controllers.add(new AnimationController<>(this, "behaviour", 2, state -> PlayState.STOP)
                .triggerableAnim("melee", RawAnimation.begin().thenPlay("melee_attack"))
        );

        controllers.add(new AnimationController<>(this, "death", 4, state -> {
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
        return RRCSounds.RRC_OPPRESSOR_HURT.get();
    };
    protected SoundEvent getAmbientSound() {
        return RRCSounds.RRC_OPPRESSOR_IDLE.get();
    };
    protected SoundEvent getStepSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    };
    protected SoundEvent getDeathSound() {
        return RRCSounds.RRC_OPPRESSOR_DEATH.get();
    };
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(this.getStepSound(), this.getSoundVolume(), 1.0F);
    }
    protected float getSoundVolume() {
        return 2F;
    };
}
