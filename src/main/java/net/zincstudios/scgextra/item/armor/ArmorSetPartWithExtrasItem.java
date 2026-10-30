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

    public final String armorTypeName;

    public ArmorSetPartWithExtrasItem(ArmorMaterial material, Type type, ArmorSet armorSet, Map<Attribute, AttributeModifier> modifiers, String armorTypeName) {
        super(material, type, armorSet);
        multimap = ArrayListMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> e : modifiers.entrySet()) {
            multimap.put(e.getKey(), e.getValue());
        }
        this.armorTypeName = armorTypeName;
    }

    public ArmorSetPartWithExtrasItem(ArmorMaterial material, Type type, ArmorSet armorSet, Properties properties, Map<Attribute, AttributeModifier> modifiers, String armorTypeName) {
        super(material, type, armorSet, properties);
        multimap = ArrayListMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> e : modifiers.entrySet()) {
            multimap.put(e.getKey(), e.getValue());
        }
        this.armorTypeName = armorTypeName;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        Multimap<Attribute, AttributeModifier> map = super.getDefaultAttributeModifiers(equipmentSlot);
        ListMultimap<Attribute, AttributeModifier> lmap = ArrayListMultimap.create();
        lmap.putAll(map);
        if(equipmentSlot == this.type.getSlot())lmap.putAll(this.multimap);
        return lmap;
    }
}
