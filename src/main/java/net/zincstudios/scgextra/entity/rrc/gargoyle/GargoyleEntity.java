package net.zincstudios.scgextra.entity.rrc.gargoyle;

import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GargoyleEntity extends Monster implements GeoEntity, FlyingAnimal{

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation MOVE = RawAnimation.begin().thenLoop("move");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenLoop("attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private static final EntityDataAccessor<Boolean> IS_ATTACKING = SynchedEntityData.defineId(GargoyleEntity.class, EntityDataSerializers.BOOLEAN);

    public GargoyleEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    protected PathNavigation createNavigation(Level p_level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, p_level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
   }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            if (state.isMoving() || this.getNavigation().isInProgress()) {
                return state.setAndContinue(MOVE);
            }
            return state.setAndContinue(IDLE);
        }).triggerableAnim("attack", ATTACK));

        controllers.add(new AnimationController<>(this, "death", 0, state -> {
            if (this.isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 50.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35F)
            .add(Attributes.ATTACK_DAMAGE, 10.0D)
            .add(Attributes.ARMOR, 0.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
            .add(Attributes.FLYING_SPEED, 0.3)
            .add(Attributes.MAX_HEALTH, 60.0D);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
      return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_ATTACKING, false);
    }
    public void setAttacking(boolean bool){
        this.entityData.set(IS_ATTACKING, bool);
    }
    public boolean isAttacking(){
        return this.entityData.get(IS_ATTACKING);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1, 35));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new GargoyleEntityWanderGoal());
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
    }

    class GargoyleEntityWanderGoal extends Goal {
        GargoyleEntityWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return GargoyleEntity.this.navigation.isDone() && GargoyleEntity.this.random.nextInt(10) == 0 && !GargoyleEntity.this.isAttacking();
        }

        public boolean canContinueToUse() {
            return GargoyleEntity.this.navigation.isInProgress() && !GargoyleEntity.this.isAttacking();
        }

        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                GargoyleEntity.this.navigation.moveTo(GargoyleEntity.this.navigation.createPath(BlockPos.containing(vec3), 1), 1);
            }

        }

        @Nullable
        private Vec3 findPos() {
            Vec3 vec3 = GargoyleEntity.this.getViewVector(0.0F);
            Vec3 vec32 = HoverRandomPos.getPos(GargoyleEntity.this, 8, 7, vec3.x, vec3.z, ((float)Math.PI / 2F), 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(GargoyleEntity.this, 8, 4, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
        }
    }
    
}
