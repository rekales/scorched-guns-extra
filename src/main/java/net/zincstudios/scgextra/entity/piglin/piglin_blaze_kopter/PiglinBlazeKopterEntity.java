package net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter;

import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.network.GunFlashMessage;
import net.zincstudios.scgextra.network.SCGEPacketHandler;
import net.zincstudios.scgextra.sounds.PiglinSounds;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;

public class PiglinBlazeKopterEntity extends Monster implements GeoEntity, Gunner, CustomGunHolder, BulletSpawnOffset{

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenLoop("shoot");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private static final EntityDataAccessor<Boolean> IS_SHOOTING = SynchedEntityData.defineId(PiglinBlazeKopterEntity.class, EntityDataSerializers.BOOLEAN);

    private final SimulatedGun customGun;

    public Vec3 bulletOffset = new Vec3(0, -7.2174/16f, -29.3119/16f * -1);

    private boolean bulletSpawnLeft = false;

    public PiglinBlazeKopterEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.BLASPHEMY.get().getGun())
            .projectileDamage(3f)
            .fireRate(2)
            .maxRange(20)
            .idealRange(16)
            .ammoCapacity(6)
            .reloadTime(120)
            .noGunFlash()
            .build();
    }

    protected PathNavigation createNavigation(Level p_level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, p_level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 128.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.23F)
        .add(Attributes.FLYING_SPEED, 0.23F)
        .add(Attributes.ATTACK_DAMAGE, 30.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
        .add(Attributes.ARMOR, 0.0D)
        .add(Attributes.MAX_HEALTH, 60.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (state.getAnimatable().isShooting()) {
                state.setAnimation(SHOOT);
            }else if (state.isMoving()) {
                state.setAnimation(WALK);
            } else {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
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
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return this.bulletOffset.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
    }

    @Override
    public SimulatedGun getCustomGun() {
        return this.customGun;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_SHOOTING, false);
    }

    public boolean isShooting(){
        return this.entityData.get(IS_SHOOTING);
    }

    public void setShooting(boolean bool){
        this.entityData.set(IS_SHOOTING, bool);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinBlazeKopterAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PiglinBlazeKopterEntity> getBrain() {
        return (Brain<PiglinBlazeKopterEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PiglinBlazeKopterEntity> brainProvider() {
        return PiglinBlazeKopterAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinBlazeKopterBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        BrainCommons.updateMaxRangeAggressive(this);
        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
            this.setYBodyRot(this.getYHeadRot());
        }
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide()){
            if(!this.level().getBlockState(this.blockPosition().below(5)).isAir()){
                this.addDeltaMovement(new Vec3(0,0.1,0));
            }
        }
    }

    @Override
    public void onGunFire(SimulatedGun gun, Vec3 targetPos) {
        this.bulletSpawnLeft = !this.bulletSpawnLeft;

        Gun.Display.Flash flash = ModItems.BIRDFEEDER.get().getGun().getDisplay().getFlash();
        if (flash == null) return;
        ResourceLocation flashTexture = ResourceLocation.fromNamespaceAndPath(ScorchedGuns.MODID,
                "textures/effect/" + flash.getTextureLocation() + ".png");
        SCGEPacketHandler.sendToNearbyPlayers(() -> MobUtil.levelLocationFromEntity(this),
                new GunFlashMessage(this.getId(), this.bulletSpawnLeft ? 0 : 1, flashTexture, false, 1.2F));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PiglinSounds.PIGLIN_BLAZE_KOPTER_IDLE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 280;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PiglinSounds.PIGLIN_BLAZE_KOPTER_DEATH.get();
    }
}
