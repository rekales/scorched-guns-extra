package net.zincstudios.scgextra.attributes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;

public final class SCGEAttributes {

    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister
            .create(ForgeRegistries.ATTRIBUTES, SCGExtra.MOD_ID);

    public static final RegistryObject<Attribute> RECOIL_MULT = ATTRIBUTES.register("recoil_multiplier",
            () -> (new RangedAttribute("attribute.scgextra.recoil_multiplier", 1.0F, 0.01, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> BULLET_DAMAGE_TAKEN_MULT = ATTRIBUTES.register("bullet_damage_taken_multiplier",
            () -> (new RangedAttribute("attribute.scgextra.recoil_multiplier", 1.0F, 0.01, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> BULLET_ADDITIONAL_CRIT_CHANCE = ATTRIBUTES.register("bullet_additional_crit_chance",
            () -> (new RangedAttribute("attribute.scgextra.recoil_multiplier", 0.0F, -100, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> BULLET_GRAVITY_MULT = ATTRIBUTES.register("bullet_gravity_multiplier",
            () -> (new RangedAttribute("attribute.scgextra.recoil_multiplier", 1.0F, 0.01, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> UNDEAD_DAMAGE_MULT = ATTRIBUTES.register("undead_damage_multiplier",
            () -> (new RangedAttribute("attribute.scgextra.undead_damage_multiplier", 1.0F, 0.0001, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> MAGIC_DAMAGE_REDUCTION = ATTRIBUTES.register("magic_damage_reduction",
            () -> (new RangedAttribute("attribute.scgextra.magic_damage_reduction", 1.0F, 0.0001, 100.0F)).setSyncable(true));

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(SCGEAttributes::onEntityAttributeModification);
    }

    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, RECOIL_MULT.get());
        event.add(EntityType.PLAYER, BULLET_DAMAGE_TAKEN_MULT.get());
        event.add(EntityType.PLAYER, BULLET_ADDITIONAL_CRIT_CHANCE.get());
        event.add(EntityType.PLAYER, BULLET_GRAVITY_MULT.get());
        event.add(EntityType.PLAYER, UNDEAD_DAMAGE_MULT.get());
        event.add(EntityType.PLAYER, MAGIC_DAMAGE_REDUCTION.get());
    }
}
