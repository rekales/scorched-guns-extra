package net.zincstudios.scgextra.entity.projectile.ink_projectile;

import net.zincstudios.scgextra.effects.ModEffects;
import net.zincstudios.scgextra.entity.ModEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.zincstudios.scgextra.entity.whaler.tentacliator.TentacliatorEntity;

public class InkProjectileEntity extends AbstractArrow{
    public InkProjectileEntity(EntityType<? extends InkProjectileEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public InkProjectileEntity(LivingEntity pShooter, Level pLevel) {
        super(ModEntities.INK_PROJECTILE.get(), pShooter, pLevel);
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
}
