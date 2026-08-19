package net.zincstudios.scgextra.entity.rrc.spring_junkie;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
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
import net.minecraft.world.level.block.state.BlockState;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.sounds.RRCSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SpringJunkieEntity extends Monster implements GeoEntity{
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(SpringJunkieEntity.class, EntityDataSerializers.BOOLEAN);

    public SpringJunkieEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (state.isMoving()) {
                if(getAttacking()){
                    state.setAndContinue(RawAnimation.begin().thenLoop("run"));
                }else{
                    state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
                }
            } else {
                state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<>(this, "behaviour", 0, state -> PlayState.CONTINUE)
        .triggerableAnim("death", RawAnimation.begin().thenPlay("death")));
        controllers.add(new AnimationController<>(this, "anim", 0, state -> PlayState.CONTINUE)
        .triggerableAnim("aggroed", RawAnimation.begin().thenPlay("aggroed")));
        controllers.add(new AnimationController<>(this, "anim1", 0, state -> PlayState.CONTINUE)
        .triggerableAnim("aggroed_long", RawAnimation.begin().thenPlay("aggroed_long")));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 60.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.3F)
        .add(Attributes.ARMOR, 6.0D)
        .add(Attributes.MAX_HEALTH, 30.0D);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }
    public void setAttacking(boolean b){
        this.entityData.set(ATTACKING, b);
    }
    public boolean getAttacking(){
        return this.entityData.get(ATTACKING);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(2, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.goalSelector.addGoal(3, new AttackAndExplodeGoal(this, 1.66F, 3));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 20));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, entity -> Faction.isEnemies(this, entity) || entity.getMobType().equals(MobType.UNDEAD)));
    }
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RRCSounds.RRC_SPRING_JUNKIE_DEATH.get();
    };
    protected SoundEvent getStepSound() {
        if(this.getAttacking()){
            if(this.random.nextFloat() < 0.4F){
                return RRCSounds.RRC_SPRING_JUNKIE_RUN.get();
            }
        }
        if(this.random.nextFloat() < 0.4F){
            return RRCSounds.RRC_SPRING_JUNKIE_WALK.get();
        }else{
            return SoundEvents.IRON_GOLEM_STEP;
        }
    }
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(this.getStepSound(), this.getSoundVolume(), 1.0F);
    }
    protected float getSoundVolume() {
        return 2F;
    };
}