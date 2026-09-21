package net.zincstudios.scgextra.entity.whaler.armoredwhale;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zincstudios.scgextra.entity.common.part.RotatedSegmentPartEntity;
import net.zincstudios.scgextra.entity.whaler.WhalerEntities;
import net.zincstudios.scgextra.item.ModItems;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.network.PacketHandler;
import top.ribs.scguns.network.message.S2CMessageTurretBulletTrail;

//custom projectile to not hit the shooter
public class MountedGunProjectileEntity extends ProjectileEntity{
    private boolean trailSpawned;
    public MountedGunProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.modifiedGun = ModItems.PLACEHOLDER_GUN.get().getGun();
        this.trailSpawned = false;
    }
    
    public MountedGunProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.trailSpawned = false;
    }
    
    public MountedGunProjectileEntity(Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        this(WhalerEntities.MOUNTED_GUN_PROJECTILE.get(), worldIn, shooter, weapon, item, modifiedGun);
    }

    public MountedGunProjectileEntity(Level worldIn, LivingEntity shooter, Gun gun) {
        this(WhalerEntities.MOUNTED_GUN_PROJECTILE.get(), worldIn, shooter, new ItemStack(ModItems.PLACEHOLDER_GUN.get()), ModItems.PLACEHOLDER_GUN.get(), gun);
    }
    @Override
    public void onHit(HitResult result, Vec3 startVec, Vec3 endVec) {
        if(result instanceof EntityHitResult ehr){
            if(ehr.getEntity() instanceof ArmoredWhaleEntity){
                return;
            }
            if(ehr.getEntity() instanceof RotatedSegmentPartEntity rspe && rspe.getParent() instanceof ArmoredWhaleEntity){
                return;
            }
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
