package net.zincstudios.scgextra.entity.whaler.salmonsaur;

import com.mojang.serialization.Dynamic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.entity.common.part.RotatedWeakPointPartEntity;
import net.zincstudios.scgextra.sounds.WhalerSounds;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidType;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
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
public class SalmonsaurEntity extends EquippedEntity implements GeoEntity {

    static final int MELEE_DAMAGE_DELAY = 12;
    static final int MELEE_DURATION = 21;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation MELEE = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final PartEntity<?>[] subEntities;
    private String lastHitByEntityUUID = "";

    public SalmonsaurEntity(EntityType<? extends SalmonsaurEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.subEntities = new PartEntity[] {
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 0, 0), 1.85f, 2.4f),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 1.75, -1.3), 0.8f, 0.8f),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 1.75, -2.1), 0.8f, 0.8f),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 1.85, -2.85), 0.7f, 0.7f),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 1.85, -3.55), 0.7f, 0.7f),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 1.1, 1.1), 0.85f, 2f),
                new RotatedWeakPointPartEntity<>(this, new Vec3(0, 2, 1.8), 0.9f, 0.9f),
                new RotatedWeakPointPartEntity<>(this, new Vec3(0, 1.9, 2.7), 0.9f, 0.9f),
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.26F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6F)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 4)
                .add(Attributes.MAX_HEALTH, 80.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return SalmonsaurAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<SalmonsaurEntity> getBrain() {
        return (Brain<SalmonsaurEntity>) super.getBrain();
    }

    protected Brain.Provider<SalmonsaurEntity> brainProvider() {
        return SalmonsaurAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("whalerSalmonsaurBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        BrainCommons.updateHasTargetAggressive(this);
        this.setSprinting(this.isAggressive());
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

        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            // TODO: sounds
            this.triggerAnim("main", "melee");
        }
    }

    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, state -> {
            if (state.isMoving()) {
                state.setControllerSpeed(1.2f);
                state.setAnimation(state.getAnimatable().isSprinting() ? RUN : WALK);
            } else {
                state.setControllerSpeed(1f);
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }).triggerableAnim("melee", MELEE));

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

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 30);
    }
    
    protected SoundEvent getAmbientSound() {
        return WhalerSounds.SALMONSAUR_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WhalerSounds.SALMONSAUR_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return WhalerSounds.SALMONSAUR_DEATH.get();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        this.playSound(WhalerSounds.SALMONSAUR_ATTACK.get());
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(source.getDirectEntity()!=null && !(source.getDirectEntity() instanceof LivingEntity)){
            if(!this.level().isClientSide()){
                if(this.lastHitByEntityUUID == source.getDirectEntity().getStringUUID())return false;
                this.lastHitByEntityUUID = source.getDirectEntity().getStringUUID();
            }
        }
        return super.hurt(source, amount);
    }
}
