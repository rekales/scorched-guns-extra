package net.zincstudios.scgextra.entity.piglin.pigdread;

import net.zincstudios.scgextra.entity.common.gun.AttachedMountedGun;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.MountedGun;
import net.zincstudios.scgextra.entity.piglin.common.StunnableHog;
import net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter.PiglinBlazeKopterProjectile;
import net.zincstudios.scgextra.sounds.PiglinSounds;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.client.ExpandedAnimationController;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

public class PigdreadEntity extends Monster implements GeoEntity, Gunner, BulletSpawnOffset{

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("attack_ranged");
    private static final RawAnimation STUN_START = RawAnimation.begin().thenPlay("stun_start");
    private static final RawAnimation STUN_MIDDLE = RawAnimation.begin().thenLoop("stun_middle");
    private static final RawAnimation STUN_END = RawAnimation.begin().thenPlay("stun_end");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private static Vec3 offset = new Vec3(-38/16, 33/16, -39/16*-1);

    public final MountedGun mountedShotGun;

    static int MELEE_DELAY = 18;
    static int MELEE_DURATION = 30;

    public static final StunnableHog instance = new StunnableHog(
        15, 
        20, 
        20
    );

    static int deathAnimTime = 40;

    private static final EntityDataAccessor<Long> TICKS_REMAINING = SynchedEntityData.defineId(PigdreadEntity.class, EntityDataSerializers.LONG);

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    public PigdreadEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.mountedShotGun = new AttachedMountedGun(
            this, 
            new CustomScorchedSimGun.Builder(ModItems.DRILL_CONVERSION.get().getGun())
                .projectileDamage(3f)
                .fireRate(1)
                .maxRange(20)
                .idealRange(16)
                .noGunFlash()
                .burstAmount(2)
                .burstInterval(1)
                .gunIndex(0)
                .projectileFactory(PiglinBlazeKopterProjectile::new)
                .build(), 
            offset, 
            (a) -> true
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 128.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.25F)
        .add(Attributes.ATTACK_DAMAGE, 35.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
        .add(Attributes.ARMOR, 8.0D)
        .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 2.0D)
        .add(Attributes.MAX_HEALTH, 600.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if(state.getAnimatable().isStunned()){
                if(PigdreadEntity.instance.isStart(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_START);
                }else if(PigdreadEntity.instance.isMiddle(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_MIDDLE);
                }else if(PigdreadEntity.instance.isEnd(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_END);
                }
            }else{
                if (state.isMoving()) {
                    state.setAnimation(WALK);
                } else {
                    state.setAnimation(IDLE);
                }
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));

        controllers.add(new ExpandedAnimationController<>(this, "behavior", 0, state -> {
            return PlayState.CONTINUE;
        })
            .triggerableAnim("melee", ATTACK)
            .triggerableAnim("shoot", SHOOT)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return offset.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PigdreadAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PigdreadEntity> getBrain() {
        return (Brain<PigdreadEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PigdreadEntity> brainProvider() {
        return PigdreadAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinBlazeKopterBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        PigdreadAi.updateActivity(this);
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
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            if (brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
                this.triggerAnim("behavior", "melee");
            }
            if(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get())){
                this.setTicksRemaining(this.getBrain().getTimeUntilExpiry(ModBrainMemories.STUNNED.get()));
            }else{
                this.setTicksRemaining(Long.valueOf(-1));
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PiglinSounds.PIGDREAD_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return PiglinSounds.GENERAL_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(PiglinSounds.PIGDREAD_WALK.get(), this.getSoundVolume() * 0.15F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.3F);
    }
    @Override
    protected SoundEvent getDeathSound() {
        return PiglinSounds.PIGDREAD_DEATH.get();
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
    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, deathAnimTime);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TICKS_REMAINING, Long.valueOf(0));
    }

    public boolean isStunned(){
        return this.entityData.get(TICKS_REMAINING) > 0;
    }

    public long getTicksRemaining(){
        return this.entityData.get(TICKS_REMAINING);
    }

    public void setTicksRemaining(long l){
        this.entityData.set(TICKS_REMAINING, l);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get()))amount*=2;
        return super.hurt(source, amount);
    }
    @Override
    public void die(DamageSource damageSource) {
        this.stopTriggeredAnimation("behavior", null);
        super.die(damageSource);
    }
}
