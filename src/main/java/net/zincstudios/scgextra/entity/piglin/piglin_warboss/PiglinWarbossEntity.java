package net.zincstudios.scgextra.entity.piglin.piglin_warboss;

import net.zincstudios.scgextra.entity.common.gun.MountedGun;
import net.zincstudios.scgextra.entity.common.gun.AttachedMountedGun;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.piglin.StunnableHog;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.client.ExpandedAnimationController;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.MobUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

public class PiglinWarbossEntity extends Monster implements GeoEntity, Gunner, BulletSpawnOffset {

    public final Vec3 BarrelOffestRight = new Vec3(17.5f/16f, 29f/16f, -54f/16f * -1);
    public final Vec3 BarrelOffestLeft = new Vec3(8.5f/16f, 30.5f/16f, -54f/16f * -1);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public final MountedGun BarrelRight;
    public final MountedGun BarrelLeft;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SHOOT_WALKING = RawAnimation.begin().thenPlay("ranged_attack");
    private static final RawAnimation SHOOT_STANDING = RawAnimation.begin().thenPlay("ranged_attack_stand");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation CHARGE_START = RawAnimation.begin().thenPlay("charge_start");
    private static final RawAnimation CHARGE_MIDDLE = RawAnimation.begin().thenPlay("charge_middle");
    private static final RawAnimation CHARGE_END = RawAnimation.begin().thenPlay("charge_end");
    private static final RawAnimation STUN_START = RawAnimation.begin().thenPlay("stun_start");
    private static final RawAnimation STUN_MIDDLE = RawAnimation.begin().thenPlay("stun_middle");
    private static final RawAnimation STUN_END = RawAnimation.begin().thenPlay("stun_end");
    private static final RawAnimation SCREAM = RawAnimation.begin().thenPlay("scream");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    static int MELEE_DELAY = 20;
    static int MELEE_DURATION = 40;

    public static final StunnableHog instance = new StunnableHog(
        20, 
        25, 
        25
    );

    private static final EntityDataAccessor<Long> TICKS_REMAINING = SynchedEntityData.defineId(PiglinWarbossEntity.class, EntityDataSerializers.LONG);

    public PiglinWarbossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.BarrelRight = new AttachedMountedGun(this,
            new CustomScorchedSimGun.Builder(ModItems.CALLWELL_CONVERSION.get().getGun())
                .projectileDamage(3f)
                .fireRate(1)
                .maxRange(20)
                .idealRange(16)
                .ammoCapacity(6)
                .reloadTime(120)
                .noGunFlash()
                .gunIndex(0)
                .build(),
            BarrelOffestRight,
            (a) -> true
        );
        this.BarrelLeft = new AttachedMountedGun(this,
            new CustomScorchedSimGun.Builder(ModItems.CALLWELL_CONVERSION.get().getGun())
                .projectileDamage(3f)
                .fireRate(1)
                .maxRange(20)
                .idealRange(16)
                .ammoCapacity(6)
                .reloadTime(120)
                .noGunFlash()
                .gunIndex(1)
                .build(),
            BarrelOffestLeft,
            (a) -> true
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 128.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.3F)
        .add(Attributes.ATTACK_DAMAGE, 35.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
        .add(Attributes.ARMOR, 6.0D)
        .add(Attributes.MAX_HEALTH, 600.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if(state.getAnimatable().isStunned()){
                if(PiglinWarbossEntity.instance.isStart(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_START);
                }else if(PiglinWarbossEntity.instance.isMiddle(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_MIDDLE);
                }else if(PiglinWarbossEntity.instance.isEnd(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_END);
                }
            }else if(!state.isCurrentAnimation(SCREAM)){
                if (state.isMoving()) {
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

        controllers.add(new ExpandedAnimationController<>(this, "behavior", 0, state -> {
            return PlayState.CONTINUE;
        })
            .triggerableAnim("melee", ATTACK)
            .triggerableAnim("scream", SCREAM)
            .triggerableAnim("shoot", (ctr)->{
                if (((this.getX() - this.xo)*(this.getX() - this.xo))+((this.getZ() - this.zo)*(this.getZ() - this.zo))>0.0002) {
                    return SHOOT_WALKING;
                } else {
                    return SHOOT_STANDING;
                }
            })
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return switch (gunIndex) {
            case 0 -> this.BarrelOffestRight.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
            case 1 -> this.BarrelOffestLeft.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
            default -> Vec3.ZERO;
        };
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 40);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinWarbossAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PiglinWarbossEntity> getBrain() {
        return (Brain<PiglinWarbossEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PiglinWarbossEntity> brainProvider() {
        return PiglinWarbossAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinBlazeKopterBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        PiglinWarbossAi.updateActivity(this);
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
                this.setTicksRemaining(this.getBrain().getTimeUntilExpiry(ModBrainMemories.STUNNED.get()));
            }else{
                this.setTicksRemaining(Long.valueOf(-1));
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TICKS_REMAINING, Long.valueOf(0));
    }

    public boolean isStunned(){
        return this.entityData.get(TICKS_REMAINING) > 0;
    }

    public long getTicksRemaining(){
        return this.entityData.get(TICKS_REMAINING);
    }

    public void setTicksRemaining(long l){
        this.entityData.set(TICKS_REMAINING, l);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get()))amount*=2;
        return super.hurt(source, amount);
    }
}
