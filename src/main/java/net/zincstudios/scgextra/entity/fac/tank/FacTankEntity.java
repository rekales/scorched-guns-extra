package net.zincstudios.scgextra.entity.fac.tank;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import net.zincstudios.scgextra.network.GunFlashMessage;
import net.zincstudios.scgextra.network.SCGEPacketHandler;
import net.zincstudios.scgextra.sounds.FACSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;

import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FacTankEntity extends Monster implements GeoEntity, Gunner, CustomGunHolder, BulletSpawnOffset {

    static final int MELEE_DURATION = 20;
    static final int MELEE_DAMAGE_DELAY = 12;

    private static final int STUN_RECOVERY_TICKS = 10;
    private static final int DEATH_ANIMATION_TICKS = 40;
    private static final Vec3 LEFT_GUN_OFFSET = new Vec3(-1.5, 2.2, 1);
    private static final Vec3 RIGHT_GUN_OFFSET = new Vec3(1.5, 2.2, 1);
    private static final Vec3 CANNON_OFFSET = new Vec3(-0.7, 2.4, 1.8);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation STOMP = RawAnimation.begin().thenPlay("stomp");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    private static final RawAnimation STUN_START = RawAnimation.begin().thenPlayAndHold("stun_start");
    private static final RawAnimation STUN_END = RawAnimation.begin().thenPlay("stun_end");
    private static final RawAnimation CANNON = RawAnimation.begin().thenPlay("cannon");
    private static final RawAnimation RIGHT_GUN_FIRE = RawAnimation.begin().thenPlay("right_gun_fire");
    private static final RawAnimation LEFT_GUN_FIRE = RawAnimation.begin().thenPlay("left_gun_fire");
    private static final RawAnimation EXHAUST = RawAnimation.begin().thenLoop("exhaust");
    private static final RawAnimation EFFECT_BASE = RawAnimation.begin().thenPlayAndHold("effect.none");
    private static final RawAnimation EYE_FLASH = RawAnimation.begin().thenPlay("effect.eye_flash");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final SimulatedGun customGun;
    private final SimulatedGun mainCannon;

    private boolean bulletSpawnLeft = false;

    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);

    public FacTankEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.BIRDFEEDER.get().getGun())
                .projectileDamage(6F)
                .fireRate(2)
                .maxRange(16)
                .idealRange(12)
                .gunIndex(0)
                .noGunFlash() // handled on onGunFire instead
                .velocityModifier(vec -> vec.scale(1/2F))
                .build();
        this.mainCannon = new CustomScorchedSimGun.Builder(ModItems.DOZIER_RL.get().getGun())
                .projectileDamage(20F)
                .fireRate(20)
                .maxRange(20)
                .idealRange(16)
                .gunIndex(1)
                .noGunFlash() // handled on onGunFire instead
                .velocityModifier(vec -> vec.scale(1/5F).add(0, 0.1F, 0))
                .projectileFactory(TankCannonProjectile::new)
                .build();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.17D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MAX_HEALTH, 400.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return FacTankAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<FacTankEntity> getBrain() {
        return (Brain<FacTankEntity>) super.getBrain();
    }

    protected Brain.Provider<FacTankEntity> brainProvider() {
        return FacTankAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("facTankBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        FacTankAi.updateActivity(this);
        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
            this.setYBodyRot(this.getYHeadRot());
        }
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        this.getBrain().getMemory(ModBrainMemories.ABILITY_STATE.get()).ifPresent(abilityState -> {
            if (abilityState.isSame(TankCannonFire.ABILITY_ID)) {
                if (abilityState.getDuration(this.level()) == 10) {
                    this.triggerAnim("effects", "eye_flash");
                }
            }
        });

        if (brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.triggerAnim("main", "stomp");
        }
        if (brain.getTimeUntilExpiry(ModBrainMemories.STUNNED.get()) == MobUtil.DEFAULT_STUN_DURATION) {
            this.playSound(FACSounds.FAC_TANK_STUN.get());
            this.triggerAnim("behavior", "stun");
        }
        if (brain.getTimeUntilExpiry(ModBrainMemories.STUNNED.get()) == STUN_RECOVERY_TICKS) {
            this.triggerAnim("behavior", "end_stun");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4,
                state -> {
                    if (state.isMoving() || state.getAnimatable().isActuallyMoving()) {
                        state.setAnimation(WALK);
                    } else {
                        state.setAnimation(IDLE);
                    }
                    return PlayState.CONTINUE;
                })
                .triggerableAnim("stomp", STOMP)
        );

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> PlayState.STOP)
                .triggerableAnim("stun", STUN_START)
                .triggerableAnim("end_stun", STUN_END)
        );

        // triggered anims don't run concurrently thus needing separate controllers
        controllers.add(new AnimationController<>(this, "effects", 2,
                state -> state.setAndContinue(EFFECT_BASE))
                .triggerableAnim("eye_flash", EYE_FLASH)
        );
        controllers.add(new AnimationController<>(this, "left_gun", 2,
                state -> PlayState.STOP)
                .triggerableAnim("fire", LEFT_GUN_FIRE)
        );
        controllers.add(new AnimationController<>(this, "right_gun", 2,
                state -> PlayState.STOP)
                .triggerableAnim("fire", RIGHT_GUN_FIRE)
        );
        controllers.add(new AnimationController<>(this, "cannon", 2,
                state -> PlayState.STOP)
                .triggerableAnim("fire", CANNON)
        );

        controllers.add(new AnimationController<>(this, "exhaust", 2, state -> state.setAndContinue(EXHAUST)));


        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));
    }

    // needed because this thing moves way too fucking slow for state.isMoving() to be true
    private boolean isActuallyMoving() {
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        return dx * dx + dz * dz > 0.000001D;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void onGunFire(SimulatedGun gun, Vec3 targetPos) {
        this.bulletSpawnLeft = !this.bulletSpawnLeft;

        if (gun == this.mainCannon) {
            this.triggerAnim("cannon", "fire");

//            Gun.Display.Flash flash = ModItems.HULLBREAKER.get().getGun().getDisplay().getFlash();
//            if (flash == null) return;
//            ResourceLocation flashTexture = ResourceLocation.fromNamespaceAndPath(ScorchedGuns.MODID,
//                    "textures/effect/" + flash.getTextureLocation() + ".png");
//            SCGEPacketHandler.sendToNearbyPlayers(() -> MobUtil.levelLocationFromEntity(this),
//                    new GunFlashMessage(this.getId(), 2, flashTexture, false, 1.2F));
            return;
        }

        if (this.bulletSpawnLeft) {
            this.triggerAnim("left_gun", "fire");
        } else {
            this.triggerAnim("right_gun", "fire");
        }
        Gun.Display.Flash flash = ModItems.BIRDFEEDER.get().getGun().getDisplay().getFlash();
        if (flash == null) return;
        ResourceLocation flashTexture = ResourceLocation.fromNamespaceAndPath(ScorchedGuns.MODID,
                "textures/effect/" + flash.getTextureLocation() + ".png");
        SCGEPacketHandler.sendToNearbyPlayers(() -> MobUtil.levelLocationFromEntity(this),
                new GunFlashMessage(this.getId(), this.bulletSpawnLeft ? 0 : 1, flashTexture, false, 1.2F));
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        if (gunIndex == 1) {
            return CANNON_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
        }
        if (this.bulletSpawnLeft) {
            return LEFT_GUN_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
        } else {
            return RIGHT_GUN_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
        }
    }

    public SimulatedGun getMainCannon() {
        return this.mainCannon;
    }

    @Override
    public SimulatedGun getCustomGun() {
        return this.customGun;
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= DEATH_ANIMATION_TICKS && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return FACSounds.FAC_TANK_HURT.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FACSounds.FAC_TANK_IDLE.get();
    }

    protected SoundEvent getDeathSound() {
        return FACSounds.FAC_TANK_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(FACSounds.FAC_TANK_WALK.get(), 0.3F, 1F);
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
}
