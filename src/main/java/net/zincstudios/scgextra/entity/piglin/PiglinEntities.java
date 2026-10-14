package net.zincstudios.scgextra.entity.piglin;

import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.ItemHoldingMobRenderer;
import net.zincstudios.scgextra.entity.piglin.pigkan.PigkanEntity;
import net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter.PiglinBlazeKopterEntity;
import net.zincstudios.scgextra.entity.piglin.piglin_hog_rider.PiglinHogRiderEntity;
import net.zincstudios.scgextra.entity.piglin.piglin_kiln_hog.PiglinKilnHogEntity;
import net.zincstudios.scgextra.entity.piglin.piglin_nob.PiglinNobEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
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
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.headshot.RotatedHeadshotBox;
import static net.zincstudios.scgextra.entity.ModEntities.ENTITY_TYPES;

import net.zincstudios.scgextra.entity.piglin.piglin_blaze_kopter.PiglinBlazeKopterRenderer;
import net.zincstudios.scgextra.entity.piglin.piglin_hog_rider.PiglinHogRiderRenderer;
import net.zincstudios.scgextra.entity.piglin.piglin_nob.PiglinNobRenderer;
import net.zincstudios.scgextra.entity.piglin.piglin_strider_rider.PiglinStriderRiderEntity;

public class PiglinEntities {
    public static final RegistryObject<EntityType<PiglinNobEntity>> PIGLIN_NOB = ENTITY_TYPES
            .register("piglin_nob", () -> EntityType.Builder.of(PiglinNobEntity::new, MobCategory.MONSTER)
                    .sized(1.6F, 2.375F).build("piglin_nob"));
    public static final RegistryObject<EntityType<PiglinStriderRiderEntity>> PIGLIN_STRIDER_RIDER = ENTITY_TYPES
            .register("piglin_strider_rider", () -> EntityType.Builder.of(PiglinStriderRiderEntity::new, MobCategory.MONSTER)
                    .sized(1.375F, 2.75F).build("piglin_strider_rider"));
    public static final RegistryObject<EntityType<PiglinHogRiderEntity>> PIGLIN_HOG_RIDER = ENTITY_TYPES
            .register("piglin_hog_rider", () -> EntityType.Builder.of(PiglinHogRiderEntity::new, MobCategory.MONSTER)
                    .sized(2.8125F, 4F).build("piglin_hog_rider"));
    public static final RegistryObject<EntityType<PiglinBlazeKopterEntity>> PIGLIN_BLAZE_KOPTER = ENTITY_TYPES
            .register("piglin_blaze_kopter", () -> EntityType.Builder.of(PiglinBlazeKopterEntity::new, MobCategory.MONSTER)
                    .sized(2.37500F, 1.6875F).build("piglin_blaze_kopter"));
    public static final RegistryObject<EntityType<PigkanEntity>> PIGKAN = ENTITY_TYPES
            .register("pigkan", () -> EntityType.Builder.of(PigkanEntity::new, MobCategory.MONSTER)
                    .sized(2.5F, 2.9375F).build("pigkan"));
    public static final RegistryObject<EntityType<PiglinKilnHogEntity>> PIGLIN_KLIN_HOG = ENTITY_TYPES
            .register("piglin_klin_hog", () -> EntityType.Builder.of(PiglinKilnHogEntity::new, MobCategory.MONSTER)
                    .sized(5F, 5F).build("piglin_klin_hog"));

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
        event.put(PiglinEntities.PIGLIN_KLIN_HOG.get(), PiglinKilnHogEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGLIN_NOB.get(), new RotatedHeadshotBox<>(12.0, 14.0, 22.0, 15.0, false, true));
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGLIN_STRIDER_RIDER.get(), new RotatedHeadshotBox<>(12.0, 8.0, 30.0, 0.0, false, true));
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGKAN.get(), new RotatedHeadshotBox<>(8.0, 7.0, 18.0, 12.0, false, true));
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
        EntityRenderers.register(PiglinEntities.PIGLIN_KLIN_HOG.get(), (ctx) ->
            new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/piglin_kiln_hog"))).hasGlowLayer().noDeathTilt()
        );
    }
}