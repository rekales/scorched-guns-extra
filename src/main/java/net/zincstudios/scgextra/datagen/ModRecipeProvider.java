package net.zincstudios.scgextra.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.item.ModItems;
import top.ribs.scguns.init.ModBlocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {

        TagKey<Item> OXIDIZED_COPPER_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(SCGExtra.MOD_ID, "oxidized_copper"));

//        superFlare(writer, ModItems.ANTIQUE_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.ANTIQUE_FLARE.get());
//        superFlare(writer, ModItems.FRONTIER_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.FRONTIER_FLARE.get());
        superFlare(writer, ModItems.COPPER_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.COPPER_FLARE.get());
        superFlare(writer, ModItems.IRON_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.IRON_FLARE.get());
        superFlare(writer, ModItems.WRECKER_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.WRECKER_FLARE.get());
        superFlare(writer, ModItems.DIAMOND_STEEL_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.DIAMOND_STEEL_FLARE.get());
        superFlare(writer, ModItems.TREATED_BRASS_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.TREATED_BRASS_FLARE.get());
        superFlare(writer, ModItems.GOLD_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.GOLD_FLARE.get());
        // superFlare(writer, ModItems.SCULK_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.SCULK_FLARE.get());
        superFlare(writer, ModItems.OCEAN_SUPER_FLARE.get(), top.ribs.scguns.init.ModItems.OCEAN_FLARE.get());

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BANDAGE.get())
                .pattern("BP")
                .pattern("PP")
                .define('P', Items.PAPER)
                .define('B', top.ribs.scguns.init.ModItems.BASIC_POULTICE.get())
                .unlockedBy("has_ingredient", has(top.ribs.scguns.init.ModItems.BASIC_POULTICE.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MEDKIT.get())
                .pattern("PSB")
                .pattern("PSB")
                .pattern("LCL")
                .define('L', Items.LEATHER)
                .define('C', Tags.Items.CHESTS)
                .define('P', top.ribs.scguns.init.ModItems.BASIC_POULTICE.get())
                .define('S', top.ribs.scguns.init.ModItems.HONEY_SULFUR_POULTICE.get())
                .define('B', ModItems.BANDAGE.get())
                .unlockedBy("has_ingredient", has(top.ribs.scguns.init.ModItems.BASIC_POULTICE.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.MEDIPACK.get())
                .pattern("SMS")
                .pattern("MCM")
                .pattern("LIL")
                .define('M', ModItems.MEDKIT.get())
                .define('C', Tags.Items.CHESTS)
                .define('L', Items.LEATHER)
                .define('S', Items.STRING)
                .define('I', top.ribs.scguns.init.ModBlocks.TREATED_IRON_BLOCK.get())
                .unlockedBy("has_ingredient", has(ModItems.MEDKIT.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CAVALRY_SABER.get())
                .pattern("I ")
                .pattern("IN")
                .pattern("BN")
                .define('I', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('N', top.ribs.scguns.init.ModItems.ANTHRALITE_NUGGET.get())
                .define('B', top.ribs.scguns.init.ModItems.IRON_BAYONET.get())
                .unlockedBy("has_ingredient", has(top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get()))
                .showNotification(false)
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OPPRESSOR_HELMET.get())
                .pattern(" C ")
                .pattern("CMC")
                .pattern("L L")
                .define('C', OXIDIZED_COPPER_TAG)
                .define('M', top.ribs.scguns.init.ModItems.RUSTY_MEDAL.get())
                .define('L', Items.LEATHER)
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.RUSTY_MEDAL.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OPPRESSOR_CHESTPLATE.get())
                .pattern("C C")
                .pattern("CMC")
                .pattern("LLL")
                .define('C', OXIDIZED_COPPER_TAG)
                .define('M', top.ribs.scguns.init.ModItems.RUSTY_MEDAL.get())
                .define('L', Items.LEATHER)
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.RUSTY_MEDAL.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OPPRESSOR_LEGGINGS.get())
                .pattern("LML")
                .pattern("L L")
                .pattern("C C")
                .define('C', Items.COPPER_INGOT)
                .define('M', top.ribs.scguns.init.ModItems.RUSTY_MEDAL.get())
                .define('L', Items.LEATHER)
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.RUSTY_MEDAL.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.OPPRESSOR_BOOTS.get())
                .pattern("LML")
                .pattern("C C")
                .define('C', OXIDIZED_COPPER_TAG)
                .define('M', top.ribs.scguns.init.ModItems.RUSTY_MEDAL.get())
                .define('L', Items.LEATHER)
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.RUSTY_MEDAL.get()))
                .showNotification(false)
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMMISSAR_HELMET.get())
                .pattern(" H ")
                .pattern("ITI")
                .pattern("ADA")
                .define('H', Items.HOPPER)
                .define('T', top.ribs.scguns.init.ModItems.LABOR_TROPHY.get())
                .define('I', Items.IRON_INGOT)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('D', Items.BLUE_DYE)
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.LABOR_TROPHY.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMMISSAR_CHESTPLATE.get())
                .pattern("ADA")
                .pattern("ITI")
                .pattern("IBI")
                .define('B', Items.IRON_BLOCK)
                .define('T', top.ribs.scguns.init.ModItems.LABOR_TROPHY.get())
                .define('I', Items.IRON_INGOT)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('D', Items.BLUE_DYE)
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.LABOR_TROPHY.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMMISSAR_LEGGINGS.get())
                .pattern("ITI")
                .pattern("IDI")
                .pattern("A A")
                .define('T', top.ribs.scguns.init.ModItems.LABOR_TROPHY.get())
                .define('I', Items.IRON_INGOT)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('D', Items.BLUE_DYE)
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.LABOR_TROPHY.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.COMMISSAR_BOOTS.get())
                .pattern(" D ")
                .pattern("ITI")
                .pattern("A A")
                .define('T', top.ribs.scguns.init.ModItems.LABOR_TROPHY.get())
                .define('I', Items.IRON_INGOT)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('D', Items.BLUE_DYE)
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.LABOR_TROPHY.get()))
                .showNotification(false)
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.WRECKER_HELMET.get())
                .pattern("NTN")
                .pattern("TCT")
                .pattern("A A")
                .define('N', top.ribs.scguns.init.ModItems.NEEDLE.get())
                .define('T', top.ribs.scguns.init.ModItems.TREATED_IRON_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.SNAPPED_COGWHEEL.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SNAPPED_COGWHEEL.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.WRECKER_CHESTPLATE.get())
                .pattern("A A")
                .pattern("TCT")
                .pattern("TBT")
                .define('B', ModBlocks.TREATED_IRON_BLOCK.get())
                .define('T', top.ribs.scguns.init.ModItems.TREATED_IRON_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.SNAPPED_COGWHEEL.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SNAPPED_COGWHEEL.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.WRECKER_LEGGINGS.get())
                .pattern("TCT")
                .pattern("T T")
                .pattern("A A")
                .define('T', top.ribs.scguns.init.ModItems.TREATED_IRON_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.SNAPPED_COGWHEEL.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SNAPPED_COGWHEEL.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.WRECKER_BOOTS.get())
                .pattern("TCT")
                .pattern("A A")
                .define('T', top.ribs.scguns.init.ModItems.TREATED_IRON_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.SNAPPED_COGWHEEL.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SNAPPED_COGWHEEL.get()))
                .showNotification(false)
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LEVIATHAN_HELMET.get())
                .pattern("HPH")
                .pattern("PTP")
                .pattern("A A")
                .define('H', Items.HEART_OF_THE_SEA)
                .define('P', Items.PRISMARINE_BRICKS)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('T', top.ribs.scguns.init.ModItems.LEVIATHAN_TOOTH.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.LEVIATHAN_TOOTH.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LEVIATHAN_CHESTPLATE.get())
                .pattern("AHA")
                .pattern("PTP")
                .pattern("PPP")
                .define('H', Items.HEART_OF_THE_SEA)
                .define('P', Items.PRISMARINE_BRICKS)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('T', top.ribs.scguns.init.ModItems.LEVIATHAN_TOOTH.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.LEVIATHAN_TOOTH.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LEVIATHAN_LEGGINGS.get())
                .pattern("PTP")
                .pattern("P P")
                .pattern("A A")
                .define('P', Items.PRISMARINE_BRICKS)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('T', top.ribs.scguns.init.ModItems.LEVIATHAN_TOOTH.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.LEVIATHAN_TOOTH.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.LEVIATHAN_BOOTS.get())
                .pattern("PTP")
                .pattern("A A")
                .define('P', Items.PRISMARINE_BRICKS)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('T', top.ribs.scguns.init.ModItems.LEVIATHAN_TOOTH.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.LEVIATHAN_TOOTH.get()))
                .showNotification(false)
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GOLDEN_IDOL_HELMET.get())
                .pattern(" B ")
                .pattern("BIB")
                .pattern("A A")
                .define('B', Items.GOLD_BLOCK)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('I', top.ribs.scguns.init.ModItems.GOLD_IDOL.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.GOLD_IDOL.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GOLDEN_IDOL_CHESTPLATE.get())
                .pattern("A A")
                .pattern("BIB")
                .pattern("BBB")
                .define('B', Items.GOLD_BLOCK)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('I', top.ribs.scguns.init.ModItems.GOLD_IDOL.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.GOLD_IDOL.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GOLDEN_IDOL_LEGGINGS.get())
                .pattern("BIB")
                .pattern("G G")
                .pattern("A A")
                .define('B', Items.GOLD_BLOCK)
                .define('G', Items.GOLD_INGOT)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('I', top.ribs.scguns.init.ModItems.GOLD_IDOL.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.GOLD_IDOL.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.GOLDEN_IDOL_BOOTS.get())
                .pattern("BIB")
                .pattern("A A")
                .define('B', Items.GOLD_BLOCK)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('I', top.ribs.scguns.init.ModItems.GOLD_IDOL.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.GOLD_IDOL.get()))
                .showNotification(false)
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ENLIGHTENED_HELMET.get())
                .pattern("ECE")
                .pattern("CTC")
                .pattern("A A")
                .define('E', Items.CALIBRATED_SCULK_SENSOR)
                .define('C', Items.SCULK_CATALYST)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('T', top.ribs.scguns.init.ModItems.SCULK_TOME.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SCULK_TOME.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ENLIGHTENED_CHESTPLATE.get())
                .pattern("A A")
                .pattern("STS")
                .pattern("CCC")
                .define('S', Items.SCULK_SHRIEKER)
                .define('C', Items.SCULK_CATALYST)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('T', top.ribs.scguns.init.ModItems.SCULK_TOME.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SCULK_TOME.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ENLIGHTENED_LEGGINGS.get())
                .pattern("CTC")
                .pattern("C C")
                .pattern("A A")
                .define('C', Items.SCULK_CATALYST)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('T', top.ribs.scguns.init.ModItems.SCULK_TOME.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SCULK_TOME.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ENLIGHTENED_BOOTS.get())
                .pattern("CTC")
                .pattern("A A")
                .define('C', Items.SCULK_CATALYST)
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('T', top.ribs.scguns.init.ModItems.SCULK_TOME.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SCULK_TOME.get()))
                .showNotification(false)
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.JUGGERNAUT_HELMET.get())
                .pattern("RBR")
                .pattern("BHB")
                .pattern("A A")
                .define('R', Items.REDSTONE)
                .define('B', top.ribs.scguns.init.ModItems.TREATED_BRASS_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('H', top.ribs.scguns.init.ModItems.COG_HEART.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.COG_HEART.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.JUGGERNAUT_CHESTPLATE.get())
                .pattern("ARA")
                .pattern("BHB")
                .pattern("BLB")
                .define('R', Items.REDSTONE)
                .define('L', ModBlocks.TREATED_BRASS_BLOCK.get())
                .define('B', top.ribs.scguns.init.ModItems.TREATED_BRASS_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('H', top.ribs.scguns.init.ModItems.COG_HEART.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.COG_HEART.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.JUGGERNAUT_LEGGINGS.get())
                .pattern("BHB")
                .pattern("BRB")
                .pattern("A A")
                .define('R', Items.REDSTONE)
                .define('B', top.ribs.scguns.init.ModItems.TREATED_BRASS_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('H', top.ribs.scguns.init.ModItems.COG_HEART.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.COG_HEART.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.JUGGERNAUT_BOOTS.get())
                .pattern(" R ")
                .pattern("BHB")
                .pattern("A A")
                .define('R', Items.REDSTONE)
                .define('B', top.ribs.scguns.init.ModItems.TREATED_BRASS_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('H', top.ribs.scguns.init.ModItems.COG_HEART.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.COG_HEART.get()))
                .showNotification(false)
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RITUAL_HELMET.get())
                .pattern("BSB")
                .pattern("SCS")
                .pattern("A A")
                .define('B', Items.LAPIS_BLOCK)
                .define('S', top.ribs.scguns.init.ModItems.DIAMOND_STEEL_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.CERIMONIAL_COD.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.CERIMONIAL_COD.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RITUAL_CHESTPLATE.get())
                .pattern("ABA")
                .pattern("SCS")
                .pattern("SLS")
                .define('B', Items.LAPIS_BLOCK)
                .define('L', ModBlocks.DIAMOND_STEEL_LAMP.get())
                .define('S', top.ribs.scguns.init.ModItems.DIAMOND_STEEL_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.CERIMONIAL_COD.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.CERIMONIAL_COD.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RITUAL_LEGGINGS.get())
                .pattern("SCS")
                .pattern("SBS")
                .pattern("A A")
                .define('B', Items.LAPIS_BLOCK)
                .define('S', top.ribs.scguns.init.ModItems.DIAMOND_STEEL_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.CERIMONIAL_COD.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.CERIMONIAL_COD.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RITUAL_BOOTS.get())
                .pattern(" B ")
                .pattern("SCS")
                .pattern("A A")
                .define('B', Items.LAPIS_BLOCK)
                .define('S', top.ribs.scguns.init.ModItems.DIAMOND_STEEL_INGOT.get())
                .define('A', top.ribs.scguns.init.ModItems.ANTHRALITE_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.CERIMONIAL_COD.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.CERIMONIAL_COD.get()))
                .showNotification(false)
                .save(writer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PIONEER_HELMET.get())
                .pattern("LSL")
                .pattern("SCS")
                .pattern("P P")
                .define('P', Items.PURPUR_BLOCK)
                .define('L', ModBlocks.DIAMOND_STEEL_LAMP.get())
                .define('S', top.ribs.scguns.init.ModItems.DIAMOND_STEEL_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.SHULKER_CORE.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SHULKER_CORE.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PIONEER_CHESTPLATE.get())
                .pattern("PLP")
                .pattern("SCS")
                .pattern("SSS")
                .define('P', Items.PURPUR_BLOCK)
                .define('L', ModBlocks.DIAMOND_STEEL_LAMP.get())
                .define('S', top.ribs.scguns.init.ModItems.DIAMOND_STEEL_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.SHULKER_CORE.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SHULKER_CORE.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PIONEER_LEGGINGS.get())
                .pattern("LCL")
                .pattern("S S")
                .pattern("P P")
                .define('P', Items.PURPUR_BLOCK)
                .define('L', ModBlocks.DIAMOND_STEEL_LAMP.get())
                .define('S', top.ribs.scguns.init.ModItems.DIAMOND_STEEL_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.SHULKER_CORE.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SHULKER_CORE.get()))
                .showNotification(false)
                .save(writer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PIONEER_BOOTS.get())
                .pattern("SCS")
                .pattern("P P")
                .define('P', Items.PURPUR_BLOCK)
                .define('S', top.ribs.scguns.init.ModItems.DIAMOND_STEEL_INGOT.get())
                .define('C', top.ribs.scguns.init.ModItems.SHULKER_CORE.get())
                .unlockedBy("has_armor_ingredient", has(top.ribs.scguns.init.ModItems.SHULKER_CORE.get()))
                .showNotification(false)
                .save(writer);
    }

    private void superFlare(Consumer<FinishedRecipe> writer, Item superFlare , Item flare) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, superFlare)
                .pattern("FBF")
                .pattern("BFB")
                .pattern("FBF")
                .define('F', flare)
                .define('B', top.ribs.scguns.init.ModItems.BLUEPRINT_SCRAP.get())
                .unlockedBy("has_flare_ingredient", has(flare))
                .showNotification(false)
                .save(writer);
    }


}
