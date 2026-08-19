package net.zincstudios.scgextra.entity.rrc.oppressor;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.common.GunnerEntity;
import net.zincstudios.scgextra.entity.common.MobUtil;
import net.zincstudios.scgextra.entity.common.goal.AlertFactionGoal;
import net.zincstudios.scgextra.entity.common.goal.FlareSummonGoal;
import net.zincstudios.scgextra.entity.common.goal.HurtByNonFactionGoal;
import net.zincstudios.scgextra.entity.common.client.ExpandedAnimationController;
import net.zincstudios.scgextra.entity.rrc.RRCEntities;
import net.zincstudios.scgextra.sounds.RRCSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OppressorEntity extends GunnerEntity implements GeoEntity {

    private static final RawAnimation AIMING = RawAnimation.begin().thenPlayAndHold("idle_aim");
    private static final RawAnimation HOLD = RawAnimation.begin().thenPlay("aim_idle");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    
    public OppressorEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isLeftHanded() {
        return true;  // Flagging doesn't seem to work
    }

    @Override
    protected void registerGoals() {
        // TODO: custom gun attack goal
        this.goalSelector.addGoal(3, new AlertFactionGoal(this, 200));
        this.goalSelector.addGoal(4, new FlareSummonGoal(this, 400, 60,
                RRCEntities.SCOUT.get(), RRCEntities.TALLMAN.get(), RRCEntities.COPPER_KNIGHT.get()));//400 cause you halve the cooldown in the goal
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByNonFactionGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
                player -> !((Player) player).isCreative() && !player.isSpectator()));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true,
                entity -> Faction.isEnemies(this, entity) || entity.getMobType().equals(MobType.UNDEAD)));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120D)
                .add(Attributes.ARMOR, 6D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walk/idle", 2, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            } else {
                return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
        }));

//        controllers.add(new AnimationController<>(this, "idle_head", 2, state -> {
//            return state.setAndContinue(RawAnimation.begin()
//                    .thenWait(50)  // TODO: random time
//                    .thenPlay("idle_head")
//            );
//        }).setAnimationSpeed(1.3));

        controllers.add(new ExpandedAnimationController<>(this, "aim", 0,
                state -> {

                    if (state.isCurrentAnimation(AIMING) && !state.getAnimatable().isAiming()) {
                        state.setAnimation(HOLD);
                    }
                    if (state.getAnimatable().isAiming()) {
                        state.setAnimation(AIMING);
                    }

                    return PlayState.CONTINUE;
                })
        );

        controllers.add(new ExpandedAnimationController<>(this, "behaviour", 0, state -> PlayState.STOP)
                .triggerableAnim("flare",
                        ctl -> {
                            AnimationController<?> aimController = this.getAnimatableInstanceCache()
                                    .getManagerForId(this.getId())
                                    .getAnimationControllers()
                                    .get("aim");

                            if (Objects.equals(aimController.getCurrentRawAnimation(), AIMING)) {
                                return RawAnimation.begin().thenPlay("aim_idle").thenPlay("flare").thenPlay("idle_aim");
                            } else {
                                return RawAnimation.begin().thenPlay("flare");
                            }
                        }
                ).triggerableAnim("alert",
                        ctl -> {
                            AnimationController<?> aimController = this.getAnimatableInstanceCache()
                                    .getManagerForId(this.getId())
                                    .getAnimationControllers()
                                    .get("aim");

                            if (Objects.equals(aimController.getCurrentRawAnimation(), AIMING)) {
                                return RawAnimation.begin().thenPlay("aim_idle").thenPlay("alert").thenPlay("idle_aim");
                            } else {
                                return RawAnimation.begin().thenPlay("alert");
                            }
                        }
                ).setCustomInstructionKeyframeHandler(event -> {
                    if (event.getKeyframeData().getInstructions().equals("spawn_flare;")) {
                        OppressorEntity self = event.getAnimatable();
                        FireworkRocketEntity firework = new FireworkRocketEntity(self.level(),
                                self.getX(), self.getY()+3, self.getZ(), createFireworkItem());
                        if (self.level().isClientSide()) {
                            self.level().addFreshEntity(firework);
                        }
                    }
                })
        );

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("death"));
            } else {
                return PlayState.STOP;
            }
        }));
    }

    private ItemStack createFireworkItem() {
        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag tag = firework.getOrCreateTag();
        CompoundTag fireworks = new CompoundTag();

        // Flight duration (1-3)
        fireworks.putByte("Flight", (byte) 1);

        // Explosions
        ListTag explosions = new ListTag();
        CompoundTag explosion = new CompoundTag();
        explosion.putByte("Type", (byte) 0); // 0=small, 1=large, 2=star, 3=creeper, 4=burst
        explosion.putIntArray("Colors", new int[]{0xFFFFFF}); // RGB colors
        explosion.putBoolean("Flicker", false);
        explosion.putBoolean("Trail", true);
        explosions.add(explosion);

        fireworks.put("Explosions", explosions);
        tag.put("Fireworks", fireworks);

        return firework;
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 30);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return RRCSounds.RRC_OPPRESSOR_HURT.get();
    };
    protected SoundEvent getAmbientSound() {
        return RRCSounds.RRC_OPPRESSOR_IDLE.get();
    };
    protected SoundEvent getStepSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    };
    protected SoundEvent getDeathSound() {
        return RRCSounds.RRC_OPPRESSOR_DEATH.get();
    };
    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(this.getStepSound(), this.getSoundVolume(), 1.0F);
    }
    protected float getSoundVolume() {
        return 2F;
    };
}