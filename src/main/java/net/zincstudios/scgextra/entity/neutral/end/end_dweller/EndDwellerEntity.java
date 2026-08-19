package net.zincstudios.scgextra.entity.neutral.end.end_dweller;

import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.sounds.NeutralSounds;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings("deprecation")
public class EndDwellerEntity extends Monster implements GeoEntity, FlyingAnimal{
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<String> WALK_ANIM = SynchedEntityData.defineId(EndDwellerEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> IS_CHARGING = SynchedEntityData.defineId(EndDwellerEntity.class, EntityDataSerializers.BOOLEAN);

    public EndDwellerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData spawnData, CompoundTag dataTag) {
                this.setWalkAnim(this.getRandom().nextBoolean() ? "walk_insect" : "walk_2");
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
        .add(Attributes.MAX_HEALTH, 20.0)
        .add(Attributes.MOVEMENT_SPEED, 0.4)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
        .add(Attributes.ARMOR, 0)
        .add(Attributes.FOLLOW_RANGE, 20)
        .add(Attributes.FLYING_SPEED, 0.4)
        .add(Attributes.ATTACK_DAMAGE, 25)
        .add(Attributes.ATTACK_KNOCKBACK, 0.1)
        ;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(3, new EndDwellerWanderGoal());
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (state.isMoving()) {
                if(state.getAnimatable().onGround()){
                    state.setAndContinue(RawAnimation.begin().thenLoop(this.getWalkAnim()));
                }else{
                    state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
                }
            } else {
                state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return NeutralSounds.END_DWELLER_HURT.get();
    };
    protected SoundEvent getAmbientSound() {
        return NeutralSounds.END_DWELLER_IDLE.get();
    };
    protected float getSoundVolume() {
        return 0.8F;
    };
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(WALK_ANIM, "walk");
        this.entityData.define(IS_CHARGING, false);
    }
    public String getWalkAnim(){
        return this.entityData.get(WALK_ANIM);
    }
    
    public void setWalkAnim(String anim){
        this.entityData.set(WALK_ANIM, anim);
    }
    public boolean isCharging(){
        return this.entityData.get(IS_CHARGING);
    }
    
    public void setCharging(boolean bool){
        this.entityData.set(IS_CHARGING, bool);
    }
    protected PathNavigation createNavigation(Level p_level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, p_level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("WALK_ANIM", this.getWalkAnim());

    }
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("WALK_ANIM")) {
            this.setWalkAnim(compound.getString("WALK_ANIM"));
        }
    }
    class EndDwellerWanderGoal extends Goal {
        EndDwellerWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return EndDwellerEntity.this.navigation.isDone() && EndDwellerEntity.this.random.nextInt(10) == 0;
        }

        public boolean canContinueToUse() {
            return EndDwellerEntity.this.navigation.isInProgress();
        }

        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                EndDwellerEntity.this.navigation.moveTo(EndDwellerEntity.this.navigation.createPath(BlockPos.containing(vec3), 1), (double)1.0F);
            }

        }

        @Nullable
        private Vec3 findPos() {
            Vec3 vec3 = EndDwellerEntity.this.getViewVector(0.0F);
            Vec3 vec32 = HoverRandomPos.getPos(EndDwellerEntity.this, 8, 7, vec3.x, vec3.z, ((float)Math.PI / 2F), 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(EndDwellerEntity.this, 8, 4, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
        }
    }
    @Override
    public boolean isFlying() {
        return !this.onGround();
    }
    @Override
    protected void tickDeath() {
        this.getNavigation().stop();
        this.setDeltaMovement(0, -0.4, 0);
        if(this.deathTime==79){
            if(!this.level().isClientSide()){
                ServerLevel sl = (ServerLevel) this.level();
                sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 3, 0.1, 0.1, 0.1, 1);
                this.playSound(SoundEvents.GENERIC_EXPLODE, this.getSoundVolume(), this.getVoicePitch());
                AABB aabb = this.getBoundingBox().inflate(10);
                List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, aabb);
                for(LivingEntity entity : entities){
                    if(this.distanceToSqr(entity)<=64){
                        entity.knockback(3, this.getX()-entity.getX(), this.getZ()-entity.getZ());
                        entity.setSecondsOnFire(3);
                        this.doHurtTarget(entity);
                    }
                }
            }
        }
        MobUtil.tickDeath(this, 80);
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean bool = super.hurt(source, amount);
        if(this.getHealth()<=0){
            this.playSound(NeutralSounds.END_DWELLER_CHARGING.get());
            this.setCharging(true);
        }
        return bool;
    }
}
