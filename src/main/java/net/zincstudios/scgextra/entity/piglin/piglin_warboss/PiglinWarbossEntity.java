package net.zincstudios.scgextra.entity.piglin.piglin_warboss;

import net.zincstudios.scgextra.entity.common.gun.MountedGun;
import net.zincstudios.scgextra.entity.common.gun.AttachedMountedGun;
import net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.MobUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

public class PiglinWarbossEntity extends Monster implements GeoEntity, Gunner, BulletSpawnOffset {

    public final Vec3 BarrelOffestRight = new Vec3(17.5f/16f, 29f/16f, -54f/16f * -1);
    public final Vec3 BarrelOffestLeft = new Vec3(8.5f/16f, 30.5f/16f, -54f/16f * -1);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public final MountedGun BarrelRight;
    public final MountedGun BarrelLeft;

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SHOOT_WALKING = RawAnimation.begin().thenPlay("ranged_attack");
    private static final RawAnimation SHOOT_STANDING = RawAnimation.begin().thenPlay("ranged_attack_stand");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation CHARGE_START = RawAnimation.begin().thenPlay("charge_start");
    private static final RawAnimation CHARGE_MIDDLE = RawAnimation.begin().thenPlay("charge_middle");
    private static final RawAnimation CHARGE_END = RawAnimation.begin().thenPlay("charge_end");
    private static final RawAnimation SCREAM = RawAnimation.begin().thenPlay("scream");
    private static final RawAnimation STUN = RawAnimation.begin().thenPlay("stun");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");


    public PiglinWarbossEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.BarrelRight = new AttachedMountedGun(this,
            new CustomScorchedSimGun.Builder(ModItems.CALLWELL_CONVERSION.get().getGun())
                .projectileDamage(3f)
                .fireRate(1)
                .maxRange(20)
                .idealRange(16)
                .ammoCapacity(6)
                .reloadTime(120)
                .noGunFlash()
                .gunIndex(0)
                .build(),
            BarrelOffestRight,
            (a) -> true
        );
        this.BarrelLeft = new AttachedMountedGun(this,
            new CustomScorchedSimGun.Builder(ModItems.CALLWELL_CONVERSION.get().getGun())
                .projectileDamage(3f)
                .fireRate(1)
                .maxRange(20)
                .idealRange(16)
                .ammoCapacity(6)
                .reloadTime(120)
                .noGunFlash()
                .gunIndex(1)
                .build(),
            BarrelOffestLeft,
            (a) -> true
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
        .add(Attributes.FOLLOW_RANGE, 128.0D)
        .add(Attributes.MOVEMENT_SPEED, 0.3F)
        .add(Attributes.ATTACK_DAMAGE, 35.0D)
        .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
        .add(Attributes.ARMOR, 6.0D)
        .add(Attributes.MAX_HEALTH, 600.0D);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (state.isMoving()) {
                state.setAnimation(WALK);
            } else {
                state.setAnimation(IDLE);
            }
            return PlayState.CONTINUE;
        }));

        controllers.add(new AnimationController<>(this, "death", 2, state -> {
            if (state.getAnimatable().isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "behavior", 0, state -> {
            return PlayState.CONTINUE;
        })
            .triggerableAnim("melee", ATTACK)
            .triggerableAnim("shoot_stand", SHOOT_STANDING)
            .triggerableAnim("shoot_walk", SHOOT_WALKING)
            .triggerableAnim("scream", SCREAM)
            .triggerableAnim("stun", STUN)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return switch (gunIndex) {
            case 0 -> this.BarrelOffestRight.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
            case 1 -> this.BarrelOffestLeft.yRot(-this.yHeadRot * Mth.DEG_TO_RAD);
            default -> Vec3.ZERO;
        };
    }

    @Override
    protected void tickDeath() {
        MobUtil.tickDeath(this, 40);
    }
    
}
