package net.zincstudios.scgextra.entity.whaler.fishfolk;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.fluids.FluidType;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FishFolkEntity extends EquippedEntity implements GeoEntity, Gunner, VariantHolder<Integer> {

    private static final EntityDataAccessor<Integer> TEXTURE_VARIANT =
            SynchedEntityData.defineId(FishFolkEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public FishFolkEntity(EntityType<? extends Monster> entity, Level level) {
        super(entity, level);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @org.jetbrains.annotations.Nullable SpawnGroupData spawnData, @org.jetbrains.annotations.Nullable CompoundTag dataTag) {
        float rand = this.getRandom().nextFloat();
        this.setVariant(rand < 0.45f ? 1 : rand < 0.90f ? 2 : 3);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 35.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.23F)
        .add(Attributes.ATTACK_DAMAGE, 3.0D)
        .add(Attributes.ARMOR, 4.0D)
        .add(Attributes.MAX_HEALTH, 20.0D)
        .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return FishFolkAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<FishFolkEntity> getBrain() {
        return (Brain<FishFolkEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<FishFolkEntity> brainProvider() {
        return FishFolkAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("facBluecoatBrain");
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
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            if (state.getAnimatable().isPassenger()) {
                state.setAndContinue(RawAnimation.begin().thenLoop("sitting"));
            }else if (state.isMoving()) {
                state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            } else {
                state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }).triggerableAnim("attack", RawAnimation.begin().thenPlay("attack")));
        controllers.add(new AnimationController<>(this, "special", 0, state -> PlayState.CONTINUE)
        .triggerableAnim("attack", RawAnimation.begin().thenPlay("attack")));
    }

    @Override
    public void setVariant(Integer variant) {
        this.entityData.set(TEXTURE_VARIANT, variant);
    }

    @Override
    public Integer getVariant() {
        return this.entityData.get(TEXTURE_VARIANT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TEXTURE_VARIANT, 1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("variant", this.entityData.get(TEXTURE_VARIANT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(TEXTURE_VARIANT, tag.getInt("variant"));
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return super.canRide(vehicle);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }
}