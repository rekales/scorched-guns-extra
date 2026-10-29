package net.zincstudios.scgextra.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.zincstudios.scgextra.attributes.SCGEAttributes;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Entity.class)
public class EntityMixin {

    @WrapMethod(method = "dampensVibrations")
    private boolean atDampensVibrations(Operation<Boolean> original) {
        if ((Object)this instanceof LivingEntity entity) {
            AttributeInstance ai = entity.getAttribute(SCGEAttributes.ENLIGHTENED.get());
            if(ai!=null && ai.getValue()>=1.0){
                return true;
            }
        }

        return original.call();
    }
}
