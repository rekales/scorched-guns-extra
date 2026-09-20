package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;

import net.zincstudios.scgextra.entity.asgharian.BulletSpawnOffset;
import net.zincstudios.scgextra.entity.common.Gunner;
import net.zincstudios.scgextra.entity.common.gun.CustomGunHolder;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;

@ParametersAreNonnullByDefault
public class ArmoredWhaleEntity extends Monster implements GeoEntity, CustomGunHolder, Gunner, BulletSpawnOffset {

    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);

    private final SimulatedGun customGun;//temporary, make 2 guns

    public ArmoredWhaleEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.customGun = new net.zincstudios.scgextra.entity.common.gun.CustomScorchedSimGun.Builder(ModItems.HOWLER.get().getGun())
            .fireRate(80)
            .burstAmount(3)
            .burstInterval(8)
            .maxRange(6)
            .idealRange(5)
            .ammoCapacity(3)
            .reloadTime(80)
            .build();
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    public Vec3 getBulletSpawnOffset(int gunIndex) {
        return Vec3.ZERO;
    }

    @Override
    public SimulatedGun getCustomGun() {
        return this.customGun;
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.FOLLOW_RANGE, 64.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35F)
            .add(Attributes.ATTACK_DAMAGE, 20.0D)
            .add(Attributes.ARMOR, 8.0D)
            .add(Attributes.MAX_HEALTH, 800.0D);
    }
}