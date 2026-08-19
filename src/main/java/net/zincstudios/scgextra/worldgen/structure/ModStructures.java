package net.zincstudios.scgextra.worldgen.structure;

import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.worldgen.structure.structures.CampingSiteStructure;
import net.zincstudios.scgextra.worldgen.structure.structures.DestroyedMinesStructure;
import net.zincstudios.scgextra.worldgen.structure.structures.MineTrenchStructure;
import net.zincstudios.scgextra.worldgen.structure.structures.TrenchesAnchorStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister
            .create(Registries.STRUCTURE_TYPE, SCGExtra.MOD_ID);

    public static final RegistryObject<StructureType<MineTrenchStructure>> MINE_TRENCH_STRUCTURE =
        STRUCTURE_TYPES.register(
            "mine_trench_structure",
            () -> () -> MineTrenchStructure.CODEC
        );

    public static final RegistryObject<StructureType<DestroyedMinesStructure>> DESTROYED_MINE_STRUCTURE =
        STRUCTURE_TYPES.register(
            "destroyed_mine_structure",
            () -> () -> DestroyedMinesStructure.CODEC
        );

    public static final RegistryObject<StructureType<TrenchesAnchorStructure>> TRENCHES_ANCHOR_STRUCTURE =
        STRUCTURE_TYPES.register(
            "trenches_anchor_structure",
            () -> () -> TrenchesAnchorStructure.CODEC
        );

    public static final RegistryObject<StructureType<CampingSiteStructure>> CAMPING_SITE_STRUCTURE =
        STRUCTURE_TYPES.register(
            "camping_site_structure",
            () -> () -> CampingSiteStructure.CODEC
        );

    public static void register(IEventBus modEventBus) {
        STRUCTURE_TYPES.register(modEventBus);
    }
}
