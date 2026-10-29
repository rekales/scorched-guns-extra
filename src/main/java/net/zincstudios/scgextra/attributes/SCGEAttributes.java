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
            () -> (new RangedAttribute("attribute.scgextra.bullet_damage_taken_multiplier", 1.0F, 0.01, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> BULLET_ADDITIONAL_CRIT_CHANCE = ATTRIBUTES.register("bullet_additional_crit_chance",
            () -> (new RangedAttribute("attribute.scgextra.bullet_additional_crit_chance", 0.0F, -100, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> BULLET_ADDITIONAL_CRIT_DAMAGE_MULT = ATTRIBUTES.register("bullet_additional_crit_damage_multiplier",
            () -> (new RangedAttribute("attribute.scgextra.bullet_additional_crit_damage_multiplier", 1.0F, -100, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> BULLET_GRAVITY_MULT = ATTRIBUTES.register("bullet_gravity_multiplier",
            () -> (new RangedAttribute("attribute.scgextra.bullet_gravity_multiplier", 1.0F, 0.01, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> UNDEAD_DAMAGE_MULT = ATTRIBUTES.register("undead_damage_multiplier",
            () -> (new RangedAttribute("attribute.scgextra.undead_damage_multiplier", 1.0F, -100, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> MAGIC_DAMAGE_REDUCTION = ATTRIBUTES.register("magic_damage_reduction",
            () -> (new RangedAttribute("attribute.scgextra.magic_damage_reduction", 1.0F, -100, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> DEBUFF_ZONE = ATTRIBUTES.register("debuff_zone",
            () -> (new RangedAttribute("attribute.scgextra.debuff_zone", 0F, 0, 100.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> GENERAL_DAMAGE_MULT = ATTRIBUTES.register("general_damage_mult",
            () -> (new RangedAttribute("attribute.scgextra.general_damage_mult", 1F, -100F, 100.0F)).setSyncable(true));
    //probably not the best way of doing it
    public static final RegistryObject<Attribute> OPPRESSOR = ATTRIBUTES.register("oppressor",
        () -> (new RangedAttribute("attribute.scgextra.oppressor", 0F, 0, 2.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> COMMISSAR = ATTRIBUTES.register("commissar",
        () -> (new RangedAttribute("attribute.scgextra.commissar", 0F, 0, 2.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> WRECEKR = ATTRIBUTES.register("wrecker",
        () -> (new RangedAttribute("attribute.scgextra.wrecker", 0F, 0, 2.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> LEVIATHAN = ATTRIBUTES.register("leviathan",
        () -> (new RangedAttribute("attribute.scgextra.leviathan", 0F, 0, 2.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> GOLDEN_IDOL = ATTRIBUTES.register("golden_idol",
        () -> (new RangedAttribute("attribute.scgextra.golden_idol", 0F, 0, 2.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> ENLIGHTENED = ATTRIBUTES.register("enlightened",
        () -> (new RangedAttribute("attribute.scgextra.enlightened", 0F, 0, 2.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> JUGGERNAUT = ATTRIBUTES.register("juggernaut",
        () -> (new RangedAttribute("attribute.scgextra.juggernaut", 0F, 0, 2.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> RITUAL = ATTRIBUTES.register("ritual",
        () -> (new RangedAttribute("attribute.scgextra.ritual", 0F, 0, 2.0F)).setSyncable(true));
    public static final RegistryObject<Attribute> PIONEER = ATTRIBUTES.register("pioneer",
        () -> (new RangedAttribute("attribute.scgextra.pioneer", 0F, 0, 2.0F)).setSyncable(true));

    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(SCGEAttributes::onEntityAttributeModification);
    }

    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, RECOIL_MULT.get());
        event.add(EntityType.PLAYER, BULLET_DAMAGE_TAKEN_MULT.get());
        event.add(EntityType.PLAYER, BULLET_ADDITIONAL_CRIT_CHANCE.get());
        event.add(EntityType.PLAYER, BULLET_ADDITIONAL_CRIT_DAMAGE_MULT.get());
        event.add(EntityType.PLAYER, BULLET_GRAVITY_MULT.get());
        event.add(EntityType.PLAYER, UNDEAD_DAMAGE_MULT.get());
        event.add(EntityType.PLAYER, MAGIC_DAMAGE_REDUCTION.get());
        event.add(EntityType.PLAYER, DEBUFF_ZONE.get());
        event.add(EntityType.PLAYER, GENERAL_DAMAGE_MULT.get());

        event.add(EntityType.PLAYER, OPPRESSOR.get());
        event.add(EntityType.PLAYER, COMMISSAR.get());
        event.add(EntityType.PLAYER, WRECEKR.get());
        event.add(EntityType.PLAYER, LEVIATHAN.get());
        event.add(EntityType.PLAYER, GOLDEN_IDOL.get());
        event.add(EntityType.PLAYER, ENLIGHTENED.get());
        event.add(EntityType.PLAYER, JUGGERNAUT.get());
        event.add(EntityType.PLAYER, RITUAL.get());
        event.add(EntityType.PLAYER, PIONEER.get());
    }
}
