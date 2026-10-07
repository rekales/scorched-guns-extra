package net.zincstudios.scgextra.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.ribs.scguns.blockentity.MineUnitBlockEntity;

@Mixin(value = MineUnitBlockEntity.class)
public class MineUnitBlockEntityMixin {

    @WrapOperation(
            method = "triggerGrenade",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/item/ItemEntity;getControllingPassenger()Lnet/minecraft/world/entity/LivingEntity;"
            )
    )
    private static LivingEntity tickHas(ItemEntity instance, Operation<LivingEntity> original) {
        return new ArmorStand(instance.level(), instance.getX(), instance.getY(), instance.getZ());
    }
}
