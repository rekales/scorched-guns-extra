package net.zincstudios.scgextra.entity.cog.centipede;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.MobUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.entity.common.part.RotatedWeakPointPartEntity;
import net.zincstudios.scgextra.sounds.COGSounds;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CogCentipedeEntity extends Monster implements GeoEntity, CustomGunHolder, Gunner {

    static final int STUN_RECOVERY_TICKS = 12;
    static final int STUN_DURATION = 80;
    static final int SLAM_DAMAGE_DELAY = 18;
    static final int SLAM_DURATION = 25;

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);
    private final SimulatedGun customGun;
    private final PartEntity<?>[] subEntities;
    private String lastHitByEntityUUID = "";

    public CogCentipedeEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.subEntities = new PartEntity[] {
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 0, 1.5), 24/16f, 24/16f),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 0, -28/16f), 28/16f, 30/16f),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 0, -28/16f - 24/16f), 22/16f, 24/16f),
                new RotatedWeakPointPartEntity<>(this, new Vec3(0, 11/16f, 2.2), 9/16f, 9/16f)
        };
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.LIBERTAS.get().getGun())
                .projectileDamage(15)
                .fireRate(80)
                .maxRange(10)
                .idealRange(8)
                .velocityModifier(vec -> vec.scale(1/3f))
                .projectileFactory(PlasmaCannonProjectileEntity::new)
                .build();
        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); 
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.MAX_HEALTH, 250.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CogCentipedeAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<CogCentipedeEntity> getBrain() {
        return (Brain<CogCentipedeEntity>) super.getBrain();
    }

    protected Brain.Provider<CogCentipedeEntity> brainProvider() {
        return Brain.provider(CogCentipedeAi.MEMORY_TYPES, CogCentipedeAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("cogCentipedeBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        CogCentipedeAi.updateActivity(this);
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

        if (brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == SLAM_DURATION) {
            this.triggerAnim("behavior", "slam");
        }
        if (brain.getTimeUntilExpiry(ModBrainMemories.STUNNED.get()) == STUN_DURATION) {
            this.triggerAnim("behavior", "stun");
        }
        if (brain.getTimeUntilExpiry(ModBrainMemories.STUNNED.get()) == STUN_RECOVERY_TICKS) {
            this.triggerAnim("behavior", "end_stun");
        }
    }

    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (state.isMoving()) {
                state.setAnimation(RawAnimation.begin().thenLoop("walk"));
            } else {
                state.setAnimation(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "gun", 2, state -> PlayState.STOP)
                .triggerableAnim("fire", RawAnimation.begin().thenPlay("fire"))
        );

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> PlayState.STOP)
                .triggerableAnim("stun", RawAnimation.begin().thenPlayAndHold("stun_start"))
                .triggerableAnim("end_stun", RawAnimation.begin().thenPlay("stun_end"))
                .triggerableAnim("slam", RawAnimation.begin().thenPlay("slam"))
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
    protected void tickDeath() {
        MobUtil.tickDeath(this, 42);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return COGSounds.COG_CENTIPEDE_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return COGSounds.GENERAL_HEAVY_HURT.get();
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
