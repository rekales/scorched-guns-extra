package net.zincstudios.scgextra.item.armor;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum ModArmorMaterials implements StringRepresentable, ArmorMaterial {

    OPPRESSOR("oppressor",
            50,
            Util.make(new EnumMap<>(ArmorItem.Type.class),
            map -> {
                map.put(ArmorItem.Type.HELMET, 5);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.BOOTS, 3);
            }),
            12,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0F,
            0.1F,
            () -> Ingredient.of(Items.COPPER_BLOCK, Items.EXPOSED_COPPER, Items.OXIDIZED_COPPER)
    ),
    COMMISSAR("commissar",
            50,
            Util.make(new EnumMap<>(ArmorItem.Type.class),
            map -> {
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.CHESTPLATE, 7);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.BOOTS, 3);
            }),
            20,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            3.0F,
            0.05F,
            () -> Ingredient.of(Items.IRON_INGOT)
    ),
    WRECKER("wrecker",
            40,
            Util.make(new EnumMap<>(ArmorItem.Type.class),
                    map -> {
                        map.put(ArmorItem.Type.HELMET, 4);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.BOOTS, 3);
                    }),
            10,
            SoundEvents.ARMOR_EQUIP_IRON,
            3.0F,
            0.2F,
            () -> Ingredient.of(ModItems.TREATED_IRON_INGOT.get())
    ),
    LEVIATHAN("leviathan",
            60,
            Util.make(new EnumMap<>(ArmorItem.Type.class),
                    map -> {
                        map.put(ArmorItem.Type.HELMET, 10);
                        map.put(ArmorItem.Type.CHESTPLATE, 9);
                        map.put(ArmorItem.Type.LEGGINGS, 5);
                        map.put(ArmorItem.Type.BOOTS, 4);
                    }),
            16,
            SoundEvents.ARMOR_EQUIP_TURTLE,
            3.0F,
            0.1F,
            () -> Ingredient.of(Items.PRISMARINE, Items.PRISMARINE_BRICKS)
    ),
    GOLDEN_IDOL("golden_idol",
            50,
            Util.make(new EnumMap<>(ArmorItem.Type.class),
                    map -> {
                        map.put(ArmorItem.Type.HELMET, 5);
                        map.put(ArmorItem.Type.CHESTPLATE, 9);
                        map.put(ArmorItem.Type.LEGGINGS, 7);
                        map.put(ArmorItem.Type.BOOTS, 5);
                    }),
            24,
            SoundEvents.ARMOR_EQUIP_GOLD,
            3.0F,
            0.1F,
            () -> Ingredient.of(Items.GOLD_INGOT)
    ),
    ENLIGHTENED("enlightened",
            50,
            Util.make(new EnumMap<>(ArmorItem.Type.class),
                    map -> {
                        map.put(ArmorItem.Type.HELMET, 5);
                        map.put(ArmorItem.Type.CHESTPLATE, 6);
                        map.put(ArmorItem.Type.LEGGINGS, 5);
                        map.put(ArmorItem.Type.BOOTS, 4);
                    }),
            16,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            3.0F,
            0.05F,
            () -> Ingredient.of(Items.IRON_INGOT)
    ),
    JUGGERNAUT("juggernaut",
            65,
            Util.make(new EnumMap<>(ArmorItem.Type.class),
                    map -> {
                        map.put(ArmorItem.Type.HELMET, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 11);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.BOOTS, 5);
                    }),
            16,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            4.0F,
            0.2F,
            () -> Ingredient.of(ModItems.TREATED_BRASS_INGOT.get())
    ),
    RITUAL("ritual",
            50,
            Util.make(new EnumMap<>(ArmorItem.Type.class),
                    map -> {
                        map.put(ArmorItem.Type.HELMET, 4);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.BOOTS, 4);
                    }),
            12,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            4.0F,
            0.1F,
            () -> Ingredient.of(ModItems.DIAMOND_STEEL_INGOT.get())
    ),
    PIONEER("pioneer",
            50,
            Util.make(new EnumMap<>(ArmorItem.Type.class),
                    map -> {
                        map.put(ArmorItem.Type.HELMET, 4);
                        map.put(ArmorItem.Type.CHESTPLATE, 9);
                        map.put(ArmorItem.Type.LEGGINGS, 7);
                        map.put(ArmorItem.Type.BOOTS, 4);
                    }),
            12,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            4.0F,
            0.2F,
            () -> Ingredient.of(Items.SHULKER_SHELL, ModItems.DIAMOND_STEEL_INGOT.get())
    );


    private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.HELMET, 10);
        map.put(ArmorItem.Type.CHESTPLATE, 14);
        map.put(ArmorItem.Type.LEGGINGS, 13);
        map.put(ArmorItem.Type.BOOTS, 12);
    });

    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, EnumMap<ArmorItem.Type, Integer> protectionFunctionForType, int enchantmentValue, SoundEvent sound, float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionFunctionForType = protectionFunctionForType;
        this.enchantmentValue = enchantmentValue;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    public int getDurabilityForType(ArmorItem.Type type) {
        return HEALTH_FUNCTION_FOR_TYPE.get(type) * this.durabilityMultiplier;
    }

    public int getDefenseForType(ArmorItem.Type type) {
        return this.protectionFunctionForType.get(type);
    }

    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    public SoundEvent getEquipSound() {
        return this.sound;
    }

    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public String getName() {
        return this.name;
    }

    public float getToughness() {
        return this.toughness;
    }

    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    public String getSerializedName() {
        return this.name;
    }
}
