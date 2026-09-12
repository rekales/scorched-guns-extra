package net.zincstudios.scgextra.entity.cog.trenchbleeder;

import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

public class CogTrenchbleederEntity extends Monster implements GeoEntity, CustomGunHolder, Gunner, BulletSpawnOffset{
    
    private static final Vec3 GUN_OFFSET = new Vec3(2.5/16f, 31.7715/16f, -17.05/16f);

    static final int MELEE_DAMAGE_DELAY = 10;
    static final int MELEE_DURATION = 15;

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);
    private final SimulatedGun customGun;

    public CogTrenchbleederEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.HOWLER.get().getGun())
        .fireRate(80)
        .burstAmount(3)
        .burstInterval(8)
        .maxRange(6)
        .idealRange(5)
        .ammoCapacity(3)
        .reloadTime(80)
        .build();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim == 20).isPresent()) {
            this.triggerAnim("main", "shoot");
        }
        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.triggerAnim("attack", "stomp");
        }
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CogTrenchbleederAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<CogTrenchbleederEntity> getBrain() {
        return (Brain<CogTrenchbleederEntity>) super.getBrain();
    }

    protected Brain.Provider<CogTrenchbleederEntity> brainProvider() {
        return Brain.provider(CogTrenchbleederAi.MEMORY_TYPES, CogTrenchbleederAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("cogTrenchbleederBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        CogTrenchbleederAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
            if (state.getAnimatable().isSprinting()) {
                state.setAnimation(RawAnimation.begin().thenLoop("run"));
            } else if (state.isMoving()) {
                state.setAnimation(RawAnimation.begin().thenLoop("walk"));
            } else if (state.getAnimatable().isAggressive()) {
                state.setAnimation(RawAnimation.begin().thenPlayAndHold("idle_1"));
            } else {
                state.setAndContinue(RawAnimation.begin()
                    .thenPlayXTimes("idle_1", state.getAnimatable().random.nextIntBetweenInclusive(2,4))
                        .thenLoop("idle_2"));
            }
            return PlayState.CONTINUE;
        }).triggerableAnim("shoot", RawAnimation.begin().thenPlay("shoot"))
        );

        controllers.add(new AnimationController<>(this, "attack", 0, state ->{
            return PlayState.CONTINUE;
        }).triggerableAnim("stomp", RawAnimation.begin().thenPlay("stomp")));

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
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return GUN_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
    }
}
