package net.zincstudios.scgextra.item.armor;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;

public class ArmorSetPartWithExtrasItem extends GeoArmorSetPartItem{
    ListMultimap<Attribute, AttributeModifier> multimap;

    public ArmorSetPartWithExtrasItem(ArmorMaterial material, Type type, ArmorSet armorSet, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet);
        multimap = ArrayListMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> e : modifiers.entrySet()) {
            multimap.put(e.getKey(), e.getValue());
        }
    }

    public ArmorSetPartWithExtrasItem(ArmorMaterial material, Type type, ArmorSet armorSet, Properties properties, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet, properties);
        multimap = ArrayListMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> e : modifiers.entrySet()) {
            multimap.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        return equipmentSlot == this.type.getSlot() ? this.multimap : super.getDefaultAttributeModifiers(equipmentSlot);
    }
}
