package net.zincstudios.scgextra.entity.whaler.turtleman;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidType;

import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.*;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.entity.projectile.LightningProjectileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TurtlemanEntity extends EquippedEntity implements GeoEntity, Gunner {

    private static final EntityDataAccessor<Boolean> TURNED_AROUND =
            SynchedEntityData.defineId(TurtlemanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STUNNED =
            SynchedEntityData.defineId(TurtlemanEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_BACK = RawAnimation.begin().thenLoop("idle_back");
    private static final RawAnimation WALK_BACK = RawAnimation.begin().thenLoop("walk_back");
    private static final RawAnimation FRONT_BACK = RawAnimation.begin().thenPlay("front_back");
    private static final RawAnimation BACK_FRONT = RawAnimation.begin().thenPlay("back_front");
    private static final RawAnimation STUN = RawAnimation.begin().thenLoop("stun");
    private static final RawAnimation STUN_BACK = RawAnimation.begin().thenLoop("stun");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public TurtlemanEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.ARMOR, 8)
                .add(Attributes.FOLLOW_RANGE, 48)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.80)
                .add(Attributes.MOVEMENT_SPEED, 0.20);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return TurtlemanAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<TurtlemanEntity> getBrain() {
        return (Brain<TurtlemanEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<TurtlemanEntity> brainProvider() {
        return TurtlemanAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("whalerTurtlemanBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        TurtlemanAi.updateActivity(this);

        LivingEntity target = this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null) {
            if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
                this.setTurnedAround(false);
                this.setAggressive(true);
                this.setYBodyRot(this.getYHeadRot());
            } else {
                this.setTurnedAround(true);
                this.setAggressive(false);
            }
        } else {
            this.setTurnedAround(false);
            this.setAggressive(false);
        }

        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
            this.setYBodyRot(this.getYHeadRot());
        }
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.setStunned(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get()));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof LightningProjectileEntity) {
            if (this.getBrain().checkMemory(ModBrainMemories.SHOCK_CELL_COUNT.get(), MemoryStatus.REGISTERED)) {
                int shockCellCount = this.getBrain().getMemory(ModBrainMemories.SHOCK_CELL_COUNT.get()).orElse(0);
                this.getBrain().setMemory(ModBrainMemories.SHOCK_CELL_COUNT.get(), ++shockCellCount);
            }
        }

        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, state -> {
            AnimationController<?> ctr = state.getController();
            RawAnimation anim = ctr.getCurrentRawAnimation();

            if (state.getAnimatable().isTurnedAround()) {
                // TODO: transition animations, maybe triggered transitions instead?
                if (state.getAnimatable().isStunned()) {
                    state.setAnimation(STUN_BACK);
                } else if (state.isMoving()) {
                    SCGExtra.LOGGER.debug("walk back");
                    state.setAnimation(WALK_BACK);
                } else {
                    state.setAnimation(IDLE_BACK);
                }
            } else {
                if (state.getAnimatable().isStunned()) {
                    state.setAnimation(STUN);
                } else if (state.isMoving()) {
                    state.setAnimation(WALK);
                } else {
                    state.setAnimation(IDLE);
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
    }

    public boolean isTurnedAround() {
        return this.entityData.get(TURNED_AROUND);
    }

    private void setTurnedAround(boolean turnedAround) {
        this.entityData.set(TURNED_AROUND, turnedAround);
    }

    // privated for internal use only, use memory accessor otherwise
    private boolean isStunned() {
        return this.entityData.get(STUNNED);
    }

    private void setStunned(boolean stunned) {
        this.entityData.set(STUNNED, stunned);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TURNED_AROUND, false);
        this.entityData.define(STUNNED, false);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }
}
