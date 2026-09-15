package net.zincstudios.scgextra.entity.rrc;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.common.OffsetRotatedHeadshotBox;
import net.zincstudios.scgextra.entity.common.WeakPointBox;
import net.zincstudios.scgextra.entity.common.WeakPointBoxManager;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.GunHoldingMobRenderer;
import net.zincstudios.scgextra.entity.rrc.arc_psycho.ArcPsychoEntity;
import net.zincstudios.scgextra.entity.rrc.copper_knight.CopperKnightEntity;
import net.zincstudios.scgextra.entity.rrc.drone.DroneEntity;
import net.zincstudios.scgextra.entity.rrc.flaminghead.FlamingHeadEntity;
import net.zincstudios.scgextra.entity.rrc.flaminghead.FlamingHeadRenderer;
import net.zincstudios.scgextra.entity.rrc.gargoyle.GargoyleEntity;
import net.zincstudios.scgextra.entity.rrc.oppressor.OppressorEntity;
import net.zincstudios.scgextra.entity.rrc.scout.ScoutEntity;
import net.zincstudios.scgextra.entity.rrc.scout.ScoutRenderer;
import net.zincstudios.scgextra.entity.rrc.scrapguard.ScrapGuardEntity;
import net.zincstudios.scgextra.entity.rrc.spring_junkie.SpringJunkieEntity;
import net.zincstudios.scgextra.entity.rrc.tallman.TallmanEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.headshot.RotatedHeadshotBox;

import static net.zincstudios.scgextra.entity.ModEntities.ENTITY_TYPES;

public class RRCEntities {

    public static final RegistryObject<EntityType<DroneEntity>> DRONE = ENTITY_TYPES
            .register("drone", () -> EntityType.Builder.of(DroneEntity::new, MobCategory.MONSTER)
                    .sized(2.7F, 3.9F).build("drone"));

    public static final RegistryObject<EntityType<TallmanEntity>> TALLMAN = ENTITY_TYPES
            .register("tallman", () -> EntityType.Builder.of(TallmanEntity::new, MobCategory.MONSTER)
                    .sized(1F, 4.5F)
                    .build("tallman"));

    public static final RegistryObject<EntityType<ScoutEntity>> SCOUT = ENTITY_TYPES
            .register("scout", () -> EntityType.Builder.of(ScoutEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .build("scout"));

    public static final RegistryObject<EntityType<OppressorEntity>> OPPRESSOR = ENTITY_TYPES
            .register("oppressor", () -> EntityType.Builder.of(OppressorEntity::new, MobCategory.MONSTER)
                    .sized(2F, 4F)
                    .build("oppressor"));

    public static final RegistryObject<EntityType<SpringJunkieEntity>> SPRING_JUNKIE = ENTITY_TYPES
            .register("spring_junkie", () -> EntityType.Builder.of(SpringJunkieEntity::new, MobCategory.MONSTER)
                    .sized(1.30F, 3.5F)
                    .build("spring_junkie"));

    public static final RegistryObject<EntityType<FlamingHeadEntity>> FLAMING_HEAD = ENTITY_TYPES
            .register("flaming_head", () -> EntityType.Builder.of(FlamingHeadEntity::new, MobCategory.MONSTER)
                    .sized(2.6F, 5F)
                    .setUpdateInterval(1)
                    .build("flaming_head"));

    public static final RegistryObject<EntityType<ScrapGuardEntity>> SCRAP_GUARD = ENTITY_TYPES
            .register("scrap_guard", () -> EntityType.Builder.of(ScrapGuardEntity::new, MobCategory.MONSTER)
                    .sized(1.17F, 3F)
                    .build("scrap_guard"));

    public static final RegistryObject<EntityType<ArcPsychoEntity>> ARC_PSYCHO = ENTITY_TYPES
            .register("arc_psycho", () -> EntityType.Builder.of(ArcPsychoEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2F)
                    .build("arc_psycho"));

    public static final RegistryObject<EntityType<CopperKnightEntity>> COPPER_KNIGHT = ENTITY_TYPES
            .register("copper_knight", () -> EntityType.Builder.of(CopperKnightEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2F)
                    .build("copper_knight"));

    public static final RegistryObject<EntityType<GargoyleEntity>> GARGOYLE = ENTITY_TYPES
            .register("gargoyle", () -> EntityType.Builder.of(GargoyleEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2F)
                    .build("gargoyle"));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RRCEntities::registerAttributes);
        modEventBus.addListener(RRCEntities::onCommonSetup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(RRCEntities::onClientSetup);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RRCEntities.DRONE.get(), DroneEntity.createAttributes().build());
        event.put(RRCEntities.TALLMAN.get(), TallmanEntity.createAttributes().build());
        event.put(RRCEntities.SCOUT.get(), ScoutEntity.createAttributes().build());
        event.put(RRCEntities.OPPRESSOR.get(), OppressorEntity.createAttributes().build());
        event.put(RRCEntities.SPRING_JUNKIE.get(), SpringJunkieEntity.createAttributes().build());
        event.put(RRCEntities.FLAMING_HEAD.get(), FlamingHeadEntity.createAttributes().build());
        event.put(RRCEntities.SCRAP_GUARD.get(), ScrapGuardEntity.createAttributes().build());
        event.put(RRCEntities.ARC_PSYCHO.get(), ArcPsychoEntity.createAttributes().build());
        event.put(RRCEntities.COPPER_KNIGHT.get(), CopperKnightEntity.createAttributes().build());
        event.put(RRCEntities.GARGOYLE.get(), GargoyleEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(RRCEntities.DRONE.get(), new RotatedHeadshotBox<>(15.0, 28.0, 20, false, true));
        BoundingBoxManager.registerHeadshotBox(RRCEntities.FLAMING_HEAD.get(), new OffsetRotatedHeadshotBox<>(10.0F, 55.0, 13, 70, false, true));
        BoundingBoxManager.registerHeadshotBox(RRCEntities.OPPRESSOR.get(), new OffsetRotatedHeadshotBox<>(10.0, 15.0, 60.0, 0.0F, 6.0, false, true));
        BoundingBoxManager.registerHeadshotBox(RRCEntities.SCRAP_GUARD.get(), new OffsetRotatedHeadshotBox<>(11.0, 14.0, 40.0, 0.0F, 1.5, false, true));
        BoundingBoxManager.registerHeadshotBox(RRCEntities.SPRING_JUNKIE.get(), new OffsetRotatedHeadshotBox<>(9.0, 20.0, 34.0, 0.0F, 0, false, true));
        BoundingBoxManager.registerHeadshotBox(RRCEntities.COPPER_KNIGHT.get(), new OffsetRotatedHeadshotBox<>(8, 9.5, 28.0, 0.0F, 2.0, false, true));

        WeakPointBoxManager.registerWeakPointBox(RRCEntities.FLAMING_HEAD.get(), new WeakPointBox<>(new OffsetRotatedHeadshotBox<>(10.0F, 55.0, 13, -70, false, true)));
    }
    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(RRCEntities.DRONE.get(), (ctx) ->
                new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/drone")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(RRCEntities.TALLMAN.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/tallman")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(RRCEntities.SCOUT.get(), (ctx) ->
                new ScoutRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/scout")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(RRCEntities.OPPRESSOR.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/oppressor")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(RRCEntities.SPRING_JUNKIE.get(), (ctx) ->
                new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/spring_junkie")))
                        .hasGlowLayer()
        );
        EntityRenderers.register(RRCEntities.FLAMING_HEAD.get(), (ctx) ->
                new FlamingHeadRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/flaming_head")))
                        .hasGlowLayer()
        );
        EntityRenderers.register(RRCEntities.SCRAP_GUARD.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/scrap_guard")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(RRCEntities.ARC_PSYCHO.get(), (ctx) ->
                new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/arc_psycho")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(RRCEntities.COPPER_KNIGHT.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/copper_knight")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(RRCEntities.GARGOYLE.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("rrc/gargoyle")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
    }
}
