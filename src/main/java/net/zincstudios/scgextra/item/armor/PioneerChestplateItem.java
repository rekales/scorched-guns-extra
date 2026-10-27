package net.zincstudios.scgextra.item.armor;

import java.util.Map;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.zincstudios.scgextra.attributes.SCGEAttributes;

public class PioneerChestplateItem extends ArmorSetPartWithExtrasItem {

    public PioneerChestplateItem(ArmorMaterial material, Type type, ArmorSet armorSet, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet, modifiers);
    }

    public PioneerChestplateItem(ArmorMaterial material, Type type, ArmorSet armorSet, Properties properties, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet, properties, modifiers);
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return (entity.getAttribute(SCGEAttributes.PIONEER_FLY.get())!=null) ? (entity.getAttribute(SCGEAttributes.PIONEER_FLY.get()).getValue() >= 1.0) : false;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!entity.level().isClientSide) {
            int nextFlightTick = flightTicks + 1;
            if (nextFlightTick % 10 == 0) {
                entity.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ELYTRA_GLIDE);
            }
        }
        return true;
    }
}
