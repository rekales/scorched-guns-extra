package net.zincstudios.scgextra.tag;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.zincstudios.scgextra.SCGExtra;

public class Tags {
    public static final TagKey<Block> NETHERRACK = BlockTags.create(SCGExtra.asResource("netherrack"));
    public static final TagKey<Block> CRIMSON_NILYUM = BlockTags.create(SCGExtra.asResource("crimson_nilyum"));
    public static final TagKey<Block> SOUL_SAND = BlockTags.create(SCGExtra.asResource("soul_sand"));
}