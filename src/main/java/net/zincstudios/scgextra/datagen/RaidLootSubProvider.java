package net.zincstudios.scgextra.datagen;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.item.ModItems;

import java.util.function.BiConsumer;

@SuppressWarnings("SameParameterValue")
public class RaidLootSubProvider implements LootTableSubProvider {

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> output) {
        output.accept(
                SCGExtra.asResource("raid/rrc_super"),
                single(ModItems.MEDAL_OF_SURVIVOR.get())
        );
        output.accept(
                SCGExtra.asResource("raid/whaler_super"),
                single(ModItems.MEDAL_OF_WONDER.get())
        );
        output.accept(
                SCGExtra.asResource("raid/fac_super"),
                single(ModItems.MEDAL_OF_IRON_WILL.get())
        );
        output.accept(
                SCGExtra.asResource("raid/wrecker_super"),
                single(ModItems.MEDAL_OF_DEFIANCE.get())
        );
        output.accept(
                SCGExtra.asResource("raid/cog_super"),
                single(ModItems.MEDAL_OF_OBEDIENCE.get())
        );
        output.accept(
                SCGExtra.asResource("raid/asgharian_super"),
                single(ModItems.MEDAL_OF_CRUELTY.get())
        );
        output.accept(
                SCGExtra.asResource("raid/piglin_super"),
                single(ModItems.MEDAL_OF_FIERY_RAGE.get())
        );
    }

    private LootTable.Builder single(Item item) {
        return this.constant(item, 1);
    }

    private LootTable.Builder constant(Item item, int count) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                        )
                );
    }

}
