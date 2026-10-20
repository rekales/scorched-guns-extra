package net.zincstudios.scgextra.entity.piglin.pigkan;

import com.mojang.serialization.Dynamic;

import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.piglin.common.StunnableHog;
import net.zincstudios.scgextra.sounds.FACSounds;
import net.zincstudios.scgextra.sounds.PiglinSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModEffects;

public class PigkanEntity extends Monster implements GeoEntity{

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SAW_ATTACK = RawAnimation.begin().thenPlay("saw_attack");
    private static final RawAnimation CLAW_ATTACK = RawAnimation.begin().thenPlay("claw_attack");
    private static final RawAnimation STUN_START = RawAnimation.begin().thenPlay("stun_start");
    private static final RawAnimation STUN_MIDDLE = RawAnimation.begin().thenLoop("stun_middle");
    private static final RawAnimation STUN_END = RawAnimation.begin().thenPlay("stun_end");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    static int CLAW_DURATION = 100;
    static int CLAW_DELAY = 23;

    static int SAW_DURATION = 100;
    static int SAW_DELAY = 17;

    public static final StunnableHog instance = new StunnableHog(
        40, 
        40, 
        40
    );

    private static final EntityDataAccessor<Long> TICKS_REMAINING = SynchedEntityData.defineId(PigkanEntity.class, EntityDataSerializers.LONG);

    public PigkanEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if(state.getAnimatable().isStunned()){
                if(PigkanEntity.instance.isStart(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_START);
                }else if(PigkanEntity.instance.isMiddle(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_MIDDLE);
                }else if(PigkanEntity.instance.isEnd(state.getAnimatable().getTicksRemaining())){
                    state.setAnimation(STUN_END);
                }
            }else{
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

        controllers.add(new AnimationController<>(this, "attack", 2, state -> {
            return PlayState.CONTINUE;
        })
            .triggerableAnim("claw_attack", CLAW_ATTACK)
            .triggerableAnim("saw_attack", SAW_ATTACK)
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 128.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.32F)
            .add(Attributes.ATTACK_DAMAGE, 30.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
            .add(Attributes.ARMOR, 6.0D)
            .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 0.8D)
            .add(Attributes.MAX_HEALTH, 200.0D);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PigkanAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<PigkanEntity> getBrain() {
        return (Brain<PigkanEntity>) super.getBrain();
    }

    @Override
    protected Brain.Provider<PigkanEntity> brainProvider() {
        return PigkanAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("pigkanBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        PigkanAi.updateActivity(this);
        BrainCommons.updateMaxRangeAggressive(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide()){
            if (brain.hasMemoryValue(ModBrainMemories.DELAYED_MELEE.get()) && brain.hasMemoryValue(ModBrainMemories.ABILITY_STATE.get())) {
                String name = brain.getMemory(ModBrainMemories.ABILITY_STATE.get()).get().abilityId();
                if(name.equals("claw")){
                    if(brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get())==CLAW_DURATION){
                        this.triggerAnim("attack", "claw_attack");
                    }
                }
                if(name.equals("saw")){
                    if(brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get())==SAW_DURATION){
                        this.triggerAnim("attack", "saw_attack");
                    }
                }
            }
            if(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get())){
                this.setTicksRemaining(this.getBrain().getTimeUntilExpiry(ModBrainMemories.STUNNED.get()));
                if(this.getTicksRemaining() == PigkanEntity.instance.getStunDuration()){
                    this.playSound(FACSounds.FAC_TANK_STUN.get());
                    this.stopTriggeredAnimation("attack", null);
                }
            }else{
                this.setTicksRemaining(Long.valueOf(-1));
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if(!this.level().isClientSide()){
            if(brain.hasMemoryValue(ModBrainMemories.ABILITY_STATE.get()) && entity instanceof LivingEntity le){
                le.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 200));
                String name = brain.getMemory(ModBrainMemories.ABILITY_STATE.get()).get().abilityId();
                if(name.equals("claw")){
                    le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
                }
                if(name.equals("saw")){
                    le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200));
                }
            }
        }
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if(this.getBrain().hasMemoryValue(ModBrainMemories.STUNNED.get()))amount*=2;
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return PiglinSounds.PIGKAN_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return PiglinSounds.GENERAL_HURT.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(PiglinSounds.GENERAL_WALK.get(), this.getSoundVolume() * 0.35F, this.getVoicePitch());
    }
    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 40);
        if(this.deathTime == 20){
            this.playSound(PiglinSounds.PIGKAN_DEATH.get());
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
    public void die(DamageSource damageSource) {
        this.stopTriggeredAnimation("attack", null);
        super.die(damageSource);
    }
}