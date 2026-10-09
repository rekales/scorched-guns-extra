package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.entity.common.part.RotatedWeakPointPartEntity;
import net.zincstudios.scgextra.sounds.WhalerSounds;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
@ParametersAreNonnullByDefault
public class ArmoredWhaleEntity extends Monster implements GeoEntity, CustomGunHolder, Gunner, BulletSpawnOffset {

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);

    static final int MELEE_DURATION = 24;
    static final int MELEE_DAMAGE_DELAY = 12;

    public boolean STARTED_SLAM = false;//server side boolean only used to stop other behaviors from hapenning

    private String lastHitByEntityUUID = "";

    private static final EntityDataAccessor<Boolean> IS_LEFT = SynchedEntityData.defineId(ArmoredWhaleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SLAMMING = SynchedEntityData.defineId(ArmoredWhaleEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("attack_melee");
    private static final RawAnimation GUN_FIRE_LEFT = RawAnimation.begin().thenPlay("attack_range_left");
    private static final RawAnimation GUN_FIRE_RIGHT = RawAnimation.begin().thenPlay("attack_range_right");
    private static final RawAnimation SLAM_UP = RawAnimation.begin().thenPlay("slam_up");
    private static final RawAnimation SLAM_AIR = RawAnimation.begin().thenLoop("slam_air");
    private static final RawAnimation SLAM_DOWN = RawAnimation.begin().thenPlay("slam_down");
    private static final RawAnimation SPLASH = RawAnimation.begin().thenPlay("splash");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final SimulatedGun customGun;
    private static final Vec3 LEFT_GUN_OFFSET = new Vec3(-31/16f, 43/16f, 70/16f);
    private static final Vec3 RIGHT_GUN_OFFSET = new Vec3(33/16f, 43/16f, 70/16f);

    public final RotatedSegmentPartEntity<ArmoredWhaleEntity> headMiddlePart;
    public final RotatedSegmentPartEntity<ArmoredWhaleEntity> headFrontPart;

    private final PartEntity<?>[] subEntities;

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);

    public ArmoredWhaleEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.MAK_MKII.get().getGun())
            .fireRate(80)
            .burstAmount(32)
            .burstInterval(2)
            .maxRange(16)
            .idealRange(10)
            .ammoCapacity(32)
            .reloadTime(100)
            .projectileFactory(MountedGunProjectileEntity::new)
            .build();
        this.headMiddlePart = new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 0/16f, -121/16f * -1f), 42/16f, 106/16f);
        this.headFrontPart = new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 0/16f, -160/16f * -1f), 42/16f, 106/16f);
        this.subEntities = new PartEntity[] {
            //head-neck
            new RotatedWeakPointPartEntity<>(this, new Vec3(0/16f, 59/16f, -190/16f * -1f), 16/16f, 19/16f),
            this.headFrontPart,
            this.headMiddlePart,
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 0/16f, -79/16f * -1f), 42/16f, 106/16f),
            //back
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 0/16f, 39/16f * -1f), 34/16f, 56/16f),
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 20/16f, 73/16f * -1f), 20/16f, 27/16f),
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 20/16f, 93/16f * -1f), 20/16f, 27/16f),
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 28/16f, 99/16f * -1f), 92/16f, 11/16f),
        };
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4,
            state -> {
                if(state.getAnimatable().isSlamming()){
                    state.setAnimation(SLAM_AIR);   
                }else if (state.isMoving() || state.isMoving()) {
                    state.setAnimation(WALK);
                } else {
                    state.setAnimation(IDLE);
                }
                return PlayState.CONTINUE;
            })
        );

        controllers.add(new AnimationController<>(this, "attack", 0,
            state -> PlayState.STOP)
                .triggerableAnim("bite", BITE)
                .triggerableAnim("fire", this.isLeft() ? GUN_FIRE_LEFT : GUN_FIRE_RIGHT)
                .triggerableAnim("slam_up", SLAM_UP)
                .triggerableAnim("slam_down", SLAM_DOWN)
                .triggerableAnim("splash", SPLASH)
        );

        controllers.add(new AnimationController<>(this, "death", 0, state -> {
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
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return this.isLeft() ? LEFT_GUN_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD) : RIGHT_GUN_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
    }

    @Override
    public SimulatedGun getCustomGun() {
        return this.customGun;
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 128.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.15F)
            .add(Attributes.ATTACK_DAMAGE, 20.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 100.0D)
            .add(Attributes.ARMOR, 8.0D)
            .add(Attributes.MAX_HEALTH, 800.0D);
    }
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return ArmoredWhaleAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<ArmoredWhaleEntity> getBrain() {
        return (Brain<ArmoredWhaleEntity>) super.getBrain();
    }

    protected Brain.Provider<ArmoredWhaleEntity> brainProvider() {
        return ArmoredWhaleAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("armoredWhaleBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        ArmoredWhaleAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
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
    public void tick() {
        super.tick();
        this.tickSubEntities();
        if(!this.level().isClientSide())this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.playSound(WhalerSounds.ARMORED_WHALE_BITE.get());
            this.triggerAnim("attack", "bite");
        }
        if(!this.level().isClientSide()){
            if(brain.getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()){
                Vec3 lookDir = this.getViewVector(1.0F).normalize();
                LivingEntity target = brain.getMemory(MemoryModuleType.ATTACK_TARGET).get();
                Vec3 toTarget = new Vec3(
                    target.getX() - this.getX(),
                    0.0D,
                    target.getZ() - this.getZ()
                ).normalize();
                double rightDot = lookDir.x * toTarget.z - lookDir.z * toTarget.x;
                this.setLeft(rightDot > 0);
            }
        }
    }
    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_LEFT, false);
        this.entityData.define(IS_SLAMMING, false);
    }
    public void setLeft(boolean bool){this.entityData.set(IS_LEFT, bool);}
    public boolean isLeft(){return this.entityData.get(IS_LEFT);}
    public void setSlamming(boolean i){this.entityData.set(IS_SLAMMING, i);}
    public boolean isSlamming(){return this.entityData.get(IS_SLAMMING);}
    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(5,0,5);
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(source.getDirectEntity()!=null && !(source.getDirectEntity() instanceof LivingEntity)){
            if(!this.level().isClientSide()){
                if(this.lastHitByEntityUUID == source.getDirectEntity().getStringUUID())return false;
                this.lastHitByEntityUUID = source.getDirectEntity().getStringUUID();
            }
        }
        if(source.getEntity()!=null && source.getEntity().is(this)){
            return false;
        }
        return super.hurt(source, amount);
    }
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }
    @Override
    protected SoundEvent getAmbientSound() {
        return WhalerSounds.ARMORED_WHALE_IDLE.get();
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WhalerSounds.ARMORED_WHALE_HURT.get();
    }
    @Override
    protected SoundEvent getDeathSound() {
        return WhalerSounds.ARMORED_WHALE_DEATH.get();
    }
    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }
    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}