package net.zincstudios.scgextra.entity.whaler.lion_fish;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.sounds.WhalerSounds;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fluids.FluidType;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LionFishEntity extends EquippedEntity implements GeoEntity{

    static final int MELEE_DAMAGE_DELAY = 15;
    static final int MELEE_DURATION = 22;

    static final int POISON_DURATION = 80;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation SPLASH = RawAnimation.begin().thenLoop("splash");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public static final EntityDataAccessor<Boolean> IS_POISON = SynchedEntityData.defineId(LionFishEntity.class, EntityDataSerializers.BOOLEAN);

    private final PartEntity<?>[] subEntities;

    public LionFishEntity(EntityType<? extends Monster> entity, Level level) {
        super(entity, level);
        this.subEntities = new PartEntity[] {
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 41/16f, -60/16f * -1f), 26/16f, 24/16f),
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 31/16f, 22/16f * -1f), 14/16f, 16/16f),
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 38/16f, 36/16f * -1f), 14/16f, 16/16f),
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 49/16f, 51/16f * -1f), 8/16f, 12/16f),
            new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 60/16f, 58/16f * -1f), 8/16f, 12/16f),
        };
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if(state.getAnimatable().isPoison()){
                state.setAnimation(SPLASH);
            }else if (state.isMoving()) {
                state.setAnimation(state.getAnimatable().isSprinting() ? RUN : WALK);
            } 
            else {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> PlayState.STOP).triggerableAnim("attack", ATTACK));

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

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 35.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.45F)
        .add(Attributes.ATTACK_DAMAGE, 16.0D)
        .add(Attributes.ARMOR, 4.0D)
        .add(Attributes.MAX_HEALTH, 240.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return LionFishAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<LionFishEntity> getBrain() {
        return (Brain<LionFishEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<LionFishEntity> brainProvider() {
        return LionFishAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("facBluecoatBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        BrainCommons.updateMaxRangeAggressive(this);
        this.setYBodyRot(this.getYHeadRot());
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        this.tickSubEntities();
        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.triggerAnim("behavior", "attack");
        }
        if (!this.level().isClientSide){
            if(brain.hasMemoryValue(ModBrainMemories.ATTACK_RUN_FOR.get()) && brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)){
                this.setPoison(true);
            }else{
                this.setPoison(false);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_POISON, false);
    }

    public boolean isPoison(){
        return this.entityData.get(IS_POISON);
    }

    public void setPoison(boolean bool){
        this.entityData.set(IS_POISON, bool);
    }
    
    protected SoundEvent getAmbientSound() {
        return WhalerSounds.LION_FISH_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WhalerSounds.LION_FISH_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return WhalerSounds.LION_FISH_DEATH.get();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        this.playSound(WhalerSounds.LION_FISH_ATTACK.get());
        return super.doHurtTarget(entity);
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
}
