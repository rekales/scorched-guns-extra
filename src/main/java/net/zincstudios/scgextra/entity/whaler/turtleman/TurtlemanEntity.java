package net.zincstudios.scgextra.entity.whaler.turtleman;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;

import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.*;
import net.zincstudios.scgextra.entity.common.part.RotatedBulletProofPartEntity;
import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
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

    private static final int TURN_TICKS = 20;  // match with BACK_FRONT and FRONT_BACK anims

    private static final EntityDataAccessor<Boolean> TURN_TARGET =  // true: turned back
            SynchedEntityData.defineId(TurtlemanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STUNNED =
            SynchedEntityData.defineId(TurtlemanEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_BACK = RawAnimation.begin().thenLoop("idle_back");
    private static final RawAnimation WALK_BACK = RawAnimation.begin().thenLoop("walk_back");
    private static final RawAnimation FRONT_BACK = RawAnimation.begin().thenPlayAndHold("front_back");
    private static final RawAnimation BACK_FRONT = RawAnimation.begin().thenPlayAndHold("back_front");
    private static final RawAnimation STUN = RawAnimation.begin().thenLoop("stun");
    private static final RawAnimation STUN_BACK = RawAnimation.begin().thenLoop("stun");
    @SuppressWarnings("unused")  // TODO: later
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final RotatedBulletProofPartEntity<?>[] subEntities;

    // hopefully synced with similar logic on both sides at #tick
    private boolean turnTo = false;
    private int turnTick = 0;

    public TurtlemanEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.subEntities = new RotatedBulletProofPartEntity[] {
                new RotatedBulletProofPartEntity<>(this, new Vec3(0.3, 0.75, -0.5), 0.6f, 1.5f),
                new RotatedBulletProofPartEntity<>(this, new Vec3(-0.3, 0.75, -0.5), 0.6f, 1.5f),
        };
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

        // TODO: adjust look direction towards the player when moving
        LivingEntity target = this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null) {
            if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
                this.setTurnTarget(false);
                this.setAggressive(true);
                this.setYBodyRot(this.getYHeadRot());
            } else {
                this.setTurnTarget(true);
                this.setAggressive(false);
            }
        } else {
            this.setTurnTarget(false);
            this.setAggressive(false);
        }

        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
            this.setYBodyRot(this.getYHeadRot());
        }
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public RotatedSegmentPartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) // Fix MC-158205: See EnderDragon class
            this.subEntities[i].setId(id + i + 1);
    }

    @Override
    public void tick() {
        super.tick();
        this.tickSubEntities();

        if (this.turnTick == 0) {
            if (this.turnTo != this.getTurnTarget()) {
                this.turnTo = this.getTurnTarget();
                this.turnTick++;
            }
        } else if (this.turnTick++ >= TURN_TICKS) {
            this.turnTick = 0;
        }

        if (!this.level().isClientSide) {
            this.setStunned(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get()));
        }
    }

    protected void tickSubEntities() {
        float rotOffset;
        if (this.turnTick != 0) {
            float rot = this.turnTo ? this.turnTick : TURN_TICKS - this.turnTick;
            rotOffset = Mth.map(rot, 0F, TURN_TICKS, -20, -180);
        } else {
            rotOffset = this.turnTo ? -180 : -20;
        }

        for(RotatedSegmentPartEntity<?> partEntity : this.getParts()) {
            partEntity.setOldPosAndRot();
            partEntity.setPos(this.position().add(partEntity.getOffset().yRot((rotOffset-this.yBodyRot) * Mth.DEG_TO_RAD)));
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

    // TODO: find a way to allow transition tick time to be 3 without jitters
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
//            AnimationController<?> ctr = state.getController();
//            RawAnimation anim = ctr.getCurrentRawAnimation();

            if (state.getAnimatable().turnTick != 0) {
//                state.getController().transitionLength(0);
                state.setAnimation(state.getAnimatable().turnTo ? FRONT_BACK : BACK_FRONT);
            } else {
//                state.getController().transitionLength(anim == FRONT_BACK || anim == BACK_FRONT ? 0 : 3);
                if (state.getAnimatable().isTurnedAround()) {
                    if (state.getAnimatable().isStunned()) {
                        state.setAnimation(STUN_BACK);
                    } else if (state.isMoving()) {
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
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "death", 6, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));
    }

    public boolean isTurnedAround() {
        return this.turnTo && this.turnTick == 0;
    }

    private void setTurnTarget(boolean turnTarget) {
        this.entityData.set(TURN_TARGET, turnTarget);
    }

    private boolean getTurnTarget() {
        return this.entityData.get(TURN_TARGET);
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
        this.entityData.define(TURN_TARGET, false);
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

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 30);
    }
}
