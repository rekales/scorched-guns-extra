package net.zincstudios.scgextra.tag;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.zincstudios.scgextra.SCGExtra;

public class Tags {
    public static final TagKey<Block> NETHERITE_EATER_SPAWNABLE_ON = BlockTags.create(SCGExtra.asResource("netherite_eater_spawnable_on"));
    public static final TagKey<Block> NITRO_BEETLE_SPAWNABLE_ON = BlockTags.create(SCGExtra.asResource("nitro_beetle_spawnable_on"));
    public static final TagKey<Block> HEAD_HUNTER_SPAWNABLE_ON = BlockTags.create(SCGExtra.asResource("head_hunter_spawnable_on"));
}