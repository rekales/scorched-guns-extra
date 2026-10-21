package net.zincstudios.scgextra.entity.piglin.piglin_kiln_hog;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.AttachedMountedGun;
import net.zincstudios.scgextra.entity.piglin.common.AggroableMob;
import net.zincstudios.scgextra.entity.piglin.common.StunnableHog;
import net.zincstudios.scgextra.sounds.PiglinSounds;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

public class PiglinKilnHogEntity extends Monster implements GeoEntity, Gunner, BulletSpawnOffset, AggroableMob{

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation AGGRO = RawAnimation.begin().thenPlay("enraged_scream");
    private static final RawAnimation MELEE = RawAnimation.begin().thenPlay("melee");
    private static final RawAnimation SMALL_CANON = RawAnimation.begin().thenPlay("small_cannon");
    private static final RawAnimation BIG_CANON = RawAnimation.begin().thenPlay("big_cannon");
    private static final RawAnimation STUN_START = RawAnimation.begin().thenPlay("stun_start");
    private static final RawAnimation STUN_MIDDLE = RawAnimation.begin().thenLoop("stun_middle");
    private static final RawAnimation STUN_END = RawAnimation.begin().thenPlay("stun_end");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public final AttachedMountedGun mountedShotGunLeft;
    public final AttachedMountedGun mountedShotGunRight;

    public Vec3 bulletOffsetL = new Vec3(-28/16f, 32/16f, -40/16f * -1);
    public Vec3 bulletOffsetR = new Vec3(28/16f, 32/16f, -40/16f * -1);

    private boolean aggroed = false;
    public int aggroTicks = 0;

    static int MELEE_DELAY = 18;
    static int MELEE_DURATION = 30;

    public static final StunnableHog instance = new StunnableHog(
        20, 
        20, 
        20
    );

    private static final EntityDataAccessor<Long> TICKS_REMAINING = SynchedEntityData.defineId(PiglinKilnHogEntity.class, EntityDataSerializers.LONG);

    //to whom ever it may concern, I am sorry for such janky solution for this lava anim
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(PiglinKilnHogEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    static final Vec3 EXHAUST = new Vec3(0/16f, 69/16f, 18/16f*-1f);

    public PiglinKilnHogEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.mountedShotGunLeft = new AttachedMountedGun(
            this, 
            new CustomScorchedSimGun.Builder(ModItems.SPITFIRE.get().getGun())
                .projectileDamage(15f)
                .fireRate(1)
                .maxRange(20)
                .idealRange(16)
                .noGunFlash()
                .gunIndex(0)
                .build(), 
            bulletOffsetL, 
            (a) -> true
        );
        this.mountedShotGunRight = new AttachedMountedGun(
            this, 
            new CustomScorchedSimGun.Builder(ModItems.SPITFIRE.get().getGun())
                .projectileDamage(15f)
                .fireRate(1)
                .maxRange(20)
                .idealRange(16)
                .noGunFlash()
                .gunIndex(1)
                .build(), 
            bulletOffsetR, 
            (a) -> true
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 128.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.25F)
        .add(Attributes.ATTACK_DAMAGE, 20.0D)
        .add(Attributes.ATTACK_KNOCKBACK, 3.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
        .add(Attributes.ARMOR, 6.0D)
        .add(Attributes.MAX_HEALTH, 600.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if(state.getAnimatable().isStunned()){
                if(PiglinKilnHogEntity.instance.isStart(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_START);
                }else if(PiglinKilnHogEntity.instance.isMiddle(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_MIDDLE);
                }else if(PiglinKilnHogEntity.instance.isEnd(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_END);
                }
            }else{
                if(!state.isCurrentAnimation(AGGRO)){
                    if (state.isMoving()) {
                        state.setAnimation(WALK);
                    } else {
                        state.setAnimation(IDLE);
                    }
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

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> {
            return PlayState.CONTINUE;
        })
            .triggerableAnim("aggro", AGGRO)
            .triggerableAnim("melee", MELEE)
            .triggerableAnim("small_canon", SMALL_CANON)
            .triggerableAnim("big_canon", BIG_CANON)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinKilnHogAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PiglinKilnHogEntity> getBrain() {
        return (Brain<PiglinKilnHogEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PiglinKilnHogEntity> brainProvider() {
        return PiglinKilnHogAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinBlazeKopterBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        PiglinKilnHogAi.updateActivity(this);
        BrainCommons.updateMaxRangeAggressive(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        SCGExtra.LOGGER.info("GUN INDEX: "+gunIndex);
        return switch (gunIndex) {
            case 0 -> this.bulletOffsetL.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
            case 1 -> this.bulletOffsetR.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
            default -> Vec3.ZERO;
        };
    }
    
    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide()){
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            this.updateAggroTicks();
            this.updateLavaPhase();
            if (brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
                this.triggerAnim("behavior", "melee");
            }
            if(!this.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)){
                this.aggroed = false;
            }
            if(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get())){
                this.setTicksRemaining(this.getBrain().getTimeUntilExpiry(ModBrainMemories.STUNNED.get()));
                if(this.getTicksRemaining() == PiglinKilnHogEntity.instance.getStunDuration()){
                    this.stopTriggeredAnimation("behavior", null);
                }
            }else{
                this.setTicksRemaining(Long.valueOf(-1));
            }
        }
        if(this.tickCount%10==0){
            Vec3 ex_pos = this.position().add(PiglinKilnHogEntity.EXHAUST.yRot(-this.yBodyRot * Mth.DEG_TO_RAD));
            this.level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, ex_pos.x, ex_pos.y, ex_pos.z, 0.1, 0.2, 0.1);
            this.level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, ex_pos.x, ex_pos.y, ex_pos.z, 0.1, 0.2, 0.1);
            this.level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, ex_pos.x, ex_pos.y, ex_pos.z, 0.1, 0.2, 0.1);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 1);
        this.entityData.define(TICKS_REMAINING, Long.valueOf(0));
    }

    public int getLavaPhase(){
        return this.entityData.get(PHASE);
    }

    public void updateLavaPhase(){
        if(this.getLavaPhase() < 20){
            this.entityData.set(PHASE, this.getLavaPhase()+1);
        }else if(this.getLavaPhase() >= 20){
            this.entityData.set(PHASE, 1);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return PiglinSounds.GENERAL_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(PiglinSounds.GENERAL_WALK.get(), this.getSoundVolume() * 0.15F, this.getVoicePitch());
    }

    @Override
    protected SoundEvent getDeathSound() {
        return PiglinSounds.PIGLIN_KILN_HOG_DEFEAT.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PiglinSounds.PIGLIN_KILN_HOG_IDLE.get();
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
        MobUtil.tickDeath(this, 40);
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

    @Override
    public int getAggroTicks() {
        return this.aggroTicks;
    }

    @Override
    public boolean isAggro() {
        return this.aggroed;
    }

    @Override
    public void updateAggroTicks() {
        if(this.aggroTicks>0)this.aggroTicks--;
    }

    @Override
    public void setAggro(int ticks) {
        this.aggroed = true;
        this.aggroTicks = ticks;
        this.triggerAnim("behavior", "aggro");
        this.playSound(PiglinSounds.PIGLIN_KILN_HOG_GROWL.get(), 2.0f, this.getVoicePitch());
    }
}