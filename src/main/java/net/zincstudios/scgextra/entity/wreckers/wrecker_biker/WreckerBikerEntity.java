package net.zincstudios.scgextra.entity.wreckers.wrecker_biker;

import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.sounds.WreckersSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings("deprecation")
public class WreckerBikerEntity extends Monster implements GeoEntity{

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation MOVE = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    public Vec3 moveTargetPoint;
    public BlockPos anchorPoint;
    public static final EntityDataAccessor<Boolean> IS_CIRCLING = SynchedEntityData.defineId(WreckerBikerEntity.class, EntityDataSerializers.BOOLEAN);
    public float clockwise = 1.0F;

    public WreckerBikerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveTargetPoint = Vec3.ZERO;
        this.anchorPoint = BlockPos.ZERO;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 35.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.45F)
            .add(Attributes.ATTACK_DAMAGE, 10.0D)
            .add(Attributes.ATTACK_KNOCKBACK, 2.0D)
            .add(Attributes.ARMOR, 6.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
            .add(Attributes.MAX_HEALTH, 60.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
            if (state.isMoving() || this.getNavigation().isInProgress()) {
                return state.setAndContinue(MOVE);
            }
            return state.setAndContinue(IDLE);
        }));

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
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false,
                        player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.goalSelector.addGoal(2, new ChargeTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity)));
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 1, 35){
            @Override
            public boolean canUse() {
                return super.canUse() && !WreckerBikerEntity.this.isCircling() && (WreckerBikerEntity.this.getTarget() != null ? WreckerBikerEntity.this.distanceToSqr(WreckerBikerEntity.this.getTarget()) >= 64 : false);
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !WreckerBikerEntity.this.isCircling() && (WreckerBikerEntity.this.getTarget() != null ? WreckerBikerEntity.this.distanceToSqr(WreckerBikerEntity.this.getTarget()) >= 64 : false);
            }
        });
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1){
            @Override
            public boolean canUse() {
                return super.canUse() && !WreckerBikerEntity.this.isCircling();
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !WreckerBikerEntity.this.isCircling();
            }
        });
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this){
            @Override
            public boolean canUse() {
                return super.canUse() && !WreckerBikerEntity.this.isCircling();
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !WreckerBikerEntity.this.isCircling();
            }
        });
    }

    @Override
    public void die(DamageSource damageSource) {
        if(!this.level().isClientSide()){
            this.level().explode(this, this.damageSources().explosion(this, this), null, new Vec3(this.getX(), this.getY(), this.getZ()), 3, false, ExplosionInteraction.NONE);
        }
        super.die(damageSource);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CIRCLING, false);
    }

    public boolean isCircling(){
        return this.entityData.get(IS_CIRCLING);
    }

    public void setCircling(boolean bool){
        this.entityData.set(IS_CIRCLING, bool);
    }

    @Override
    public void playAmbientSound() {
        SoundEvent soundevent = this.getAmbientSound();
        if (soundevent != null) {
            this.playSound(soundevent, this.isCircling() ? this.getSoundVolume() * 1.5f : this.getSoundVolume(), this.getVoicePitch());
        }
        this.playSound(WreckersSounds.WRECKER_BIKER_RIDER_IDLE.get(), this.isCircling() ? this.getSoundVolume() * 1.5f : this.getSoundVolume(), this.getVoicePitch());
    }
    @Override
    protected void playHurtSound(DamageSource source) {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.playSound(WreckersSounds.WRECKER_BIKER_RIDER_HURT.get(), this.getSoundVolume(), this.getVoicePitch());
        super.playHurtSound(source);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isCircling() ? WreckersSounds.WRECKER_BIKER_BIKE_IDLE_WHILE_CIRCLING.get() : WreckersSounds.WRECKER_BIKER_BIKE_IDLE.get();
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WreckersSounds.WRECKER_BIKER_BIKE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WreckersSounds.WRECKER_BIKER_BIKE_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return this.isCircling() ? 3 : 2;
    }

    @Override
    public void tick() {
        super.tick();
        if(this.level().isClientSide)return;
        if(this.getTarget()!=null){
            this.anchorPoint = this.getTarget().blockPosition();
        }else{
            this.anchorPoint = BlockPos.ZERO;
        }
    }
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData spawnData, CompoundTag dataTag) {
        this.clockwise = this.random.nextBoolean() ? 1.0F : -1.0F;
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 3:
                this.playSound(WreckersSounds.WRECKER_BIKER_RIDER_DEATH.get(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                break;
            default:
                break;
        }
        super.handleEntityEvent(id);
    }
}
