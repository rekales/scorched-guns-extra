package net.zincstudios.scgextra.entity.wreckers.wrecker_dozer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerBossEvent;
import net.zincstudios.scgextra.CommonConfig;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.cog.ApproachTargetGoal;
import net.zincstudios.scgextra.entity.cog.MountedGunGoal;
import net.zincstudios.scgextra.entity.common.HeadShotHandler;
import net.zincstudios.scgextra.entity.common.Stunnable;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.entity.common.goal.StunnedWithVisualGoal;
import net.zincstudios.scgextra.entity.wreckers.WreckersEntities;
import net.zincstudios.scgextra.sounds.WreckersSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.item.GunItem;

public class WreckerDozerEntity extends Monster implements GeoEntity, Stunnable, HeadShotHandler {

    private static final int DEATH_ANIMATION_TICKS = 53;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    public static final Vec3 EXHAUST_1 = new Vec3(19.5D / 16.0D, 45.8024D / 16.0D, -38.5339D / 16.0D);
    public static final Vec3 EXHAUST_2 = new Vec3(-19.5D / 16.0D, 45.8024D / 16.0D, -38.5339D / 16.0D);
    public static final Vec3 EXHAUST_3 = new Vec3(-0.5D / 16.0D, 48.8024D / 16.0D, 15.4661D / 16.0D);

    private static final EntityDataAccessor<Boolean> CHARGE_WARNING =
            SynchedEntityData.defineId(WreckerDozerEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private int headshotCounter = 0;
    private boolean stunned = false;
    private boolean stunCooldown = false;
    private boolean summonedOnDeath = false;

    public WreckerDozerEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
        this.setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARGE_WARNING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new StunnedWithVisualGoal<>(this).smoking(true));
        this.goalSelector.addGoal(2, new WreckerDozerChargeGoal(this, 200, 20.0F));
        this.goalSelector.addGoal(3, new WreckerDozerSummonGoal(this, 400, 3));
        this.goalSelector.addGoal(4, new ApproachTargetGoal(this, 5.0, 3.5, 1.0) {
            @Override
            public boolean canUse() {
                return !WreckerDozerEntity.this.isStunned() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new MountedGunGoal<>(this, (GunItem) top.ribs.scguns.init.ModItems.GREASER_SMG.get())
                .maxRange(8)
                .burstAmount(10)
                .burstIntervalTicks(1)
                .attackInterval(30)
                .spawnOffset(new Vec3(0.0D, 3.2D, 1.0D))
                .spread(10.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(2, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity)));
    }

    public void setChargeWarning(boolean warning) {
        this.entityData.set(CHARGE_WARNING, warning);
    }

    public boolean isChargeWarning() {
        return this.entityData.get(CHARGE_WARNING);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
        if(this.isChargeWarning()){
            if(this.level().isClientSide()){
                if (CommonConfig.enableAbilityWarning) {
                    Vec3 ex_pos_1 = this.position().add(WreckerDozerEntity.EXHAUST_1.yRot(-this.yBodyRot * Mth.DEG_TO_RAD));
                    Vec3 ex_pos_2 = this.position().add(WreckerDozerEntity.EXHAUST_2.yRot(-this.yBodyRot * Mth.DEG_TO_RAD));
                    Vec3 ex_pos_3 = this.position().add(WreckerDozerEntity.EXHAUST_3.yRot(-this.yBodyRot * Mth.DEG_TO_RAD));
                    this.level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, ex_pos_1.x, ex_pos_1.y, ex_pos_1.z, 0, 0.2, 0);
                    this.level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, ex_pos_2.x, ex_pos_2.y, ex_pos_2.z, 0, 0.2, 0);
                    this.level().addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, ex_pos_3.x, ex_pos_3.y, ex_pos_3.z, 0, 0.2, 0);
                }
            }
        }
    }

    @Override
    public int shouldStun() {
        if (!CommonConfig.enableAbilityWeakness) {
            return 0;
        }
        if (this.headshotCounter >= CommonConfig.abilityWeaknessHeadshots) {
            return CommonConfig.abilityWeaknessDuration;
        }
        return 0;
    }

    @Override
    public void setStunned(boolean stunned) {
        this.stunned = stunned;
        if (stunned) {
            this.playSound(WreckersSounds.DOZER_STUN_DEATH.get(), 1.0F, 1.2F);
        } else {
            this.headshotCounter = 0;
        }
    }

    @Override
    public boolean isStunned() {
        return this.stunned;
    }

    @Override
    public void setStunCooldown(boolean cooldown) {
        this.stunCooldown = cooldown;
    }

    @Override
    public boolean headshot(DamageSource source, float amount) {
        if (this.headshotCounter < CommonConfig.abilityWeaknessHeadshots - 1 || !this.stunCooldown) {
            this.headshotCounter++;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isStunned()) {
            amount *= (float) CommonConfig.abilityWeaknessDamageMult;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.level().isClientSide() && !this.summonedOnDeath) {
            this.summonedOnDeath = true;
            this.summonDeathWave();
        }
        super.die(cause);
    }

    @SuppressWarnings("deprecation")
    private void summonDeathWave() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        EntityType<?>[] types = {
                WreckersEntities.WRECKER_RED.get(),
                WreckersEntities.WRECKER_BLUE.get()
        };
        for (EntityType<?> type : types) {
            BlockPos pos = this.blockPosition().offset(
                    -2 + this.getRandom().nextInt(5), 1, -2 + this.getRandom().nextInt(5));
            if (type.create(level) instanceof Mob mob) {
                mob.moveTo(pos, 0.0F, 0.0F);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
                level.addFreshEntityWithPassengers(mob);
            }
        }
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
    public void setCustomName(net.minecraft.network.chat.Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
            if (this.isStunned()) {
                return state.setAndContinue(IDLE);
            }
            if (state.isMoving() || this.getNavigation().isInProgress()) {
                return state.setAndContinue(WALK);
            }
            return state.setAndContinue(IDLE);
        }).setAnimationSpeed(0.9));

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
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(WreckersSounds.DOZER_MOVE.get(), 0.5F, 0.9F + this.random.nextFloat() * 0.1F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WreckersSounds.DOZER_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WreckersSounds.DOZER_HURT.get();
    }
}
