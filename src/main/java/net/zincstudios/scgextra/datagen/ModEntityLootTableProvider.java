package net.zincstudios.scgextra.datagen;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.zincstudios.scgextra.entity.asgharian.AsgharianEntities;
import net.zincstudios.scgextra.entity.cog.COGEntities;
import net.zincstudios.scgextra.entity.fac.FACEntities;
import net.zincstudios.scgextra.entity.neutral.NeutralEntities;
import net.zincstudios.scgextra.entity.rrc.RRCEntities;
import net.zincstudios.scgextra.entity.whaler.WhalerEntities;
import net.zincstudios.scgextra.entity.wreckers.WreckersEntities;
import top.ribs.scguns.init.ModBlocks;
import top.ribs.scguns.init.ModItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModEntityLootTableProvider extends EntityLootSubProvider {

    protected ModEntityLootTableProvider() {
        super(FeatureFlags.REGISTRY.allFlags(), FeatureFlagSet.of());
    }

    @Override
    public void generate() {

        basicLoot(WhalerEntities.FISH_FOLK.get())
                .range(ModItems.FROG_DART.get(), 1, 3)
                .range(ModItems.ADVANCED_ROUND.get(), 1, 3)
                .constant(Items.TROPICAL_FISH, 1);
        basicLoot(WhalerEntities.SALMONSAUR.get())
                .constant(Items.COD, 1)
                .chance(Items.SADDLE,  0.2f);
        basicLoot(WhalerEntities.TURTLEMAN.get())
                .range(ModItems.SHOTBALL.get(), 1, 3)
                .range(ModItems.ADVANCED_ROUND.get(), 1, 3)
                .constant(Items.SCUTE, 1);
        basicLoot(WhalerEntities.TENTACLIATOR.get())
                .range(ModItems.FROG_DART.get(), 1, 3)
                .range(ModItems.ADVANCED_ROUND.get(), 1, 3)
                .range(Items.INK_SAC, 1, 3);
        basicLoot(WhalerEntities.PUFFICUS.get())
                .range(ModItems.FROG_DART.get(), 1, 3)
                .range(ModItems.ADVANCED_ROUND.get(), 1, 3)
                .constant(Items.PUFFERFISH, 1);
        basicLoot(WhalerEntities.GUARDIAN_STATUE.get())
                .altsChance(List.of(
                        net.zincstudios.scgextra.item.ModItems.LEVIATHAN_HELMET.get(),
                        net.zincstudios.scgextra.item.ModItems.LEVIATHAN_CHESTPLATE.get(),
                        net.zincstudios.scgextra.item.ModItems.LEVIATHAN_LEGGINGS.get(),
                        net.zincstudios.scgextra.item.ModItems.LEVIATHAN_BOOTS.get()
                ), 0.3F)
                .constant(ModItems.OCEAN_FLARE.get(), 1)
                .range(ModItems.BLUEPRINT_SCRAP.get(), 1, 2)
                .range(Items.HEART_OF_THE_SEA, 1, 3)
                .range(Items.PRISMARINE_SHARD, 3, 9);
        basicLoot(WhalerEntities.ARMORED_WHALE.get())
                .altsChance(List.of(
                        net.zincstudios.scgextra.item.ModItems.LEVIATHAN_HELMET.get(),
                        net.zincstudios.scgextra.item.ModItems.LEVIATHAN_CHESTPLATE.get(),
                        net.zincstudios.scgextra.item.ModItems.LEVIATHAN_LEGGINGS.get(),
                        net.zincstudios.scgextra.item.ModItems.LEVIATHAN_BOOTS.get()
                ), 0.3F)
                .range(ModItems.BLUEPRINT_SCRAP.get(), 1, 2)
                .constant(ModItems.LEVIATHAN_TOOTH.get(), 1)
                .range(ModBlocks.SUPPLY_CRATE.get().asItem(), 3, 6);

        basicLoot(FACEntities.FAC_TRENCHER.get())
                .range(Items.IRON_NUGGET, 1, 3);
        basicLoot(FACEntities.FAC_BLUECOAT.get())
                .range(Items.IRON_NUGGET, 1, 3);
        basicLoot(FACEntities.TRENCH_GOBLIN.get())
                .range(Items.IRON_NUGGET, 1, 3)
                .range(ModItems.ANTHRALITE_NUGGET.get(), 1, 3);
        basicLoot(FACEntities.TRENCH_SNIPER.get())
                .range(Items.IRON_NUGGET, 1, 3)
                .range(ModItems.ANTHRALITE_INGOT.get(), 1, 3);
        basicLoot(FACEntities.SHOVEL_KNIGHT.get())
                .range(Items.IRON_NUGGET, 1, 3)
                .range(ModItems.ANTHRALITE_INGOT.get(), 1, 3);
        basicLoot(FACEntities.FAC_TANK_BUSTER.get())
                .range(Items.IRON_NUGGET, 1, 3)
                .range(ModItems.ANTHRALITE_NUGGET.get(), 1, 3)
                .range(ModItems.MICROJET.get(), 1, 3);
        basicLoot(FACEntities.FAC_LION.get())
                .range(Items.IRON_INGOT, 1, 3)
                .range(ModItems.ANTHRALITE_INGOT.get(), 1, 3);
        basicLoot(FACEntities.FAC_COMMISSAR.get())
                .altsChance(List.of(
                        net.zincstudios.scgextra.item.ModItems.COMMISSAR_HELMET.get(),
                        net.zincstudios.scgextra.item.ModItems.COMMISSAR_CHESTPLATE.get(),
                        net.zincstudios.scgextra.item.ModItems.COMMISSAR_LEGGINGS.get(),
                        net.zincstudios.scgextra.item.ModItems.COMMISSAR_BOOTS.get()
                ), 0.3F)
                .range(Items.IRON_INGOT, 1, 3)
                .range(ModItems.BLUEPRINT_SCRAP.get(), 1, 2);
        basicLoot(FACEntities.FAC_WALKER.get())
                .range(ModItems.ANTHRALITE_INGOT.get(), 1, 3)
                .range(Items.COAL, 1, 3);
        basicLoot(FACEntities.FAC_TANK.get())
                .altsChance(List.of(
                        net.zincstudios.scgextra.item.ModItems.COMMISSAR_HELMET.get(),
                        net.zincstudios.scgextra.item.ModItems.COMMISSAR_CHESTPLATE.get(),
                        net.zincstudios.scgextra.item.ModItems.COMMISSAR_LEGGINGS.get(),
                        net.zincstudios.scgextra.item.ModItems.COMMISSAR_BOOTS.get()
                ), 0.3F)
                .constant(ModItems.LABOR_TROPHY.get(), 1)
                .range(Items.IRON_BLOCK, 1, 3)
                .range(Items.COAL_BLOCK, 1, 3)
                .range(ModItems.ANTHRALITE_INGOT.get(), 1, 3);

        basicLoot(WreckersEntities.WRECKER_RED.get())
                .range(Items.IRON_NUGGET, 1, 3);
        basicLoot(WreckersEntities.WRECKER_BLUE.get())
                .range(Items.IRON_NUGGET, 1, 3);
        basicLoot(WreckersEntities.WRECKER_GREEN.get())
                .range(Items.IRON_NUGGET, 1, 3);
        basicLoot(WreckersEntities.WRECKER_JUMBO.get())
                .range(Items.IRON_INGOT, 1, 3);
        basicLoot(WreckersEntities.WRECKER_HELICUBE.get())
                .range(Items.IRON_NUGGET, 1, 3);
        basicLoot(WreckersEntities.WRECKER_TURRET_GUNNER.get())
                .range(Items.IRON_NUGGET, 1, 3)
                .range(Items.GUNPOWDER, 1, 3)
                .chance(net.zincstudios.scgextra.item.ModItems.WRECKER_TURRET.get(), 0.5f);
        basicLoot(WreckersEntities.WRECKER_DOZER.get())
                .range(Items.IRON_INGOT, 3, 6)
                .range(ModBlocks.SUPPLY_CRATE.get().asItem(), 3, 6)
                .constant(ModItems.SNAPPED_COGWHEEL.get(), 1);

        basicLoot(RRCEntities.COPPER_KNIGHT.get())
                .range(Items.COPPER_INGOT, 1, 3)
                .altsChance(List.of(
                        ModItems.SCRAP_HELMET.get(),
                        ModItems.SCRAP_CHESTPLATE.get(),
                        ModItems.SCRAP_LEGGINGS.get(),
                        ModItems.SCRAP_BOOTS.get()
                ), 0.1F);
        basicLoot(RRCEntities.DRONE.get())
                .range(Items.COPPER_INGOT, 1, 3)
                .range(Items.GUNPOWDER, 3, 5);
        basicLoot(RRCEntities.TALLMAN.get())
                .range(Items.COPPER_INGOT, 1, 3);
        basicLoot(RRCEntities.SCOUT.get())
                .range(ModItems.SMALL_COPPER_CASING.get(), 1, 3);
        basicLoot(RRCEntities.OPPRESSOR.get())
                .altsChance(List.of(
                        net.zincstudios.scgextra.item.ModItems.OPPRESSOR_HELMET.get(),
                        net.zincstudios.scgextra.item.ModItems.OPPRESSOR_CHESTPLATE.get(),
                        net.zincstudios.scgextra.item.ModItems.OPPRESSOR_LEGGINGS.get(),
                        net.zincstudios.scgextra.item.ModItems.OPPRESSOR_BOOTS.get()
                ), 0.3F)
                .range(ModItems.BLUEPRINT_SCRAP.get(), 1, 2)
                .range(Items.COPPER_INGOT, 1, 3);
        basicLoot(RRCEntities.SPRING_JUNKIE.get())
                .range(Items.GUNPOWDER, 1, 3)
                .range(ModItems.FLECHETTE.get(), 1, 3);
        basicLoot(RRCEntities.FLAMING_HEAD.get())
                .altsChance(List.of(
                        net.zincstudios.scgextra.item.ModItems.OPPRESSOR_HELMET.get(),
                        net.zincstudios.scgextra.item.ModItems.OPPRESSOR_CHESTPLATE.get(),
                        net.zincstudios.scgextra.item.ModItems.OPPRESSOR_LEGGINGS.get(),
                        net.zincstudios.scgextra.item.ModItems.OPPRESSOR_BOOTS.get()
                ), 0.3F)
                .constant(ModItems.RUSTY_MEDAL.get(), 1)
                .range(ModItems.BLAZE_FUEL.get(), 3, 6)
                .range(Items.COPPER_BLOCK, 1, 3);
        basicLoot(RRCEntities.SCRAP_GUARD.get())
                .range(Items.COPPER_INGOT, 1, 3)
                .range(ModItems.BLUEPRINT_SCRAP.get(), 1, 2)
                .range(ModItems.NEEDLE.get(), 1, 3);
        basicLoot(RRCEntities.ARC_PSYCHO.get())
                .range(Items.REDSTONE, 1, 3)
                .range(ModItems.SHOCK_CELL.get(), 1, 3);

        basicLoot(AsgharianEntities.FAILED_ONE.get())
                .range(Items.ROTTEN_FLESH, 1, 3)
                .range(Items.CLAY_BALL, 1, 3);
        basicLoot(AsgharianEntities.ASGHAR_SURGEON.get())
                .range(Items.RED_CANDLE, 1, 3)
                .chance(ModItems.DIAMOND_STEEL_INGOT.get(), 0.5f);
        basicLoot(AsgharianEntities.ASGHAR_WORKER.get())
                .range(Items.REDSTONE, 1, 3)
                .constant(ModItems.DEPLETED_DIAMOND_STEEL_INGOT.get(), 1);
        basicLoot(AsgharianEntities.ASGHAR_FLAMER.get())
                .range(Items.GUNPOWDER, 1, 3)
                .constant(ModItems.DEPLETED_DIAMOND_STEEL_INGOT.get(), 1);
        basicLoot(AsgharianEntities.CANDLE_FIEND.get())
                .altsChance(List.of(
                        net.zincstudios.scgextra.item.ModItems.RITUAL_HELMET.get(),
                        net.zincstudios.scgextra.item.ModItems.RITUAL_CHESTPLATE.get(),
                        net.zincstudios.scgextra.item.ModItems.RITUAL_LEGGINGS.get(),
                        net.zincstudios.scgextra.item.ModItems.RITUAL_BOOTS.get()
                ), 0.3F)
                .range(Items.RED_CANDLE, 3, 6)
                .range(ModItems.BLUEPRINT_SCRAP.get(), 1, 3)
                .constant(ModItems.CERIMONIAL_COD.get(), 1);
        basicLoot(AsgharianEntities.SOUL_RIPPER.get())
                .altsChance(List.of(
                        net.zincstudios.scgextra.item.ModItems.RITUAL_HELMET.get(),
                        net.zincstudios.scgextra.item.ModItems.RITUAL_CHESTPLATE.get(),
                        net.zincstudios.scgextra.item.ModItems.RITUAL_LEGGINGS.get(),
                        net.zincstudios.scgextra.item.ModItems.RITUAL_BOOTS.get()
                ), 0.3F)
                .range(Items.SOUL_LANTERN, 1, 3)
                .range(ModItems.DEPLETED_DIAMOND_STEEL_INGOT.get(), 1, 3)
                .constant(ModItems.CERIMONIAL_COD.get(), 1);

        basicLoot(COGEntities.VULTURE.get())
                .range(ModItems.ANCIENT_BRASS.get(), 1, 2)
                .range(ModItems.COMPACT_ADVANCED_ROUND.get(), 1, 3);
        basicLoot(COGEntities.DEVASTATOR.get())
                .range(ModItems.TREATED_BRASS_INGOT.get(), 1, 3)
                .range(Items.GUNPOWDER, 3, 5);
        basicLoot(COGEntities.BOMBARDIER.get())
                .range(ModItems.ANCIENT_BRASS.get(), 1, 2)
                .range(ModItems.ROCKET.get(), 1, 3);
        basicLoot(COGEntities.GIGANTES.get())
                .range(ModItems.TREATED_BRASS_INGOT.get(), 1, 3)
                .range(Items.GUNPOWDER, 3, 5);
        basicLoot(COGEntities.VENATOR.get())
                .range(ModItems.GIBBS_ROUND.get(), 1, 3);
        basicLoot(COGEntities.CENTIPEDE.get())
                .range(ModItems.TREATED_BRASS_INGOT.get(), 1, 3)
                .range(ModItems.ENERGY_CELL.get(), 1, 3);
        basicLoot(COGEntities.JUGGERNAUT.get())
                .altsChance(List.of(
                        net.zincstudios.scgextra.item.ModItems.JUGGERNAUT_HELMET.get(),
                        net.zincstudios.scgextra.item.ModItems.JUGGERNAUT_CHESTPLATE.get(),
                        net.zincstudios.scgextra.item.ModItems.JUGGERNAUT_LEGGINGS.get(),
                        net.zincstudios.scgextra.item.ModItems.JUGGERNAUT_BOOTS.get()
                ), 0.3F)
                .constant(ModItems.COG_HEART.get(), 1)
                .range(ModItems.TREATED_BRASS_INGOT.get(), 1, 3);

        basicLoot(NeutralEntities.INFLICTED_BOAR.get())
                .range(Items.PORKCHOP, 1, 3)
                .range(Items.LEATHER, 1, 3)
                .chance(Items.SKELETON_SKULL, 0.1F);
        basicLoot(NeutralEntities.INFLICTED_WOLF.get())
                .range(Items.BONE, 1, 3)
                .range(Items.LEATHER, 1, 3);
        basicLoot(NeutralEntities.AMMO_GOBLIN.get())
                .range(Items.GUNPOWDER, 3, 6)
                .constant(ModBlocks.SUPPLY_CRATE.get().asItem(), 1)
                .range(ModItems.ADVANCED_ROUND.get(), 1, 3)
                .range(ModItems.COMPACT_ADVANCED_ROUND.get(), 1, 3)
                .range(ModItems.STANDARD_COPPER_ROUND.get(), 1, 3)
                .range(ModItems.COMPACT_COPPER_ROUND.get(), 1, 3)
                .range(ModItems.SHOTGUN_SHELL.get(), 1, 3)
                .range(ModItems.RAMROD_ROUND.get(), 1, 3);
        basicLoot(NeutralEntities.BIG_LUMP.get())
                .range(Items.ROTTEN_FLESH, 3, 6)
                .range(ModItems.RAW_ANTHRALITE.get(), 3, 6)
                .range(ModItems.ADVANCED_ROUND.get(), 3, 9);
        basicLoot(NeutralEntities.MUTANT_BAT.get())
                .range(ModItems.RAW_PHOSPHOR.get(), 3, 6);
        basicLoot(NeutralEntities.NITRO_BEETLE.get())
                .range(ModItems.NITRO_POWDER_DUST.get(), 1, 3);
        basicLoot(NeutralEntities.HEAD_HUNTER.get())
                .chanceRange(Items.WITHER_SKELETON_SKULL, 0.5F, 1, 2)
                .range(Items.BONE, 1, 3)
                .range(Items.COAL, 1, 3);
        basicLoot(NeutralEntities.NETHERITE_EATER.get())
                .range(Items.NETHERITE_SCRAP, 1, 3)
                .range(ModItems.NETHERITE_SCRAP_CHUNK.get(), 1, 3);
        basicLoot(NeutralEntities.END_POD.get())
                .range(net.zincstudios.scgextra.item.ModItems.END_SHELL.get(), 1, 3);
        basicLoot(NeutralEntities.END_DWELLER.get())
                .range(ModItems.PLASMA.get(), 1, 3);
        basicLoot(NeutralEntities.END_STONE_CRAB.get())
                .range(Items.END_STONE, 1, 3)
                .range(net.zincstudios.scgextra.item.ModItems.END_SHELL.get(), 3, 6);
        basicLoot(NeutralEntities.END_SCORPION.get())
                .constant(ModItems.NETHER_STAR_FRAGMENT.get(), 1)
                .range(net.zincstudios.scgextra.item.ModItems.END_SHELL.get(), 3, 6)
                .range(ModItems.PLASMA.get(), 3, 6);
    }

    // don't use this for more complex loot tables
    private LootTableEntryBuilder basicLoot(EntityType<?> type) {
        LootTable.Builder lootTableRef = LootTable.lootTable();
        add(type, lootTableRef);
        return LootTableEntryBuilder.of(lootTableRef);
    }

    // Reviewing the implementation of the LootProvider class, it should be
    // fine to mutate the builder after adding
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    private static class LootTableEntryBuilder {

        private final LootTable.Builder lootTableRef;

        private LootTableEntryBuilder(LootTable.Builder lootTableRef) {
            this.lootTableRef = lootTableRef;
        }

        public static LootTableEntryBuilder of(LootTable.Builder lootTableRef) {
            return new LootTableEntryBuilder(lootTableRef);
        }

        public LootTable.Builder getLootTableRef() {
            return this.lootTableRef;
        }

        public LootTableEntryBuilder constant(Item item, int count) {
            this.lootTableRef.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(item)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                    )
            );

            return this;
        }

        public LootTableEntryBuilder range(Item item, int min, int max) {
            this.lootTableRef.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add( LootItem.lootTableItem(item)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                    )
            );
            return this;
        }

        public LootTableEntryBuilder chance(Item item, float chance) {
            return this.chanceConstant(item, chance, 1);
        }

        public LootTableEntryBuilder chanceConstant(Item item, float chance, int count) {
            this.lootTableRef.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(item)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                    )
            );
            return this;
        }

        public LootTableEntryBuilder chanceRange(Item item, float chance, int min, int max) {
            this.lootTableRef.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance))
                    .add(LootItem.lootTableItem(item)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                    )
            );
            return this;
        }

        public LootTableEntryBuilder altsChance(List<Item> items, float chance) {
            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(chance));

            for (Item item : items) {
                poolBuilder.add(LootItem.lootTableItem(item).setWeight(1));
            }

            this.lootTableRef.withPool(poolBuilder);

            return this;
        }
    }
}
