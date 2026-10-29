package net.zincstudios.scgextra.entity.fac.trench_goblin;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.client.ExpandedAnimationController;
import net.zincstudios.scgextra.sounds.FACSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FacTrenchGoblinEntity extends EquippedEntity implements GeoEntity {

    static final int MELEE_DAMAGE_DELAY = 10;
    static final int MELEE_DURATION = 18;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation ATTACK_NO_LEGS = RawAnimation.begin().thenPlay("attack_no_legs");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public FacTrenchGoblinEntity(EntityType<? extends EquippedEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32F)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.MAX_HEALTH, 30.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return FacTrenchGoblinAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<FacTrenchGoblinEntity> getBrain() {
        return (Brain<FacTrenchGoblinEntity>) super.getBrain();
    }

    protected Brain.Provider<FacTrenchGoblinEntity> brainProvider() {
        return Brain.provider(FacTrenchGoblinAi.MEMORY_TYPES, FacTrenchGoblinAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("cogTrenchGoblinBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            if (this.getRandom().nextFloat() < 0.35F) {
                this.playSound(FACSounds.FAC_TRENCH_GOBLIN_ATTACK.get());
            }
            this.triggerAnim("attack", "melee");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new ExpandedAnimationController<>(this, "main", 2, state -> {
                    if (state.isMoving()) {
                        state.setControllerSpeed(1.5f);
                        return state.setAndContinue(WALK);
                    } else {
                        state.setControllerSpeed(0.8f);
                        return state.setAndContinue(IDLE);
                    }
                }
        ));

        controllers.add(new ExpandedAnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                .triggerableAnim("melee", (ctr) -> {
                    FacTrenchGoblinEntity entity = (FacTrenchGoblinEntity) ctr.getAnimatable();
                    AnimationController<?> mainCtr = entity.getAnimatableInstanceCache().getManagerForId(entity.getId()).getAnimationControllers().get("main");
                    return mainCtr.getCurrentRawAnimation() == WALK ? ATTACK_NO_LEGS : ATTACK;
                })
                .setAnimationSpeed(1.4f)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return FACSounds.FAC_TRENCH_GOBLIN_HURT.get();
    }

    protected SoundEvent getAmbientSound() {
        return FACSounds.FAC_TRENCH_GOBLIN_IDLE.get();
    }

    protected SoundEvent getDeathSound() {
        return FACSounds.FAC_TRENCH_GOBLIN_DEATH.get();
    }

    public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && spawnType == MobSpawnType.SPAWNER ? checkMobSpawnRules(type, level, spawnType, pos, random) : ( isDarkEnoughToSpawn(level, pos, random) && checkMobSpawnRules(type, level, spawnType, pos, random));
    }
}

