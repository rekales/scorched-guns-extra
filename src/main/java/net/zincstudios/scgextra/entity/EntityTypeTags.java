package net.zincstudios.scgextra.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.zincstudios.scgextra.SCGExtra;

public class EntityTypeTags {

    // Faction
    public static final TagKey<EntityType<?>> ASGHARIAN = tag("factions/asgharian");
    public static final TagKey<EntityType<?>> COG = tag("factions/cog");
    public static final TagKey<EntityType<?>> FAC = tag("factions/fac");
    public static final TagKey<EntityType<?>> RRC = tag("factions/rrc");
    public static final TagKey<EntityType<?>> WHALER = tag("factions/whaler");
    public static final TagKey<EntityType<?>> WRECKERS = tag("factions/wreckers");
    public static final TagKey<EntityType<?>> PIGLIN = tag("factions/piglin");

    public static TagKey<EntityType<?>> tag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, SCGExtra.asResource(name));
    }
}