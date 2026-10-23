package net.zincstudios.scgextra.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;

@Mixin(value = Piglin.class)
public class PiglinMixin {
    @Inject(method = "maybeWearArmor", at = @At(value="HEAD"))
    private void atMaybeWearArmor(EquipmentSlot slot, ItemStack stack, RandomSource random, CallbackInfo ci) {
        if (
            ((Object)this instanceof Mob mob) && 
            (mob.getSpawnType() != null) && 
            (mob.getSpawnType() == MobSpawnType.EVENT)
        )ci.cancel();
    }
}
