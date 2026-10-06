package net.zincstudios.scgextra.worldgen.biome;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.Parameter;
import net.zincstudios.scgextra.CommonConfig;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

public class ModOverworldRegion extends Region {
public ModOverworldRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
            VanillaParameterOverlayBuilder builder = new VanillaParameterOverlayBuilder();

            new ParameterUtils.ParameterPointListBuilder()
                    .temperature(
                        Parameter.span(
                            CommonConfig.WARZONE_SPAWN_MINIMUM_TEMPERATURE.get().floatValue(), 
                            CommonConfig.WARZONE_SPAWN_MAXIMUM_TEMPERATURE.get().floatValue()
                        )
                    )
                    .humidity(
                        Parameter.span(
                            CommonConfig.WARZONE_SPAWN_MINIMUM_HUMIDITY.get().floatValue(), 
                            CommonConfig.WARZONE_SPAWN_MAXIMUM_HUMIDITY.get().floatValue()
                        )
                    )
                    .continentalness(
                        Parameter.span(
                            CommonConfig.WARZONE_SPAWN_MINIMUM_CONTINENTALNESS.get().floatValue(), 
                            CommonConfig.WARZONE_SPAWN_MAXIMUM_CONTINENTALNESS.get().floatValue()
                        )
                    )
                    .erosion(
                        Parameter.span(
                            CommonConfig.WARZONE_SPAWN_MINIMUM_EROSION.get().floatValue(), 
                            CommonConfig.WARZONE_SPAWN_MAXIMUM_EROSION.get().floatValue()
                        )
                    )
                    .depth(
                        ParameterUtils.Depth.SURFACE, 
                        ParameterUtils.Depth.FLOOR
                    )
                    .weirdness(
                        Parameter.span(
                            CommonConfig.WARZONE_SPAWN_MINIMUM_WEIRDNESS.get().floatValue(), 
                            CommonConfig.WARZONE_SPAWN_MAXIMUM_WEIRDNESS.get().floatValue()
                        )
                    )
                    .build().forEach(
                        point -> builder.add(
                            point, 
                            ModBiomes.WARZONE_BIOME
                        )
                    );

            builder.build().forEach(mapper);
    }
}