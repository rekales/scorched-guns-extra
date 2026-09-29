package net.zincstudios.scgextra.worldgen;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.zincstudios.scgextra.SCGExtra;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> WARZONE_SPRUCE_KEY = registerKey("warzone_spruce");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GRAVEL_PATCH_KEY = registerKey("gravel_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GRASS_PATCH_KEY = registerKey("grass_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GRASS_PATCH_TALL_KEY = registerKey("grass_patch_tall");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {

        register(context, WARZONE_SPRUCE_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.SPRUCE_LOG),
                new StraightTrunkPlacer(2, 2, 1),

                BlockStateProvider.simple(Blocks.AIR),
                new BlobFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), 0),

                new TwoLayersFeatureSize(1, 0, 2)).build());
        register(context, GRAVEL_PATCH_KEY, Feature.DISK, 
            new DiskConfiguration(
                new RuleBasedBlockStateProvider(
                    BlockStateProvider.simple(Blocks.GRAVEL), 
                    List.of()
                ), 
                BlockPredicate.matchesBlocks(
                    Blocks.DIRT, 
                    Blocks.GRASS_BLOCK
                ), 
                UniformInt.of(2, 5), 
                2
            )
        );
        register(context, GRASS_PATCH_KEY, Feature.RANDOM_PATCH, 
            new RandomPatchConfiguration(
                32, 
                7, 
                3, 
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.GRASS)))
            )
        );
        register(context, GRASS_PATCH_TALL_KEY, Feature.RANDOM_PATCH, 
            new RandomPatchConfiguration(
                96, 
                7, 
                3, 
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.TALL_GRASS.defaultBlockState().trySetValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))))
            )
        );
    }


    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(SCGExtra.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
