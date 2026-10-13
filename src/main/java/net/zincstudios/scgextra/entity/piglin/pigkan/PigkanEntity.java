package net.zincstudios.scgextra.entity.piglin.pigkan;

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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PigkanEntity extends Monster implements GeoEntity{

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SAW_ATTACK = RawAnimation.begin().thenPlay("saw_attack");
    private static final RawAnimation CLAW_ATTACK = RawAnimation.begin().thenPlay("claw_attack");
    private static final RawAnimation STUN_FULL = RawAnimation.begin().thenPlay("stun_full");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("claw_attack");


    public PigkanEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (state.isMoving()) {
                state.setAnimation(WALK);
            } else {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }).triggerableAnim("stun", STUN_FULL));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "attack", 2, state -> {
            return PlayState.CONTINUE;
        })
            .triggerableAnim("claw_attack", CLAW_ATTACK)
            .triggerableAnim("saw_attack", SAW_ATTACK)
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 128.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.31F)
            .add(Attributes.ATTACK_DAMAGE, 30.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
            .add(Attributes.ARMOR, 6.0D)
            .add(Attributes.MAX_HEALTH, 200.0D);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PigkanAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PigkanEntity> getBrain() {
        return (Brain<PigkanEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PigkanEntity> brainProvider() {
        return PigkanAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("pigkanBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        BrainCommons.updateMaxRangeAggressive(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }
    
}
