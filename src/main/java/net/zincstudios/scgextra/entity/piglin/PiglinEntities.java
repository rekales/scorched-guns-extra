package net.zincstudios.scgextra.entity.piglin;

import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.common.OffsetRotatedHeadshotBox;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.ItemHoldingMobRenderer;
import net.zincstudios.scgextra.entity.piglin.pigdread.PigdreadEntity;
import net.zincstudios.scgextra.entity.piglin.pigkan.PigkanEntity;
import net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter.PiglinBlazeKopterEntity;
import net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter.PiglinBlazeKopterProjectile;
import net.zincstudios.scgextra.entity.piglin.piglin_hog_rider.PiglinHogRiderEntity;
import net.zincstudios.scgextra.entity.piglin.piglin_kiln_hog.PiglinKilnHogEntity;
import net.zincstudios.scgextra.entity.piglin.piglin_kiln_hog.PiglinKilnHogRenderer;
import net.zincstudios.scgextra.entity.piglin.piglin_nob.PiglinNobEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;
import top.ribs.scguns.client.render.entity.ProjectileRenderer;
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.headshot.RotatedHeadshotBox;
import static net.zincstudios.scgextra.entity.ModEntities.ENTITY_TYPES;

import net.zincstudios.scgextra.entity.piglin.piglin_warboss.PiglinWarbossEntity;
import net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter.PiglinBlazeKopterRenderer;
import net.zincstudios.scgextra.entity.piglin.piglin_hog_rider.PiglinHogRiderRenderer;
import net.zincstudios.scgextra.entity.piglin.piglin_nob.PiglinNobRenderer;
import net.zincstudios.scgextra.entity.piglin.piglin_strider_rider.PiglinStriderRiderEntity;
import net.zincstudios.scgextra.entity.projectile.piglin_kiln_hog_magma_ball.PiglinKilnHogMagmaBall;

public class PiglinEntities {
    public static final RegistryObject<EntityType<PiglinNobEntity>> PIGLIN_NOB = ENTITY_TYPES
            .register("piglin_nob", () -> EntityType.Builder.of(PiglinNobEntity::new, MobCategory.MONSTER)
                    .sized(1.6F, 2.375F).build("piglin_nob"));
    public static final RegistryObject<EntityType<PiglinStriderRiderEntity>> PIGLIN_STRIDER_RIDER = ENTITY_TYPES
            .register("piglin_strider_rider", () -> EntityType.Builder.of(PiglinStriderRiderEntity::new, MobCategory.MONSTER)
                    .sized(1.375F, 2.75F).build("piglin_strider_rider"));
    public static final RegistryObject<EntityType<PiglinHogRiderEntity>> PIGLIN_HOG_RIDER = ENTITY_TYPES
            .register("piglin_hog_rider", () -> EntityType.Builder.of(PiglinHogRiderEntity::new, MobCategory.MONSTER)
                    .sized(2F, 3.6875F).build("piglin_hog_rider"));
    public static final RegistryObject<EntityType<PiglinBlazeKopterEntity>> PIGLIN_BLAZE_KOPTER = ENTITY_TYPES
            .register("piglin_blaze_kopter", () -> EntityType.Builder.of(PiglinBlazeKopterEntity::new, MobCategory.MONSTER)
                    .sized(2.37500F, 1.6875F).build("piglin_blaze_kopter"));
    public static final RegistryObject<EntityType<PigkanEntity>> PIGKAN = ENTITY_TYPES
            .register("pigkan", () -> EntityType.Builder.of(PigkanEntity::new, MobCategory.MONSTER)
                    .sized(2.5F, 2.9375F).build("pigkan"));
    public static final RegistryObject<EntityType<PiglinKilnHogEntity>> PIGLIN_KILN_HOG = ENTITY_TYPES
            .register("piglin_kiln_hog", () -> EntityType.Builder.of(PiglinKilnHogEntity::new, MobCategory.MONSTER)
                    .sized(3.9F, 5F).build("piglin_kiln_hog"));
    public static final RegistryObject<EntityType<PiglinKilnHogMagmaBall>> PIGLIN_KILN_HOG_MAGMA_BALL = ENTITY_TYPES
            .register("piglin_kiln_hog_magma_ball", () -> EntityType.Builder.<PiglinKilnHogMagmaBall>of(PiglinKilnHogMagmaBall::new, MobCategory.MISC).sized(1F, 1F).clientTrackingRange(4).updateInterval(20).build("piglin_kiln_hog_magma_ball"));
    public static final RegistryObject<EntityType<PigdreadEntity>> PIGDREAD = ENTITY_TYPES
            .register("pigdread", () -> EntityType.Builder.of(PigdreadEntity::new, MobCategory.MONSTER)
                .sized(3F, 4.3125F).build("pigdread"));
    public static final RegistryObject<EntityType<PiglinWarbossEntity>> PIGLIN_WARBOSS = ENTITY_TYPES
            .register("piglin_warboss", () -> EntityType.Builder.of(PiglinWarbossEntity::new, MobCategory.MONSTER)
                .sized(2.75F, 3.625F).build("piglin_warboss"));

    public static final RegistryObject<EntityType<PiglinBlazeKopterProjectile>> PIGLIN_BLAZE_KOPTER_PROJECTILE = ENTITY_TYPES
            .register("piglin_blaze_kopter_projectile", () -> EntityType.Builder.of(
                            (EntityType<PiglinBlazeKopterProjectile> type, Level level) -> new PiglinBlazeKopterProjectile(type, level), MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(3)
                    .build("piglin_blaze_kopter_projectile"));
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PiglinEntities::registerAttributes);
        modEventBus.addListener(PiglinEntities::onCommonSetup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(PiglinEntities::onClientSetup);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(PiglinEntities.PIGLIN_NOB.get(), PiglinNobEntity.createAttributes().build());
        event.put(PiglinEntities.PIGLIN_STRIDER_RIDER.get(), PiglinStriderRiderEntity.createAttributes().build());
        event.put(PiglinEntities.PIGLIN_HOG_RIDER.get(), PiglinHogRiderEntity.createAttributes().build());
        event.put(PiglinEntities.PIGLIN_BLAZE_KOPTER.get(), PiglinBlazeKopterEntity.createAttributes().build());
        event.put(PiglinEntities.PIGKAN.get(), PigkanEntity.createAttributes().build());
        event.put(PiglinEntities.PIGLIN_KILN_HOG.get(), PiglinKilnHogEntity.createAttributes().build());
        event.put(PiglinEntities.PIGDREAD.get(), PigdreadEntity.createAttributes().build());
        event.put(PiglinEntities.PIGLIN_WARBOSS.get(), PiglinWarbossEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGLIN_NOB.get(), new RotatedHeadshotBox<>(12.0, 14.0, 22.0, 15.0, false, true));
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGLIN_STRIDER_RIDER.get(), new RotatedHeadshotBox<>(12.0, 8.0, 30.0, 0.0, false, true));
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGLIN_HOG_RIDER.get(), new RotatedHeadshotBox<>(14.0, 14.0, 42.0, 8, false, true));
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGLIN_BLAZE_KOPTER.get(), new RotatedHeadshotBox<>(10.0, 8.0, 18.0, 0.0, false, true));
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGKAN.get(), new RotatedHeadshotBox<>(8.0, 7.0, 18.0, 12.0, false, true));
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGLIN_KILN_HOG.get(), new RotatedHeadshotBox<>(20.0, 22.0, 49.0, 20.0, false, true));
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGDREAD.get(), new OffsetRotatedHeadshotBox<>(
            24.0, 
            11.0, 
            42.0, 
            0, 
            10.5,
            false, 
            true,
            true
        ));
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGLIN_WARBOSS.get(), new RotatedHeadshotBox<>(14.0, 15.0, 30.0, 11.0, false, true));
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(PiglinEntities.PIGLIN_NOB.get(), (ctx) ->
            new PiglinNobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/piglin_nob"))).hasGlowLayer().noDeathTilt()
        );
        EntityRenderers.register(PiglinEntities.PIGLIN_STRIDER_RIDER.get(), (ctx) ->
            new ItemHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/piglin_strider_rider"))).hasGlowLayer().noDeathTilt()
        );
        EntityRenderers.register(PiglinEntities.PIGLIN_HOG_RIDER.get(), (ctx) ->
            new PiglinHogRiderRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/piglin_hog_rider"))).hasGlowLayer().noDeathTilt()
        );
        EntityRenderers.register(PiglinEntities.PIGLIN_BLAZE_KOPTER.get(), (ctx) ->
            new PiglinBlazeKopterRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/piglin_blaze_kopter"))).hasGlowLayer().noDeathTilt()
        );
        EntityRenderers.register(PiglinEntities.PIGKAN.get(), (ctx) ->
            new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/pigkan"))).hasGlowLayer().noDeathTilt()
        );
        EntityRenderers.register(PiglinEntities.PIGLIN_KILN_HOG.get(), (ctx) ->
            new PiglinKilnHogRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/piglin_kiln_hog"))).hasGlowLayer().noDeathTilt()
        );
        EntityRenderers.register(PiglinEntities.PIGDREAD.get(), (ctx) ->
            new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/pigdread"))).hasGlowLayer().noDeathTilt()
        );

        EntityRenderers.register(PiglinEntities.PIGLIN_WARBOSS.get(), (ctx) ->
            new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/piglin_warboss"))).hasGlowLayer().noDeathTilt()
        );

        EntityRenderers.register(PiglinEntities.PIGLIN_KILN_HOG_MAGMA_BALL.get(), (ctx) -> new GeoEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/piglin_kiln_hog_magma_ball"))));
        
        EntityRenderers.register(PiglinEntities.PIGLIN_BLAZE_KOPTER_PROJECTILE.get(), ProjectileRenderer::new);
    }
}