package net.zincstudios.scgextra.entity.projectile.piglin_kiln_hog_magma_ball;

import net.zincstudios.scgextra.entity.piglin.PiglinEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
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
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.discard();
    }
}
