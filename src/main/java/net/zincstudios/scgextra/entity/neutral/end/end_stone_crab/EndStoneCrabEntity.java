package net.zincstudios.scgextra.entity.neutral.end.end_stone_crab;

import net.zincstudios.scgextra.entity.common.part.RotatedWeakPointPartEntity;
import net.zincstudios.scgextra.entity.common.goal.BreakBlocksGoal;
import net.zincstudios.scgextra.sounds.NeutralSounds;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.entity.common.MobUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EndStoneCrabEntity extends Monster implements GeoEntity{
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Boolean> EATING = SynchedEntityData.defineId(EndStoneCrabEntity.class, EntityDataSerializers.BOOLEAN);
    private final PartEntity<?>[] subEntities;
    private static final int MELEE_DAMAGE_DELAY = 7;
    private int hurtDelay = -1;

    public EndStoneCrabEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.subEntities = new PartEntity[] {
                new RotatedWeakPointPartEntity<>(this, new Vec3(1.2, 1.6, 1.2), 0.5f, 0.3f),
        };
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.goalSelector.addGoal(2, new EndStoneCrabMeleeAttackGoal(this, 0.5, true));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 0.5, 10));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(4, new BreakBlocksGoal(this, 60));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }
    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (state.isMoving()) {
                state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            } else {
                if(!state.getAnimatable().isEating()){
                    state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
                }else{
                    state.setAndContinue(RawAnimation.begin().thenLoop("idle_eating"));
                }
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
    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 20);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.MAX_HEALTH, 150.0)
        .add(Attributes.MOVEMENT_SPEED, 0.5)
        .add(Attributes.KNOCKBACK_RESISTANCE, 100)
        .add(Attributes.ATTACK_KNOCKBACK, 0.5)
        .add(Attributes.ATTACK_DAMAGE, 15)
        .add(Attributes.ARMOR, 16)
        .add(Attributes.FOLLOW_RANGE, 50);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EATING, false);
    }
    public Boolean isEating(){
        return this.entityData.get(EATING);
    }
    
    public void setEating(boolean bool){
        this.entityData.set(EATING, bool);
    }
    @Override
    public void tick() {
        super.tick();
        if(this.getDeltaMovement().length() == Vec3.ZERO.length() && this.getTarget()==null){
            if(this.tickCount%40==0){
                this.setEating(this.getRandom().nextBoolean());
            }
        }
        this.tickSubEntities();
        if (this.level().isClientSide) return;

        this.hurtDelay--;
        if (this.hurtDelay == 0) {
            LivingEntity target = this.getTarget();
            if (target != null) {
                double distToEnemySqr = this.getPerceivedTargetDistanceSquareForMeleeAttack(target);
                double reach = this.getAttackReachSqr(target) * 1.3;
                if (distToEnemySqr <= reach) {
                    super.doHurtTarget(target);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3));
                }
            }
        }
    }
    private double getAttackReachSqr(LivingEntity attackTarget) {
        return (((this.getBbWidth() * this.getBbWidth())*2) + attackTarget.getBbWidth());
    }
    @Override
    public boolean isPushable() {
        return false;
    }
    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }
    @Override
    public PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }
    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) // Fix MC-158205: See EnderDragon class
            this.subEntities[i].setId(id + i + 1);
    }
    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!this.level().isClientSide) {
            if (this.hurtDelay > 0) return false;

            this.triggerAnim("controller", "attack");
            this.hurtDelay = MELEE_DAMAGE_DELAY;
            this.playSound(NeutralSounds.END_STONE_CRAB_ATTACK.get());
        }
        return true;
    }
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return NeutralSounds.END_STONE_CRAB_HURT.get();
    };
    protected SoundEvent getAmbientSound() {
        return NeutralSounds.END_STONE_CRAB_IDLE.get();
    };
    protected SoundEvent getDeathSound() {
        return NeutralSounds.END_STONE_CRAB_DEATH.get();
    };
    protected float getSoundVolume() {
        return 0.8F;
    };
    protected void playStepSound(net.minecraft.core.BlockPos pPos, net.minecraft.world.level.block.state.BlockState pState) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, this.getSoundVolume(), 1);
    };
    @Override
    public double getPerceivedTargetDistanceSquareForMeleeAttack(LivingEntity entity) {
        return distanceToSqr(entity);
    }
    @Override
    public void setMaxUpStep(float maxUpStep) {
        super.setMaxUpStep(1F);
    }
    @Override
    public float getStepHeight() {
        return 1F;
    }
    public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        List<EndStoneCrabEntity> list = level.getEntitiesOfClass(EndStoneCrabEntity.class, type.getAABB(pos.getX(), pos.getY(), pos.getZ()).inflate(300));
        if(list.size()>2){
            return false;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(level, pos, random) && checkMobSpawnRules(type, level, spawnType, pos, random);
    }
}