package net.zincstudios.scgextra.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.client.particle.CopperFireBallParticle;
import net.zincstudios.scgextra.client.particle.CopperFlameParticle;

@SuppressWarnings("unused")
public class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SCGExtra.MOD_ID);

    public static final RegistryObject<SimpleParticleType> COPPER_FIRE_BALL = PARTICLE_TYPES
            .register("copper_fire_ball", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> COPPER_FLAME = PARTICLE_TYPES
            .register("copper_flame", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> LARGE_FLAME = PARTICLE_TYPES
            .register("large_flame", () -> new SimpleParticleType(true));


    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ModParticleTypes::registerParticleFactories);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.COPPER_FIRE_BALL.get(), CopperFireBallParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.COPPER_FLAME.get(), CopperFlameParticle.Provider::new);
    }
}
