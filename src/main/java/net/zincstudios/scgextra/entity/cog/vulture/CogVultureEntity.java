package net.zincstudios.scgextra.entity.cog.vulture;


import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
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
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.network.GunFlashMessage;
import net.zincstudios.scgextra.network.SCGEPacketHandler;
import net.zincstudios.scgextra.sounds.COGSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.ScorchedGuns;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.init.ModItems;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CogVultureEntity extends Monster implements GeoEntity, Gunner, BulletSpawnOffset, CustomGunHolder {

    private static final Vec3 LEFT_GUN_OFFSET = new Vec3(0.45,1.4,-0.4);
    private static final Vec3 RIGHT_GUN_OFFSET = new Vec3(-0.45,1.5,-0.4);

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);
    private final SimulatedGun customGun;
    private final PartEntity<?>[] subEntities;
    private String lastHitByEntityUUID = "";

    private boolean bulletSpawnLeft = false;

    public CogVultureEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.subEntities = new PartEntity[] {
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 0, -0.85), 0.8f, 1.9f),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 0, 0.65), 0.8f, 1f),
                new RotatedSegmentPartEntity<>(this, new Vec3(0, 0,  1.3), 0.8f, 1f)
        };
        this.customGun = new CustomScorchedSimGun.Builder(ModItems.VALORA.get().getGun())
                .projectileDamage(1.5f)
                .fireRate(2)
                .maxRange(10)
                .idealRange(8)
                .noGunFlash() // handled on onGunFire instead
                .velocityModifier(vec -> vec.scale(1/3f))
                .build();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.MAX_HEALTH, 15.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CogVultureAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<CogVultureEntity> getBrain() {
        return (Brain<CogVultureEntity>) super.getBrain();
    }

    protected Brain.Provider<CogVultureEntity> brainProvider() {
        return Brain.provider(CogVultureAi.MEMORY_TYPES, CogVultureAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("cogVultureBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
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

    @Override
    public void tick() {
        super.tick();
        this.tickSubEntities();
    }

    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this) return false;
        if(source.getDirectEntity()!=null && !(source.getDirectEntity() instanceof LivingEntity)){
            if(!this.level().isClientSide()){
                if(this.lastHitByEntityUUID == source.getDirectEntity().getStringUUID())return false;
                this.lastHitByEntityUUID = source.getDirectEntity().getStringUUID();
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state ->
                state.setAndContinue(RawAnimation.begin().thenLoop("idle"))));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
            }
            return PlayState.STOP;
        }).setAnimationSpeed(1.3f));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return COGSounds.GENERAL_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return COGSounds.GENERAL_LIGHT_HURT.get();
    }

    @Override
    public void onGunFire(SimulatedGun gun, Vec3 targetPos) {
        this.bulletSpawnLeft = !this.bulletSpawnLeft;

        Gun.Display.Flash flash = ModItems.VALORA.get().getGun().getDisplay().getFlash();
        if (flash == null) return;
        ResourceLocation flashTexture = ResourceLocation.fromNamespaceAndPath(ScorchedGuns.MODID,
                "textures/effect/" + flash.getTextureLocation() + ".png");
        SCGEPacketHandler.sendToNearbyPlayers(() -> MobUtil.levelLocationFromEntity(this),
                new GunFlashMessage(this.getId(), this.bulletSpawnLeft ? 0 : 1, flashTexture, false, 0.5F));
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        if (this.bulletSpawnLeft) {
            return CogVultureEntity.LEFT_GUN_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
        } else {
            return CogVultureEntity.RIGHT_GUN_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
        }
    }

    @Override
    public SimulatedGun getCustomGun() {
        return this.customGun;
    }
}
