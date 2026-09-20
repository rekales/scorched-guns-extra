package net.zincstudios.scgextra.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.zincstudios.scgextra.entity.whaler.armoredwhale.ArmoredWhaleEntity;
// import net.zincstudios.scgextra.entity.whaler.armoredwhale.ArmoredWhalePart;
import org.spongepowered.asm.mixin.Mixin;
import top.ribs.scguns.entity.projectile.EnemyProjectileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@Mixin(value = EnemyProjectileEntity.class)
public abstract class EnemyProjectileEntityMixin extends AbstractArrow {

    private EnemyProjectileEntityMixin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    // fucky solution for making the client side projectiles pass through the whale's big hitboxes.
    // NOTE: Only causes rare visual issues but still needs to be replaced with a better solution later.
    @Override
    protected boolean canHitEntity(Entity target) {
        // Spawned clientside projectiles have no owners for some fucking reason.
        if (target.level().isClientSide() && (
            target instanceof ArmoredWhaleEntity 
            // || 
            // target instanceof ArmoredWhalePart
        )) {
            return false;
        }
        return super.canHitEntity(target);
    }
}
