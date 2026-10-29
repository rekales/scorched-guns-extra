package net.zincstudios.scgextra.entity.fac.shovel_knight;

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
public class FacShovelKnightEntity extends Monster implements GeoEntity {

    static final int MELEE_DAMAGE_DELAY = 10;
    static final int MELEE_DURATION = 22;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_AGGRO = RawAnimation.begin().thenLoop("idle_aggro");
    private static final RawAnimation RUN_AGGRO = RawAnimation.begin().thenLoop("run_aggro");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation ATTACK_NO_LEGS = RawAnimation.begin().thenPlay("attack_no_legs");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public FacShovelKnightEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20F)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.MAX_HEALTH, 50.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return FacShovelKnightAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<FacShovelKnightEntity> getBrain() {
        return (Brain<FacShovelKnightEntity>) super.getBrain();
    }

    protected Brain.Provider<FacShovelKnightEntity> brainProvider() {
        return Brain.provider(FacShovelKnightAi.MEMORY_TYPES, FacShovelKnightAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("facShovelKnightBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        BrainCommons.updateHasTargetAggressive(this);
        this.setSprinting(this.isAggressive());
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.triggerAnim("attack", "melee");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new ExpandedAnimationController<>(this, "main", 3, state -> {
            if (state.getAnimatable().isAggressive()) {
                if (state.isMoving()) {
                    state.setControllerSpeed(0.9f);
                    return state.setAndContinue(RUN_AGGRO);
                } else {
                    return state.setAndContinue(IDLE_AGGRO);
                }
            } else {
                if (state.isMoving()) {
                    state.setControllerSpeed(1.6f);
                    return state.setAndContinue(WALK);
                } else {
                    return state.setAndContinue(IDLE);
                }
            }
        }
        ));

        controllers.add(new ExpandedAnimationController<>(this, "attack", 2, state -> PlayState.STOP)
                .triggerableAnim("melee", (ctr) -> {
                    FacShovelKnightEntity entity = (FacShovelKnightEntity) ctr.getAnimatable();
                    AnimationController<?> mainCtr = entity.getAnimatableInstanceCache().getManagerForId(entity.getId()).getAnimationControllers().get("main");
                    return mainCtr.getCurrentRawAnimation() == RUN_AGGRO ? ATTACK_NO_LEGS : ATTACK;
                })
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return FACSounds.FAC_SHOVEL_KNIGHT_HURT.get();
    }

    protected SoundEvent getAmbientSound() {
        return FACSounds.FAC_SHOVEL_KNIGHT_IDLE.get();
    }

    protected SoundEvent getDeathSound() {
        return FACSounds.FAC_SHOVEL_KNIGHT_DEATH.get();
    }

    public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && spawnType == MobSpawnType.SPAWNER ? checkMobSpawnRules(type, level, spawnType, pos, random) : ( isDarkEnoughToSpawn(level, pos, random) && checkMobSpawnRules(type, level, spawnType, pos, random));
    }
}
