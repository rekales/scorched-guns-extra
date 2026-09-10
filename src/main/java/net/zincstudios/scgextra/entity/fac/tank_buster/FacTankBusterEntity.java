package net.zincstudios.scgextra.entity.fac.tank_buster;

import com.mojang.serialization.Dynamic;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.common.EquippedEntity;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.brain.BrainCommons;
import net.zincstudios.scgextra.entity.common.part.RotatedDamageMultPartEntity;
import net.zincstudios.scgextra.sounds.FACSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FacTankBusterEntity extends EquippedEntity implements GeoEntity, Gunner {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_AIM = RawAnimation.begin().thenLoop("idle_aim");
    private static final RawAnimation WALK_AIM = RawAnimation.begin().thenLoop("walk_aim");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final PartEntity<?>[] subEntities;

    private DamageSource deathDamage = null;

    public FacTankBusterEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.subEntities = new PartEntity[] {
                new RotatedDamageMultPartEntity<>(this, 0.5F, new Vec3(0,1.35,0), 1.05F, 0.85f, false),
        };
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.17F)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.MAX_HEALTH, 40.0D);
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return FacTankBusterAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    public Brain<FacTankBusterEntity> getBrain() {
        return (Brain<FacTankBusterEntity>) super.getBrain();
    }

    protected Brain.Provider<FacTankBusterEntity> brainProvider() {
        return FacTankBusterAi.brainProvider();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("facTankBusterBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        BrainCommons.updateActivity(this);
        BrainCommons.updateAimingAggressive(this);
        if (this.getBrain().getMemory(ModBrainMemories.AIM_TICKS.get()).filter(aim -> aim > 5).isPresent()) {
            this.setYBodyRot(this.getYHeadRot());
        }
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
    }

    protected void tickSubEntities() {
        for(PartEntity<?> partEntity : this.getParts()) {
            partEntity.tick();
        }
    }

    @Override
    protected void dropAllDeathLoot(DamageSource damageSource) {
        if (this.deathTime < 18) {
            this.deathDamage = damageSource;
        } else {
            super.dropAllDeathLoot(damageSource);
        }
    }

    @Override
    protected void tickDeath() {
        if (this.deathTime == 17 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 3F, Level.ExplosionInteraction.NONE);
        } else if (this.deathTime == 18 && !this.level().isClientSide() && !this.isRemoved() && this.deathDamage != null) {
            this.dropAllDeathLoot(this.deathDamage);
        }
        super.tickDeath();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 8, state -> {
            if (state.getAnimatable().isAggressive()) {
                return state.setAndContinue(state.isMoving() || state.getAnimatable().isActuallyMoving()? WALK_AIM : IDLE_AIM);
            } else {
                return state.setAndContinue(state.isMoving() || state.getAnimatable().isActuallyMoving() ? WALK : IDLE);
            }
        }));
    }

    // needed because this thing moves way too fucking slow for state.isMoving() to be true
    private boolean isActuallyMoving() {
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        return dx * dx + dz * dz > 0.000001D;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean isLeftHanded() {
        return true;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return FACSounds.FAC_TANK_BUSTER_HURT.get();
    }

    protected SoundEvent getAmbientSound() {
        return FACSounds.FAC_TANK_BUSTER_IDLE.get();
    }

    protected SoundEvent getDeathSound() {
        return FACSounds.FAC_TANK_BUSTER_DEATH.get();
    }
}
