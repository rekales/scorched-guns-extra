package net.zincstudios.scgextra.worldgen.features;

import java.util.List;
import java.util.Optional;

import net.zincstudios.scgextra.data.CraterData.Crater;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.zincstudios.scgextra.data.CraterDataLoader;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class CratersFeature extends Feature<NoneFeatureConfiguration>{

    public CratersFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        ServerLevelAccessor level = context.level();
        BlockPos pos = context.origin();
        List<Crater> craters = CraterDataLoader.INSTANCE.data.craters();
        if(craters.isEmpty())return false;

        Crater crater = craters.get(context.random().nextInt(craters.size()));
        
        Optional<StructureTemplate> op = level.getLevel().getServer().getStructureManager().get(ResourceLocation.bySeparator(crater.name(), ':'));
        
        if (op.isEmpty()) {
            return false;
        }
        
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false)
                .addProcessor(new GravityProcessor(Types.WORLD_SURFACE, crater.gravity()));

        return op.get().placeInWorld(level, pos, pos, settings, context.random(), 3);
    }
}
