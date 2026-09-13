package net.zincstudios.scgextra.entity.neutral.overworld.anormaly;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AnormalyEntity extends Monster implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");
    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);

    public AnormalyEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new AnormalyMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MAX_HEALTH, 10.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(WALK);
            } else {
                return state.setAndContinue(IDLE);
            }
        }).triggerableAnim("attack", ATTACK));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AnormalyRandomMoveGoal());
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
    }

    class AnormalyMoveControl extends MoveControl {
        public AnormalyMoveControl(AnormalyEntity anormaly) {
            super(anormaly);
        }

        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                Vec3 vec3 = new Vec3(this.wantedX - AnormalyEntity.this.getX(),
                        this.wantedY - AnormalyEntity.this.getY(), this.wantedZ - AnormalyEntity.this.getZ());
                double d0 = vec3.length();
                if (d0 < AnormalyEntity.this.getBoundingBox().getSize()) {
                    this.operation = Operation.WAIT;
                    AnormalyEntity.this.setDeltaMovement(AnormalyEntity.this.getDeltaMovement().scale((double) 0.5F));
                } else {
                    AnormalyEntity.this.setDeltaMovement(
                            AnormalyEntity.this.getDeltaMovement().add(vec3.scale(this.speedModifier * 0.05 / d0)));
                    if (AnormalyEntity.this.getTarget() == null) {
                        Vec3 vec31 = AnormalyEntity.this.getDeltaMovement();
                        AnormalyEntity.this.setYRot(-((float) Mth.atan2(vec31.x, vec31.z)) * (180F / (float) Math.PI));
                        AnormalyEntity.this.yBodyRot = AnormalyEntity.this.getYRot();
                    } else {
                        double d2 = AnormalyEntity.this.getTarget().getX() - AnormalyEntity.this.getX();
                        double d1 = AnormalyEntity.this.getTarget().getZ() - AnormalyEntity.this.getZ();
                        AnormalyEntity.this.setYRot(-((float) Mth.atan2(d2, d1)) * (180F / (float) Math.PI));
                        AnormalyEntity.this.yBodyRot = AnormalyEntity.this.getYRot();
                    }
                }
            }
        }
    }

    class AnormalyRandomMoveGoal extends Goal {
        public AnormalyRandomMoveGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return !AnormalyEntity.this.getMoveControl().hasWanted()
                    && AnormalyEntity.this.random.nextInt(reducedTickDelay(7)) == 0;
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void tick() {
            BlockPos blockpos = AnormalyEntity.this.blockPosition();

            for (int i = 0; i < 3; ++i) {
                BlockPos blockpos1 = blockpos.offset(AnormalyEntity.this.random.nextInt(15) - 7,
                        AnormalyEntity.this.random.nextInt(11) - 5, AnormalyEntity.this.random.nextInt(15) - 7);
                if (AnormalyEntity.this.level().isEmptyBlock(blockpos1)) {
                    AnormalyEntity.this.moveControl.setWantedPosition((double) blockpos1.getX() + (double) 0.5F,
                            (double) blockpos1.getY() + (double) 0.5F, (double) blockpos1.getZ() + (double) 0.5F,
                            (double) 0.25F);
                    if (AnormalyEntity.this.getTarget() == null) {
                        AnormalyEntity.this.getLookControl().setLookAt((double) blockpos1.getX() + (double) 0.5F,
                                (double) blockpos1.getY() + (double) 0.5F, (double) blockpos1.getZ() + (double) 0.5F,
                                180.0F, 20.0F);
                    }
                    break;
                }
            }

        }
    }
}
