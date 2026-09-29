package net.zincstudios.scgextra.worldgen;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.block.ModBlocks;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> WARZONE_SPRUCE_PLACED_KEY = registerKey("warzone_spruce_placed");
    public static final ResourceKey<PlacedFeature> GRAVEL_PATCH_PLACED_KEY = registerKey("gravel_patch");
    public static final ResourceKey<PlacedFeature> GRASS_PATCH_PLACED_KEY = registerKey("grass_patch");
    public static final ResourceKey<PlacedFeature> GRASS_PATCH_TALL_PLACED_KEY = registerKey("grass_patch_tall");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, WARZONE_SPRUCE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.WARZONE_SPRUCE_KEY),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(3, 0.1f, 2),
                        ModBlocks.WARZONE_SPRUCE_SAPLING.get()));
        register(context, GRAVEL_PATCH_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.GRAVEL_PATCH_KEY), List.of(
            RarityFilter.onAverageOnceEvery(10),
            InSquarePlacement.spread(),
            HeightmapPlacement.onHeightmap(Types.WORLD_SURFACE),
            BiomeFilter.biome()
        ));
        register(context, GRASS_PATCH_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.GRASS_PATCH_KEY), List.of(
            NoiseThresholdCountPlacement.of(-0.8, 5, 10),
            InSquarePlacement.spread(),
            HeightmapPlacement.onHeightmap(Types.WORLD_SURFACE_WG),
            BiomeFilter.biome()
        ));
        register(context, GRASS_PATCH_TALL_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.GRASS_PATCH_TALL_KEY), List.of(
            NoiseThresholdCountPlacement.of(-0.8, 0, 7),
            RarityFilter.onAverageOnceEvery(32),
            InSquarePlacement.spread(),
            HeightmapPlacement.onHeightmap(Types.MOTION_BLOCKING),
            BiomeFilter.biome()
        ));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(SCGExtra.MOD_ID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}