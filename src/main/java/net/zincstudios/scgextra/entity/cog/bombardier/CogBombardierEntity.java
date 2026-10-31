package net.zincstudios.scgextra.entity.cog.bombardier;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CogBombardierEntity extends Monster implements GeoEntity, CustomGunHolder, Gunner {

    static final int STUN_RECOVERY_TICKS = 12;
    static final int ALERT_ANIM_TICKS = 38;

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);
    private final SimulatedGun customGun;

    public CogBombardierEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.ROCKET_RIFLE.get().getGun())
                .projectileDamage(10)
                .fireRate(10)
                .maxRange(50)
                .idealRange(48)
                .ammoCapacity(6)
                .reloadTime(120)
                .reloads()
                .noGunFlash()
                .velocityModifier(vec -> new Vec3(vec.x/4, (vec.y/4) * 1.2 + 0.1, vec.z/4))
                .build();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.MAX_HEALTH, 40.0D);
    }

    @Override
    public void tick() {
        super.tick();

        Brain<?> brain = this.getBrain();
        if (brain.getTimeUntilExpiry(ModBrainMemories.TO_ALERT.get()) == ALERT_ANIM_TICKS) {
            this.triggerAnim("behavior", "alert");
            this.playSound(COGSounds.COG_BOMBARDIER_SCAN.get(), 1.2f, 1.0f);
        }
        if (brain.getTimeUntilExpiry(ModBrainMemories.STUNNED.get()) == STUN_RECOVERY_TICKS) {
            this.triggerAnim("behavior", "end_stun");
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this) {
            return super.hurt(source, amount * 0.2f);
        }
        return super.hurt(source, amount);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CogBombardierAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<CogBombardierEntity> getBrain() {
        return (Brain<CogBombardierEntity>) super.getBrain();
    }

    protected Brain.Provider<CogBombardierEntity> brainProvider() {
        return Brain.provider(CogBombardierAi.MEMORY_TYPES, CogBombardierAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("cogBombardierBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        CogBombardierAi.updateActivity(this);
        this.setAggressive(this.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.customGun.getAmmoCount() > 0);
        if (this.isAggressive()) {
            this.setYBodyRot(this.getYHeadRot());
        }
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
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
                })
        );

        controllers.add(new AnimationController<>(this, "gun", 0, state -> PlayState.STOP)
                .triggerableAnim("fire", RawAnimation.begin().thenPlay("fire"))
        );

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> PlayState.STOP)
                .triggerableAnim("stun", RawAnimation.begin().thenPlayAndHold("stun_start"))
                .triggerableAnim("end_stun", RawAnimation.begin().thenPlay("stun_end"))
                .triggerableAnim("alert", RawAnimation.begin().thenPlay("alert"))
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
        this.triggerAnim("gun", "fire");
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return COGSounds.COG_DEVASTATOR_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return COGSounds.GENERAL_LIGHT_HURT.get();
    }

    protected SoundEvent getStepSound() {
        return COGSounds.COG_BOMBARDIER_WALK.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }
}
