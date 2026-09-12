package net.zincstudios.scgextra.entity.cog;

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
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.cog.centipede.CogCentipedeRenderer;
import net.zincstudios.scgextra.entity.cog.devastator.CogDevastatorRenderer;
import net.zincstudios.scgextra.entity.cog.gigantes.CogGigantesRenderer;
import net.zincstudios.scgextra.entity.cog.juggernaut.CogJuggernautRenderer;
import net.zincstudios.scgextra.entity.cog.venator.CogVenatorRenderer;
import net.zincstudios.scgextra.entity.cog.vulture.CogVultureRenderer;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.cog.bombardier.CogBombardierEntity;
import net.zincstudios.scgextra.entity.cog.centipede.CogCentipedeEntity;
import net.zincstudios.scgextra.entity.cog.centipede.PlasmaCannonProjectileEntity;
import net.zincstudios.scgextra.entity.cog.devastator.CogDevastatorEntity;
import net.zincstudios.scgextra.entity.cog.gigantes.CogGigantesEntity;
import net.zincstudios.scgextra.entity.cog.juggernaut.CogJuggernautEntity;
import net.zincstudios.scgextra.entity.cog.juggernaut.RocketBarrageProjectileEntity;
import net.zincstudios.scgextra.entity.cog.trenchbleeder.CogTrenchbleederEntity;
import net.zincstudios.scgextra.entity.cog.trenchbleeder.CogTrenchbleederRenderer;
import net.zincstudios.scgextra.entity.cog.venator.CogVenatorEntity;
import net.zincstudios.scgextra.entity.cog.vulture.CogVultureEntity;
import net.zincstudios.scgextra.entity.common.WeakPointBox;
import net.zincstudios.scgextra.entity.common.WeakPointBoxManager;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import top.ribs.scguns.client.render.entity.ProjectileRenderer;
import top.ribs.scguns.client.render.entity.RocketRenderer;
import top.ribs.scguns.common.BoundingBoxManager;
import top.ribs.scguns.common.headshot.BasicHeadshotBox;
import top.ribs.scguns.common.headshot.RotatedHeadshotBox;

import static net.zincstudios.scgextra.entity.ModEntities.ENTITY_TYPES;

public class COGEntities {

    public static final RegistryObject<EntityType<CogVultureEntity>> VULTURE = ENTITY_TYPES
            .register("cog_vulture", () -> EntityType.Builder.of(CogVultureEntity::new, MobCategory.MONSTER)
                    .sized(0.85F, 1.7F)
                    .build("cog_vulture"));

    public static final RegistryObject<EntityType<CogDevastatorEntity>> DEVASTATOR = ENTITY_TYPES
            .register("cog_devastator", () -> EntityType.Builder.of(CogDevastatorEntity::new, MobCategory.MONSTER)
                    .sized(1.9F, 3.25F)
                    .build("cog_devastator"));

    public static final RegistryObject<EntityType<CogBombardierEntity>> BOMBARDIER = ENTITY_TYPES
            .register("cog_bombardier", () -> EntityType.Builder.of(CogBombardierEntity::new, MobCategory.MONSTER)
                    .sized(2.5F, 2.9F)
                    .build("cog_bombardier"));

    public static final RegistryObject<EntityType<CogGigantesEntity>> GIGANTES = ENTITY_TYPES
            .register("cog_gigantes", () -> EntityType.Builder.of(CogGigantesEntity::new, MobCategory.MONSTER)
                    .sized(1.95F, 3F)
                    .build("cog_gigantes"));

    public static final RegistryObject<EntityType<CogVenatorEntity>> VENATOR = ENTITY_TYPES
            .register("cog_venator", () -> EntityType.Builder.of(CogVenatorEntity::new, MobCategory.MONSTER)
                    .sized(0.92F, 1.5F)
                    .build("cog_venator"));

    public static final RegistryObject<EntityType<CogCentipedeEntity>> CENTIPEDE = ENTITY_TYPES
            .register("cog_centipede", () -> EntityType.Builder.of(CogCentipedeEntity::new, MobCategory.MONSTER)
                    .sized(2F, 2.9F)
                    .build("cog_centipede"));

    public static final RegistryObject<EntityType<CogJuggernautEntity>> JUGGERNAUT = ENTITY_TYPES
            .register("cog_juggernaut", () -> EntityType.Builder.of(CogJuggernautEntity::new, MobCategory.MONSTER)
                    .sized(1.9F, 3.0F)
                    .setUpdateInterval(1)
                    .build("cog_juggernaut"));

    public static final RegistryObject<EntityType<CogTrenchbleederEntity>> TRENCHBLEEDER = ENTITY_TYPES
            .register("cog_trenchbleeder", () -> EntityType.Builder.of(CogTrenchbleederEntity::new, MobCategory.MONSTER)
                    .sized(2F, 3.0F)
                    .setUpdateInterval(1)
                    .build("cog_trenchbleeder"));

    public static final RegistryObject<EntityType<PlasmaCannonProjectileEntity>> PLASMA_CANNON_PROJECTILE = ENTITY_TYPES
            .register("plasma_cannon_projectile", () -> EntityType.Builder.of(
                            (EntityType<PlasmaCannonProjectileEntity> type, Level level) -> new PlasmaCannonProjectileEntity(type, level), MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(4)
                    .updateInterval(3)
                    .build("plasma_cannon_projectile"));

    public static final RegistryObject<EntityType<RocketBarrageProjectileEntity>> ROCKET_BARRAGE_PROJECTILE = ENTITY_TYPES
            .register("rocket_barrage_projectile", () -> EntityType.Builder.of(
                            (EntityType<RocketBarrageProjectileEntity> type, Level level) -> new RocketBarrageProjectileEntity(type, level), MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .setTrackingRange(100)
                    .setUpdateInterval(1)
                    .noSummon()
                    .fireImmune()
                    .noSave()
                    .setShouldReceiveVelocityUpdates(true)
                    .build("rocket_barrage_projectile"));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(COGEntities::registerAttributes);
        modEventBus.addListener(COGEntities::onCommonSetup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(COGEntities::onClientSetup);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(COGEntities.VULTURE.get(), CogVultureEntity.createAttributes().build());
        event.put(COGEntities.DEVASTATOR.get(), CogDevastatorEntity.createAttributes().build());
        event.put(COGEntities.BOMBARDIER.get(), CogBombardierEntity.createAttributes().build());
        event.put(COGEntities.GIGANTES.get(), CogGigantesEntity.createAttributes().build());
        event.put(COGEntities.VENATOR.get(), CogVenatorEntity.createAttributes().build());
        event.put(COGEntities.CENTIPEDE.get(), CogCentipedeEntity.createAttributes().build());
        event.put(COGEntities.JUGGERNAUT.get(), CogJuggernautEntity.createAttributes().build());
        event.put(COGEntities.TRENCHBLEEDER.get(), CogTrenchbleederEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(COGEntities.BOMBARDIER.get(), new RotatedHeadshotBox<>(8, 36, 6, false, true));
        WeakPointBoxManager.registerWeakPointBox(COGEntities.BOMBARDIER.get(), new WeakPointBox<>(new RotatedHeadshotBox<>(8, 36, -6, false, true)));
        BoundingBoxManager.registerHeadshotBox(COGEntities.GIGANTES.get(), new RotatedHeadshotBox<>(8, 18, 18, false, true));
        BoundingBoxManager.registerHeadshotBox(COGEntities.CENTIPEDE.get(), new BasicHeadshotBox<>(1,0));
        BoundingBoxManager.registerHeadshotBox(COGEntities.VENATOR.get(), new BasicHeadshotBox<>(0,0));
        BoundingBoxManager.registerHeadshotBox(COGEntities.VULTURE.get(), new BasicHeadshotBox<>(10,16));
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(COGEntities.VULTURE.get(), (ctx) ->
                new CogVultureRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("cog/cog_vulture")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(COGEntities.DEVASTATOR.get(), (ctx) ->
                new CogDevastatorRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("cog/cog_devastator"), "chest"))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(COGEntities.BOMBARDIER.get(), (ctx) ->
                new BaseEntityRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("cog/cog_bombardier")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(COGEntities.GIGANTES.get(), (ctx) ->
                new CogGigantesRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("cog/cog_gigantes")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(COGEntities.VENATOR.get(), (ctx) ->
                new CogVenatorRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("cog/cog_venator")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(COGEntities.CENTIPEDE.get(), (ctx) ->
                new CogCentipedeRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("cog/cog_centipede")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(COGEntities.JUGGERNAUT.get(), (ctx) ->
                new CogJuggernautRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("cog/cog_juggernaut"), "chest"))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(COGEntities.TRENCHBLEEDER.get(), (ctx) ->
                new CogTrenchbleederRenderer(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("cog/cog_trenchbleeder")))
                        .hasGlowLayer()
                        .noDeathTilt()
        );
        EntityRenderers.register(COGEntities.PLASMA_CANNON_PROJECTILE.get(), ProjectileRenderer::new);
        EntityRenderers.register(COGEntities.ROCKET_BARRAGE_PROJECTILE.get(), RocketRenderer::new);
    }

}
