package net.zincstudios.scgextra.entity.whaler.lion_fish;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidType;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LionFishEntity extends EquippedEntity implements GeoEntity{

    static final int MELEE_DAMAGE_DELAY = 15;
    static final int MELEE_DURATION = 22;

    static final int POISON_DURATION = 57;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation SPLASH = RawAnimation.begin().thenPlay("splash");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public LionFishEntity(EntityType<? extends Monster> entity, Level level) {
        super(entity, level);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (state.isMoving()) {
                state.setAnimation(state.getAnimatable().isSprinting() ? RUN : WALK);
            } else {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> PlayState.STOP).triggerableAnim("attack", ATTACK).triggerableAnim("splash", SPLASH));

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

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 35.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.45F)
        .add(Attributes.ATTACK_DAMAGE, 8.0D)
        .add(Attributes.ARMOR, 4.0D)
        .add(Attributes.MAX_HEALTH, 240.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return LionFishAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<LionFishEntity> getBrain() {
        return (Brain<LionFishEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<LionFishEntity> brainProvider() {
        return LionFishAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("facBluecoatBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        BrainCommons.updateMaxRangeAggressive(this);
        this.setYBodyRot(this.getYHeadRot());
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.triggerAnim("behavior", "attack");
        }
        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.ATTACK_RUN_FOR.get()) == POISON_DURATION) {
            this.triggerAnim("behavior", "splash");
        }
    }
}
