package net.zincstudios.scgextra;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.config.ModConfig;
import net.zincstudios.scgextra.block.ModBlockEntities;
import net.zincstudios.scgextra.block.ModBlocks;
import net.zincstudios.scgextra.attributes.SCGEAttributes;
import net.zincstudios.scgextra.data.RaidDataLoader;
import net.zincstudios.scgextra.datagen.DataGenerators;
import net.zincstudios.scgextra.debug.DevTestCommands;
import net.zincstudios.scgextra.effects.ModEffects;
import net.zincstudios.scgextra.entity.Faction;
import net.zincstudios.scgextra.entity.ModEntities;
import net.zincstudios.scgextra.entity.common.client.GunFlashHandler;
import net.zincstudios.scgextra.entity.wreckers.wrecker_green.WreckerGreenEntity;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.zincstudios.scgextra.item.ModItems;
import net.zincstudios.scgextra.network.SCGEPacketHandler;
import net.zincstudios.scgextra.particle.ModParticleTypes;
import net.zincstudios.scgextra.raid.WaveRaidManager;
import net.zincstudios.scgextra.sounds.ModSounds;
import net.zincstudios.scgextra.worldgen.biome.ModBiomes;
import net.zincstudios.scgextra.worldgen.biome.ModTerrablender;
import net.zincstudios.scgextra.worldgen.biome.surface.ModSurfaceRules;
import net.zincstudios.scgextra.worldgen.structure.ModStructureProcessors;
import net.zincstudios.scgextra.worldgen.structure.ModStructures;
import terrablender.api.SurfaceRuleManager;
import top.ribs.scguns.entity.projectile.RocketEntity;

import org.slf4j.Logger;

@SuppressWarnings("unused")
@Mod(SCGExtra.MOD_ID)
public class SCGExtra
{
    public static final String MOD_ID = "scgextra";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SCGExtra(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        ModEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        ModItems.register(modEventBus);
        ModSounds.register(modEventBus);
        ModParticleTypes.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModStructures.register(modEventBus);
        ModStructureProcessors.register(modEventBus);

        SCGEAttributes.register(modEventBus);

        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        modEventBus.addListener(CommonConfig::onLoad);
        modEventBus.addListener(CommonConfig::onReload);
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.addListener(GunFlashHandler::onTick);
        MinecraftForge.EVENT_BUS.addListener(WaveRaidManager::onLevelTick);
        MinecraftForge.EVENT_BUS.addListener(WaveRaidManager::onLevelLoad);
        MinecraftForge.EVENT_BUS.addListener(Faction::onTagsUpdated);
        MinecraftForge.EVENT_BUS.addListener(RaidDataLoader::onAddReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(DevTestCommands::registerCommands);

        modEventBus.addListener(DataGenerators::gatherData);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        SCGEPacketHandler.init();
        event.enqueueWork(()->{
            ModTerrablender.registerBiomes();
            SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MOD_ID, ModSurfaceRules.makeRules());
        });
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Mod.EventBusSubscriber(modid = SCGExtra.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class SpawnEvent {
        @SubscribeEvent
        public static void onCheckSpawn(MobSpawnEvent.PositionCheck event) {
            if (event.getLevel().getBiome(event.getEntity().blockPosition()).is(ModBiomes.WARZONE_BIOME)) {
                if (event.getEntity() instanceof Animal) {
                    event.setResult(Result.DENY);
                }
            }
        }
        @SubscribeEvent
        public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
            if(event.getExplosion().getDirectSourceEntity() instanceof RocketEntity re){
                if(re.getShooter().getMainHandItem().is(ModItems.WRECKER_RPG.get())){
                    event.getAffectedEntities().removeIf(entity -> entity instanceof ItemEntity);
                }
            }
            if(event.getExplosion().getDirectSourceEntity() instanceof WreckerGreenEntity){
                event.getAffectedEntities().removeIf(entity -> entity instanceof ItemEntity);
            }
        }
    }
}