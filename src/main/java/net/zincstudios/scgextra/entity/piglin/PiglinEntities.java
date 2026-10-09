package net.zincstudios.scgextra.entity.piglin;

import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.common.client.GunHoldingMobRenderer;
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

public class PiglinEntities {
    public static final RegistryObject<EntityType<PiglinNobEntity>> PIGLIN_NOB = ENTITY_TYPES
            .register("piglin_nob", () -> EntityType.Builder.of(PiglinNobEntity::new, MobCategory.MONSTER)
                    .sized(1.125F, 2.375F).build("piglin_nob"));

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PiglinEntities::registerAttributes);
        modEventBus.addListener(PiglinEntities::onCommonSetup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(PiglinEntities::onClientSetup);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(PiglinEntities.PIGLIN_NOB.get(), PiglinNobEntity.createAttributes().build());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        BoundingBoxManager.registerHeadshotBox(PiglinEntities.PIGLIN_NOB.get(), new RotatedHeadshotBox<>(12.0, 14.0, 22.0, 15.0, false, true));
    }

    @OnlyIn(value = Dist.CLIENT)
    private static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(PiglinEntities.PIGLIN_NOB.get(), (ctx) ->
            new GunHoldingMobRenderer<>(ctx, new DefaultedEntityGeoModel<>(SCGExtra.asResource("piglin/piglin_nob"))).hasGlowLayer().noDeathTilt()
        );
    }
}