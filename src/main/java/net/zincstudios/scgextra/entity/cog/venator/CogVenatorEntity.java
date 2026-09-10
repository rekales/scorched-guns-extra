package net.zincstudios.scgextra.entity.cog.venator;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
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

import static net.zincstudios.scgextra.entity.common.brain.AvoidTargetIfClose.AVOID_TARGET_TTL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CogVenatorEntity extends Monster implements GeoEntity, CustomGunHolder, Gunner, BulletSpawnOffset {

    private static final Vec3 GUN_OFFSET = new Vec3(0, 1.1, 2.1);

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);
    private final SimulatedGun customGun;
    private final PartEntity<?>[] subEntities;

    public CogVenatorEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.HOWLER.get().getGun())
                .projectileDamage(15)
                .fireRate(80)
                .maxRange(25)
                .idealRange(20)
                .build();
        this.subEntities = new PartEntity[] {
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 0.92, 0.7), 0.4f, 0.4f, false),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 1, 1.05), 0.25f, 0.25f, false),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 1, 1.3), 0.25f, 0.25f, false),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 1, 1.55), 0.25f, 0.25f, false),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 1, 1.8), 0.25f, 0.25f, false)
        };

        // because it's getting stuck somehow
        this.setDeltaMovement(this.getDeltaMovement().add(0.02, 0.1, 0.02));
        this.hasImpulse = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.MAX_HEALTH, 15.0D);
    }

    @Override
    public void tick() {
        super.tick();
        this.tickSubEntities();
        if (brain.getTimeUntilExpiry(MemoryModuleType.AVOID_TARGET) == AVOID_TARGET_TTL-5) {
            this.playSound(COGSounds.COG_VENATOR_RUN.get());
        }
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CogVenatorAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<CogVenatorEntity> getBrain() {
        return (Brain<CogVenatorEntity>) super.getBrain();
    }

    protected Brain.Provider<CogVenatorEntity> brainProvider() {
        return Brain.provider(CogVenatorAi.MEMORY_TYPES, CogVenatorAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("cogVenatorBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        CogVenatorAi.updateActivity(this);
        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
            this.setYBodyRot(this.getYHeadRot());
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

    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
            if (state.getAnimatable().isSprinting()) {
                state.setAnimation(RawAnimation.begin().thenLoop("run"));
            } else if (state.isMoving()) {
                state.setAnimation(RawAnimation.begin().thenLoop("walk"));
            } else if (state.getAnimatable().isAggressive()) {
                state.setAnimation(RawAnimation.begin().thenPlayAndHold("aim"));
            } else {
                state.setAnimation(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }).triggerableAnim("fire", RawAnimation.begin().thenPlay("fire"))
        );

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    public SimulatedGun getCustomGun() {
        return this.customGun;
    }

    @Override
    public void onGunFire(SimulatedGun gun, Vec3 targetPos) {
        if (this.getRandom().nextBoolean()) {
            this.playSound(this.getAttackSound());
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return COGSounds.GENERAL_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return COGSounds.GENERAL_LIGHT_HURT.get();
    }

    protected SoundEvent getAttackSound() {
        return COGSounds.COG_VENATOR_ATTACK.get();
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return GUN_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
    }
}
