package net.zincstudios.scgextra.entity.piglin.piglin_hog_rider;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.piglin.common.AggroableMob;
import net.zincstudios.scgextra.entity.piglin.common.StunnableHog;
import net.zincstudios.scgextra.sounds.PiglinSounds;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.PartEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModTags;

public class PiglinHogRiderEntity extends EquippedEntity implements GeoEntity, VariantHolder<Integer>, AggroableMob{

    private static final EntityDataAccessor<Integer> MODEL_VARIANT =
            SynchedEntityData.defineId(PiglinHogRiderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHARGE_PHASE =
            SynchedEntityData.defineId(PiglinHogRiderEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("melee");
    private static final RawAnimation AGGROED = RawAnimation.begin().thenPlay("aggroed");
    private static final RawAnimation CHARGE_START = RawAnimation.begin().thenPlay("charge_start");
    private static final RawAnimation CHARGE_MIDDLE = RawAnimation.begin().thenLoop("charge_middle");
    private static final RawAnimation CHARGE_END = RawAnimation.begin().thenPlay("charge_end");
    private static final RawAnimation STUN_START = RawAnimation.begin().thenPlay("stun_start");
    private static final RawAnimation STUN_MIDDLE = RawAnimation.begin().thenLoop("stun_middle");
    private static final RawAnimation STUN_END = RawAnimation.begin().thenPlay("stun_end");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    static final int MELEE_DAMAGE_DELAY = 13;
    static final int MELEE_DURATION = 20;

    static int CHARGE_START_DURATION = 35;
    static int CHARGE_MIDDLE_DURATION = 400;
    static int CHARGE_END_DURATION = 20;
    static int CHARGE_DURATION = CHARGE_START_DURATION+CHARGE_MIDDLE_DURATION+CHARGE_END_DURATION;

    private static final EntityDataAccessor<Long> CHARGE_TICKS = SynchedEntityData.defineId(PiglinHogRiderEntity.class, EntityDataSerializers.LONG);


    private boolean aggroed = false;
    public int aggroTicks = 0;

    public static final StunnableHog instance = new StunnableHog(
        10, 
        80, 
        20
    );

    private final PartEntity<?>[] subEntities;

    private static final EntityDataAccessor<Long> TICKS_REMAINING = SynchedEntityData.defineId(PiglinHogRiderEntity.class, EntityDataSerializers.LONG);

    public PiglinHogRiderEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.subEntities = new PartEntity[] {
           new RotatedSegmentPartEntity<>(this, new Vec3(0/16f, 12/16f, -30/16f * -1f), 22/16f, 22/16f)
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 128.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.31F)
        .add(Attributes.ATTACK_DAMAGE, 30.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
        .add(Attributes.ARMOR, 12.0D)
        .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 0.8D)
        .add(Attributes.MAX_HEALTH, 200.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 3, state -> {
            if(state.getAnimatable().isStunned()){
                if(PiglinHogRiderEntity.instance.isStart(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_START);
                }else if(PiglinHogRiderEntity.instance.isMiddle(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_MIDDLE);
                }else if(PiglinHogRiderEntity.instance.isEnd(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_END);
                }
            }else if (state.isCurrentAnimation(AGGROED)){}
            else if(state.getAnimatable().isCharging()){
                if(state.getAnimatable().isStart(state.getAnimatable().getChargeTicksRemaining())){
                    state.setAnimation(CHARGE_START);
                }else if(state.getAnimatable().isMiddle(state.getAnimatable().getChargeTicksRemaining())){
                    state.setAnimation(CHARGE_MIDDLE);
                }else if(state.getAnimatable().isEnd(state.getAnimatable().getChargeTicksRemaining())){
                    state.setAnimation(CHARGE_END);
                }
            }else if (state.isMoving()) {
                state.setAndContinue(WALK);
            } else {
                state.setAndContinue(IDLE);
            }   
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "attack", 0, state -> {
            return PlayState.CONTINUE;
        })
        .triggerableAnim("melee", ATTACK)
        .triggerableAnim("aggro", AGGROED)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void setVariant(Integer variant) {
        this.entityData.set(MODEL_VARIANT, variant);
    }

    @Override
    public Integer getVariant() {
        return this.entityData.get(MODEL_VARIANT);
    }

    public Integer getChargePhase(){
        return this.entityData.get(CHARGE_PHASE);
    }
    public void setChargePhase(int i){
        this.entityData.set(CHARGE_PHASE, i);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODEL_VARIANT, 1);
        this.entityData.define(CHARGE_PHASE, -1);
        this.entityData.define(TICKS_REMAINING, Long.valueOf(0));
        this.entityData.define(CHARGE_TICKS, Long.valueOf(0));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("variant", this.entityData.get(MODEL_VARIANT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(MODEL_VARIANT, tag.getInt("variant"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData spawnData, CompoundTag dataTag) {
        this.setVariant(this.getRandom().nextFloat() < 0.5f ? 1 : 2);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinHogRiderAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PiglinHogRiderEntity> getBrain() {
        return (Brain<PiglinHogRiderEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PiglinHogRiderEntity> brainProvider() {
        return PiglinHogRiderAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("piglinHogRiderBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        PiglinHogRiderAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        this.tickSubEntities();
        if(!this.level().isClientSide){
            this.updateAggroTicks();
            if(this.getAggroTicks()==20){
                this.playSound(PiglinSounds.PIGLIN_NOB_SCREAM.get());
                this.playSound(PiglinSounds.PIGLIN_HOG_RIDER_ROAR.get());
            }
            if (brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
                this.triggerAnim("attack", "melee");
                this.playSound(PiglinSounds.PIGLIN_HOG_RIDER_ATTACK.get());
            }
            if(!this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()){
                this.aggroed = false;
            }
            if(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get())){
                this.setTicksRemaining(this.getBrain().getTimeUntilExpiry(ModBrainMemories.STUNNED.get()));
                if(this.getTicksRemaining() == PiglinHogRiderEntity.instance.getStunDuration()){
                    this.playSound(PiglinSounds.PIGLIN_HOG_RIDER_STUN.get());
                }
            }else{
                this.setTicksRemaining(Long.valueOf(-1));
            }
            if(this.getBrain().hasMemoryValue(ModBrainMemories.ABILITY_STATE.get())){
                this.setChargeTicksRemaining(this.getBrain().getTimeUntilExpiry(ModBrainMemories.ABILITY_STATE.get()));
            }else{
                this.setChargeTicksRemaining(Long.valueOf(-1));
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if(hurt && entity instanceof LivingEntity le && le.isAlive()){
            if (!le.getType().is(ModTags.Entities.CANNOT_BE_LACERATED)) {
                le.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 200));
            }
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200));
        }
        return hurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PiglinSounds.PIGLIN_HOG_RIDER_IDLE.get();
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HOGLIN_HURT;
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        SoundEvent soundevent = this.getHurtSound(source);
        if (soundevent != null) {
            this.playSound(soundevent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.02F + 0.2F);
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.HOGLIN_STEP, this.getSoundVolume() * 0.15F, this.getVoicePitch());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get()))amount*=2;
        return super.hurt(source, amount);
    }
    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 30);
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
    public boolean isStart(long ticksRemaining){
        return (CHARGE_DURATION - ticksRemaining) < CHARGE_START_DURATION;
    }

    public boolean isMiddle(long ticksRemaining){
        return (CHARGE_DURATION - ticksRemaining) < (CHARGE_START_DURATION + CHARGE_MIDDLE_DURATION);
    }

    public boolean isEnd(long ticksRemaining){
        return (CHARGE_DURATION - ticksRemaining) < (CHARGE_START_DURATION + CHARGE_MIDDLE_DURATION + CHARGE_START_DURATION + CHARGE_END_DURATION);
    }
    
    public long getChargeTicksRemaining(){
        return this.entityData.get(CHARGE_TICKS);
    }

    public void setChargeTicksRemaining(long l){
        this.entityData.set(CHARGE_TICKS, l);
    }
    public boolean isCharging(){
        return this.entityData.get(CHARGE_TICKS) > 0;
    }

    @Override
    public int getAggroTicks() {
        return this.aggroTicks;
    }

    @Override
    public boolean isAggro() {
        return this.aggroed;
    }

    @Override
    public void updateAggroTicks() {
        if(this.aggroTicks>0)this.aggroTicks--;
    }

    @Override
    public void setAggro(int ticks) {
        this.aggroed = true;
        this.aggroTicks = ticks;
        this.triggerAnim("attack", "aggro");
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
    public void die(DamageSource damageSource) {
        this.stopTriggeredAnimation("attack", null);
        super.die(damageSource);
    }
    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
