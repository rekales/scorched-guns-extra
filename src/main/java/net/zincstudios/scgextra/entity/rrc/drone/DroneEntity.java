package net.zincstudios.scgextra.entity.rrc.drone;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.zincstudios.scgextra.CommonConfig;
import net.zincstudios.scgextra.entity.common.HeadShotHandler;
import org.jetbrains.annotations.Nullable;

import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.entity.common.part.RotatedWeakPointPartEntity;
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
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.Stunnable;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.entity.common.goal.StunnedWithVisualGoal;
import net.zincstudios.scgextra.sounds.RRCSounds;
import net.minecraftforge.entity.PartEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.zincstudios.scgextra.entity.Faction;

import javax.annotation.ParametersAreNonnullByDefault;

//The health checks in the play sound is just to have it not play any extra sounds while it's about to die
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DroneEntity extends Monster implements GeoEntity, Stunnable, HeadShotHandler {

    private static final EntityDataAccessor<Float> INACCURACY = SynchedEntityData.defineId(DroneEntity.class, EntityDataSerializers.FLOAT);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final PartEntity<?>[] subEntities;

    // Server-side only for stunnable handling
    private int headshotCounter = 0;
    private boolean stunned = false;
    private boolean stunCooldown = false;

    public DroneEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.subEntities = new PartEntity[] {
                //back
                //TODO: HOW DO I MAKE A THIN ONE?  DON'T WANNA MAKE A SEPARATE PART ENTITY CLASS FOR THIS, NOT SURE HOW TO DO IT IN THE RotatedSegmentPartEntity
                //body
                new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 30/16f, 0/16f), 32/16f, 32/16f),
                //arm1-joint-1
                new RotatedSegmentPartEntity<>(this, new Vec3(24/16f, 29/16f, 4/16f), 8/16f, 26/16f),
                //arm1-joint-2
                new RotatedSegmentPartEntity<>(this, new Vec3(25/16f, 17.5/16f, 24.5/16f), 14/16f, 16/16f),
                //arm2
                new RotatedSegmentPartEntity<>(this, new Vec3(-10.5/16f, 20.75/16f, 6/16f), 18/16f, 27/16f),
                //leg1
                new RotatedSegmentPartEntity<>(this, new Vec3(-5/16f, 0/16f, 4/16f), 9/16f, 23/16f),
                //leg2
                new RotatedSegmentPartEntity<>(this, new Vec3(14/16f, 0/16f, 4/16f), 9/16f, 23/16f),
                //pipe
                new RotatedWeakPointPartEntity<>(this, new Vec3(-15/16f, 47/16f, -6/16f), 5/16f, 24/16f)
        };
    }

    @Override
    public int shouldStun() {
        if (!CommonConfig.enableAbilityWeakness) return 0;

        if (this.headshotCounter >= CommonConfig.abilityWeaknessHeadshots) {
//            return CommonConfig.abilityWeaknessDuration;
            return 100;
        }

        return 0;
    }

    @Override
    public void setStunned(boolean stunned) {
        this.stunned = stunned;
        if (stunned) {
            this.triggerAnim("behaviour", "stun");
        } else {
            this.headshotCounter = 0;
        }
    }

    @Override
    public boolean isStunned() {
        return this.stunned;
    }

    @Override
    public boolean headshot(DamageSource source, float amount) {
        if (this.headshotCounter < CommonConfig.abilityWeaknessHeadshots-1 || !this.stunCooldown) {
            this.headshotCounter++;
        }

        return false;
    }

    @Override
    public void setStunCooldown(boolean cooldown) {
        this.stunCooldown = cooldown;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    public PartEntity<?>[] getSubEntities() {
        return this.subEntities;
    }

    @Override
    public @Nullable PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StunnedWithVisualGoal<>(this));
        this.targetSelector.addGoal(2, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.goalSelector.addGoal(2, new ClawAttackGoal(this, 1, true));
        this.goalSelector.addGoal(3, new MountedGunAttackGoal(this, 20));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 20));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, entity -> Faction.isEnemies(this, entity) || entity.getMobType().equals(MobType.UNDEAD)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (((this.getX() - this.xo)*(this.getX() - this.xo))+((this.getZ() - this.zo)*(this.getZ() - this.zo))>0.0002) {
                state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            } else {
                state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<>(this, "mAttack", 0, state -> PlayState.CONTINUE)
        .triggerableAnim("melee_attack", RawAnimation.begin().thenPlay("melee_attack")));
        controllers.add(new AnimationController<>(this, "attack", 0, state -> PlayState.CONTINUE)
        .triggerableAnim("gun_firing", RawAnimation.begin().thenPlay("gun_firing")));
        controllers.add(new AnimationController<>(this, "behaviour", 0, state -> PlayState.CONTINUE)
        .triggerableAnim("stun", RawAnimation.begin().thenPlay("stun")));
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
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2F)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.MAX_HEALTH, 120.0D);
    }
    @Override
    public void tick() {
        super.tick();
        tickSubEntities();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(this.isStunned()){
            return super.hurt(pSource, pAmount*2);
        }
        return super.hurt(pSource, pAmount);
    }

    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(INACCURACY, 8F);
    }
    public void lowerInaccuracy(){
        float inaccuracy = getInaccuracy();
        if(inaccuracy>0){
            this.entityData.set(INACCURACY, getInaccuracy()-0.2F);
        }
    }
    public float getInaccuracy(){
        return this.entityData.get(INACCURACY);
    }

    protected SoundEvent getDeathSound() {
        return RRCSounds.RRC_DRONE_DEATH.get();
    }

    protected SoundEvent getAmbientSound() {
        return RRCSounds.RRC_DRONE_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RRCSounds.RRC_DRONE_HURT.get();
    }

    protected SoundEvent getStepSound() {
        if(this.random.nextFloat() < 0.4F){
            return RRCSounds.RRC_DRONE_WALK.get();
        }else{
            return SoundEvents.IRON_GOLEM_STEP;
        }
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(this.getStepSound(), this.getSoundVolume(), 1.0F);
    }

    protected float getSoundVolume() {
        return 2F;
    }

    protected void tickDeath() {
        MobUtil.tickDeath(this, 18);
    }
}