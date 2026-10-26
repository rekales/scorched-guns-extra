package net.zincstudios.scgextra.item.armor;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArmorSetPartWithExtrasItem extends GeoArmorSetPartItem{
    private final Map<Attribute, AttributeModifier> mod;
    private final Map<UUID, Attribute> ALL_MODIFIER_ATTRIBUTES;
    private static final Map<ArmorItem.Type, Integer> SLOT_MAP = Map.of(
        ArmorItem.Type.HELMET, 3,
        ArmorItem.Type.CHESTPLATE, 2,
        ArmorItem.Type.LEGGINGS, 1,
        ArmorItem.Type.BOOTS, 0
    );

    public ArmorSetPartWithExtrasItem(ArmorMaterial material, Type type, ArmorSet armorSet, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet);
        mod = new HashMap<>();
        ALL_MODIFIER_ATTRIBUTES = new HashMap<>();
        for (Map.Entry<Attribute, AttributeModifier> e : modifiers.entrySet()) {
            ALL_MODIFIER_ATTRIBUTES.put(e.getValue().getId(), e.getKey());
            mod.put(e.getKey(), e.getValue());
        }
    }

    public ArmorSetPartWithExtrasItem(ArmorMaterial material, Type type, ArmorSet armorSet, Properties properties, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet, properties);
        mod = new HashMap<>();
        ALL_MODIFIER_ATTRIBUTES = new HashMap<>();
        for (Map.Entry<Attribute, AttributeModifier> e : modifiers.entrySet()) {
            ALL_MODIFIER_ATTRIBUTES.put(e.getValue().getId(), e.getKey());
            mod.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if(!(entity instanceof LivingEntity owner))return;
        if(level.isClientSide())return;
        for (Map.Entry<UUID, Attribute> entry : ALL_MODIFIER_ATTRIBUTES.entrySet()) {
            UUID uuid = entry.getKey();
            AttributeInstance attr = owner.getAttribute(entry.getValue());
            if (attr == null) continue;
            for(ArmorItem.Type type : SLOT_MAP.keySet()){
                if(this.getType() == type){
                    if(slotId == SLOT_MAP.get(type)){
                        if(!attr.hasModifier(mod.get(entry.getValue()))){
                            attr.addTransientModifier(mod.get(entry.getValue()));
                        }
                    }else{
                        if(attr.hasModifier(mod.get(entry.getValue()))){
                            attr.removeModifier(uuid);
                        }
                    }
                }
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
}
