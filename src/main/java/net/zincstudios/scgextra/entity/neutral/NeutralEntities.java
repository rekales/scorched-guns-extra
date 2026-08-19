package net.zincstudios.scgextra.entity.neutral;

import static net.zincstudios.scgextra.entity.ModEntities.ENTITY_TYPES;

import net.zincstudios.scgextra.entity.common.WeakPointBox;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.entity.neutral.end.end_stone_crab.EndStoneCrabEntity;
import net.zincstudios.scgextra.entity.neutral.end.end_stone_crab.EndStoneCrabRenderer;
import net.zincstudios.scgextra.entity.neutral.end.end_dweller.EndDwellerEntity;
import net.zincstudios.scgextra.entity.neutral.end.end_dweller.EndDwellerRenderer;
import net.zincstudios.scgextra.entity.neutral.end.end_pod.EndPodEntity;
import net.zincstudios.scgextra.entity.neutral.end.end_pod.EndPodRenderer;
import net.zincstudios.scgextra.entity.neutral.end.end_scorpion.EndScorpionEntity;
import net.zincstudios.scgextra.entity.neutral.end.end_scorpion.EndScorpionRenderer;
import net.zincstudios.scgextra.entity.neutral.nether.head_hunter.HeadHunterEntity;
import net.zincstudios.scgextra.entity.neutral.nether.head_hunter.HeadHunterRenderer;
import net.zincstudios.scgextra.entity.neutral.nether.netherite_eater.NetheriteEaterEntity;
import net.zincstudios.scgextra.entity.neutral.nether.netherite_eater.NetheriteEaterRenderer;
import net.zincstudios.scgextra.entity.neutral.nether.nitro_beetle.NitroBeetleEntity;
import net.zincstudios.scgextra.entity.neutral.nether.nitro_beetle.NitroBeetleRenderer;
import net.zincstudios.scgextra.entity.neutral.overworld.ammo_goblin.AmmoGoblinEntity;
import net.zincstudios.scgextra.entity.neutral.overworld.ammo_goblin.AmmoGoblinRenderer;
import net.zincstudios.scgextra.entity.neutral.overworld.big_lump.BigLumpEntity;
import net.zincstudios.scgextra.entity.neutral.overworld.big_lump.BigLumpRenderer;
import net.zincstudios.scgextra.entity.neutral.overworld.inflicted_boar.InflictedBoarEntity;
import net.zincstudios.scgextra.entity.neutral.overworld.inflicted_boar.InflictedBoarRenderer;
import net.zincstudios.scgextra.entity.neutral.overworld.inflicted_wolf.InflictedWolfEntity;
import net.zincstudios.scgextra.entity.neutral.overworld.inflicted_wolf.InflictedWolfRenderer;
import net.zincstudios.scgextra.entity.neutral.overworld.mutant_bat.MutantBatEntity;
import net.zincstudios.scgextra.entity.neutral.overworld.mutant_bat.MutantBatRenderer;
import net.zincstudios.scgextra.entity.common.OffsetRotatedHeadshotBox;
import net.zincstudios.scgextra.entity.common.WeakPointBoxManager;
import top.ribs.scguns.common.BoundingBoxManager;

public class NeutralEntities {
    
    public static final RegistryObject<EntityType<InflictedBoarEntity>> INFLICTED_BOAR = ENTITY_TYPES.register(
        "inflicted_boar", 
        () -> EntityType.Builder.of(
            InflictedBoarEntity::new, 
            MobCategory.MONSTER
        )
        .sized(1.3964844F, 1.4F)
        .clientTrackingRange(8)
        .build("inflicted_boar")
    );
    
    public static final RegistryObject<EntityType<InflictedWolfEntity>> INFLICTED_WOLF = ENTITY_TYPES.register(
        "inflicted_wolf", 
        () -> EntityType.Builder.of(
            InflictedWolfEntity::new, 
            MobCategory.MONSTER
        )
        .sized(1.3964844F, 1.4F)
        .build("inflicted_wolf")
    );
    
    public static final RegistryObject<EntityType<AmmoGoblinEntity>> AMMO_GOBLIN = ENTITY_TYPES.register(
        "ammo_goblin", 
        () -> EntityType.Builder.of(
            AmmoGoblinEntity::new, 
            MobCategory.MONSTER
        )
        .sized(0.6F, 1.4F)
        .build("ammo_goblin")
    );
    
    public static final RegistryObject<EntityType<BigLumpEntity>> BIG_LUMP = ENTITY_TYPES.register(
        "big_lump", 
        () -> EntityType.Builder.of(
            BigLumpEntity::new, 
            MobCategory.MONSTER
        )
        .sized(3F, 3F)
        .build("big_lump")
    );
    
    public static final RegistryObject<EntityType<MutantBatEntity>> MUTANT_BAT = ENTITY_TYPES.register(
        "mutant_bat", 
        () -> EntityType.Builder.of(
            MutantBatEntity::new, 
            MobCategory.MONSTER
        )
        .sized(2F, 2F)
        .build("mutant_bat")
    );
    
    public static final RegistryObject<EntityType<NitroBeetleEntity>> NITRO_BEETLE = ENTITY_TYPES.register(
        "nitro_beetle", 
        () -> EntityType.Builder.of(
            NitroBeetleEntity::new, 
            MobCategory.MONSTER
        )
        .sized(1F, 0.5F)
        .build("nitro_beetle")
    );
    
    public static final RegistryObject<EntityType<HeadHunterEntity>> HEAD_HUNTER = ENTITY_TYPES.register(
        "head_hunter", 
        () -> EntityType.Builder.of(
            HeadHunterEntity::new, 
            MobCategory.MONSTER
        )
        .sized(0.8F, 2.5F)
        .build("head_hunter")
    );

    public static final RegistryObject<EntityType<NetheriteEaterEntity>> NETHERITE_EATER = ENTITY_TYPES.register(
        "netherite_eater", 
        () -> EntityType.Builder.of(
            NetheriteEaterEntity::new, 
            MobCategory.MONSTER
        )
        .sized(1.6F, 2.5F)
        .build("netherite_eater")
    );

    public static final RegistryObject<EntityType<EndPodEntity>> END_POD = ENTITY_TYPES.register(
        "end_pod", 
        () -> EntityType.Builder.of(
            EndPodEntity::new, 
            MobCategory.CREATURE
        )
        .sized(0.8F, 0.5F)
        .build("end_pod")
    );

    public static final RegistryObject<EntityType<EndDwellerEntity>> END_DWELLER = ENTITY_TYPES.register(
        "end_dweller", 
        () -> EntityType.Builder.of(
            EndDwellerEntity::new, 
            MobCategory.MONSTER
        )
        .sized(1.6F, 1.5F)
        .build("end_dweller")
    );

    public static final RegistryObject<EntityType<EndStoneCrabEntity>> END_STONE_CRAB = ENTITY_TYPES.register(
        "end_stone_crab", 
        () -> EntityType.Builder.of(
            EndStoneCrabEntity::new, 
            MobCategory.MONSTER
        )
        .sized(3F, 3F)
        .build("end_stone_crab")
    );

    public static final RegistryObject<EntityType<EndScorpionEntity>> END_SCORPION = ENTITY_TYPES.register(
        "end_scorpion", 
        () -> EntityType.Builder.of(
            EndScorpionEntity::new, 
            MobCategory.MONSTER
        )
        .sized(3F, 0.5F)
        .build("end_scorpion")
    );
    
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NeutralEntities::registerAttributes);
        modEventBus.addListener(NeutralEntities::onCommonSetup);
        modEventBus.addListener(NeutralEntities::registerSpawnPlacements);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(NeutralEntities::onClientSetup);
        }
    }
    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(NeutralEntities.INFLICTED_BOAR.get(), InflictedBoarEntity.createAttributes().build());
        event.put(NeutralEntities.INFLICTED_WOLF.get(), InflictedWolfEntity.createAttributes().build());
        event.put(NeutralEntities.AMMO_GOBLIN.get(), AmmoGoblinEntity.createAttributes().build());
        event.put(NeutralEntities.BIG_LUMP.get(), BigLumpEntity.createAttributes().build());
        event.put(NeutralEntities.MUTANT_BAT.get(), MutantBatEntity.createAttributes().build());
        event.put(NeutralEntities.NITRO_BEETLE.get(), NitroBeetleEntity.createAttributes().build());
        event.put(NeutralEntities.HEAD_HUNTER.get(), HeadHunterEntity.createAttributes().build());
        event.put(NeutralEntities.NETHERITE_EATER.get(), NetheriteEaterEntity.createAttributes().build());
        event.put(NeutralEntities.END_POD.get(), EndPodEntity.createAttributes().build());
        event.put(NeutralEntities.END_DWELLER.get(), EndDwellerEntity.createAttributes().build());
        event.put(NeutralEntities.END_STONE_CRAB.get(), EndStoneCrabEntity.createAttributes().build());
        event.put(NeutralEntities.END_SCORPION.get(), EndScorpionEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.INFLICTED_BOAR.get(), new OffsetRotatedHeadshotBox<>(
            8, 
            9.5, 
            15.0, 
            0.0F, 
            8.0, 
            false, 
            true
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.INFLICTED_WOLF.get(), new OffsetRotatedHeadshotBox<>(
            8, 
            9.5, 
            15.0, 
            0.0F, 
            10.0, 
            false, 
            true
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.AMMO_GOBLIN.get(), new OffsetRotatedHeadshotBox<>(
            8, 
            9.5, 
            15.0, 
            0.0F, 
            5.0, 
            false, 
            true
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.BIG_LUMP.get(), new OffsetRotatedHeadshotBox<>(
            8, 
            9.5, 
            2.0, 
            0.0F, 
            15.0, 
            false, 
            true
        ));
        WeakPointBoxManager.registerWeakPointBox(NeutralEntities.BIG_LUMP.get(), 
            new WeakPointBox<>(
                new OffsetRotatedHeadshotBox<>(
                    12.0, 
                    12.0, 
                    37.0, 
                    42.0F, 
                    18.0, 
                    false, 
                    true, 
                    true
                )
            ),
            new WeakPointBox<>(
                new OffsetRotatedHeadshotBox<>(
                    12.0, 
                    12.0, 
                    37.0, 
                    -55.0F, 
                    18.0, 
                    false, 
                    true, 
                    true
                )
            )
        );
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.MUTANT_BAT.get(), new OffsetRotatedHeadshotBox<>(
            11, 
            12.5, 
            20.0, 
            0.0F, 
            5.0, 
            false, 
            true
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.HEAD_HUNTER.get(), new OffsetRotatedHeadshotBox<>(
            8, 
            8, 
            35, 
            0, 
            0, 
            5, 
            false, 
            true, 
            true
        ));
        WeakPointBoxManager.registerWeakPointBox(NeutralEntities.HEAD_HUNTER.get(), 
        new WeakPointBox<>(
            new OffsetRotatedHeadshotBox<>(
                8, 
                8, 
                30, 
                0, 
                0, 
                -8, 
                false, 
                true, 
                true
            )
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.NETHERITE_EATER.get(), new OffsetRotatedHeadshotBox<>(
            12, 
            13.5, 
            30.0, 
            0.0F, 
            10.0, 
            false, 
            true
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.NITRO_BEETLE.get(), new OffsetRotatedHeadshotBox<>(
            4, 
            3, 
            1.5, 
            0.0F, 
            3.5,
            false, 
            true
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.END_POD.get(), new OffsetRotatedHeadshotBox<>(
            4, 
            3, 
            1.5, 
            0.0F, 
            3.5,
            false, 
            true
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.END_DWELLER.get(), new OffsetRotatedHeadshotBox<>(
            6, 
            8, 
            10.5, 
            0.0F, 
            10.5,
            false, 
            true
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.END_STONE_CRAB.get(), new OffsetRotatedHeadshotBox<>(
            8, 
            6, 
            27,
            0.0F, 
            18, 
            20,
            false, 
            true, 
            true
        ));
        BoundingBoxManager.registerHeadshotBox(NeutralEntities.END_SCORPION.get(), new OffsetRotatedHeadshotBox<>(
            12, 
            8,
            0, 
            0.0F, 
            6,
            false, 
            true
        ));
    }
    //mostly to stop them from spawning in water
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
            NeutralEntities.AMMO_GOBLIN.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            AmmoGoblinEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            NeutralEntities.BIG_LUMP.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            BigLumpEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            NeutralEntities.NITRO_BEETLE.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            NitroBeetleEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            NeutralEntities.HEAD_HUNTER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            HeadHunterEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            NeutralEntities.NETHERITE_EATER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            NetheriteEaterEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            NeutralEntities.END_DWELLER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            EndDwellerEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            NeutralEntities.END_STONE_CRAB.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            EndStoneCrabEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            NeutralEntities.END_SCORPION.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            EndScorpionEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            NeutralEntities.END_POD.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, level, spawnType, pos, random) -> true,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        //my dumbass forgot these
        event.register(
            NeutralEntities.INFLICTED_BOAR.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            EndScorpionEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            NeutralEntities.INFLICTED_WOLF.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            EndScorpionEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }
    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(NeutralEntities.INFLICTED_BOAR.get(), InflictedBoarRenderer::new);
        EntityRenderers.register(NeutralEntities.INFLICTED_WOLF.get(), InflictedWolfRenderer::new);
        EntityRenderers.register(NeutralEntities.AMMO_GOBLIN.get(), AmmoGoblinRenderer::new);
        EntityRenderers.register(NeutralEntities.BIG_LUMP.get(), BigLumpRenderer::new);
        EntityRenderers.register(NeutralEntities.MUTANT_BAT.get(), MutantBatRenderer::new);
        EntityRenderers.register(NeutralEntities.NITRO_BEETLE.get(), NitroBeetleRenderer::new);
        EntityRenderers.register(NeutralEntities.HEAD_HUNTER.get(), HeadHunterRenderer::new);
        EntityRenderers.register(NeutralEntities.NETHERITE_EATER.get(), NetheriteEaterRenderer::new);
        EntityRenderers.register(NeutralEntities.END_POD.get(), EndPodRenderer::new);
        EntityRenderers.register(NeutralEntities.END_DWELLER.get(), EndDwellerRenderer::new);
        EntityRenderers.register(NeutralEntities.END_STONE_CRAB.get(), EndStoneCrabRenderer::new);
        EntityRenderers.register(NeutralEntities.END_SCORPION.get(), EndScorpionRenderer::new);
    }
}