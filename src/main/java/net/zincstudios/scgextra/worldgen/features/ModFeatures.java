package net.zincstudios.scgextra.worldgen.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(Registries.FEATURE, SCGExtra.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CRATERS_FEATURE =
        FEATURES.register("craters_feature", CratersFeature::new);

    public static void register(IEventBus eventBus){
        FEATURES.register(eventBus);
    }
        
}
