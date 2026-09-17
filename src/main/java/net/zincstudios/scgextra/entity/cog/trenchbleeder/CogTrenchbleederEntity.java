package net.zincstudios.scgextra.entity.cog.trenchbleeder;

import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.sounds.COGSounds;
import net.zincstudios.scgextra.sounds.FACSounds;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
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
    private final PartEntity<?>[] subEntities;

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
        //some offsets are multiplied by -1 because blockbench xyz for some reason are opposites of this xyz
        this.subEntities = new PartEntity[] {
            //leg_part_1
            new RotatedSegmentPartEntity<>(
                this, 
                new Vec3(
                    (11/16f)*-1f,
                    0/16f, 
                    (-16/16f)*-1f
                ),
                5/16f, 
                25/16f, 
                false
            ),
            //leg_part_2
            new RotatedSegmentPartEntity<>(
                this, 
                new Vec3(
                    (-15/16f)*-1f,
                    0/16f, 
                    (-15/16f)*-1f
                ),
                5/16f, 
                25/16f, 
                false
            ),
            //leg_part_3
            new RotatedSegmentPartEntity<>(
                this, 
                new Vec3(
                    (-3/16f)*-1f,
                    0/16f,
                    (12.4/16f)*-1f
                ),
                6/16f, 
                25/16f, 
                false
            )
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D);
    }

    @Override
    public void tick() {
        super.tick();
        this.tickSubEntities();
        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim == 20).isPresent()) {
            this.triggerAnim("main", "shoot_start");
        }
        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim == 48).isPresent()) {
            this.triggerAnim("main", "shoot_end");
        }
        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.playSound(COGSounds.COG_TRENCHBLEEDER_STOMP.get());
            this.triggerAnim("main", "stomp");
        }
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
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            if (state.getAnimatable().isSprinting()) {
                state.setAnimation(RawAnimation.begin().thenLoop("run"));
            } else if (state.isMoving()) {
                state.setAnimation(RawAnimation.begin().thenLoop("walk"));
            } else if (state.getAnimatable().isAggressive()) {
                state.setAnimation(RawAnimation.begin().thenPlayAndHold("idle_1"));
            } else {
                state.setAndContinue(RawAnimation.begin()
                .thenPlayXTimes("idle_1", 2).thenLoop("idle_2"));
            }
            return PlayState.CONTINUE;
        }).triggerableAnim("stomp", RawAnimation.begin().thenPlay("stomp"))
        .triggerableAnim("shoot_start", RawAnimation.begin().thenPlay("shoot_start"))
        .triggerableAnim("fire", RawAnimation.begin().thenPlay("fire"))
        .triggerableAnim("shoot_end", RawAnimation.begin().thenPlay("shoot_end")));

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
        this.triggerAnim("main", "fire");
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(source.getEntity()!=null && source.getEntity().is(this)){
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return GUN_OFFSET.yRot(-this.yBodyRot * Mth.DEG_TO_RAD);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FACSounds.FAC_WALKER_IDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return COGSounds.COG_TRENCHBLEEDER_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return COGSounds.COG_TRENCHBLEEDER_HURT.get();
    }
    
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        SoundType soundtype = state.getSoundType(this.level(), pos, this);
        this.playSound(COGSounds.COG_TRENCHBLEEDER_STEP.get(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
    }
}
