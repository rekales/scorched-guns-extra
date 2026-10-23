package net.zincstudios.scgextra.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import top.ribs.scguns.config.EntityEquipmentConfig;

@Mixin(value = EntityEquipmentConfig.class, remap = false)
public class EntityEquipmentConfigMixin {
    @Inject(method = "equipEntity", at = @At(value="HEAD"))
    private static void equipEntity(Mob mob, String entityId, CallbackInfo ci) {
        if(mob.getSpawnType() != null && mob.getSpawnType() == MobSpawnType.EVENT)ci.cancel();
    }
}