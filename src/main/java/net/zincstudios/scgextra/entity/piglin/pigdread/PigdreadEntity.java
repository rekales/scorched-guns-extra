package net.zincstudios.scgextra.entity.piglin.pigdread;

import net.zincstudios.scgextra.entity.common.gun.AttachedMountedGun;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.MountedGun;
import net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter.PiglinBlazeKopterProjectile;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

public class PigdreadEntity extends Monster implements GeoEntity, Gunner, BulletSpawnOffset{

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("attack_ranged");
    private static final RawAnimation STUN = RawAnimation.begin().thenPlay("stun_full");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private static Vec3 offset = new Vec3(-38/16, 33/16, -39/16*-1);

    public final MountedGun mountedShotGun;

    public static int STUN_DURATION = 100;

    static int MELEE_DELAY = 18;
    static int MELEE_DURATION = 30;

    private boolean stunAnimPlaying = false;

    public PigdreadEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.mountedShotGun = new AttachedMountedGun(
            this, 
            new CustomScorchedSimGun.Builder(ModItems.DRILL_CONVERSION.get().getGun())
                .projectileDamage(3f)
                .fireRate(1)
                .maxRange(20)
                .idealRange(16)
                .noGunFlash()
                .burstAmount(2)
                .burstInterval(1)
                .gunIndex(0)
                .projectileFactory(PiglinBlazeKopterProjectile::new)
                .build(), 
            offset, 
            (a) -> true
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 128.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.25F)
        .add(Attributes.ATTACK_DAMAGE, 35.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
        .add(Attributes.ARMOR, 8.0D)
        .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 2.0D)
        .add(Attributes.MAX_HEALTH, 600.0D);
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
        }));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> {
            return PlayState.CONTINUE;
        })
            .triggerableAnim("melee", ATTACK)
            .triggerableAnim("shoot", SHOOT)
            .triggerableAnim("stun", STUN)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return offset.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PigdreadAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PigdreadEntity> getBrain() {
        return (Brain<PigdreadEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PigdreadEntity> brainProvider() {
        return PigdreadAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinBlazeKopterBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        PigdreadAi.updateActivity(this);
        BrainCommons.updateMaxRangeAggressive(this);
        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
            this.setYBodyRot(this.getYHeadRot());
        }
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide()){
            if (brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
                this.triggerAnim("behavior", "melee");
            }
            if(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get())){
                if(!this.stunAnimPlaying){
                    this.stunAnimPlaying = true;
                    this.triggerAnim("behavior", "stun");
                }
            }else{
                this.stunAnimPlaying = false;
            }
        }
    }
}
