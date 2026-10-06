package net.zincstudios.scgextra.entity;

import net.zincstudios.scgextra.item.curios.MedKitRenderer;
import net.zincstudios.scgextra.item.curios.MediPackRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.asgharian.AsgharianEntities;
import net.zincstudios.scgextra.entity.cog.COGEntities;
import net.zincstudios.scgextra.entity.common.client.BaseEntityRenderer;
import net.zincstudios.scgextra.entity.common.raid_summoner.RaidSummonerEntity;
import net.zincstudios.scgextra.entity.fac.FACEntities;
import net.zincstudios.scgextra.entity.neutral.NeutralEntities;
import net.zincstudios.scgextra.entity.projectile.ArmoredWhaleProjectileEntity;
import net.zincstudios.scgextra.entity.projectile.BigLumpProjectileEntity;
import net.zincstudios.scgextra.entity.projectile.FireProjectile;
import net.zincstudios.scgextra.entity.projectile.SoulFireBallRenderer;
import net.zincstudios.scgextra.entity.projectile.SoulFireball;
import net.zincstudios.scgextra.entity.rrc.RRCEntities;
import net.zincstudios.scgextra.entity.turret.TurretManningHandler;
import net.zincstudios.scgextra.entity.turret.TurretSeatEntity;
import net.zincstudios.scgextra.entity.whaler.WhalerEntities;
import net.zincstudios.scgextra.entity.wreckers.WreckersEntities;
import net.zincstudios.scgextra.item.ModItems;
import top.ribs.scguns.entity.client.EnemyProjectileRenderer;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class ModEntities {

    // TODO: move miscellaneous entity type registration to their related packages
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SCGExtra.MOD_ID);

    // TODO: Implement night raids
    public static final RegistryObject<EntityType<RaidSummonerEntity>> RAID_SUMMONER = ENTITY_TYPES
            .register("raid_summoner", () -> EntityType.Builder.of(RaidSummonerEntity::new, MobCategory.MONSTER)
                    .updateInterval(1)
                    .sized(0.5F, 0.5F)
                    .build("raid_summoner"));

    public static final RegistryObject<EntityType<ArmoredWhaleProjectileEntity>> WHALE_PROJECTILE = ENTITY_TYPES
            .register("whale_tank_projectile", () -> EntityType.Builder.of(ArmoredWhaleProjectileEntity::create, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("whale_tank_projectile"));

    public static final RegistryObject<EntityType<ArmoredWhaleProjectileEntity>> BIG_LUMP_PROJECTILE = ENTITY_TYPES
            .register("big_lump_projectile", () -> EntityType.Builder.of(BigLumpProjectileEntity::create, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("big_lump_projectile"));

    public static final RegistryObject<EntityType<FireProjectile>> FIRE_PROJECTILE = ENTITY_TYPES
            .register("fire_projectile", () -> EntityType.Builder.of(FireProjectile::create, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .setTrackingRange(64)
                    .setUpdateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("fire_projectile"));

    public static final RegistryObject<EntityType<TurretSeatEntity>> TURRET_SEAT = ENTITY_TYPES
            .register("turret_seat", () -> EntityType.Builder.<TurretSeatEntity>of(TurretSeatEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(8)
                    .updateInterval(20)
                    .build("turret_seat"));

    public static final RegistryObject<EntityType<SoulFireball>> LARGE_SOUL_FIREBALL = ENTITY_TYPES
            .register("soul_fireball", () -> EntityType.Builder.of(
                    (EntityType<SoulFireball> type, Level level) -> new SoulFireball(type, level), MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(4)
                    .updateInterval(3)
                    .build("soul_fireball"));

    public static void register(IEventBus modEventBus) {
        WhalerEntities.register(modEventBus);
        RRCEntities.register(modEventBus);
        FACEntities.register(modEventBus);
        NeutralEntities.register(modEventBus);
        AsgharianEntities.register(modEventBus);
        COGEntities.register(modEventBus);
        WreckersEntities.register(modEventBus);

        ModBrainMemories.register(modEventBus);
        ModBrainSensors.register(modEventBus);
        ModBrainActivities.register(modEventBus);

        ENTITY_TYPES.register(modEventBus);

        modEventBus.addListener(ModEntities::registerAttributes);
        MinecraftForge.EVENT_BUS.addListener(EntityAdjustments::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(TurretManningHandler::onLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(TurretManningHandler::onAttackEntity);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ModEntities::onClientSetup);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RAID_SUMMONER.get(), RaidSummonerEntity.createAttributes().build());
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.WHALE_PROJECTILE.get(), EnemyProjectileRenderer::new);
        EntityRenderers.register(ModEntities.BIG_LUMP_PROJECTILE.get(), EnemyProjectileRenderer::new);
        EntityRenderers.register(ModEntities.FIRE_PROJECTILE.get(), EnemyProjectileRenderer::new);
        EntityRenderers.register(ModEntities.RAID_SUMMONER.get(), (ctx) -> new BaseEntityRenderer<>(ctx, SCGExtra.asResource("whaler/net")));
        EntityRenderers.register(ModEntities.LARGE_SOUL_FIREBALL.get(), SoulFireBallRenderer::new);
        EntityRenderers.register(ModEntities.TURRET_SEAT.get(), NoopRenderer::new);
        CuriosRendererRegistry.register(ModItems.MEDKIT.get(), MedKitRenderer::new);
        CuriosRendererRegistry.register(ModItems.MEDIPACK.get(), MediPackRenderer::new);
    }
}
