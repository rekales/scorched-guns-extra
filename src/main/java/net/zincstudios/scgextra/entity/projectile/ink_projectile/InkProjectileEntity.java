package net.zincstudios.scgextra.entity.projectile.ink_projectile;

import net.zincstudios.scgextra.effects.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.zincstudios.scgextra.entity.whaler.WhalerEntities;
import net.zincstudios.scgextra.entity.whaler.tentacliator.TentacliatorEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class InkProjectileEntity extends AbstractArrow implements GeoEntity, VariantHolder<Integer>{
    private final AnimatableInstanceCache geocache = GeckoLibUtil.createInstanceCache(this);

    public InkProjectileEntity(EntityType<? extends InkProjectileEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public InkProjectileEntity(LivingEntity pShooter, Level pLevel) {
        super(WhalerEntities.INK_PROJECTILE.get(), pShooter, pLevel);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if(this.getOwner() instanceof TentacliatorEntity te){
            if(te.isGlowing()){
                if(entityHitResult.getEntity() instanceof Player p){
                    p.addEffect(new MobEffectInstance(ModEffects.GLOWING_INK_EFFECT.get(), 100, 1));
                    this.remove(RemovalReason.DISCARDED);
                    return;
                }
            }else{
                if(entityHitResult.getEntity() instanceof Player p){
                    p.addEffect(new MobEffectInstance(ModEffects.INK_EFFECT.get(), 100, 1));
                    this.remove(RemovalReason.DISCARDED);
                    return;
                }
            }
        }
        super.onHitEntity(entityHitResult);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.discard();
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.STRING);
    }
    @Override
    public void tick() {
        super.tick();
        if(!this.level().isClientSide())this.level().addParticle(ParticleTypes.DRIPPING_WATER, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geocache;
    }

    @Override
    public Integer getVariant() {
        return (this.getOwner() instanceof TentacliatorEntity te) ? te.getVariant() : 1;
    }

    @Override
    public void setVariant(Integer variant) {
    }
}
