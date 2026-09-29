package net.zincstudios.scgextra.entity.neutral.overworld.anomaly;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AnomalyEntity extends Monster implements GeoEntity, FlyingAnimal{
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(AnomalyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_BEAM_TIME = SynchedEntityData.defineId(AnomalyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> TARGET_POS = SynchedEntityData.defineId(AnomalyEntity.class, EntityDataSerializers.VECTOR3);

    private static final int MELEE_DAMAGE_DELAY = 5;
    private int hurtDelay = -1;

    public AnomalyEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    protected PathNavigation createNavigation(Level p_level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, p_level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
   }

    @Override
    protected float getSoundVolume() {
        return 2;
    }
    
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public double getPerceivedTargetDistanceSquareForMeleeAttack(LivingEntity entity) {
        return distanceToSqr(entity);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(Attributes.FLYING_SPEED, 0.2)
                .add(Attributes.MAX_HEALTH, 10.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(WALK);
            } else {
                return state.setAndContinue(IDLE);
            }
        }).triggerableAnim("attack", ATTACK));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.goalSelector.addGoal(1, new AnomalyAttackGoal(this, 1, true, 40));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1, 35));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new AnomalyEntityWanderGoal());
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        super.tick();
        if (this.level().isClientSide) return;

        this.hurtDelay--;
        if (this.hurtDelay == 0) {
            LivingEntity target = this.getTarget();
            if (target != null) {
                BlockPos start = this.blockPosition();
         BlockPos end = this.getTarget().blockPosition();
         double startX = start.getX() + 0.5;
         double startY = start.getY() + 2.5;
         double startZ = start.getZ() + 0.5;
      
         double endX = end.getX() + 0.5;
         double endY = end.getY() + 0.5;
         double endZ = end.getZ() + 0.5;
      
         double dx = endX - startX;
         double dy = endY - startY;
         double dz = endZ - startZ;
      
         double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
      
         double stepX = dx / distance;
         double stepY = dy / distance;
         double stepZ = dz / distance;
         for (double i = 0; i < distance; i += 0.1) {
             double px = startX + stepX * i;
             double py = startY + stepY * i;
             double pz = startZ + stepZ * i;
             if (this.level() instanceof ServerLevel serverLevel)serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz,3,0.3,0.3,0.3,0.1);
         }
                double distToEnemySqr = this.getPerceivedTargetDistanceSquareForMeleeAttack(target);
                double reach = 64;
                if (distToEnemySqr <= reach) {
                    this.setTargetPos(target.position());
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level());
                    lightning.moveTo(target.blockPosition(), 0, 0);
                    this.level().addFreshEntity(lightning);
                    this.setBeamTime(false);
                }else{
                    this.getNavigation().moveTo(target.getX(), target.getY()+2, target.getZ(), 0.6);
                    this.getLookControl().setLookAt(target);
                    this.setTargetPos(Vec3.ZERO);
                }
            }else{
                this.setBeamTime(false);
                this.setTargetPos(Vec3.ZERO);
            }
        }
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(7), player -> !((Player) player).isCreative() && !player.isSpectator());
        for(Player player : players){
            if(this.distanceToSqr(player)<=36){
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 3));
            }
        }
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(source.is(DamageTypes.FALL))return false;
        if(source.is(DamageTypes.LIGHTNING_BOLT))return false;
        if(source.is(DamageTypes.ON_FIRE))return false;
        if(source.is(DamageTypes.IN_FIRE))return false;
        return super.hurt(source, amount);
    }
    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!this.level().isClientSide) {
            if (this.hurtDelay > 0) return false;
            this.triggerAnim("main", "attack");
            this.hurtDelay = MELEE_DAMAGE_DELAY;
            this.setBeamTime(true);
            this.setTargetPos(entity.position());
        }
        return true;
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ATTACKING, false);
        this.entityData.define(IS_BEAM_TIME, false);
        this.entityData.define(TARGET_POS, new Vector3f());
    }
    public void setAttacking(boolean bool){
        this.entityData.set(IS_ATTACKING, bool);
    }
    public boolean isAttacking(){
        return this.entityData.get(IS_ATTACKING);
    }
    public void setBeamTime(boolean bool){
        this.entityData.set(IS_BEAM_TIME, bool);
    }
    public boolean isBeamTime(){
        return this.entityData.get(IS_BEAM_TIME);
    }
    public void setTargetPos(Vec3 pos){
        this.entityData.set(TARGET_POS, new Vector3f((float)pos.x, (float)pos.y, (float)pos.z));
    }
    public Vector3f getTargetPos(){
        return this.entityData.get(TARGET_POS);
    }
    class AnomalyEntityWanderGoal extends Goal {
        AnomalyEntityWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return AnomalyEntity.this.navigation.isDone() && AnomalyEntity.this.random.nextInt(10) == 0 && !AnomalyEntity.this.isAttacking();
        }

        public boolean canContinueToUse() {
            return AnomalyEntity.this.navigation.isInProgress() && !AnomalyEntity.this.isAttacking();
        }

        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                AnomalyEntity.this.navigation.moveTo(AnomalyEntity.this.navigation.createPath(BlockPos.containing(vec3), 1), 1);
            }

        }

        @Nullable
        private Vec3 findPos() {
            Vec3 vec3 = AnomalyEntity.this.getViewVector(0.0F);
            Vec3 vec32 = HoverRandomPos.getPos(AnomalyEntity.this, 8, 7, vec3.x, vec3.z, ((float)Math.PI / 2F), 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(AnomalyEntity.this, 8, 4, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
        }
    }
    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ALLAY_HURT;
    }
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }
    public Vec3 getBeamLoc(){
        return this.isBeamTime() ? new Vec3(this.getTargetPos()) : Vec3.ZERO;
    }
}
