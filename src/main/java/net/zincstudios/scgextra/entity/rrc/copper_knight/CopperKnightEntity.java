package net.zincstudios.scgextra.entity.rrc.copper_knight;

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
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.common.GunnerEntity;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.goal.AlertFactionGoal;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.sounds.RRCSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CopperKnightEntity extends GunnerEntity implements GeoEntity{
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation AIMING = RawAnimation.begin().thenPlayAndHold("idle_aim");
    public CopperKnightEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk/idle/aim", 2,
                state -> {
                    if (state.getAnimatable().isAiming()) {
                        if(((this.getX() - this.xo)*(this.getX() - this.xo))+((this.getZ() - this.zo)*(this.getZ() - this.zo))>0.0002){//extremely precise movement checks cause state.isMoving sometimes returns false while moving which causes sliding
                            return state.setAndContinue(RawAnimation.begin().thenLoop("walk_holding_aim"));
                        }else{
                            return state.setAndContinue(AIMING);
                        }
                    } else {
                        RawAnimation anim = RawAnimation.begin();
                        if (state.isCurrentAnimation(AIMING)) {
                            anim = anim.thenPlay("aim_idle");
                        }
                        if (((this.getX() - this.xo)*(this.getX() - this.xo))+((this.getZ() - this.zo)*(this.getZ() - this.zo))>0.0002){ //extremely precise movement checks cause state.isMoving sometimes returns false while moving which causes sliding
                            return state.setAndContinue(anim.thenLoop("walk"));
                        } else {
                            return state.setAndContinue(RawAnimation.begin()
                                .thenPlayXTimes("idle", state.getAnimatable().random.nextIntBetweenInclusive(2,4))
                                .thenLoop("idle_2"));
                        }
                    }
                }
        ).setAnimationSpeed(1.3));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
            } else {
                return PlayState.STOP;
            }
        }));
        controllers.add(new AnimationController<>(this, "kick", 0, state -> PlayState.CONTINUE)
        .triggerableAnim("kick", RawAnimation.begin().thenPlay("kick")));
    }

    @Override
    protected void registerGoals() {
        // gun attack goal to be automatically added on finalizeSpawn
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(3, new AlertFactionGoal(this, 200, true));
        this.targetSelector.addGoal(0, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity) || entity.getMobType().equals(MobType.UNDEAD)));
        this.goalSelector.addGoal(3, new CopperKnightKickGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.ARMOR, 6D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15F);
    }

    public boolean isLeftHanded() {
        return true;  // Flagging doesn't seem to work
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 32);
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RRCSounds.RRC_COPPER_KNIGHT_HURT.get();
    };
    protected SoundEvent getAmbientSound() {
        return RRCSounds.RRC_COPPER_KNIGHT_IDLE.get();
    };
    protected SoundEvent getStepSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    };
    protected SoundEvent getDeathSound() {
        return RRCSounds.RRC_COPPER_KNIGHT_DEAD.get();
    };
    protected float getSoundVolume() {
        return 2F;
    };
    protected void playStepSound(net.minecraft.core.BlockPos pPos, net.minecraft.world.level.block.state.BlockState pState) {
      this.playSound(this.getStepSound(), this.getSoundVolume() * 0.15F, 1F);
    };
}