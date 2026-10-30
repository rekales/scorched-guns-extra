package net.zincstudios.scgextra.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.zincstudios.scgextra.attributes.SCGEAttributes;
import net.zincstudios.scgextra.entity.ModBrainMemories;
import net.zincstudios.scgextra.entity.asgharian.WeakPointPart;
import net.zincstudios.scgextra.entity.common.HeadShotHandler;
import net.zincstudios.scgextra.entity.common.WeakPointBox;
import net.zincstudios.scgextra.entity.common.WeakPointBoxManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.Config;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.init.ModDamageTypes;
import top.ribs.scguns.util.math.ExtendedEntityRayTraceResult;

import java.util.List;
import java.util.Optional;

// TODO: test if GunProjectileHitEvent can be utilized
@Mixin(value = ProjectileEntity.class, remap = false)
public class ProjectileEntityMixin {

    @WrapOperation(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Ltop/ribs/scguns/item/GunItem;Ltop/ribs/scguns/common/Gun;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ltop/ribs/scguns/util/GunModifierHelper;getModifiedProjectileGravity(Lnet/minecraft/world/item/ItemStack;D)D"
            )
    )
    private double atGetModifiedGravity(ItemStack weapon, double gravity, Operation<Double> original, @Local(argsOnly = true) LivingEntity shooter) {
        double modifiedGravity = original.call(weapon, gravity);
        if (shooter instanceof Player player) {
            modifiedGravity *= (float) (1+(player.getAttributeValue(SCGEAttributes.BULLET_GRAVITY_MULT.get())/100));
        }
        return modifiedGravity;
    }

    @WrapOperation(
            method = "getCriticalDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Ltop/ribs/scguns/util/GunModifierHelper;getCriticalChance(Lnet/minecraft/world/item/ItemStack;)F"
            ))
    private float atGetCriticalChance(ItemStack weapon, Operation<Float> original) {
        ProjectileEntity self = (ProjectileEntity) (Object) this;
        float chance = original.call(weapon);
        if (self.getShooter() instanceof Player player) {
            chance += (float) ((player.getAttributeValue(SCGEAttributes.BULLET_ADDITIONAL_CRIT_CHANCE.get())/100));
            return Mth.clamp(chance, 0, 1);
        }
        return chance;
    }
    @WrapOperation(
        method = "getCriticalDamage",
        at = @At(
            value = "INVOKE",
            target = "Ltop/ribs/scguns/common/Gun$Projectile;getCritDamageMultiplier()F"
        ))
    private float atGetCritDamageMultiplier(Gun.Projectile instance, Operation<Float> original) {
        ProjectileEntity self = (ProjectileEntity) (Object) this;
        float damage = original.call(instance);
        if (self.getShooter() instanceof Player player && (player.getAttributeValue(SCGEAttributes.BULLET_ADDITIONAL_CRIT_DAMAGE_MULT.get())/100)>0.0) {
            damage *= (float) (1+(player.getAttributeValue(SCGEAttributes.BULLET_ADDITIONAL_CRIT_DAMAGE_MULT.get())/100));
            return damage;
        }
        return damage;
    }

    @WrapOperation(
            method = "onHit",
            at = @At(
                    value = "INVOKE",
                    target = "Ltop/ribs/scguns/entity/projectile/ProjectileEntity;onHitEntity(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Z)V"
            ))
    private void atOnHit(ProjectileEntity instance, Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot,
                         Operation<Void> original, @Local(name = "entityHitResult") ExtendedEntityRayTraceResult entityHitResult) {
        ProjectileEntity self = (ProjectileEntity) (Object) this;

        if (entityHitResult.getEntity() instanceof PartEntity<?> partEntity && partEntity instanceof WeakPointPart) {
            headshot = true;
            SCGE$handleHeadshot(partEntity.getParent(), self);
        } else if (headshot) {
            SCGE$handleHeadshot(entityHitResult.getEntity(), self);
        }

        original.call(instance, entity, hitVec, startVec, endVec, headshot);
    }

    @Unique
    private static void SCGE$handleHeadshot(Entity hitEntity, ProjectileEntity projectile) {
        DamageSource source = ModDamageTypes.Sources.projectile(projectile.level().registryAccess(), projectile, projectile.getShooter());
        if (hitEntity instanceof HeadShotHandler headShotHandler) {
            headShotHandler.headshot(source, projectile.getDamage());
        }
        if (hitEntity instanceof LivingEntity livingEntity) {
            Brain<?> brain = livingEntity.getBrain();
            if (brain.checkMemory(ModBrainMemories.HEADSHOT_COUNT.get(), MemoryStatus.REGISTERED)) {
                int headshotCount = brain.getMemory(ModBrainMemories.HEADSHOT_COUNT.get()).orElse(0);
                brain.setMemory(ModBrainMemories.HEADSHOT_COUNT.get(), ++headshotCount);
            }
        }
    }

        // TODO: Requires fix on ScorchedGuns to use this proper method
//    @Inject(
//            method = "onHitEntity",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
//            )
//    )
//    private void beforeEntityHurt(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot,
//                                  CallbackInfo ci, @Local(name = "source") DamageSource source, @Local(name = "damage") float damage) {
//        if (headshot && entity instanceof HeadShotHandler headShotHandler) {
//            headShotHandler.headshot(source, damage);
//        }
//    }

    @Inject(
            method = "getHitResult",
            at = @At(
                    value = "INVOKE",
                    target = "Ltop/ribs/scguns/common/BoundingBoxManager;getHeadshotBoxes(Lnet/minecraft/world/entity/EntityType;)Ltop/ribs/scguns/interfaces/IHeadshotBox;"
            ),
            remap = false
    )
    private void beforeGetHeadshotBoxes(Entity entity, Vec3 startVec, Vec3 endVec, CallbackInfoReturnable<ProjectileEntity.EntityResult> cir,
                                        @Local(name = "boundingBox") AABB boundingBox, @Local(name = "hitPos") Vec3 hitPos, @Local(name = "headshot") LocalBooleanRef headshot) {
        List<WeakPointBox<LivingEntity>> weakPointBoxList = WeakPointBoxManager.getWeakPointBoxesList(entity.getType());

        if (weakPointBoxList != null) {
            for (WeakPointBox<LivingEntity> weakPointBox : weakPointBoxList) {
                AABB box = weakPointBox.getBoundingBox((LivingEntity)entity);
                if (box != null) {
                    box = box.move(boundingBox.getCenter().x, boundingBox.minY, boundingBox.getCenter().z);
                    Optional<Vec3> headshotHitPos = box.clip(startVec, endVec);
                    if (headshotHitPos.isEmpty()) {
                        box = box.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0.0, Config.COMMON.gameplay.growBoundingBoxAmount.get());
                        headshotHitPos = box.clip(startVec, endVec);
                    }

                    if (headshotHitPos.isPresent() && (hitPos == null || headshotHitPos.get().distanceTo(hitPos) < 0.5)) {
                        hitPos = headshotHitPos.get();
                        headshot.set(true);
//                        headshot = true;
                    }
                }
            }
        }
    }
}