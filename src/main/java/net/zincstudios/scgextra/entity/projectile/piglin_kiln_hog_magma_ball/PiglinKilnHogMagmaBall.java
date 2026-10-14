package net.zincstudios.scgextra.entity.projectile.piglin_kiln_hog_magma_ball;

import net.zincstudios.scgextra.entity.piglin.PiglinEntities;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiglinKilnHogMagmaBall extends AbstractArrow implements GeoEntity{

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public PiglinKilnHogMagmaBall(EntityType<? extends PiglinKilnHogMagmaBall> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public PiglinKilnHogMagmaBall(LivingEntity pShooter, Level pLevel) {
        super(PiglinEntities.PIGLIN_KILN_HOG_MAGMA_BALL.get(), pShooter, pLevel);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if(!this.level().isClientSide()){
            List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(3));
            for(LivingEntity le : list){
                le.setSecondsOnFire(3);
                le.hurt(this.damageSources().lava(), 30);
            }
            EntityType.MAGMA_CUBE.spawn((ServerLevel)this.level(), this.blockPosition(), MobSpawnType.MOB_SUMMONED).setSize(2, true);
            EntityType.MAGMA_CUBE.spawn((ServerLevel)this.level(), this.blockPosition(), MobSpawnType.MOB_SUMMONED).setSize(2, true);
            EntityType.MAGMA_CUBE.spawn((ServerLevel)this.level(), this.blockPosition(), MobSpawnType.MOB_SUMMONED).setSize(2, true);
            this.discard();
        }
    }
}
