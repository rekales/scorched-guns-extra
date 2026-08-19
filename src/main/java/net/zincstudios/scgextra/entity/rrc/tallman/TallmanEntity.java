package net.zincstudios.scgextra.entity.rrc.tallman;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TallmanEntity extends GunnerEntity implements GeoEntity {

    private static final RawAnimation AIMING = RawAnimation.begin().thenPlayAndHold("idle_aim");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public TallmanEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isLeftHanded() {
        return true;  // Flagging doesn't seem to work
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
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.ARMOR, 3D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk/idle/aim", 2,
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
                            return state.setAndContinue(anim.thenLoop("idle_2"));
                            // TODO: idle variation switching
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
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 32);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RRCSounds.RRC_TALLMAN_HURT.get();
    }

    protected SoundEvent getAmbientSound() {
        return RRCSounds.RRC_TALLMAN_IDLE.get();
    }

    protected SoundEvent getStepSound() {
        if(this.random.nextFloat() < 0.4F){
            return RRCSounds.RRC_TALLMAN_WALK.get();
        }else{
            return SoundEvents.IRON_GOLEM_STEP;
        }
    }

    protected SoundEvent getDeathSound() {
        return RRCSounds.RRC_TALLMAN_DEATH.get();
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        SoundEvent event = this.getStepSound();
        if(event.equals(SoundEvents.IRON_GOLEM_STEP)){
            this.playSound(event, this.getSoundVolume(), 3.0F);
        }else{
            this.playSound(event, this.getSoundVolume(), 1.0F);
        }
    }

    protected float getSoundVolume() {
        return 2F;
    }
}
