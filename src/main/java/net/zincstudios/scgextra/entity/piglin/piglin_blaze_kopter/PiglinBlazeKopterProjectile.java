package net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter;

import net.zincstudios.scgextra.entity.piglin.PiglinEntities;
import net.zincstudios.scgextra.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageTurretBulletTrail;

public class PiglinBlazeKopterProjectile extends ProjectileEntity{
private boolean trailSpawned;
    public PiglinBlazeKopterProjectile(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.modifiedGun = ModItems.PLACEHOLDER_GUN.get().getGun();
        this.trailSpawned = false;
    }
    
    public PiglinBlazeKopterProjectile(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.trailSpawned = false;
    }
    
    public PiglinBlazeKopterProjectile(Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        this(PiglinEntities.PIGLIN_BLAZE_HOPTER_PROJECTILE.get(), worldIn, shooter, weapon, item, modifiedGun);
    }

    public PiglinBlazeKopterProjectile(Level worldIn, LivingEntity shooter, Gun gun) {
        this(PiglinEntities.PIGLIN_BLAZE_HOPTER_PROJECTILE.get(), worldIn, shooter, new ItemStack(ModItems.PLACEHOLDER_GUN.get()), ModItems.PLACEHOLDER_GUN.get(), gun);
    }

    @Override
    public void onHit(HitResult result, Vec3 startVec, Vec3 endVec) {
        if(result instanceof EntityHitResult ehr){
            ehr.getEntity().setSecondsOnFire(3);
        }
        super.onHit(result, startVec, endVec);
    }
    //copied from EnemyProjectileEntity
    @Override
    public void tick() {
        if (!this.level().isClientSide && !this.trailSpawned && this.tickCount == 1) {
            this.spawnBulletTrail();
            this.trailSpawned = true;
        }
        super.tick();
    }
    private void spawnBulletTrail() {
        Vec3 position = this.position();
        Vec3 motion = this.getDeltaMovement();
        int trailColor = 16755200;
        double trailLength = (double)1.0F;
        int maxAge = 300;
        double trailThickness = 0.7;
        S2CMessageTurretBulletTrail message = new S2CMessageTurretBulletTrail(this.getId(), position, motion, trailColor, trailLength, maxAge, trailThickness);
        PacketHandler.getPlayChannel().sendToTrackingEntity(() -> this, message);
    }
}
