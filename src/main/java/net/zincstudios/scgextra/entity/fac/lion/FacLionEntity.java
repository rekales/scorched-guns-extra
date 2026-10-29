package net.zincstudios.scgextra.entity.fac.lion;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.part.RotatedBulletProofPartEntity;
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
public class FacLionEntity extends EquippedEntity implements GeoEntity, Gunner {

    private static final Vec3 LEFT_SHIELD_DOWN = new Vec3(0.7, 0.2, 0.9);
    private static final Vec3 RIGHT_SHIELD_DOWN = new Vec3(0.7 + 0.7f, 0.2, 0.9);
    private static final Vec3 LEFT_SHIELD_UP = new Vec3(-0.35, 0.4, 1.3);
    private static final Vec3 RIGHT_SHIELD_UP = new Vec3(-0.35 + 0.7f, 0.4, 1.3);

    private static final EntityDataAccessor<Boolean> SHIELD_UP =
            SynchedEntityData.defineId(FacLionEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation AIM = RawAnimation.begin().thenLoop("aim");
    private static final RawAnimation IDLE_SHIELD = RawAnimation.begin().thenLoop("idle_shield");
    private static final RawAnimation WALK_SHIELD = RawAnimation.begin().thenLoop("walk_shield");
    private static final RawAnimation SHIELD_BASH = RawAnimation.begin().thenPlay("shield_bash");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final RotatedBulletProofPartEntity<?>[] subEntities;

    public FacLionEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.subEntities = new RotatedBulletProofPartEntity[] {
                new RotatedBulletProofPartEntity<>(this, LEFT_SHIELD_DOWN, 0.7f, 2f),
                new RotatedBulletProofPartEntity<>(this, RIGHT_SHIELD_DOWN, 0.7f, 2f),
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.17F)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.MAX_HEALTH, 200.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return FacLionAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<FacLionEntity> getBrain() {
        return (Brain<FacLionEntity>) super.getBrain();
    }

    protected Brain.Provider<FacLionEntity> brainProvider() {
        return FacLionAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("facLionBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);

        LivingEntity target = this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target != null) {
            if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
                this.setShieldUp(false);
                this.setAggressive(true);
                this.setYBodyRot(this.getYHeadRot());
            } else {
                this.setShieldUp(true);
                this.setAggressive(false);
            }
        } else {
            this.setShieldUp(false);
            this.setAggressive(false);
        }

        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public PartEntity<?>[] getParts() {
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
    }

    protected void tickSubEntities() {
        if (this.isShieldUp()) {
            ((RotatedBulletProofPartEntity<?>)this.getParts()[0]).setOffset(LEFT_SHIELD_UP);
            ((RotatedBulletProofPartEntity<?>)this.getParts()[1]).setOffset(RIGHT_SHIELD_UP);
        } else {
            ((RotatedBulletProofPartEntity<?>)this.getParts()[0]).setOffset(LEFT_SHIELD_DOWN);
            ((RotatedBulletProofPartEntity<?>)this.getParts()[1]).setOffset(RIGHT_SHIELD_DOWN);
        }
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }

    @Override
    public boolean doHurtTarget(Entity other) {
        boolean res = super.doHurtTarget(other);
        if (res && other instanceof LivingEntity target) {
            double dx = this.getX() - target.getX();
            double dz = this.getZ() - target.getZ();
            target.knockback(1, dx, dz);
        }
        return res;
    }

    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        this.triggerAnim("main", "melee");
        this.setYBodyRot(this.getYHeadRot());
    }

    public double getMeleeAttackRangeSqr(LivingEntity target) {
        return this.getBbWidth() * this.getBbWidth() * 1.4F * 1.4F + target.getBbWidth();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (state.isMoving() || state.getAnimatable().isActuallyMoving()) {
                return state.setAndContinue(state.getAnimatable().isShieldUp() ? WALK_SHIELD : WALK);
            } else if (state.getAnimatable().isAggressive()) {
                return state.setAndContinue(AIM);
            } else {
                return state.setAndContinue(state.getAnimatable().isShieldUp() ? IDLE_SHIELD : IDLE);
            }
        })
                .triggerableAnim("melee", SHIELD_BASH)
                .setAnimationSpeed(1.2));

        controllers.add(new AnimationController<>(this, "death", 3, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));
    }

    // needed because this thing moves way too fucking slow for state.isMoving() to be true
    private boolean isActuallyMoving() {
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        return dx * dx + dz * dz > 0.000001D;
    }

    public boolean isShieldUp() {
        return this.entityData.get(SHIELD_UP);
    }

    private void setShieldUp(boolean shieldUp) {
        this.entityData.set(SHIELD_UP, shieldUp);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHIELD_UP, false);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return FACSounds.FAC_LION_HURT.get();
    }

    protected SoundEvent getAmbientSound() {
        return FACSounds.FAC_LION_IDLE.get();
    }

    protected SoundEvent getDeathSound() {
        return FACSounds.FAC_LION_DEATH.get();
    }
}
