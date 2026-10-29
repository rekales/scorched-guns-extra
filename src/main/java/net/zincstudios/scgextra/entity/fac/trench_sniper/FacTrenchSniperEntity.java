package net.zincstudios.scgextra.entity.fac.trench_sniper;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.sounds.FACSounds;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FacTrenchSniperEntity extends EquippedEntity implements GeoEntity, Gunner {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_AIM = RawAnimation.begin().thenLoop("idle_aim");
    private static final RawAnimation WALK_AIM = RawAnimation.begin().thenLoop("walk_aim");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public FacTrenchSniperEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18F)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.MAX_HEALTH, 40.0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        Brain<?> brain = this.getBrain();
        if (brain.getTimeUntilExpiry(ModBrainMemories.TO_ALERT.get()) == 8) {
            this.playSound(FACSounds.FAC_TRENCH_SNIPER_ALERT.get());
        }
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return FacTrenchSniperAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<FacTrenchSniperEntity> getBrain() {
        return (Brain<FacTrenchSniperEntity>) super.getBrain();
    }

    protected Brain.Provider<FacTrenchSniperEntity> brainProvider() {
        return FacTrenchSniperAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("facTrenchSniperBrain");
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
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData ret = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        MobUtil.addGunAttachment(this.getItemInHand(InteractionHand.MAIN_HAND), new ItemStack(ModItems.EXTENDED_BARREL.get()));
        MobUtil.addGunAttachment(this.getItemInHand(InteractionHand.MAIN_HAND), new ItemStack(ModItems.WOODEN_STOCK.get()));
        return ret;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 8, state -> {
            if (state.getAnimatable().isAggressive()) {
                return state.setAndContinue(state.isMoving() ? WALK_AIM : IDLE_AIM);
            } else {
                return state.setAndContinue(state.isMoving() ? WALK : IDLE);
            }
        }));
    }

    @Override
    public boolean isLeftHanded() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return FACSounds.FAC_TRENCHER_HURT.get();
    }

    protected SoundEvent getAmbientSound() {
        return FACSounds.FAC_TRENCH_SNIPER_IDLE.get();
    }

    protected SoundEvent getDeathSound() {
        return FACSounds.FAC_TRENCH_SNIPER_DEATH.get();
    }
    
    public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && spawnType == MobSpawnType.SPAWNER ? checkMobSpawnRules(type, level, spawnType, pos, random) : ( isDarkEnoughToSpawn(level, pos, random) && checkMobSpawnRules(type, level, spawnType, pos, random));
    }
}
