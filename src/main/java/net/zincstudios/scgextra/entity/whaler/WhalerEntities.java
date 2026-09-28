package net.zincstudios.scgextra.entity.whaler;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.common.client.GunHoldingMobRenderer;
import net.zincstudios.scgextra.entity.common.client.TransformableItemHoldingMobRenderer;
import net.zincstudios.scgextra.entity.projectile.net.NetEntityModel;
import net.zincstudios.scgextra.entity.projectile.ink_projectile.InkProjectileEntityModel;
import net.zincstudios.scgextra.entity.whaler.armoredwhale.ArmoredWhaleEntity;
import net.zincstudios.scgextra.entity.whaler.armoredwhale.MountedGunProjectileEntity;
import net.zincstudios.scgextra.entity.whaler.fishfolk.FishFolkEntity;
import net.zincstudios.scgextra.entity.whaler.fishfolk.FishFolkModel;
import net.zincstudios.scgextra.entity.whaler.guardian_statue.GuardianStatueEntity;
import net.zincstudios.scgextra.entity.whaler.guardian_statue.GuardianStatueRenderer;
import net.zincstudios.scgextra.entity.whaler.lion_fish.LionFishEntity;
import net.zincstudios.scgextra.entity.whaler.pufficus.PufficusEntity;
import net.zincstudios.scgextra.entity.whaler.salmonsaur.SalmonsaurEntity;
import net.zincstudios.scgextra.entity.whaler.salmonsaur.SalmonsaurRenderer;
import net.zincstudios.scgextra.entity.whaler.tentacliator.TentacliatorEntity;
import net.zincstudios.scgextra.entity.whaler.tentacliator.TentacliatorModel;
import net.zincstudios.scgextra.entity.whaler.turtleman.TurtlemanEntity;
import net.zincstudios.scgextra.entity.whaler.turtleman.TurtlemanRenderer;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import top.ribs.scguns.client.render.entity.ProjectileRenderer;
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.headshot.BasicHeadshotBox;
import top.ribs.scguns.common.headshot.RotatedHeadshotBox;

import static net.zincstudios.scgextra.entity.ModEntities.ENTITY_TYPES;

import net.zincstudios.scgextra.entity.whaler.armoredwhale.ArmoredWhaleRenderer;

public class WhalerEntities {

    public static final RegistryObject<EntityType<FishFolkEntity>> FISH_FOLK = ENTITY_TYPES
            .register("fish_folk", () -> EntityType.Builder.of(FishFolkEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).build("fish_folk"));

    public static final RegistryObject<EntityType<TurtlemanEntity>> TURTLEMAN = ENTITY_TYPES
            .register("turtleman", () -> EntityType.Builder.of(TurtlemanEntity::new, MobCategory.MONSTER)
                    .updateInterval(1)
                    .sized(1F, 2.3F)
                    .build("turtleman"));

    public static final RegistryObject<EntityType<SalmonsaurEntity>> SALMONSAUR = ENTITY_TYPES
            .register("salmonsaur", () -> EntityType.Builder.of(SalmonsaurEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 3.7F).build("salmonsaur"));

    public static final RegistryObject<EntityType<GuardianStatueEntity>> GUARDIAN_STATUE = ENTITY_TYPES
            .register("guardian_statue", () -> EntityType.Builder.of(GuardianStatueEntity::new, MobCategory.MONSTER)
                    .setUpdateInterval(1)
                    .sized(1.4F, 6.6F)
                    .build("guardian_statue"));

    public static final RegistryObject<EntityType<TentacliatorEntity>> TENTACLIATOR = ENTITY_TYPES
            .register("tentacliator", () -> EntityType.Builder.of(TentacliatorEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).build("tentacliator"));

    public static final RegistryObject<EntityType<PufficusEntity>> PUFFICUS = ENTITY_TYPES
            .register("pufficus", () -> EntityType.Builder.of(PufficusEntity::new, MobCategory.MONSTER)
                    .sized(1F, 3F).build("pufficus"));

    public static final RegistryObject<EntityType<ArmoredWhaleEntity>> ARMORED_WHALE = ENTITY_TYPES
            .register("armored_whale", () -> EntityType.Builder.of(ArmoredWhaleEntity::new, MobCategory.MONSTER)
                    .updateInterval(1)
                    .sized(5.5F, 6F)
                    .build("armored_whale"));

    public static final RegistryObject<EntityType<LionFishEntity>> LION_FISH = ENTITY_TYPES
            .register("lion_fish", () -> EntityType.Builder.of(LionFishEntity::new, MobCategory.MONSTER)
                    .sized(3.5F, 4F).build("lion_fish"));

    public static final RegistryObject<EntityType<MountedGunProjectileEntity>> MOUNTED_GUN_PROJECTILE = ENTITY_TYPES
            .register("mounted_gun_projectile", () -> EntityType.Builder.of(
                            (EntityType<MountedGunProjectileEntity> type, Level level) -> new MountedGunProjectileEntity(type, level), MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(3)
                    .build("mounted_gun_projectile"));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(WhalerEntities::registerAttributes);
        modEventBus.addListener(WhalerEntities::onCommonSetup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(WhalerEntities::onClientSetup);
            modEventBus.addListener(WhalerEntities::registerLayers);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WhalerEntities.FISH_FOLK.get(), FishFolkEntity.createAttributes().build());
        event.put(WhalerEntities.TURTLEMAN.get(), TurtlemanEntity.createAttributes().build());
        event.put(WhalerEntities.SALMONSAUR.get(), SalmonsaurEntity.createAttributes().build());
        event.put(WhalerEntities.GUARDIAN_STATUE.get(), GuardianStatueEntity.createAttributes().build());
        event.put(WhalerEntities.TENTACLIATOR.get(), TentacliatorEntity.createAttributes().build());
        event.put(WhalerEntities.PUFFICUS.get(), PufficusEntity.createAttributes().build());
        event.put(WhalerEntities.ARMORED_WHALE.get(), ArmoredWhaleEntity.createAttributes().build());
        event.put(WhalerEntities.LION_FISH.get(), LionFishEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(WhalerEntities.TURTLEMAN.get(), new BasicHeadshotBox<>(11.0, 28.0));
        BoundingBoxManager.registerHeadshotBox(WhalerEntities.TENTACLIATOR.get(), new BasicHeadshotBox<>(11.0, 20.0));
        BoundingBoxManager.registerHeadshotBox(WhalerEntities.FISH_FOLK.get(), new BasicHeadshotBox<>(8.0, 25.0));
        BoundingBoxManager.registerHeadshotBox(WhalerEntities.PUFFICUS.get(), new BasicHeadshotBox<>(11.0, 35.0));
//        BoundingBoxManager.registerHeadshotBox(WhalerEntities.GUARDIAN_STATUE.get(), new BasicHeadshotBox<>(0.0, 0.0, 0.0));
        BoundingBoxManager.registerHeadshotBox(WhalerEntities.GUARDIAN_STATUE.get(), new RotatedHeadshotBox<>(12.0, 8.0, 91.0, 10.0, false, true));
        BoundingBoxManager.registerHeadshotBox(WhalerEntities.LION_FISH.get(), new RotatedHeadshotBox<>(30.0, 36.0, 52.0, true, true));
        BoundingBoxManager.registerHeadshotBox(WhalerEntities.ARMORED_WHALE.get(), new RotatedHeadshotBox<>(16.0, 19.0, 59.0, 183.0, false, true));
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(WhalerEntities.FISH_FOLK.get(), (ctx) ->
                new GunHoldingMobRenderer<>(ctx, new FishFolkModel<>(SCGExtra.asResource("whaler/fish_folk")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(WhalerEntities.SALMONSAUR.get(), (ctx) ->
                new SalmonsaurRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("whaler/salmonsaur")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(WhalerEntities.TURTLEMAN.get(), (ctx) ->
                new TurtlemanRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("whaler/turtleman")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(WhalerEntities.GUARDIAN_STATUE.get(), GuardianStatueRenderer::new);
        EntityRenderers.register(WhalerEntities.TENTACLIATOR.get(), (ctx) -> 
                new GunHoldingMobRenderer<>(ctx, new TentacliatorModel<>(SCGExtra.asResource("whaler/tentacliator")))
                        .hasGlowLayer()
                        .noDeathTilt()
                        .noDeathRedTint()
        );
        EntityRenderers.register(WhalerEntities.PUFFICUS.get(), (ctx) ->
                new TransformableItemHoldingMobRenderer<>(ctx, 
                        new DefaultedEntityGeoModel<>(SCGExtra.asResource("whaler/pufficus")), 
                        new Vec3(-0.3, 0, 0), // translate
                        new Vec3(0, 80, -90), // rotate
                        new Vec3(1.5, 1.5, 1.5) // scale
                )
                .hasGlowLayer()
                .noDeathTilt()
                .noDeathRedTint()
        );
        EntityRenderers.register(WhalerEntities.LION_FISH.get(), (ctx) ->
                new TransformableItemHoldingMobRenderer<>(ctx, 
                        new DefaultedEntityGeoModel<>(SCGExtra.asResource("whaler/lion_fish")), 
                        new Vec3(0.7, -1.2, -1), // translate
                        new Vec3(-90, 0, 0), // rotate
                        new Vec3(1.5, 1.5, 1.5) // scale
                )
                .hasGlowLayer()
                .noDeathTilt()
                .noDeathRedTint()
        );
        EntityRenderers.register(WhalerEntities.ARMORED_WHALE.get(), (ctx) -> new ArmoredWhaleRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("whaler/armored_whale")))
                        .hasGlowLayer()
                        .noDeathTilt());
        EntityRenderers.register(WhalerEntities.MOUNTED_GUN_PROJECTILE.get(), ProjectileRenderer::new);
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // TODO: replace with geomodel
        event.registerLayerDefinition(NetEntityModel.LAYER_LOCATION, NetEntityModel::createBodyLayer);
        event.registerLayerDefinition(InkProjectileEntityModel.LAYER_LOCATION, InkProjectileEntityModel::createBodyLayer);
    }

}
