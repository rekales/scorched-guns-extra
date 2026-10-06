package net.zincstudios.scgextra.entity.projectile;

import net.zincstudios.scgextra.entity.whaler.armoredwhale.ArmoredWhaleEntity;
// import net.zincstudios.scgextra.entity.whaler.armoredwhale.ArmoredWhalePart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import top.ribs.scguns.entity.projectile.EnemyProjectileEntity;

import javax.annotation.ParametersAreNonnullByDefault;

// Just so it can pass through itself
@ParametersAreNonnullByDefault
public class ArmoredWhaleProjectileEntity extends EnemyProjectileEntity {

    public ArmoredWhaleProjectileEntity(EntityType<? extends AbstractArrow> type, Level world) {
        super(type, world);
    }

    public ArmoredWhaleProjectileEntity(Level world, LivingEntity shooter) {
        super(world, shooter);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target)
                && !(target instanceof ArmoredWhaleEntity);
                // && !(target instanceof ArmoredWhalePart);
    }


    // For registration purposes
    @SuppressWarnings("unchecked")
    public static ArmoredWhaleProjectileEntity create(EntityType<? extends Entity> type, Level world) {
        return new ArmoredWhaleProjectileEntity((EntityType<? extends AbstractArrow>) type, world);
    }
}
