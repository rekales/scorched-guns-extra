package net.zincstudios.scgextra.entity.wreckers.wrecker_jumbo;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.GunnerEntity;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.part.RotatedBulletProofPartEntity;
import net.zincstudios.scgextra.sounds.InterruptibleVoice;
import net.zincstudios.scgextra.sounds.WreckersSounds;
import software.bernie.geckolib.animatable.GeoEntity;

import java.util.List;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WreckerJumboEntity extends GunnerEntity implements GeoEntity, InterruptibleVoice {

    static final int MELEE_DAMAGE_DELAY = 15;
    static final int MELEE_DURATION = 32;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final RotatedBulletProofPartEntity<?>[] subEntities;

    public WreckerJumboEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(1.0F);

        this.subEntities = new RotatedBulletProofPartEntity[] {
                new RotatedBulletProofPartEntity<>(this, new Vec3(0, 1, 0.3), this.getBbWidth(), 1.1F),
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24F)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.MAX_HEALTH, 300.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return WreckerJumboAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<WreckerJumboEntity> getBrain() {
        return (Brain<WreckerJumboEntity>) super.getBrain();
    }

    protected Brain.Provider<WreckerJumboEntity> brainProvider() {
        return Brain.provider(WreckerJumboAi.MEMORY_TYPES, WreckerJumboAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("wreckerJumboBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        BrainCommons.updateHasTargetAggressive(this);
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

        if (!this.level().isClientSide && brain.getTimeUntilExpiry(ModBrainMemories.DELAYED_MELEE.get()) == MELEE_DURATION) {
            this.triggerAnim("attack", "melee");
        }
    }

    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
            boolean moving = state.isMoving() || this.getNavigation().isInProgress();
            return state.setAndContinue(moving ? WALK : IDLE);
        }));

        controllers.add(new AnimationController<>(this, "attack", 2, state -> PlayState.STOP)
                .triggerableAnim("melee", ATTACK));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            this.playSound(WreckersSounds.JUMBO_ATTACK.get(), 1.0F, 1.0F);
        }
        return hit;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected float getSoundVolume() {
        return 0.95F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAggressive() && this.random.nextInt(4) == 0) {
            return WreckersSounds.JUMBO_LINE.get();
        }
        return WreckersSounds.JUMBO_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WreckersSounds.JUMBO_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WreckersSounds.JUMBO_DEATH.get();
    }

    @Override
    public List<SoundEvent> voiceLinesToSilenceOnDeath() {
        return WreckersSounds.jumboVoiceLines();
    }
}
