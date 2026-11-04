package net.zincstudios.scgextra.entity.wreckers.wrecker_turret_gunner;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.zincstudios.scgextra.CommonConfig;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.asgharian.SimpleBurstGunAttackGoal;
import net.zincstudios.scgextra.entity.common.GunnerEntity;
import net.zincstudios.scgextra.entity.common.goal.AlertFactionGoal;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.sounds.InterruptibleVoice;
import net.zincstudios.scgextra.sounds.WreckersSounds;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;

import java.util.List;
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
public class WreckerTurretGunnerEntity extends GunnerEntity implements GeoEntity, InterruptibleVoice {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("attack");
    private static final RawAnimation ALERT = RawAnimation.begin().thenPlay("alert");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private static final EntityDataAccessor<Integer> SHOOT_TICKS =
            SynchedEntityData.defineId(WreckerTurretGunnerEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public WreckerTurretGunnerEntity(EntityType<? extends GunnerEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.MAX_HEALTH, 30.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOOT_TICKS, 0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.GREASER_SMG.get()));
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void registerGoals() {
        if (CommonConfig.enableAbilityAlert) {
            this.goalSelector.addGoal(2, new AlertFactionGoal(this, CommonConfig.abilityAlertCooldown * 20));
        }
        this.goalSelector.addGoal(3, new SimpleBurstGunAttackGoal<>(this) {
                    @Override
                    protected void tickMovement(LivingEntity target, double dist) {}
                }
                .burstAmount(16)
                .burstIntervalTicks(1)
                .maxRange(16)
                .attackInterval(40)
                .accuracy(1.0f)
        );
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 1, true, false,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 1, true, false,
                entity -> Faction.isEnemies(this, entity)));
    }

    @Override
    public void onGunAttack(LivingEntity target, ItemStack itemStack) {
        this.entityData.set(SHOOT_TICKS, 4);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            int shootTicks = this.entityData.get(SHOOT_TICKS);
            if (shootTicks > 0) {
                this.entityData.set(SHOOT_TICKS, shootTicks - 1);
            }
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 2, state -> {
            if (this.entityData.get(SHOOT_TICKS) > 0) {
                return state.setAndContinue(ATTACK);
            }
            return state.setAndContinue(IDLE);
        }));

        controllers.add(new AnimationController<>(this, "behaviour", 0, state -> PlayState.STOP)
                .triggerableAnim("alert", ALERT));

        controllers.add(new AnimationController<>(this, "death", 0, state -> {
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected float getSoundVolume() {
        return 0.85F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WreckersSounds.GANG_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return WreckersSounds.GANG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WreckersSounds.GANG_DEATH.get();
    }

    @Override
    public List<SoundEvent> voiceLinesToSilenceOnDeath() {
        return WreckersSounds.gangVoiceLines();
    }
}
