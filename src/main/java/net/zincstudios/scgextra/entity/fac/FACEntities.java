package net.zincstudios.scgextra.entity.fac;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.GunHoldingMobRenderer;
import net.zincstudios.scgextra.entity.common.client.ItemHoldingMobRenderer;
import net.zincstudios.scgextra.entity.fac.bluecoat.FacBluecoatEntity;
import net.zincstudios.scgextra.entity.fac.commissar.FacCommissarEntity;
import net.zincstudios.scgextra.entity.fac.commissar.FacCommissarRenderer;
import net.zincstudios.scgextra.entity.fac.lion.FacLionEntity;
import net.zincstudios.scgextra.entity.fac.tank.FacTankEntity;
import net.zincstudios.scgextra.entity.fac.tank.FacTankRenderer;
import net.zincstudios.scgextra.entity.fac.tank.TankCannonProjectile;
import net.zincstudios.scgextra.entity.fac.tank.TankCannonProjectileRenderer;
import net.zincstudios.scgextra.entity.fac.tank_buster.FacTankBusterEntity;
import net.zincstudios.scgextra.entity.fac.trench_sniper.FacTrenchSniperEntity;
import net.zincstudios.scgextra.entity.fac.trencher.FacTrencherEntity;
import net.zincstudios.scgextra.entity.fac.walker.FacWalkerEntity;
import net.zincstudios.scgextra.entity.fac.shovel_knight.FacShovelKnightEntity;
import net.zincstudios.scgextra.entity.fac.trench_goblin.FacTrenchGoblinEntity;
import net.zincstudios.scgextra.entity.fac.walker.FacWalkerRenderer;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.headshot.BasicHeadshotBox;
import top.ribs.scguns.common.headshot.RotatedHeadshotBox;

import static net.zincstudios.scgextra.entity.ModEntities.ENTITY_TYPES;

public class FACEntities {

    public static final RegistryObject<EntityType<FacTrencherEntity>> FAC_TRENCHER = ENTITY_TYPES
            .register("fac_trencher", () -> EntityType.Builder.of(FacTrencherEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build("fac_trencher"));

    public static final RegistryObject<EntityType<FacBluecoatEntity>> FAC_BLUECOAT = ENTITY_TYPES
            .register("fac_bluecoat", () -> EntityType.Builder.of(FacBluecoatEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .build("fac_bluecoat"));

    public static final RegistryObject<EntityType<FacTrenchGoblinEntity>> TRENCH_GOBLIN = ENTITY_TYPES
            .register("fac_trench_goblin", () -> EntityType.Builder.of(FacTrenchGoblinEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 1.7F)
                    .build("fac_trench_goblin"));

    public static final RegistryObject<EntityType<FacTrenchSniperEntity>> TRENCH_SNIPER = ENTITY_TYPES
            .register("fac_trench_sniper", () -> EntityType.Builder.of(FacTrenchSniperEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.2F)
                    .build("fac_trench_sniper"));

    public static final RegistryObject<EntityType<FacShovelKnightEntity>> SHOVEL_KNIGHT = ENTITY_TYPES
            .register("fac_shovel_knight", () -> EntityType.Builder.of(FacShovelKnightEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.0F)
                    .build("fac_shovel_knight"));

    public static final RegistryObject<EntityType<FacTankBusterEntity>> FAC_TANK_BUSTER = ENTITY_TYPES
            .register("fac_tank_buster", () -> EntityType.Builder.of(FacTankBusterEntity::new, MobCategory.MONSTER)
                    .sized(0.98F, 2.1F)
                    .build("fac_tank_buster"));

    public static final RegistryObject<EntityType<FacLionEntity>> FAC_LION = ENTITY_TYPES
            .register("fac_lion", () -> EntityType.Builder.of(FacLionEntity::new, MobCategory.MONSTER)
                    .sized(1.8F, 3F)
                    .build("fac_lion"));

    public static final RegistryObject<EntityType<FacCommissarEntity>> FAC_COMMISSAR = ENTITY_TYPES
            .register("fac_commissar", () -> EntityType.Builder.of(FacCommissarEntity::new, MobCategory.MONSTER)
                    .sized(0.85F, 2.4F)
                    .build("fac_commissar"));

    public static final RegistryObject<EntityType<FacWalkerEntity>> FAC_WALKER = ENTITY_TYPES
            .register("fac_walker", () -> EntityType.Builder.of(FacWalkerEntity::new, MobCategory.MONSTER)
                    .setUpdateInterval(1)
                    .sized(1.4F, 3.85F)
                    .build("fac_walker"));

    public static final RegistryObject<EntityType<FacTankEntity>> FAC_TANK = ENTITY_TYPES
            .register("fac_tank", () -> EntityType.Builder.of(FacTankEntity::new, MobCategory.MONSTER)
                    .setUpdateInterval(1)
                    .sized(2.5F, 3.3F)
                    .build("fac_tank"));

    public static final RegistryObject<EntityType<TankCannonProjectile>> TANK_CANNON_PROJECTILE = ENTITY_TYPES
            .register("tank_cannon_projectile", () -> EntityType.Builder.of(
                            (EntityType<TankCannonProjectile> type, Level level) -> new TankCannonProjectile(type, level), MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .setTrackingRange(100)
                    .setUpdateInterval(1)
                    .noSummon()
                    .fireImmune()
                    .noSave()
                    .setShouldReceiveVelocityUpdates(true)
                    .build("tank_cannon_projectile"));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(FACEntities::registerAttributes);
        modEventBus.addListener(FACEntities::onCommonSetup);
        modEventBus.addListener(FACEntities::registerSpawnPlacements);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(FACEntities::onClientSetup);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(FACEntities.FAC_TRENCHER.get(), FacTrencherEntity.createAttributes().build());
        event.put(FACEntities.FAC_BLUECOAT.get(), FacBluecoatEntity.createAttributes().build());
        event.put(FACEntities.TRENCH_GOBLIN.get(), FacTrenchGoblinEntity.createAttributes().build());
        event.put(FACEntities.TRENCH_SNIPER.get(), FacTrenchSniperEntity.createAttributes().build());
        event.put(FACEntities.SHOVEL_KNIGHT.get(), FacShovelKnightEntity.createAttributes().build());
        event.put(FACEntities.FAC_TANK_BUSTER.get(), FacTankBusterEntity.createAttributes().build());
        event.put(FACEntities.FAC_LION.get(), FacLionEntity.createAttributes().build());
        event.put(FACEntities.FAC_COMMISSAR.get(), FacCommissarEntity.createAttributes().build());
        event.put(FACEntities.FAC_WALKER.get(), FacWalkerEntity.createAttributes().build());
        event.put(FACEntities.FAC_TANK.get(), FacTankEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(FACEntities.FAC_TRENCHER.get(), new BasicHeadshotBox<>(9.0, 20.0));
        BoundingBoxManager.registerHeadshotBox(FACEntities.FAC_BLUECOAT.get(), new BasicHeadshotBox<>(11.0, 20.0));
        BoundingBoxManager.registerHeadshotBox(FACEntities.TRENCH_GOBLIN.get(), new RotatedHeadshotBox<>(8.0, 7.0, 21.0, 7.0, false, true));
        BoundingBoxManager.registerHeadshotBox(FACEntities.TRENCH_SNIPER.get(), new RotatedHeadshotBox<>(9.0, 11.0, 30.0, 2.0, false, true));
        BoundingBoxManager.registerHeadshotBox(FACEntities.SHOVEL_KNIGHT.get(), new BasicHeadshotBox<>(9.0, 24.0));
        BoundingBoxManager.registerHeadshotBox(FACEntities.FAC_COMMISSAR.get(), new BasicHeadshotBox<>(10.0, 17.0, 30.0));
        BoundingBoxManager.registerHeadshotBox(FACEntities.FAC_TANK_BUSTER.get(), new RotatedHeadshotBox<>(10.0, 1.0, 26.5, 6.0, false, true));
        BoundingBoxManager.registerHeadshotBox(FACEntities.FAC_LION.get(), new RotatedHeadshotBox<>(12, 35.0, 9.0, false, true));
        BoundingBoxManager.registerHeadshotBox(FACEntities.FAC_WALKER.get(), new RotatedHeadshotBox<>(11, 7, 48, 8, false, true));
        BoundingBoxManager.registerHeadshotBox(FACEntities.FAC_TANK.get(), new RotatedHeadshotBox<>(14.0, 12.0, 13.0, 20.0, false, true));
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(FACEntities.FAC_TRENCHER.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_trencher")))
                        .hasGlowLayer()
        );
        EntityRenderers.register(FACEntities.FAC_BLUECOAT.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_bluecoat")))
                        .hasGlowLayer()
        );
        EntityRenderers.register(FACEntities.TRENCH_GOBLIN.get(), (ctx) ->
                new ItemHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_trench_goblin")))
                        .hasGlowLayer()
        );
        EntityRenderers.register(FACEntities.TRENCH_SNIPER.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_trench_sniper")))
                        .hasGlowLayer()
        );
        EntityRenderers.register(FACEntities.SHOVEL_KNIGHT.get(), (ctx) ->
                new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_shovel_knight")))
                        .hasGlowLayer()
        );  // TODO: check head bones
        EntityRenderers.register(FACEntities.FAC_TANK_BUSTER.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_tank_buster")))
                        .hasGlowLayer()
        );
        EntityRenderers.register(FACEntities.FAC_LION.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_lion")))
                        .hasGlowLayer()
        );
        EntityRenderers.register(FACEntities.FAC_COMMISSAR.get(), (ctx) ->
                new FacCommissarRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_commissar"), "head"))
                        .hasGlowLayer()
        );
        EntityRenderers.register(FACEntities.FAC_WALKER.get(), (ctx) ->
                new FacWalkerRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_walker"), "inner_upper_body"))
                        .noDeathTilt()
                        .hasGlowLayer()
        );
        EntityRenderers.register(FACEntities.FAC_TANK.get(), (ctx) ->
                new FacTankRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("fac/fac_tank")))
                        .noDeathTilt()
                        .hasGlowLayer()
        );
        EntityRenderers.register(FACEntities.TANK_CANNON_PROJECTILE.get(), TankCannonProjectileRenderer::new);
    }
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
            FACEntities.FAC_TRENCHER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacTrencherEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            FACEntities.FAC_BLUECOAT.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacBluecoatEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            FACEntities.TRENCH_GOBLIN.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacTrenchGoblinEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            FACEntities.TRENCH_SNIPER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacTrenchSniperEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            FACEntities.SHOVEL_KNIGHT.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacShovelKnightEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            FACEntities.FAC_COMMISSAR.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacCommissarEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            FACEntities.FAC_TANK_BUSTER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacTankBusterEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            FACEntities.FAC_LION.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacLionEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            FACEntities.FAC_WALKER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacWalkerEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
        event.register(
            FACEntities.FAC_TANK.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.WORLD_SURFACE,
            FacTankEntity::checkMonsterSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }
}
