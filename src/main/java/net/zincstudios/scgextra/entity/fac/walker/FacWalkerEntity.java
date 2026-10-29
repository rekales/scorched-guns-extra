package net.zincstudios.scgextra.entity.fac.walker;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.*;
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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FacWalkerEntity extends Monster implements GeoEntity, Gunner, CustomGunHolder, BulletSpawnOffset {

    static final int MELEE_DURATION = 18;
    static final int MELEE_DAMAGE_DELAY = 10;

    private static final int STUN_RECOVERY_TICKS = 12;
    private static final int DEATH_ANIM_TICKS = 35;
    private static final Vec3 LEFT_GUN_OFFSET = new Vec3(-0.65,3.5,-0.4);  // TODO: refine
    private static final Vec3 RIGHT_GUN_OFFSET = new Vec3(0.65,3.5,-0.4);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
//    private static final RawAnimation SCAN = RawAnimation.begin().thenLoop("scan");
    private static final RawAnimation STOMP = RawAnimation.begin().thenPlay("stomp");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    private static final RawAnimation STUN_START = RawAnimation.begin().thenPlayAndHold("stun_start");
    private static final RawAnimation STUN_END = RawAnimation.begin().thenPlay("stun_end");
//    private static final RawAnimation FIRE = RawAnimation.begin().thenLoop("fire");
    private static final RawAnimation EXHAUST = RawAnimation.begin().thenLoop("exhaust");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final SimulatedGun customGun;

    private boolean bulletSpawnLeft = false;

    public FacWalkerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.BIRDFEEDER.get().getGun())
                .projectileDamage(4f)
                .fireRate(2)
                .maxRange(16)
                .idealRange(12)
                .noGunFlash() // handled on onGunFire instead
                .velocityModifier(vec -> vec.scale(1/2f))
                .build();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MAX_HEALTH, 250.0D);
    }

    @Override
    public void tick() {
        super.tick();
        this.setXRot(0);

        if (brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.triggerAnim("behavior", "stomp");
        }
        if (brain.getTimeUntilExpiry(ModBrainMemories.STUNNED.get()) == MobUtil.DEFAULT_STUN_DURATION) {
            this.triggerAnim("behavior", "stun");
        }
        if (brain.getTimeUntilExpiry(ModBrainMemories.STUNNED.get()) == STUN_RECOVERY_TICKS) {
            this.triggerAnim("behavior", "end_stun");
        }
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return FacWalkerAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<FacWalkerEntity> getBrain() {
        return (Brain<FacWalkerEntity>) super.getBrain();
    }

    protected Brain.Provider<FacWalkerEntity> brainProvider() {
        return FacWalkerAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("facWalkerBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        FacWalkerAi.updateActivity(this);
        this.setSprinting(this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
                .filter(target -> !target.closerThan(this, 20))
                .isPresent() && !this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get()));
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (state.isMoving()) {
                state.setAnimation(state.getAnimatable().isSprinting() ? RUN : WALK);
            } else {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> PlayState.STOP)
                .triggerableAnim("stun", STUN_START)
                .triggerableAnim("end_stun", STUN_END)
                .triggerableAnim("stomp", STOMP)
        );

        controllers.add(new AnimationController<>(this, "exhaust", 2, state -> state.setAndContinue(EXHAUST)));

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
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        if (this.bulletSpawnLeft) {
            return LEFT_GUN_OFFSET.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
        } else {
            return RIGHT_GUN_OFFSET.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
        }
    }

    @Override
    public SimulatedGun getCustomGun() {
        return this.customGun;
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= DEATH_ANIM_TICKS && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return FACSounds.FAC_WALKER_HURT.get();
    }

    protected SoundEvent getAmbientSound() {
        return FACSounds.FAC_WALKER_IDLE.get();
    }

    protected SoundEvent getDeathSound() {
        return FACSounds.FAC_WALKER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (this.isSprinting()) {
            this.playSound(FACSounds.FAC_WALKER_RUN.get(), 0.3F, 1F);
        } else {
            this.playSound(FACSounds.FAC_WALKER_WALK.get(), 0.3F, 1F);
        }
    }
    public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && spawnType == MobSpawnType.SPAWNER ? checkMobSpawnRules(type, level, spawnType, pos, random) : ( isDarkEnoughToSpawn(level, pos, random) && checkMobSpawnRules(type, level, spawnType, pos, random));
    }
}
