package net.zincstudios.scgextra.entity.asgharian.soulripper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.asgharian.AbilityGoal;
import net.zincstudios.scgextra.entity.asgharian.GoalState;
import net.zincstudios.scgextra.entity.asgharian.GoalStateHandler;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.entity.common.part.RotatedWeakPointPartEntity;
import net.zincstudios.scgextra.sounds.AsgharianSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModEffects;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SoulRipperEntity extends Monster implements GeoEntity, GoalStateHandler {

    public enum BehaviourState {
        NONE, MELEE, FIREBALL, SUMMON, DYING
    }

    private static final EntityDataAccessor<Integer> LIVES =
            SynchedEntityData.defineId(SoulRipperEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation MOVE = RawAnimation.begin().thenLoop("move");
    private static final RawAnimation MELEE = RawAnimation.begin().thenPlay("melee");
    private static final RawAnimation FIREBALL = RawAnimation.begin().thenPlay("fireball");
    private static final RawAnimation SUMMON = RawAnimation.begin().thenPlay("summon");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("death");
    private static final RawAnimation REVIVE = RawAnimation.begin().thenPlay("revive");
    private static final RawAnimation LANTERN_1_OFF = RawAnimation.begin().thenPlayAndHold("lantern_1_flickering");
    private static final RawAnimation LANTERN_2_OFF = RawAnimation.begin().thenPlayAndHold("lantern_2_flickering");
    private static final RawAnimation LANTERN_3_OFF = RawAnimation.begin().thenPlayAndHold("lantern_3_flickering");

    // Match with anims
    private static final int DEATH_DURATION_TICKS = 40;
    private static final int REVIVE_DURATION_TICKS = 50;

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);
    private final PartEntity<?>[] subEntities;

    // Serverside only
    private BehaviourState behaviourState = BehaviourState.NONE;
    private BlockPos boundOrigin = null;
    private int lastDeath = 0;  // tickCount timestamp

    public SoulRipperEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.subEntities = new PartEntity[] {
                new RotatedWeakPointPartEntity<>(this, new Vec3(-1.1, 2.3, -0.15), 5/16f, 8/16f),
                new RotatedWeakPointPartEntity<>(this, new Vec3(0.6, 2.15, -0.25), 5/16f, 8/16f),
                new RotatedWeakPointPartEntity<>(this, new Vec3(0.075, 2.575, -0.275), 5/16f, 8/16f)
        };
        this.moveControl = new SoulRipperMoveControl(this);

        if (!level.isClientSide) {
            this.setLives(3);
        }
    }

    public void tick() {
        this.noPhysics = !this.isDeadOrDying();
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);
        this.tickSubEntities();
        if (this.tickCount % 10 == 0) {
            this.updateBoundOrigin();
        }
    }

    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.getLives() > 0) {
            if (this.deathTime >= DEATH_DURATION_TICKS + REVIVE_DURATION_TICKS - 15) {
                this.deathTime = 0;
                this.setLives(this.getLives() - 1);
                this.setHealth(this.getMaxHealth());
                this.dead = false;
                this.lastDeath = this.tickCount;
                this.behaviourState = BehaviourState.NONE;
                this.playSound(AsgharianSounds.SOUL_RIPPER_LANTERN_SHATTER.get(), 0.75F, 1.0F);
            } else if (this.deathTime == DEATH_DURATION_TICKS && !this.level().isClientSide) {
                this.triggerAnim("revive", "revive");
                this.playSound(this.getReviveSound());
                this.setDeltaMovement(this.getDeltaMovement().add(MobUtil.vecFromRot(this.getYRot()).scale(0.15)));
            } else {
                this.behaviourState = BehaviourState.DYING;
            }
        } else {
            if (this.deathTime >= 35 && !this.level().isClientSide() && !this.isRemoved()) {
                this.level().broadcastEntityEvent(this, (byte)60);
                this.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (damageSource.getEntity() == null && damageSource.getDirectEntity() == null && damageSource.is(DamageTypes.GENERIC_KILL)) {
            this.setLives(0);  // Kill command, unsure if it could be caused by other things
        }
        super.die(damageSource);

        if (damageSource.getEntity() != null) {
            Vec3 sourcePos = damageSource.getEntity().position();
            Vec3 dir = sourcePos.subtract(this.position());
            dir = new Vec3(dir.x, 0, dir.z).normalize();
            this.setDeltaMovement(dir.scale(-0.5).add(0,-0.15,0));
            this.hasImpulse = true;
            float yRot = MobUtil.rotFromVec(dir);
            this.setYRot(yRot);
            this.setYBodyRot(yRot);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0,-0.15,0));
        }
    }

    @Override
    protected boolean shouldDropLoot() {
        return this.getLives() == 0;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    public void updateBoundOrigin() {
        Vec3 start = this.position().add(0,2,0);
        Vec3 end = start.add(0, -64, 0);

        BlockHitResult result = this.level().clip(new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                this
        ));

        if (result.getType() != HitResult.Type.MISS) {
            this.boundOrigin = result.getBlockPos();
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (entity instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 60));
            if (this.getLives() == 0) {
                target.setSecondsOnFire(3);
            }
        }
        return super.doHurtTarget(entity);
    }

    public BlockPos getBoundOrigin() {
        return Objects.requireNonNullElseGet(this.boundOrigin, () -> BlockPos.containing(this.position()));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SoulRipperChargeAttackGoal(this, 70));
        this.goalSelector.addGoal(3, new SoulRipperFireballGoal(this).cooldown(200).windup(13).recovery(7));
        this.goalSelector.addGoal(4, new SoulRipperSummonVexGoal(this).cooldown(2000).windup(18).recovery(7));
        this.goalSelector.addGoal(8, new SoulRipperRandomMoveGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));

        // Bosses should prioritize players
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(3, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.ARMOR, 7.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MAX_HEALTH, 400.0D);
    }

    public boolean isMovingForward() {
//        Vec3 oldPos = new Vec3(this.xOld, this.yOld, this.zOld);
        Vec3 dir = this.position().subtract(new Vec3(this.xOld, this.yOld, this.zOld));

//        SCGExtra.LOGGER.debug("dev: " + dir.length());
//        float deltaRot = MobUtil.rotFromVec(this.getDeltaMovement());

        float oldRot = MobUtil.rotFromVec(dir);

//        SCGExtra.LOGGER.debug("old: " + oldRot);
//        SCGExtra.LOGGER.debug("delta: " + deltaRot);
//        SCGExtra.LOGGER.debug("rot: " + this.getYRot());
//        SCGExtra.LOGGER.debug("dev: " + Math.abs((oldRot + 1000) - (this.getYRot() + 1000)));
        return Math.abs((oldRot + 1000) - (this.yBodyRot + 1000)) < 200 && dir.length() > 0.075;
    }

    // NOTE: There has to be a better way of doing this no? maybe an ability queue or something?
    @Override
    public void onGoalStateChanged(Goal goal, GoalState state) {
        if (goal instanceof SoulRipperSummonVexGoal) {
            if (this.behaviourState == BehaviourState.NONE
                    && (state == AbilityGoal.WINDUP_STATE
                    || state == AbilityGoal.ACTIVE_STATE
                    || state == AbilityGoal.RECOVERY_STATE)) {
                this.behaviourState = BehaviourState.SUMMON;
                this.triggerAnim("behaviour", "summon");
            }
            if (this.behaviourState == BehaviourState.SUMMON
                    && (state == AbilityGoal.COOLDOWN_STATE
                    || state == AbilityGoal.IDLE_STATE)) {
                this.behaviourState = BehaviourState.NONE;
            }
        } else if (goal instanceof SoulRipperFireballGoal) {
            if (this.behaviourState == BehaviourState.NONE
                    && (state == AbilityGoal.WINDUP_STATE
                    || state == AbilityGoal.ACTIVE_STATE
                    || state == AbilityGoal.RECOVERY_STATE)) {
                this.behaviourState = BehaviourState.FIREBALL;
                this.triggerAnim("behaviour", "fireball");
                this.playSound(this.getAttackSounds());
            }
            if (this.behaviourState == BehaviourState.FIREBALL
                    && (state == AbilityGoal.COOLDOWN_STATE
                    || state == AbilityGoal.IDLE_STATE)) {
                this.behaviourState = BehaviourState.NONE;
            }
        } else if (goal instanceof SoulRipperChargeAttackGoal) {
            if (this.behaviourState == BehaviourState.NONE && state != SoulRipperChargeAttackGoal.COOLDOWN) {
                this.behaviourState = BehaviourState.MELEE;
            }
            if (state == SoulRipperChargeAttackGoal.MELEE) {
                this.triggerAnim("behaviour", "melee");
                this.playSound(this.getAttackSounds());
            }
            if (this.behaviourState == BehaviourState.MELEE && state == SoulRipperChargeAttackGoal.COOLDOWN) {
                this.behaviourState = BehaviourState.NONE;
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (state.getAnimatable().isMovingForward()) {
                return state.setAndContinue(MOVE);
            } else {
                return state.setAndContinue(IDLE);
            }
        }));

        controllers.add(new AnimationController<>(this, "behaviour", 2, state -> PlayState.STOP)
                .triggerableAnim("melee", MELEE)
                .triggerableAnim("fireball", FIREBALL)
                .triggerableAnim("summon", SUMMON)

        );

        controllers.add(new AnimationController<>(this, "death", 3, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            } else {
                state.resetCurrentAnimation();
                return PlayState.STOP;
            }
        }));

        controllers.add(new AnimationController<>(this, "revive", 2, state -> PlayState.STOP)
                .triggerableAnim("revive", REVIVE));

        controllers.add(new AnimationController<>(this, "lantern", 0, state ->
                switch(state.getAnimatable().getLives()) {
                    case 2 -> state.setAndContinue(LANTERN_1_OFF);
                    case 1 -> state.setAndContinue(LANTERN_2_OFF);
                    case 0 -> state.setAndContinue(LANTERN_3_OFF);
                    default -> PlayState.STOP;
                }));

//                controllers.add(new ExpandedAnimationController<>(this, "lantern_1", 0,
//                state -> state.setAndContinue(state.getAnimatable().getLives() >= 2 ? LANTERN_1_ON : LANTERN_1_OFF)));
//        controllers.add(new ExpandedAnimationController<>(this, "lantern_2", 0,
//                state -> state.setAndContinue(state.getAnimatable().getLives() >= 3 ? LANTERN_2_ON : LANTERN_2_OFF)));
//        controllers.add(new ExpandedAnimationController<>(this, "lantern_3", 0,
//                state -> state.setAndContinue(state.getAnimatable().getLives() >= 1 ? LANTERN_3_ON : LANTERN_3_OFF)));

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.boundOrigin = MobUtil.getBlocKPosFromTag("Bound", tag);
        if (tag.contains("Lives")) {
            this.setLives(tag.getInt("Lives"));
        }
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.boundOrigin != null) {
            MobUtil.putBlockPosToTag(this.boundOrigin, "Bound", tag);
        }
        tag.putInt("Lives", this.getLives());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LIVES, 3);
    }

    public boolean canFireball() {
        LivingEntity target = this.getTarget();
        if (target == null) return false;
        return this.getLives() <= 2
                && (this.behaviourState == BehaviourState.NONE || this.behaviourState == BehaviourState.FIREBALL)
                && this.distanceToSqr(target) > 4.0D;

        // TODO: state checks
    }

    public boolean canMelee() {
        return this.behaviourState == BehaviourState.NONE || this.behaviourState == BehaviourState.MELEE;
    }

    public boolean canSummon() {
        LivingEntity target = this.getTarget();
        if (target == null) return false;
        return this.getLives() <= 1
                && (this.behaviourState == BehaviourState.NONE || this.behaviourState == BehaviourState.SUMMON)
                && this.tickCount > this.lastDeath + 120
                && this.distanceToSqr(target) > 4.0D;
    }

    public int getLives() {
        return this.entityData.get(LIVES);
    }

    private void setLives(int lives) {
        this.entityData.set(LIVES, lives);
    }

    protected SoundEvent getAttackSounds() {
        return AsgharianSounds.SOUL_RIPPER_ATTACK.get();
    }

    protected SoundEvent getAmbientSound() {
        return AsgharianSounds.SOUL_RIPPER_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AsgharianSounds.SOUL_RIPPER_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return AsgharianSounds.SOUL_RIPPER_DEATH.get();
    }

    protected SoundEvent getReviveSound() {
        return AsgharianSounds.SOUL_RIPPER_REVIVE.get();
    }

}
