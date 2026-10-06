package net.zincstudios.scgextra.entity.cog.juggernaut;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.MobUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.AbilityState;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.sounds.COGSounds;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModParticleTypes;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CogJuggernautEntity extends EquippedEntity implements GeoEntity, Gunner {

    private static final EntityDataAccessor<Boolean> JET_ACTIVE =
            SynchedEntityData.defineId(CogJuggernautEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> JET_DURATION = // Might be unnecessary
            SynchedEntityData.defineId(CogJuggernautEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation EFFECTS_BASE = RawAnimation.begin().thenPlayAndHold("effect.none");
    private static final RawAnimation EYE_FLASH = RawAnimation.begin().thenPlay("effect.eye_flash");

    private static final Set<MobEffect> IMMUNE_EFFECTS = Set.of(
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.LEVITATION,
            ModEffects.DEAFENED.get(),
            ModEffects.BLINDED.get(),
            ModEffects.SULFUR_POISONING.get(),
            ModEffects.LACERATED.get()
    );

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);

    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    public CogJuggernautEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            Optional<AbilityState> optional = this.getBrain().getMemory(ModBrainMemories.ABILITY_STATE.get());
            if (optional.isPresent()) {
                AbilityState abilityState = optional.get();
                if (abilityState.isSame(JetBootsAbility.ABILITY_ID)) {
                    this.setJetDuration((int) optional.get().getDuration(this.level()));
                } else if (abilityState.isSame(RocketBarrageAbility.ABILITY_ID)) {
                    if (abilityState.getDuration(this.level()) == 12) {
                        this.triggerAnim("effects", "eye_flash");
                    }
                }
            }

            if (optional.isPresent() && optional.get().isSame(JetBootsAbility.ABILITY_ID)) {
                this.setJetDuration((int) optional.get().getDuration(this.level()));
            }
        } else if (this.isJetActive() && !this.isDeadOrDying()) {
            if (this.getJetDuration() > 24) {
                tickClientJetParticles();
            }
        }
    }

    private void tickClientJetParticles() {
        ClientLevel clientLevel = (ClientLevel) this.level();
        RandomSource rand = this.getRandom();

        Vec3 pos;
        Vec3 delta = new Vec3(0,-0.35, -0.0);
        delta = delta.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);

        for (int i = 0; i < 3; i++) {
            pos = generateRandomParticlePos();
            clientLevel.addParticle(
                    ParticleTypes.SMOKE,
                    pos.x, pos.y, pos.z,
                    delta.x + (rand.nextDouble()-0.5) * 0.15f,
                    delta.y + (rand.nextDouble()-0.5) * 0.15f,
                    delta.z + (rand.nextDouble()-0.5) * 0.15f
            );
        }

        pos = generateRandomParticlePos();
        clientLevel.addParticle(
                ParticleTypes.FLAME,
                pos.x, pos.y, pos.z,
                delta.x + (rand.nextDouble()-0.5) * 0.15f,
                delta.y + (rand.nextDouble()-0.5) * 0.15f,
                delta.z + (rand.nextDouble()-0.5) * 0.15f
        );

        if(this.tickCount%2==0){
            pos = generateRandomParticlePos();
            clientLevel.addParticle(
                    ModParticleTypes.FIREBALL.get(),
                    pos.x, pos.y, pos.z,
                    delta.x + (rand.nextDouble()-0.5) * 0.1f,
                    delta.y + (rand.nextDouble()-0.5) * 0.1f,
                    delta.z + (rand.nextDouble()-0.5) * 0.1f
            );
        }

        pos = generateRandomParticlePos();
        clientLevel.addParticle(
                ModParticleTypes.FIREBALL.get(),
                pos.x, pos.y, pos.z,
                delta.x + (rand.nextDouble()-0.5) * 0.1f,
                delta.y + (rand.nextDouble()-0.5) * 0.1f,
                delta.z + (rand.nextDouble()-0.5) * 0.1f
        );

    }

    private Vec3 generateRandomParticlePos() {
        RandomSource rand = this.getRandom();
        return new Vec3(0,0.6,-0.3).add(
                        (rand.nextDouble() - 0.5) * 0.4 + (rand.nextBoolean() ? 0.6 : -0.6),
                        0,
                        (rand.nextDouble() - 0.5) * 0.4
                ).yRot(-this.yBodyRot * Mth.DEG_TO_RAD)
                .add(this.position());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20F)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MAX_HEALTH, 1000.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CogJuggernautAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<CogJuggernautEntity> getBrain() {
        return (Brain<CogJuggernautEntity>) super.getBrain();
    }

    protected Brain.Provider<CogJuggernautEntity> brainProvider() {
        return Brain.provider(CogJuggernautAi.MEMORY_TYPES, CogJuggernautAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("cogJuggernautBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        CogJuggernautAi.updateActivity(this);
        this.setAggressive(getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (this.isJetActive()) return;  // To cancel out sound, particle, and events
        super.checkFallDamage(y, onGround, state, pos);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL) && source.is(DamageTypes.ON_FIRE) && this.isJetActive()) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected void onEffectAdded(MobEffectInstance effectInstance, @Nullable Entity entity) {
        super.onEffectAdded(effectInstance, entity);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        return super.canBeAffected(effectInstance) && !IMMUNE_EFFECTS.contains(effectInstance.getEffect());
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 35);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
                if (state.getAnimatable().isAggressive()) {
                    if (state.isMoving()) {
                        state.setAnimation(RawAnimation.begin().thenLoop("walk_hold"));
                    } else {
                        state.setAnimation(RawAnimation.begin().thenLoop("idle_hold"));
                    }
                } else {
                    if (state.isMoving()) {
                        state.setAnimation(RawAnimation.begin().thenLoop("walk"));
                    } else {
                        state.setAnimation(RawAnimation.begin().thenLoop("idle"));
                    }
                }
                return PlayState.CONTINUE;
                }).setAnimationSpeed(1.2f)
        );

        controllers.add(new AnimationController<>(this, "jet", 4, state -> {
                    if (state.getAnimatable().isJetActive()) {
                        return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("fly"));
                    }
                    return PlayState.STOP;
                })
                .triggerableAnim("land", RawAnimation.begin().thenPlay("land"))
        );

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "effects", 0,
                state -> state.setAndContinue(EFFECTS_BASE))
                .triggerableAnim("eye_flash", EYE_FLASH)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(JET_ACTIVE, false);
        this.entityData.define(JET_DURATION, 0);
    }

    public void setJetActive(boolean jetActive) {
        if (this.entityData.get(JET_ACTIVE) && !jetActive) {
            this.triggerAnim("jet", "land");
        }
        this.entityData.set(JET_ACTIVE, jetActive);
    }

    public boolean isJetActive() {
        return this.entityData.get(JET_ACTIVE);
    }

    private void setJetDuration(int jetDuration) {
        this.entityData.set(JET_DURATION, jetDuration);
    }

    private int getJetDuration() {
        return this.entityData.get(JET_DURATION);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return COGSounds.GENERAL_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return COGSounds.COG_JUGGERNAUT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return COGSounds.COG_JUGGERNAUT_DEAD.get();
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
