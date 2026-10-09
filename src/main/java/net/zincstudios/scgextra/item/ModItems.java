package net.zincstudios.scgextra.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.attributes.SCGEAttributes;
import net.zincstudios.scgextra.block.ModBlocks;
import net.zincstudios.scgextra.effects.ModEffects;
import net.zincstudios.scgextra.entity.cog.COGEntities;
import net.zincstudios.scgextra.entity.asgharian.AsgharianEntities;
import net.zincstudios.scgextra.entity.fac.FACEntities;
import net.zincstudios.scgextra.entity.neutral.NeutralEntities;
import net.zincstudios.scgextra.entity.piglin.PiglinEntities;
import net.zincstudios.scgextra.entity.rrc.RRCEntities;
import net.zincstudios.scgextra.entity.whaler.WhalerEntities;
import net.zincstudios.scgextra.entity.wreckers.WreckersEntities;
import net.zincstudios.scgextra.item.armor.*;
import net.zincstudios.scgextra.item.custom.BandageItem;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.HealingBandageItem;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister
            .create(ForgeRegistries.ITEMS, SCGExtra.MOD_ID);

    public static final RegistryObject<GunItem> PLACEHOLDER_GUN = ITEMS.register("placeholder_gun",
            () -> new GunItem(new Item.Properties())
    );

    // NOTE: custom tier or nah?
    public static final RegistryObject<AtlanticMaceItem> ATLANTIC_MACE = ITEMS.register("atlantic_mace",
            () -> new AtlanticMaceItem(Tiers.IRON, 5, -3.2F, new Item.Properties())
    );

    public static final RegistryObject<CavalrySaberItem> CAVALRY_SABER = ITEMS.register("cavalry_saber",
            () -> new CavalrySaberItem(Tiers.IRON, 3, -2.2F, new Item.Properties())
    );

    public static final RegistryObject<SpearShovelItem> SPEAR_SHOVEL = ITEMS.register("spear_shovel",
            () -> new SpearShovelItem(Tiers.IRON, 2.5F, -2.8F, new Item.Properties())
    );

    public static final RegistryObject<WreckingToolItem> WRECKING_TOOL = ITEMS.register("wrecking_tool",
            () -> new WreckingToolItem(Tiers.IRON, 1, 0.0F, -0.5D, new Item.Properties().durability(50))
    );

    public static final RegistryObject<WreckerSledgeHammerItem> WRECKER_SLEDGE_HAMMER = ITEMS.register("wrecker_sledge_hammer",
            () -> new WreckerSledgeHammerItem(Tiers.IRON, 5, -3.4F, 1.0D, new Item.Properties().durability(50))
    );

    public static final RegistryObject<WreckerRpgItem> WRECKER_RPG = ITEMS.register("wrecker_rpg",
            () -> new WreckerRpgItem(new Item.Properties().stacksTo(1).durability(100))
    );

    public static final RegistryObject<Item> WRECKER_TURRET = ITEMS.register("wrecker_turret",
            () -> new net.zincstudios.scgextra.item.custom.WreckerTurretItem(ModBlocks.WRECKER_TURRET.get(), new Item.Properties())
    );

    public static final RegistryObject<BandageItem> BANDAGE = ITEMS.register("bandage", () -> new BandageItem(
            (new Item.Properties()).stacksTo(16), 4, 8,
            List.of(
                new MobEffectInstance(MobEffects.REGENERATION, 200, 0),
                new MobEffectInstance(MobEffects.ABSORPTION, 400, 0),
                new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2)
            ),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0)
    ));

    public static final RegistryObject<MultiUseHealingItem> MEDKIT = ITEMS.register("medkit", () -> new MultiUseHealingItem(
            (new Item.Properties()).stacksTo(1).durability(12), 4,
            new MobEffectInstance(MobEffects.REGENERATION, 100, 0),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 0)
    ));

    public static final RegistryObject<Item> MEDIPACK = ITEMS.register("medipack", () -> new Item((new Item.Properties()).stacksTo(1).durability(64)){
        public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("item.scgextra.medipack.desc").withStyle(ChatFormatting.GREEN));
                super.appendHoverText(stack, world, tooltip, flag);
            }
        });

    public static final RegistryObject<Item> END_SHELL = ITEMS.register("end_shell",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    public static final RegistryObject<Item>
//            ANTIQUE_SUPER_FLARE = ITEMS.register("antique_super_flare", () ->
//                    new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "antique_super", "antique")),
//            FRONTIER_SUPER_FLARE = ITEMS.register("frontier_super_flare", () ->
//                    new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "frontier_super", "frontier")),
            COPPER_SUPER_FLARE = ITEMS.register("copper_super_flare", () ->
                    new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "copper_super", "copper")),
            IRON_SUPER_FLARE = ITEMS.register("iron_super_flare", () ->
                    new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "iron_super", "iron")),
            WRECKER_SUPER_FLARE = ITEMS.register("wrecker_super_flare", () ->
                    new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "wrecker_super", "wrecker")),
            DIAMOND_STEEL_SUPER_FLARE = ITEMS.register("diamond_steel_super_flare", () ->
                    new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "diamond_steel_super", "diamond_steel")),
            TREATED_BRASS_SUPER_FLARE = ITEMS.register("treated_brass_super_flare", () ->
                    new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "treated_brass_super", "treated_brass")),
        //     GOLD_SUPER_FLARE = ITEMS.register("gold_super_flare", () ->
        //             new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "piglin_super", "piglin")),
        //     SCULK_SUPER_FLARE = ITEMS.register("sculk_super_flare", () ->
        //             new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "sculk_super", "sculk")),
            OCEAN_SUPER_FLARE = ITEMS.register("ocean_super_flare", () ->
                    new SuperRaidFlareItem(new Item.Properties().stacksTo(16), "ocean_super", "ocean"));


    public static final RegistryObject<MedalItem>
            MEDAL_OF_SURVIVOR = ITEMS.register("medal_of_survivor", () -> new MedalItem(new MedalItem.Traits()
                    .modifier(Attributes.ARMOR, new AttributeModifier("survivor_medal_armor", 5, AttributeModifier.Operation.ADDITION))
            )),
            MEDAL_OF_IRON_WILL = ITEMS.register("medal_of_iron_will", () -> new MedalItem(new MedalItem.Traits()
                    .modifier(SCGEAttributes.BULLET_DAMAGE_TAKEN_MULT.get(), new AttributeModifier("iron_will_medal_bullet_damage_taken", -0.2, AttributeModifier.Operation.ADDITION))
            )),
            MEDAL_OF_DEFIANCE = ITEMS.register("medal_of_defiance", () -> new MedalItem(new MedalItem.Traits()
                    .modifier(Attributes.MOVEMENT_SPEED, new AttributeModifier("defiance_medal_movement_speed", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    .modifier(SCAttributes.RELOAD_SPEED.get(), new AttributeModifier("defiance_medal_reload_speed", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL))
            )),
            MEDAL_OF_WONDER = ITEMS.register("medal_of_wonder", () -> new MedalItem(new MedalItem.Traits()
                    .effect(MobEffects.DAMAGE_RESISTANCE, 0)
                    .resistance(MobEffects.POISON)
                    .resistance(top.ribs.scguns.init.ModEffects.SULFUR_POISONING.get())
            )),
            MEDAL_OF_FIERY_RAGE = ITEMS.register("medal_of_fiery_rage", () -> new MedalItem(new MedalItem.Traits()
                    .effect(MobEffects.DAMAGE_BOOST, 1)
                    .effect(MobEffects.REGENERATION, 0)
            )),
            MEDAL_OF_OBEDIENCE = ITEMS.register("medal_of_obedience", () -> new MedalItem(new MedalItem.Traits()
                    .modifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier("obedience_medal_armor_toughness", 3, AttributeModifier.Operation.ADDITION))
                    .modifier(SCGEAttributes.RECOIL_MULT.get(), new AttributeModifier("obedience_medal_recoil", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL))
            )),
            MEDAL_OF_CRUELTY = ITEMS.register("medal_of_cruelty", () -> new MedalItem(new MedalItem.Traits()
                    .modifier(SCAttributes.BULLET_DAMAGE_MULTIPLIER.get(), new AttributeModifier("cruelty_medal_bullet_damage", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL))
            )),
            MEDAL_OF_ENLIGHTENMENT = ITEMS.register("medal_of_enlightenment", () -> new MedalItem(new MedalItem.Traits()
                    .effect(ModEffects.ENLIGHTENMENT.get(), 0)
            )),
            MEDAL_OF_CONQUEROR = ITEMS.register("medal_of_conqueror", () -> new MedalItem(new MedalItem.Traits()
                    .modifier(SCGEAttributes.BULLET_ADDITIONAL_CRIT_CHANCE.get(), new AttributeModifier("conqueror_medal_crit_chance", 0.2, AttributeModifier.Operation.ADDITION))
                    .modifier(SCGEAttributes.BULLET_GRAVITY_MULT.get(), new AttributeModifier("conqueror_medal_gravity", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL))
            ));

    public static final RegistryObject<ArmorSetPartItem>
            OPPRESSOR_HELMET = ITEMS.register("oppressor_helmet", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.OPPRESSOR, ArmorItem.Type.HELMET, ArmorSets.OPPRESSOR)),
            OPPRESSOR_CHESTPLATE = ITEMS.register("oppressor_chestplate", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.OPPRESSOR, ArmorItem.Type.CHESTPLATE, ArmorSets.OPPRESSOR)),
            OPPRESSOR_LEGGINGS = ITEMS.register("oppressor_leggings", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.OPPRESSOR, ArmorItem.Type.LEGGINGS, ArmorSets.OPPRESSOR)),
            OPPRESSOR_BOOTS = ITEMS.register("oppressor_boots", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.OPPRESSOR, ArmorItem.Type.BOOTS, ArmorSets.OPPRESSOR)),

            COMMISSAR_HELMET = ITEMS.register("commissar_helmet", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.COMMISSAR, ArmorItem.Type.HELMET, ArmorSets.COMMISSAR)),
            COMMISSAR_CHESTPLATE = ITEMS.register("commissar_chestplate", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.COMMISSAR, ArmorItem.Type.CHESTPLATE, ArmorSets.COMMISSAR)),
            COMMISSAR_LEGGINGS = ITEMS.register("commissar_leggings", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.COMMISSAR, ArmorItem.Type.LEGGINGS, ArmorSets.COMMISSAR)),
            COMMISSAR_BOOTS = ITEMS.register("commissar_boots", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.COMMISSAR, ArmorItem.Type.BOOTS, ArmorSets.COMMISSAR)),

            WRECKER_HELMET = ITEMS.register("wrecker_helmet", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.WRECKER, ArmorItem.Type.HELMET, ArmorSets.WRECKER)),
            WRECKER_CHESTPLATE = ITEMS.register("wrecker_chestplate", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.WRECKER, ArmorItem.Type.CHESTPLATE, ArmorSets.WRECKER)),
            WRECKER_LEGGINGS = ITEMS.register("wrecker_leggings", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.WRECKER, ArmorItem.Type.LEGGINGS, ArmorSets.WRECKER)),
            WRECKER_BOOTS = ITEMS.register("wrecker_boots", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.WRECKER, ArmorItem.Type.BOOTS, ArmorSets.WRECKER)),

            LEVIATHAN_HELMET = ITEMS.register("leviathan_helmet", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.LEVIATHAN, ArmorItem.Type.HELMET, ArmorSets.LEVIATHAN)),
            LEVIATHAN_CHESTPLATE = ITEMS.register("leviathan_chestplate", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.LEVIATHAN, ArmorItem.Type.CHESTPLATE, ArmorSets.LEVIATHAN)),
            LEVIATHAN_LEGGINGS = ITEMS.register("leviathan_leggings", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.LEVIATHAN, ArmorItem.Type.LEGGINGS, ArmorSets.LEVIATHAN)),
            LEVIATHAN_BOOTS = ITEMS.register("leviathan_boots", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.LEVIATHAN, ArmorItem.Type.BOOTS, ArmorSets.LEVIATHAN)),

            GOLDEN_IDOL_HELMET = ITEMS.register("golden_idol_helmet", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.GOLDEN_IDOL, ArmorItem.Type.HELMET, ArmorSets.GOLDEN_IDOL)),
            GOLDEN_IDOL_CHESTPLATE = ITEMS.register("golden_idol_chestplate", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.GOLDEN_IDOL, ArmorItem.Type.CHESTPLATE, ArmorSets.GOLDEN_IDOL)),
            GOLDEN_IDOL_LEGGINGS = ITEMS.register("golden_idol_leggings", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.GOLDEN_IDOL, ArmorItem.Type.LEGGINGS, ArmorSets.GOLDEN_IDOL)),
            GOLDEN_IDOL_BOOTS = ITEMS.register("golden_idol_boots", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.GOLDEN_IDOL, ArmorItem.Type.BOOTS, ArmorSets.GOLDEN_IDOL)),

            ENLIGHTENED_HELMET = ITEMS.register("enlightened_helmet", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.ENLIGHTENED, ArmorItem.Type.HELMET, ArmorSets.ENLIGHTENED)),
            ENLIGHTENED_CHESTPLATE = ITEMS.register("enlightened_chestplate", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.ENLIGHTENED, ArmorItem.Type.CHESTPLATE, ArmorSets.ENLIGHTENED)),
            ENLIGHTENED_LEGGINGS = ITEMS.register("enlightened_leggings", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.ENLIGHTENED, ArmorItem.Type.LEGGINGS, ArmorSets.ENLIGHTENED)),
            ENLIGHTENED_BOOTS = ITEMS.register("enlightened_boots", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.ENLIGHTENED, ArmorItem.Type.BOOTS, ArmorSets.ENLIGHTENED)),

            JUGGERNAUT_HELMET = ITEMS.register("juggernaut_helmet", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.JUGGERNAUT, ArmorItem.Type.HELMET, ArmorSets.JUGGERNAUT)),
            JUGGERNAUT_CHESTPLATE = ITEMS.register("juggernaut_chestplate", () ->
                    new JuggernautChestplateItem(ModArmorMaterials.JUGGERNAUT, ArmorItem.Type.CHESTPLATE, ArmorSets.JUGGERNAUT)),
            JUGGERNAUT_LEGGINGS = ITEMS.register("juggernaut_leggings", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.JUGGERNAUT, ArmorItem.Type.LEGGINGS, ArmorSets.JUGGERNAUT)),
            JUGGERNAUT_BOOTS = ITEMS.register("juggernaut_boots", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.JUGGERNAUT, ArmorItem.Type.BOOTS, ArmorSets.JUGGERNAUT)),

            RITUAL_HELMET = ITEMS.register("ritual_helmet", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.RITUAL, ArmorItem.Type.HELMET, ArmorSets.RITUAL)),
            RITUAL_CHESTPLATE = ITEMS.register("ritual_chestplate", () ->
                    new RitualChestplateItem(ModArmorMaterials.RITUAL, ArmorItem.Type.CHESTPLATE, ArmorSets.RITUAL)),
            RITUAL_LEGGINGS = ITEMS.register("ritual_leggings", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.RITUAL, ArmorItem.Type.LEGGINGS, ArmorSets.RITUAL)),
            RITUAL_BOOTS = ITEMS.register("ritual_boots", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.RITUAL, ArmorItem.Type.BOOTS, ArmorSets.RITUAL)),

            PIONEER_HELMET = ITEMS.register("pioneer_helmet", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.PIONEER, ArmorItem.Type.HELMET, ArmorSets.PIONEER)),
            PIONEER_CHESTPLATE = ITEMS.register("pioneer_chestplate", () ->
                    new PioneerChestplateItem(ModArmorMaterials.PIONEER, ArmorItem.Type.CHESTPLATE, ArmorSets.PIONEER)),
            PIONEER_LEGGINGS = ITEMS.register("pioneer_leggings", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.PIONEER, ArmorItem.Type.LEGGINGS, ArmorSets.PIONEER)),
            PIONEER_BOOTS = ITEMS.register("pioneer_boots", () ->
                    new GeoArmorSetPartItem(ModArmorMaterials.PIONEER, ArmorItem.Type.BOOTS, ArmorSets.PIONEER));

    public static final RegistryObject<SpawnEggItem>
            // Whaler
            FISH_FOLK_SPAWN_EGG = basicSpawnEgg(WhalerEntities.FISH_FOLK),
            TURTLEMAN_SPAWN_EGG = basicSpawnEgg(WhalerEntities.TURTLEMAN),
            SALMONSAUR_SPAWN_EGG = basicSpawnEgg(WhalerEntities.SALMONSAUR),
            GUARDIAN_STATUE_SPAWN_EGG = basicSpawnEgg(WhalerEntities.GUARDIAN_STATUE),
            TENTACLIATOR_SPAWN_EGG = basicSpawnEgg(WhalerEntities.TENTACLIATOR),
            PUFFICUS_SPAWN_EGG = basicSpawnEgg(WhalerEntities.PUFFICUS),
            ARMORED_WHALE_SPAWN_EGG = basicSpawnEgg(WhalerEntities.ARMORED_WHALE),
            LION_FISH_SPAWN_EGG = basicSpawnEgg(WhalerEntities.LION_FISH),

            // RRC
            COPPER_KNIGHT_SPAWN_EGG = basicSpawnEgg(RRCEntities.COPPER_KNIGHT),
            DRONE_SPAWN_EGG = basicSpawnEgg(RRCEntities.DRONE),
            TALLMAN_SPAWN_EGG = basicSpawnEgg(RRCEntities.TALLMAN),
            SCOUT_SPAWN_EGG = basicSpawnEgg(RRCEntities.SCOUT),
            OPPRESSOR_SPAWN_EGG = basicSpawnEgg(RRCEntities.OPPRESSOR),
            SPRING_JUNKIE_SPAWN_EGG = basicSpawnEgg(RRCEntities.SPRING_JUNKIE),
            FLAMING_HEAD_SPAWN_EGG = basicSpawnEgg(RRCEntities.FLAMING_HEAD),
            SCRAP_GUARD_SPAWN_EGG = basicSpawnEgg(RRCEntities.SCRAP_GUARD),
            ARC_PSYCHO_SPAWN_EGG = basicSpawnEgg(RRCEntities.ARC_PSYCHO),
            GARGOYLE_SPAWN_EGG = basicSpawnEgg(RRCEntities.GARGOYLE),

            // FAC
            FAC_TRENCHER_SPAWN_EGG = basicSpawnEgg(FACEntities.FAC_TRENCHER),
            FAC_BLUECOAT_SPAWN_EGG = basicSpawnEgg(FACEntities.FAC_BLUECOAT),
            TRENCH_GOBLIN_SPAWN_EGG = basicSpawnEgg(FACEntities.TRENCH_GOBLIN),
            TRENCH_SNIPER_SPAWN_EGG = basicSpawnEgg(FACEntities.TRENCH_SNIPER),
            SHOVEL_KNIGHT_SPAWN_EGG = basicSpawnEgg(FACEntities.SHOVEL_KNIGHT),
            FAC_TANK_BUSTER_SPAWN_EGG = basicSpawnEgg(FACEntities.FAC_TANK_BUSTER),
            FAC_LION_SPAWN_EGG = basicSpawnEgg(FACEntities.FAC_LION),
            FAC_COMMISSAR_SPAWN_EGG = basicSpawnEgg(FACEntities.FAC_COMMISSAR),
            FAC_WALKER_SPAWN_EGG = basicSpawnEgg(FACEntities.FAC_WALKER),
            FAC_TANK_SPAWN_EGG = basicSpawnEgg(FACEntities.FAC_TANK),

    // Wreckers
            WRECKER_RED_SPAWN_EGG = basicSpawnEgg(WreckersEntities.WRECKER_RED),
            WRECKER_BLUE_SPAWN_EGG = basicSpawnEgg(WreckersEntities.WRECKER_BLUE),
            WRECKER_GREEN_SPAWN_EGG = basicSpawnEgg(WreckersEntities.WRECKER_GREEN),
            WRECKER_JUMBO_SPAWN_EGG = basicSpawnEgg(WreckersEntities.WRECKER_JUMBO),
            WRECKER_HELICUBE_SPAWN_EGG = basicSpawnEgg(WreckersEntities.WRECKER_HELICUBE),
            WRECKER_TURRET_SPAWN_EGG = basicSpawnEgg(WreckersEntities.WRECKER_TURRET_GUNNER),
            WRECKER_DOZER_SPAWN_EGG = basicSpawnEgg(WreckersEntities.WRECKER_DOZER),
            WRECKER_BIKER_SPAWN_EGG = basicSpawnEgg(WreckersEntities.WRECKER_BIKER),

            // Asgharian
            FAILED_ONE_SPAWN_EGG = basicSpawnEgg(AsgharianEntities.FAILED_ONE),
            ASGHAR_SURGEON_SPAWN_EGG = basicSpawnEgg(AsgharianEntities.ASGHAR_SURGEON),
            ASGHAR_WORKER_SPAWN_EGG = basicSpawnEgg(AsgharianEntities.ASGHAR_WORKER),
            ASGHAR_FLAMER_SPAWN_EGG = basicSpawnEgg(AsgharianEntities.ASGHAR_FLAMER),
            CANDLE_FIEND_SPAWN_EGG = basicSpawnEgg(AsgharianEntities.CANDLE_FIEND),
            SOUL_RIPPER_SPAWN_EGG = basicSpawnEgg(AsgharianEntities.SOUL_RIPPER),

            // COG
            COG_VULTURE_SPAWN_EGG = basicSpawnEgg(COGEntities.VULTURE),
            COG_DEVASTATOR_SPAWN_EGG = basicSpawnEgg(COGEntities.DEVASTATOR),
            COG_BOMBARDIER_SPAWN_EGG = basicSpawnEgg(COGEntities.BOMBARDIER),
            COG_GIGANTES_SPAWN_EGG = basicSpawnEgg(COGEntities.GIGANTES),
            COG_VENATOR_SPAWN_EGG = basicSpawnEgg(COGEntities.VENATOR),
            COG_CENTIPEDE_SPAWN_EGG = basicSpawnEgg(COGEntities.CENTIPEDE),
            COG_JUGGERNAUT_SPAWN_EGG = basicSpawnEgg(COGEntities.JUGGERNAUT),
            COG_TRENCHBLEEDER_SPAWN_EGG = basicSpawnEgg(COGEntities.TRENCHBLEEDER),

            // Neutral
            INFLICTED_BOAR_SPAWN_EGG = basicSpawnEgg(NeutralEntities.INFLICTED_BOAR),
            INFLICTED_WOLF_SPAWN_EGG = basicSpawnEgg(NeutralEntities.INFLICTED_WOLF),
            AMMO_GOBLIN_SPAWN_EGG = basicSpawnEgg(NeutralEntities.AMMO_GOBLIN),
            BIG_LUMP_SPAWN_EGG = basicSpawnEgg(NeutralEntities.BIG_LUMP),
            MUTANT_BAT_SPAWN_EGG = basicSpawnEgg(NeutralEntities.MUTANT_BAT),
            ANOMALY_SPAWN_EGG = basicSpawnEgg(NeutralEntities.ANOMALY),
            NITRO_BEETLE_SPAWN_EGG = basicSpawnEgg(NeutralEntities.NITRO_BEETLE),
            HEAD_HUNTER_SPAWN_EGG = basicSpawnEgg(NeutralEntities.HEAD_HUNTER),
            NETHERITE_EATER_SPAWN_EGG = basicSpawnEgg(NeutralEntities.NETHERITE_EATER),
            END_POD_SPAWN_EGG = basicSpawnEgg(NeutralEntities.END_POD),
            END_DWELLER_SPAWN_EGG = basicSpawnEgg(NeutralEntities.END_DWELLER),
            END_STONE_CRAB_SPAWN_EGG = basicSpawnEgg(NeutralEntities.END_STONE_CRAB),
            END_SCORPION_SPAWN_EGG = basicSpawnEgg(NeutralEntities.END_SCORPION),
            
            //Piglin
            PIGLIN_NOB_SPAWN_EGG = basicSpawnEgg(PiglinEntities.PIGLIN_NOB)
            ;

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        modEventBus.addListener(ModItems::buildContents);

        MinecraftForge.EVENT_BUS.addListener(MedalItem::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(MedalItem::onMobEffectApplicable);
        MinecraftForge.EVENT_BUS.addListener(ArmorSet::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(ArmorSet::onMobEffectApplicable);
        MinecraftForge.EVENT_BUS.addListener(ArmorSets::onEntityHurt);
        MinecraftForge.EVENT_BUS.addListener(CavalrySaberItem::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(CavalrySaberItem::onEntityHurt);
        MinecraftForge.EVENT_BUS.addListener(ArmorPiercingHandler::onLivingHurt);
        MinecraftForge.EVENT_BUS.addListener(ArmorPiercingHandler::onLivingDamage);
        MinecraftForge.EVENT_BUS.addListener(HoldAttackHandler::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(HoldAttackHandler::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(WreckingToolItemHandler::onKnockback);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.addListener(BaseArmorSetPartRenderer::hideHeadWithCommissarHelmet);
        }
    }

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, SCGExtra.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register("scgextra_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup."+SCGExtra.MOD_ID+".tab"))
                    .icon(() -> new ItemStack(FISH_FOLK_SPAWN_EGG.get()))
                    .build()
    );

    private static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CREATIVE_TAB.getKey()) {
            Set<RegistryObject<GunItem>> notInTab = Set.of(  // Turn to RegistryObject<Item> later
                    PLACEHOLDER_GUN
            );

            ITEMS.getEntries().stream()
                    .filter(item -> !notInTab.contains(item) && !(item.get() instanceof WreckerRpgItem))
                    .map(RegistryObject::get)
                    .forEach(event::accept);
        }
    }

    private static RegistryObject<SpawnEggItem> basicSpawnEgg(RegistryObject<? extends EntityType<? extends Mob>> type) {
        assert type.getId() != null;
        return ITEMS.register(type.getId().getPath() + "_spawn_egg",
                () -> new ForgeSpawnEggItem(
                        type,
                        0xFFFFFF,
                        0xFFFFFF,
                        new Item.Properties()
                ));
    }
}

