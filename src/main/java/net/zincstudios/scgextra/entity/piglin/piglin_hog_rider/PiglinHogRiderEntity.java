package net.zincstudios.scgextra.entity.piglin.piglin_hog_rider;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinHogRiderEntity extends Monster implements GeoEntity, VariantHolder<Integer>{

    private static final EntityDataAccessor<Integer> MODEL_VARIANT =
            SynchedEntityData.defineId(PiglinHogRiderEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("melee");
    private static final RawAnimation AGGROED = RawAnimation.begin().thenPlay("aggroed");
    private static final RawAnimation CHARGE_START = RawAnimation.begin().thenPlay("charge_start");
    private static final RawAnimation CHARGE_MIDDLE = RawAnimation.begin().thenLoop("charge_middle");
    private static final RawAnimation CHARGE_END = RawAnimation.begin().thenPlay("charge_end");
    private static final RawAnimation STUN_START = RawAnimation.begin().thenPlay("stun_start");
    private static final RawAnimation STUN_MIDDLE = RawAnimation.begin().thenLoop("stun_middle");
    private static final RawAnimation STUN_END = RawAnimation.begin().thenPlay("stun_end");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    static final int MELEE_DAMAGE_DELAY = 13;
    static final int MELEE_DURATION = 20;

    public PiglinHogRiderEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 64.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.31F)
        .add(Attributes.ATTACK_DAMAGE, 30.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
        .add(Attributes.ARMOR, 12.0D)
        .add(Attributes.MAX_HEALTH, 200.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 3, state -> {
            if (state.isMoving()) {
                state.setAndContinue(WALK);
            } else {
                state.setAndContinue(IDLE);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "attack", 0, state -> {
            return PlayState.CONTINUE;
        })
        .triggerableAnim("melee", ATTACK)
        .triggerableAnim("aggro", AGGROED)
    );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void setVariant(Integer variant) {
        this.entityData.set(MODEL_VARIANT, variant);
    }

    @Override
    public Integer getVariant() {
        return this.entityData.get(MODEL_VARIANT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODEL_VARIANT, 1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("variant", this.entityData.get(MODEL_VARIANT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(MODEL_VARIANT, tag.getInt("variant"));
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData spawnData, CompoundTag dataTag) {
        this.setVariant(this.getRandom().nextFloat() < 0.5f ? 1 : 2);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinHogRiderAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PiglinHogRiderEntity> getBrain() {
        return (Brain<PiglinHogRiderEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PiglinHogRiderEntity> brainProvider() {
        return PiglinHogRiderAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinHogRiderBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide){
            if (brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
                this.triggerAnim("attack", "melee");
            }
        }
    }
}
