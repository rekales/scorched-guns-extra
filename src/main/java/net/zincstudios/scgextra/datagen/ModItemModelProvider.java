package net.zincstudios.scgextra.datagen;


import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.item.ModItems;

import java.util.Objects;

@SuppressWarnings({"UnusedReturnValue", "unused", "removal"})
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.END_SHELL.get());
        basicItem(ModItems.BANDAGE);

//        copyTexture(ModItems.ANTIQUE_SUPER_FLARE, top.ribs.scguns.init.ModItems.ANTIQUE_FLARE);
//        copyTexture(ModItems.FRONTIER_SUPER_FLARE, top.ribs.scguns.init.ModItems.FRONTIER_FLARE);
        copyTexture(ModItems.COPPER_SUPER_FLARE, top.ribs.scguns.init.ModItems.COPPER_FLARE);
        copyTexture(ModItems.IRON_SUPER_FLARE, top.ribs.scguns.init.ModItems.IRON_FLARE);
        copyTexture(ModItems.WRECKER_SUPER_FLARE, top.ribs.scguns.init.ModItems.WRECKER_FLARE);
        copyTexture(ModItems.DIAMOND_STEEL_SUPER_FLARE, top.ribs.scguns.init.ModItems.DIAMOND_STEEL_FLARE);
        copyTexture(ModItems.TREATED_BRASS_SUPER_FLARE, top.ribs.scguns.init.ModItems.TREATED_BRASS_FLARE);
        // copyTexture(ModItems.GOLD_SUPER_FLARE, top.ribs.scguns.init.ModItems.GOLD_FLARE);
        // copyTexture(ModItems.SCULK_SUPER_FLARE, top.ribs.scguns.init.ModItems.SCULK_FLARE);
        copyTexture(ModItems.OCEAN_SUPER_FLARE, top.ribs.scguns.init.ModItems.OCEAN_FLARE);

        basicItem("medal/", ModItems.MEDAL_OF_SURVIVOR);
        basicItem("medal/", ModItems.MEDAL_OF_IRON_WILL);
        basicItem("medal/", ModItems.MEDAL_OF_DEFIANCE);
        basicItem("medal/", ModItems.MEDAL_OF_WONDER);
        basicItem("medal/", ModItems.MEDAL_OF_FIERY_RAGE);
        basicItem("medal/", ModItems.MEDAL_OF_OBEDIENCE);
        basicItem("medal/", ModItems.MEDAL_OF_CRUELTY);
        basicItem("medal/", ModItems.MEDAL_OF_ENLIGHTENMENT);
        basicItem("medal/", ModItems.MEDAL_OF_CONQUEROR);

        basicItem("armor/", ModItems.OPPRESSOR_HELMET);
        basicItem("armor/", ModItems.OPPRESSOR_CHESTPLATE);
        basicItem("armor/", ModItems.OPPRESSOR_LEGGINGS);
        basicItem("armor/", ModItems.OPPRESSOR_BOOTS);
        basicItem("armor/", ModItems.COMMISSAR_HELMET);
        basicItem("armor/", ModItems.COMMISSAR_CHESTPLATE);
        basicItem("armor/", ModItems.COMMISSAR_LEGGINGS);
        basicItem("armor/", ModItems.COMMISSAR_BOOTS);
        basicItem("armor/", ModItems.WRECKER_HELMET);
        basicItem("armor/", ModItems.WRECKER_CHESTPLATE);
        basicItem("armor/", ModItems.WRECKER_LEGGINGS);
        basicItem("armor/", ModItems.WRECKER_BOOTS);
        basicItem("armor/", ModItems.LEVIATHAN_HELMET);
        basicItem("armor/", ModItems.LEVIATHAN_CHESTPLATE);
        basicItem("armor/", ModItems.LEVIATHAN_LEGGINGS);
        basicItem("armor/", ModItems.LEVIATHAN_BOOTS);
        basicItem("armor/", ModItems.GOLDEN_IDOL_HELMET);
        basicItem("armor/", ModItems.GOLDEN_IDOL_CHESTPLATE);
        basicItem("armor/", ModItems.GOLDEN_IDOL_LEGGINGS);
        basicItem("armor/", ModItems.GOLDEN_IDOL_BOOTS);
        basicItem("armor/", ModItems.ENLIGHTENED_HELMET);
        basicItem("armor/", ModItems.ENLIGHTENED_CHESTPLATE);
        basicItem("armor/", ModItems.ENLIGHTENED_LEGGINGS);
        basicItem("armor/", ModItems.ENLIGHTENED_BOOTS);
        basicItem("armor/", ModItems.JUGGERNAUT_HELMET);
        basicItem("armor/", ModItems.JUGGERNAUT_CHESTPLATE);
        basicItem("armor/", ModItems.JUGGERNAUT_LEGGINGS);
        basicItem("armor/", ModItems.JUGGERNAUT_BOOTS);
        basicItem("armor/", ModItems.RITUAL_HELMET);
        basicItem("armor/", ModItems.RITUAL_CHESTPLATE);
        basicItem("armor/", ModItems.RITUAL_LEGGINGS);
        basicItem("armor/", ModItems.RITUAL_BOOTS);
        basicItem("armor/", ModItems.PIONEER_HELMET);
        basicItem("armor/", ModItems.PIONEER_CHESTPLATE);
        basicItem("armor/", ModItems.PIONEER_LEGGINGS);
        basicItem("armor/", ModItems.PIONEER_BOOTS);

        basicItem("spawn_egg/", ModItems.FISH_FOLK_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.TURTLEMAN_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.SALMONSAUR_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.GUARDIAN_STATUE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.TENTACLIATOR_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.PUFFICUS_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.ARMORED_WHALE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.LION_FISH_SPAWN_EGG.get());

        basicItem("spawn_egg/", ModItems.FAC_TRENCHER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.FAC_BLUECOAT_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.TRENCH_GOBLIN_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.TRENCH_SNIPER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.SHOVEL_KNIGHT_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.FAC_TANK_BUSTER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.FAC_LION_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.FAC_COMMISSAR_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.FAC_WALKER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.FAC_TANK_SPAWN_EGG.get());

        basicItem("spawn_egg/", ModItems.WRECKER_RED_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.WRECKER_BLUE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.WRECKER_GREEN_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.WRECKER_JUMBO_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.WRECKER_HELICUBE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.WRECKER_TURRET_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.WRECKER_DOZER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.WRECKER_BIKER_SPAWN_EGG.get());

        basicItem("spawn_egg/", ModItems.COPPER_KNIGHT_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.DRONE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.TALLMAN_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.SCOUT_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.OPPRESSOR_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.SPRING_JUNKIE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.FLAMING_HEAD_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.SCRAP_GUARD_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.ARC_PSYCHO_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.GARGOYLE_SPAWN_EGG.get());

        basicItem("spawn_egg/", ModItems.FAILED_ONE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.ASGHAR_SURGEON_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.ASGHAR_WORKER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.ASGHAR_FLAMER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.CANDLE_FIEND_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.SOUL_RIPPER_SPAWN_EGG.get());

        basicItem("spawn_egg/", ModItems.COG_VULTURE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.COG_DEVASTATOR_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.COG_BOMBARDIER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.COG_GIGANTES_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.COG_VENATOR_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.COG_CENTIPEDE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.COG_JUGGERNAUT_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.COG_TRENCHBLEEDER_SPAWN_EGG.get());

        basicItem("spawn_egg/", ModItems.INFLICTED_BOAR_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.INFLICTED_WOLF_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.AMMO_GOBLIN_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.BIG_LUMP_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.MUTANT_BAT_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.ANOMALY_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.NITRO_BEETLE_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.HEAD_HUNTER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.NETHERITE_EATER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.END_POD_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.END_DWELLER_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.END_STONE_CRAB_SPAWN_EGG.get());
        basicItem("spawn_egg/", ModItems.END_SCORPION_SPAWN_EGG.get());
        
        basicItem("spawn_egg/", ModItems.PIGLIN_NOB_SPAWN_EGG.get());
    }

    public ItemModelBuilder basicItem(RegistryObject<? extends Item> item) {
        return basicItem(item.get());
    }

    public ItemModelBuilder basicItem(String prefix, RegistryObject<? extends Item> item) {
        return basicItem(prefix, item.get());
    }

    public ItemModelBuilder basicItem(String prefix, Item item) {
        return basicItem(prefix, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)));
    }

    public ItemModelBuilder basicItem(String prefix, ResourceLocation loc) {
        return getBuilder(loc.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", new ResourceLocation(loc.getNamespace(), "item/" + prefix + loc.getPath()));
    }

    public ItemModelBuilder copyTexture(RegistryObject<? extends Item> item, RegistryObject<? extends Item> copiedItem) {
        return copyTexture(item.get(), copiedItem.get());
    }

    public ItemModelBuilder copyTexture(Item item, Item copiedItem) {
        return copyTexture(
                Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item)),
                Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(copiedItem))
        );
    }

    public ItemModelBuilder copyTexture(ResourceLocation itemLoc, ResourceLocation copiedLoc) {
        existingFileHelper.trackGenerated(new ResourceLocation(copiedLoc.getNamespace(), "item/" + copiedLoc.getPath()),
                PackType.CLIENT_RESOURCES, ".png", "textures");
        return getBuilder(itemLoc.toString())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", new ResourceLocation(copiedLoc.getNamespace(), "item/" + copiedLoc.getPath()));
    }

    private void spawnEgg(RegistryObject<SpawnEggItem> item) {
        if (item.getId() == null) return;
        withExistingParent(item.getId().getPath(), mcLoc("item/template_spawn_egg"));
    }
}
