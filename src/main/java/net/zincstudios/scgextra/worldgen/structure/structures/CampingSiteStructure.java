package net.zincstudios.scgextra.worldgen.structure.structures;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.zincstudios.scgextra.worldgen.structure.ModStructures;

public class CampingSiteStructure extends Structure{
   public static final Codec<CampingSiteStructure> CODEC = RecordCodecBuilder.<CampingSiteStructure>mapCodec((codex) -> codex.group(
            settingsCodec(codex),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.heightmap)
    ).apply(codex, CampingSiteStructure::new)).codec();

    public final Holder<StructureTemplatePool> startPool;
    public final HeightProvider startHeight;
    public final Optional<Heightmap.Types> heightmap;

    public CampingSiteStructure(Structure.StructureSettings config, Holder<StructureTemplatePool> pool, HeightProvider height, Optional<Heightmap.Types> map) {
        super(config);
        this.startPool = pool;
        this.startHeight = height;
        this.heightmap = map;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkpos = context.chunkPos();
        int i = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), i, chunkpos.getMinBlockZ());
        BlockState state = context
                .chunkGenerator().getBaseColumn(context.chunkPos().getMinBlockX(),
                        context.chunkPos().getMinBlockZ(), context.heightAccessor(), context.randomState())
                .getBlock(62);
        if (state.is(Blocks.WATER) || state.is(Blocks.KELP_PLANT)) {
            return Optional.empty();
        }
        return JigsawPlacement.addPieces(
                context, 
                startPool, 
                Optional.empty(), 
                1, 
                blockpos, 
                false, 
                heightmap, 
                64
        );
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CAMPING_SITE_STRUCTURE.get();
    }
}