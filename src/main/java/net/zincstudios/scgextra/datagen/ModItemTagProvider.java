package net.zincstudios.scgextra.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.zincstudios.scgextra.item.ModItems;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class ModItemTagProvider extends ItemTagsProvider {

    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "charm")))
                .add(ModItems.MEDAL_OF_SURVIVOR.get())
                .add(ModItems.MEDAL_OF_IRON_WILL.get())
                .add(ModItems.MEDAL_OF_DEFIANCE.get())
                .add(ModItems.MEDAL_OF_WONDER.get())
                .add(ModItems.MEDAL_OF_FIERY_RAGE.get())
                .add(ModItems.MEDAL_OF_OBEDIENCE.get())
                .add(ModItems.MEDAL_OF_CRUELTY.get())
                .add(ModItems.MEDAL_OF_ENLIGHTENMENT.get())
                .add(ModItems.MEDAL_OF_CONQUEROR.get());
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "belt")))
                .add(ModItems.MEDKIT.get());
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "back")))
                .add(ModItems.MEDIPACK.get());
        tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", "body")))
                .add(ModItems.MEDIPACK.get());
    }
}
