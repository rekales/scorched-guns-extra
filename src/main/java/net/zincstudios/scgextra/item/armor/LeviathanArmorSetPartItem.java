package net.zincstudios.scgextra.item.armor;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.ribs.scguns.init.ModEffects;

public class LeviathanArmorSetPartItem extends ArmorSetPartWithExtrasItem{
    private static final Map<ArmorItem.Type, Integer> SLOT_MAP = Map.of(
        ArmorItem.Type.HELMET, 3,
        ArmorItem.Type.CHESTPLATE, 2,
        ArmorItem.Type.LEGGINGS, 1,
        ArmorItem.Type.BOOTS, 0
    );

    private final List<MobEffect> MOB_EFFECTS = List.of(
        MobEffects.POISON,
        MobEffects.WITHER,
        MobEffects.BLINDNESS,
        MobEffects.CONFUSION,
        MobEffects.MOVEMENT_SLOWDOWN,
        ModEffects.LACERATED.get(),
        ModEffects.SULFUR_POISONING.get()
    );

    public LeviathanArmorSetPartItem(ArmorMaterial material, Type type, ArmorSet armorSet, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet, modifiers);
    }

    public LeviathanArmorSetPartItem(ArmorMaterial material, Type type, ArmorSet armorSet, Properties properties, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet, properties, modifiers);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if(!(entity instanceof LivingEntity owner))return;
        if(level.isClientSide())return;
        CompoundTag tag = entity.getPersistentData();
        if(level.getGameTime()>tag.getLong("leviathanClenseCooldownEnd")){
            for(ArmorItem.Type type : SLOT_MAP.keySet()){
                for (MobEffect entry : MOB_EFFECTS) {
                    if(this.getType() == type && slotId == SLOT_MAP.get(type) && owner.hasEffect(entry)){
                        owner.removeEffect(entry);
                        tag.putLong("leviathanClenseCooldownEnd", entity.level().getGameTime() + 200);
                    }
                }
            }
        }
    }
}
