package net.zincstudios.scgextra.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zincstudios.scgextra.attributes.SCGEAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.ribs.scguns.client.handler.RecoilHandler;
import top.ribs.scguns.common.Gun;

@Mixin(value = RecoilHandler.class, remap = false)
public class RecoilHandlerMixin {

    @WrapOperation(
            method = "onGunFire",
            at = @At(
                    value = "INVOKE",
                    target = "Ltop/ribs/scguns/client/handler/RecoilHandler;getAdsRecoilReduction(Ltop/ribs/scguns/common/Gun;)D"
            )
    )
    private double onGetAdsRecoilReduction(RecoilHandler instance, Gun gun, Operation<Double> original) {
        assert Minecraft.getInstance().player != null;  // already checked in the condition block where this is called
        return (float) Minecraft.getInstance().player.getAttributeValue(SCGEAttributes.RECOIL_MULT.get()) * original.call(instance, gun);
    }

    @WrapOperation(
            method = "onRenderOverlay",
            at = @At(
                    value = "INVOKE",
                    target = "Ltop/ribs/scguns/util/GunEnchantmentHelper;getRecoilModifier(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)F"
            )
    )
    private float onGetEnchantmentModifier(Player player, ItemStack weapon, Operation<Float> original) {
        return (float)(1+(player.getAttributeValue(SCGEAttributes.RECOIL_MULT.get())/100)) * original.call(player, weapon);
    }

}
