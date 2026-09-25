package net.zincstudios.scgextra.worldgen.structure.structures;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.zincstudios.scgextra.worldgen.structure.ModStructures;

public class TrenchesAnchorStructure extends Structure {
    public static final Codec<TrenchesAnchorStructure> CODEC = ExtraCodecs
            .validate(RecordCodecBuilder.mapCodec((p_227640_) -> p_227640_.group(settingsCodec(p_227640_),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter((p_227656_) -> p_227656_.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
                            .forGetter((p_227654_) -> p_227654_.startJigsawName),
                    Codec.intRange(0, 14).fieldOf("size").forGetter((p_227652_) -> p_227652_.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter((p_227649_) -> p_227649_.startHeight),
                    Codec.BOOL.fieldOf("use_expansion_hack").forGetter((p_227646_) -> p_227646_.useExpansionHack),
                    Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                            .forGetter((p_227644_) -> p_227644_.projectStartToHeightmap),
                    Codec.intRange(1, 256).fieldOf("max_distance_from_center")
                            .forGetter((p_227642_) -> p_227642_.maxDistanceFromCenter))
                    .apply(p_227640_, TrenchesAnchorStructure::new)), TrenchesAnchorStructure::verifyRange)
            .codec();
    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    private static DataResult<TrenchesAnchorStructure> verifyRange(TrenchesAnchorStructure structure) {
        return DataResult.success(structure);
    }

    public TrenchesAnchorStructure(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool,
            Optional<ResourceLocation> startJigsawName, int maxDepth, HeightProvider startHeight,
            boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceToCenter) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceToCenter;
    }

    public TrenchesAnchorStructure(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool,
            int maxDepth, HeightProvider startHeight, boolean useExpansionHack,
            Heightmap.Types projectStartToHeightmap) {
        this(settings, startPool, Optional.empty(), maxDepth, startHeight, useExpansionHack,
                Optional.of(projectStartToHeightmap), 80);
    }

    public TrenchesAnchorStructure(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool,
            int maxDepth, HeightProvider startHeight, boolean useExpansionHack) {
        this(settings, startPool, Optional.empty(), maxDepth, startHeight, useExpansionHack, Optional.empty(), 80);
    }

    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunkpos = context.chunkPos();
        int i = this.startHeight.sample(context.random(),
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        int y = context.chunkGenerator().getFirstFreeHeight(chunkpos.getMinBlockX(), chunkpos.getMinBlockZ(), Heightmap.Types.WORLD_SURFACE, context.heightAccessor(), context.randomState() );
        BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), i, chunkpos.getMinBlockZ());
        BlockState state = context
                .chunkGenerator().getBaseColumn(context.chunkPos().getMinBlockX(),
                        context.chunkPos().getMinBlockZ(), context.heightAccessor(), context.randomState())
                .getBlock(62);
        if (state.is(Blocks.WATER) || state.is(Blocks.KELP_PLANT)) {
            return Optional.empty();
        }
        if (y > 70) {
            return Optional.empty();
        }
        return JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.maxDepth, blockpos,
                this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.TRENCHES_ANCHOR_STRUCTURE.get();
    }
}