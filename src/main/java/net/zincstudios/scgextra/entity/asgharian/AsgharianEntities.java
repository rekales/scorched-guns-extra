package net.zincstudios.scgextra.entity.asgharian;

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
import net.zincstudios.scgextra.entity.asgharian.candlefiend.CandleFiendEntity;
import net.zincstudios.scgextra.entity.asgharian.candlefiend.CandleFiendRenderer;
import net.zincstudios.scgextra.entity.asgharian.failedone.FailedOneEntity;
import net.zincstudios.scgextra.entity.asgharian.flamer.AsgharFlamerEntity;
import net.zincstudios.scgextra.entity.asgharian.soulripper.SoulRipperEntity;
import net.zincstudios.scgextra.entity.asgharian.surgeon.AsgharSurgeonEntity;
import net.zincstudios.scgextra.entity.asgharian.worker.AsgharWorkerEntity;
import net.zincstudios.scgextra.entity.asgharian.worker.AsgharWorkerModel;
import net.zincstudios.scgextra.entity.common.WeakPointBox;
import net.zincstudios.scgextra.entity.common.WeakPointBoxManager;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.client.GunHoldingMobRenderer;
import net.zincstudios.scgextra.entity.common.client.ItemHoldingMobRenderer;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.headshot.BasicHeadshotBox;
import top.ribs.scguns.common.headshot.RotatedHeadshotBox;

import static net.zincstudios.scgextra.entity.ModEntities.ENTITY_TYPES;

public class AsgharianEntities {

    public static final RegistryObject<EntityType<FailedOneEntity>> FAILED_ONE = ENTITY_TYPES
            .register("failed_one", () -> EntityType.Builder.of(FailedOneEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.1F)
                    .build("failed_one"));

    public static final RegistryObject<EntityType<AsgharSurgeonEntity>> ASGHAR_SURGEON = ENTITY_TYPES
            .register("asghar_surgeon", () -> EntityType.Builder.of(AsgharSurgeonEntity::new, MobCategory.MONSTER)
                    .sized(1.4F, 2.3F)
                    .build("asghar_surgeon"));

    public static final RegistryObject<EntityType<AsgharWorkerEntity>> ASGHAR_WORKER = ENTITY_TYPES
            .register("asghar_worker", () -> EntityType.Builder.of(AsgharWorkerEntity::new, MobCategory.MONSTER)
                    .sized(1F, 2.1F)
                    .build("asghar_worker"));

    public static final RegistryObject<EntityType<AsgharFlamerEntity>> ASGHAR_FLAMER = ENTITY_TYPES
            .register("asghar_flamer", () -> EntityType.Builder.of(AsgharFlamerEntity::new, MobCategory.MONSTER)
                    .sized(1.1F, 2.1F)
                    .build("asghar_flamer"));

    public static final RegistryObject<EntityType<CandleFiendEntity>> CANDLE_FIEND = ENTITY_TYPES
            .register("candle_fiend", () -> EntityType.Builder.of(CandleFiendEntity::new, MobCategory.MONSTER)
                    .sized(1.45F, 3.2F)
                    .setUpdateInterval(1)
                    .build("candle_fiend"));

    public static final RegistryObject<EntityType<SoulRipperEntity>> SOUL_RIPPER = ENTITY_TYPES
            .register("soul_ripper", () -> EntityType.Builder.of(SoulRipperEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.6F)
                    .setUpdateInterval(1)
                    .build("soul_ripper"));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AsgharianEntities::registerAttributes);
        modEventBus.addListener(AsgharianEntities::onCommonSetup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(AsgharianEntities::onClientSetup);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(AsgharianEntities.FAILED_ONE.get(), FailedOneEntity.createAttributes().build());
        event.put(AsgharianEntities.ASGHAR_SURGEON.get(), AsgharSurgeonEntity.createAttributes().build());
        event.put(AsgharianEntities.ASGHAR_WORKER.get(), AsgharWorkerEntity.createAttributes().build());
        event.put(AsgharianEntities.ASGHAR_FLAMER.get(), AsgharFlamerEntity.createAttributes().build());
        event.put(AsgharianEntities.CANDLE_FIEND.get(), CandleFiendEntity.createAttributes().build());
        event.put(AsgharianEntities.SOUL_RIPPER.get(), SoulRipperEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(AsgharianEntities.FAILED_ONE.get(), new BasicHeadshotBox<>(9.0, 24.0));
        BoundingBoxManager.registerHeadshotBox(AsgharianEntities.ASGHAR_SURGEON.get(), new RotatedHeadshotBox<>(9, 20.0, 14, false, true));
        BoundingBoxManager.registerHeadshotBox(AsgharianEntities.ASGHAR_WORKER.get(), new BasicHeadshotBox<>(14.0, 17, 17.0));
        BoundingBoxManager.registerHeadshotBox(AsgharianEntities.ASGHAR_FLAMER.get(), new RotatedHeadshotBox<>(8.0, 13, 17.0, 3, false, true));
        BoundingBoxManager.registerHeadshotBox(AsgharianEntities.CANDLE_FIEND.get(), new RotatedHeadshotBox<>(9.0, 39.0, 10, false, true));
        WeakPointBoxManager.registerWeakPointBox(AsgharianEntities.ASGHAR_FLAMER.get(), new WeakPointBox<>(new RotatedHeadshotBox<>(11.0, 16.0, 12.0, -8, false, true)));
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(AsgharianEntities.FAILED_ONE.get(), (ctx) -> new ItemHoldingMobRenderer<>(ctx,
                new DefaultedEntityGeoModel<>(SCGExtra.asResource("asgharian/failed_one"))).noDeathTilt().shadowRadius(0.5f));
        EntityRenderers.register(AsgharianEntities.ASGHAR_SURGEON.get(), (ctx) -> new BaseEntityRenderer<>(ctx,
                new DefaultedEntityGeoModel<>(SCGExtra.asResource("asgharian/asghar_surgeon"))).noDeathTilt());
        EntityRenderers.register(AsgharianEntities.ASGHAR_WORKER.get(), (ctx) -> new BaseEntityRenderer<>(ctx,
                new AsgharWorkerModel<>(SCGExtra.asResource("asgharian/asghar_worker"))).noDeathTilt());
        EntityRenderers.register(AsgharianEntities.ASGHAR_FLAMER.get(), (ctx) -> new GunHoldingMobRenderer<>(ctx,
                new DefaultedEntityGeoModel<>(SCGExtra.asResource("asgharian/asghar_flamer")), 10).noDeathTilt());
        EntityRenderers.register(AsgharianEntities.CANDLE_FIEND.get(), (ctx) -> new CandleFiendRenderer<>(ctx,
                new DefaultedEntityGeoModel<>(SCGExtra.asResource("asgharian/candle_fiend"))).noDeathTilt());
        EntityRenderers.register(AsgharianEntities.SOUL_RIPPER.get(), (ctx) -> new BaseEntityRenderer<>(ctx,
                new DefaultedEntityGeoModel<>(SCGExtra.asResource("asgharian/soul_ripper"))).noDeathTilt());
    }
}