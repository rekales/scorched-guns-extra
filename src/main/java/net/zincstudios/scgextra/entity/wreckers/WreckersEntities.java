package net.zincstudios.scgextra.entity.wreckers;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
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
import net.zincstudios.scgextra.entity.wreckers.wrecker_biker.WreckerBikerEntity;
import net.zincstudios.scgextra.entity.wreckers.wrecker_blue.WreckerBlueEntity;
import net.zincstudios.scgextra.entity.wreckers.wrecker_dozer.WreckerDozerEntity;
import net.zincstudios.scgextra.entity.wreckers.wrecker_dozer.WreckerDozerRenderer;
import net.zincstudios.scgextra.entity.wreckers.wrecker_green.WreckerGreenEntity;
import net.zincstudios.scgextra.entity.wreckers.wrecker_green.WreckerGreenRenderer;
import net.zincstudios.scgextra.entity.wreckers.wrecker_helicube.WreckerHelicubeEntity;
import net.zincstudios.scgextra.entity.wreckers.wrecker_helicube.WreckerHelicubeRenderer;
import net.zincstudios.scgextra.entity.wreckers.wrecker_jumbo.WreckerJumboEntity;
import net.zincstudios.scgextra.entity.wreckers.wrecker_red.WreckerRedEntity;
import net.zincstudios.scgextra.entity.wreckers.wrecker_turret_gunner.WreckerTurretGunnerEntity;
import net.zincstudios.scgextra.entity.wreckers.wrecker_turret_gunner.WreckerTurretGunnerRenderer;
import net.zincstudios.scgextra.sounds.WreckerDeathSounds;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.headshot.BasicHeadshotBox;

import static net.zincstudios.scgextra.entity.ModEntities.ENTITY_TYPES;

public class WreckersEntities {

    public static final RegistryObject<EntityType<WreckerRedEntity>> WRECKER_RED = ENTITY_TYPES
            .register("wrecker_red", () -> EntityType.Builder.of(WreckerRedEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.0F)
                    .build("wrecker_red"));

    public static final RegistryObject<EntityType<WreckerBlueEntity>> WRECKER_BLUE = ENTITY_TYPES
            .register("wrecker_blue", () -> EntityType.Builder.of(WreckerBlueEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.3F)
                    .build("wrecker_blue"));

    public static final RegistryObject<EntityType<WreckerGreenEntity>> WRECKER_GREEN = ENTITY_TYPES
            .register("wrecker_green", () -> EntityType.Builder.of(WreckerGreenEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.3F)
                    .build("wrecker_green"));

    public static final RegistryObject<EntityType<WreckerJumboEntity>> WRECKER_JUMBO = ENTITY_TYPES
            .register("wrecker_jumbo", () -> EntityType.Builder.of(WreckerJumboEntity::new, MobCategory.MONSTER)
                    .sized(1.9F, 4.1F)
                    .build("wrecker_jumbo"));

    public static final RegistryObject<EntityType<WreckerHelicubeEntity>> WRECKER_HELICUBE = ENTITY_TYPES
            .register("wrecker_helicube", () -> EntityType.Builder.of(WreckerHelicubeEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 0.9F)
                    .updateInterval(1)
                    .build("wrecker_helicube"));

    public static final RegistryObject<EntityType<WreckerTurretGunnerEntity>> WRECKER_TURRET_GUNNER = ENTITY_TYPES
            .register("wrecker_turret_gunner", () -> EntityType.Builder.of(WreckerTurretGunnerEntity::new, MobCategory.MONSTER)
                    .sized(1.8F, 2.1F)
                    .build("wrecker_turret_gunner"));

    public static final RegistryObject<EntityType<WreckerDozerEntity>> WRECKER_DOZER = ENTITY_TYPES
            .register("wrecker_dozer", () -> EntityType.Builder.of(WreckerDozerEntity::new, MobCategory.MONSTER)
                    .setUpdateInterval(1)
                    .sized(4.0F, 3.5F)
                    .build("wrecker_dozer"));

    public static final RegistryObject<EntityType<WreckerBikerEntity>> WRECKER_BIKER = ENTITY_TYPES
            .register("wrecker_biker", () -> EntityType.Builder.of(WreckerBikerEntity::new, MobCategory.MONSTER)
                    .sized(1.375F, 2.5125F)
                    .build("wrecker_biker"));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(WreckersEntities::registerAttributes);
        modEventBus.addListener(WreckersEntities::onCommonSetup);

        MinecraftForge.EVENT_BUS.addListener(WreckerDeathSounds::onLivingDeath);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(WreckersEntities::onClientSetup);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WRECKER_RED.get(), WreckerRedEntity.createAttributes().build());
        event.put(WRECKER_BLUE.get(), WreckerBlueEntity.createAttributes().build());
        event.put(WRECKER_GREEN.get(), WreckerGreenEntity.createAttributes().build());
        event.put(WRECKER_JUMBO.get(), WreckerJumboEntity.createAttributes().build());
        event.put(WRECKER_HELICUBE.get(), WreckerHelicubeEntity.createAttributes().build());
        event.put(WRECKER_TURRET_GUNNER.get(), WreckerTurretGunnerEntity.createAttributes().build());
        event.put(WRECKER_DOZER.get(), WreckerDozerEntity.createAttributes().build());
        event.put(WRECKER_BIKER.get(), WreckerBikerEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(WRECKER_RED.get(), new BasicHeadshotBox<>(10.0, 10.0, 23.0));
        BoundingBoxManager.registerHeadshotBox(WRECKER_BLUE.get(),
                new OffsetRotatedHeadshotBox<>(10.0, 28.0, 1.0, 0.0F, false, true));
        BoundingBoxManager.registerHeadshotBox(WRECKER_GREEN.get(), new BasicHeadshotBox<>(10.0, 10.0, 28.0));
        BoundingBoxManager.registerHeadshotBox(WRECKER_JUMBO.get(),
                new OffsetRotatedHeadshotBox<>(16.0, 14.0, 52.0, 0.0F, 4.25, false, true));
        BoundingBoxManager.registerHeadshotBox(WRECKER_TURRET_GUNNER.get(),
                new OffsetRotatedHeadshotBox<>(10.0, 9.0, 23.5, 0.0F, -6.0, false, true));
        BoundingBoxManager.registerHeadshotBox(WRECKER_DOZER.get(),
                new OffsetRotatedHeadshotBox<>(13.0, 16.0, 9.0, 0.0F, false, true));
        WeakPointBoxManager.registerWeakPointBox(WRECKER_DOZER.get(),
                new WeakPointBox<>(new OffsetRotatedHeadshotBox<>(13.0, 16.0, 9.0, 0.0F, false, true)),
                new WeakPointBox<>(new OffsetRotatedHeadshotBox<>(24.0, 24.0, 18.0, 0.0F, -24.0, false, true)));
        BoundingBoxManager.registerHeadshotBox(WRECKER_BIKER.get(), new BasicHeadshotBox<>(10.0, 10.0, 28.0));
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(WRECKER_RED.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("wreckers/wrecker_red")))
                        .noDeathTilt()
                        .hasGlowLayer());
        EntityRenderers.register(WRECKER_BLUE.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("wreckers/wrecker_blue")))
                        .noDeathTilt()
                        .hasGlowLayer());
        EntityRenderers.register(WRECKER_GREEN.get(), (ctx) ->
                new WreckerGreenRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("wreckers/wrecker_green")))
                        .noDeathTilt()
                        .hasGlowLayer());
        EntityRenderers.register(WRECKER_JUMBO.get(), (ctx) ->
                new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("wreckers/wrecker_jumbo")))
                        .noDeathTilt()
                        .hasGlowLayer());
        EntityRenderers.register(WRECKER_TURRET_GUNNER.get(), (ctx) ->
                new WreckerTurretGunnerRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("wreckers/wrecker_turret_gunner")))
                        .noDeathTilt()
                        .hasGlowLayer());
        EntityRenderers.register(WRECKER_DOZER.get(), (ctx) ->
                new WreckerDozerRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("wreckers/wrecker_dozer")))
                        .noDeathTilt()
                        .hasGlowLayer());
        EntityRenderers.register(WRECKER_BIKER.get(), (ctx) ->
                new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("wreckers/wrecker_biker")))
                        .noDeathTilt()
                        .hasGlowLayer());

        EntityRenderers.register(WRECKER_HELICUBE.get(), WreckerHelicubeRenderer::new);
    }
}
