package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
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
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.Dynamic;

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

    private static final EntityDataAccessor<Boolean> IS_LEFT = SynchedEntityData.defineId(ArmoredWhaleEntity.class, EntityDataSerializers.BOOLEAN);

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

    // private static final int STUN_RECOVERY_TICKS = 10;

    private final SimulatedGun customGun;//temporary, make 2 guns
    private static final Vec3 LEFT_GUN_OFFSET = new Vec3(-31/16f, 43/16f, 6/16f);
    private static final Vec3 RIGHT_GUN_OFFSET = new Vec3(33/16f, 43/16f, 6/16f);

    public ArmoredWhaleEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.BIRDFEEDER.get().getGun())
            .fireRate(80)
            .burstAmount(50)
            .burstInterval(4)
            .maxRange(20)
            .idealRange(15)
            .ammoCapacity(50)
            .reloadTime(20)
            .build();
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4,
                state -> {
                    if (state.isMoving() || state.isMoving()) {
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
                .triggerableAnim("slam_air", SLAM_AIR)
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
        return this.isLeft() ? LEFT_GUN_OFFSET : RIGHT_GUN_OFFSET;
    }

    @Override
    public SimulatedGun getCustomGun() {
        return this.customGun;
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 64.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35F)
            .add(Attributes.ATTACK_DAMAGE, 20.0D)
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
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
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
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_LEFT, false);
    }
    public void setLeft(boolean bool){this.entityData.set(IS_LEFT, bool);}
    public boolean isLeft(){return this.entityData.get(IS_LEFT);}
}