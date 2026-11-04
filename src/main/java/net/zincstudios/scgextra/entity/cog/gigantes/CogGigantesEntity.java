package net.zincstudios.scgextra.entity.cog.gigantes;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.FlyDirectMoveControl;
import net.zincstudios.scgextra.entity.common.gun.*;
import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.sounds.COGSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CogGigantesEntity extends FlyingMob implements GeoEntity, Enemy, BulletSpawnOffset, CustomGunHolder {

    public static final Vec3 MACHINE_GUN_OFFSET = new Vec3(0, 0.3, 0);

    static final int STUN_RECOVERY_TICKS = 16;
    static final int STUN_DURATION = 80;

    private static final EntityDataAccessor<Boolean> FIRING =
            SynchedEntityData.defineId(CogGigantesEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);
    private final SimulatedGun customGun;
    private final PartEntity<?>[] subEntities;
    private String lastHitByEntityUUID = "";

    public CogGigantesEntity(EntityType<? extends FlyingMob> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.moveControl = new FlyDirectMoveControl(this, 0.1, 0.75);
        this.subEntities = new PartEntity[] {
                new RotatedSegmentPartEntity<>(this, new Vec3(1.5, 1.4, -0.5), 20/16f, 0.5f),
                new RotatedSegmentPartEntity<>(this, new Vec3(-1.5, 1.4, -0.5), 20/16f, 0.5f)
        };
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.PRUSH_GUN.get().getGun())
                .burstAmount(16)
                .burstInterval(2)
                .fireRate(70)
                .maxRange(32)
                .idealRange(16)
                .build();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.FLYING_SPEED, 2F)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.MAX_HEALTH, 250.0D);
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

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CogGigantesAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<CogGigantesEntity> getBrain() {
        return (Brain<CogGigantesEntity>) super.getBrain();
    }

    protected Brain.Provider<CogGigantesEntity> brainProvider() {
        return CogGigantesAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("cogGigantesBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        CogGigantesAi.updateActivity(this);
        BrainCommons.updateHasTargetAggressive(this);
        this.setSprinting(this.isAggressive());
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        this.tickSubEntities();

        if (brain.getTimeUntilExpiry(ModBrainMemories.STUNNED.get()) == STUN_DURATION) {
            this.triggerAnim("behavior", "stun");
        }
        if (brain.getTimeUntilExpiry(ModBrainMemories.STUNNED.get()) == STUN_RECOVERY_TICKS) {
            this.triggerAnim("behavior", "end_stun");
        }

        this.setYBodyRot(this.getYHeadRot());
        this.setYRot(this.getYHeadRot());
    }

    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, state -> {
            if (state.getAnimatable().isFiring()) {
                state.setAnimation(RawAnimation.begin().thenLoop("attack"));
            } else if (state.isMoving()) {
                state.setAnimation(RawAnimation.begin().thenLoop("move"));
            } else {
                state.setAnimation(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> PlayState.STOP)
                .triggerableAnim("stun", RawAnimation.begin().thenPlayAndHold("stun_start"))
                .triggerableAnim("end_stun", RawAnimation.begin().thenPlay("stun_end"))
        );

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("stun_start"));
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FIRING, false);
    }

    public void setFiring(boolean firing) {
        this.entityData.set(FIRING, firing);
    }

    public boolean isFiring() {
        return this.entityData.get(FIRING);
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return MACHINE_GUN_OFFSET.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return COGSounds.COG_GIGANTES_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return COGSounds.GENERAL_HEAVY_HURT.get();
    }

    protected SoundEvent getStepSound() {
        return COGSounds.COG_GIGANTES_FLY.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide) {
            if(source.getDirectEntity() != null && !(source.getDirectEntity() instanceof LivingEntity)){
                if(Objects.equals(this.lastHitByEntityUUID, source.getDirectEntity().getStringUUID())) return false;
                this.lastHitByEntityUUID = source.getDirectEntity().getStringUUID();
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public SimulatedGun getCustomGun() {
        return this.customGun;
    }
}
