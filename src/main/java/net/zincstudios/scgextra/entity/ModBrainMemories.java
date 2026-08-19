package net.zincstudios.scgextra.entity;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.common.gun.SimulatedGun;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// TODO: replace boolean with unit
/*
AbilityState exists as a general purpose container for abilities to use since they are all run
exclusively with one another (or at least seem to be).

DELAYED_MELEE long value is for when the damage will take place (gameTime timestamp). It is intended to have an
expiry time, that will tell the brain how long does the whole melee sequence lasts.
 */
public class ModBrainMemories {

    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister
            .create(ForgeRegistries.MEMORY_MODULE_TYPES, SCGExtra.MOD_ID);

    public static final Supplier<MemoryModuleType<Integer>> AIM_TICKS = MEMORY_MODULE_TYPES
            .register("aim_ticks", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final Supplier<MemoryModuleType<Float>> WEAPON_IDEAL_RANGE = MEMORY_MODULE_TYPES
            .register("approach_dist", () -> new MemoryModuleType<>(Optional.of(Codec.FLOAT)));
    public static final Supplier<MemoryModuleType<Float>> WEAPON_MAX_RANGE = MEMORY_MODULE_TYPES
            .register("weapon_range", () -> new MemoryModuleType<>(Optional.of(Codec.FLOAT)));
    public static final Supplier<MemoryModuleType<SimulatedGun>> SIMULATED_GUN = MEMORY_MODULE_TYPES
            .register("simulated_gun", () -> new MemoryModuleType<>(Optional.empty()));
    public static final Supplier<MemoryModuleType<Boolean>> STUNNED = MEMORY_MODULE_TYPES
            .register("stunned", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
    public static final Supplier<MemoryModuleType<List<LivingEntity>>> TO_ALERT = MEMORY_MODULE_TYPES
            .register("to_alert", () -> new MemoryModuleType<>(Optional.empty()));
    public static final Supplier<MemoryModuleType<Boolean>> STUNNED_COOLING_DOWN = MEMORY_MODULE_TYPES
            .register("stunned_cooling_down", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));
    public static final Supplier<MemoryModuleType<Integer>> HEADSHOT_COUNT = MEMORY_MODULE_TYPES
            .register("headshot_count", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    public static final Supplier<MemoryModuleType<AbilityState>> ABILITY_STATE = MEMORY_MODULE_TYPES
            .register("ability_state", () -> new MemoryModuleType<>(Optional.of(AbilityState.CODEC)));
    public static final Supplier<MemoryModuleType<PositionTracker>> RELOCATE_TARGET = MEMORY_MODULE_TYPES
            .register("relocate_target", () -> new MemoryModuleType<>(Optional.empty()));
    public static final Supplier<MemoryModuleType<Long>> DELAYED_MELEE = MEMORY_MODULE_TYPES
            .register("delayed_melee", () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));

    // Mob Specific Memories
    public static final Supplier<MemoryModuleType<Boolean>> JET_BOOTS_COOLING_DOWN = MEMORY_MODULE_TYPES
            .register("jet_boots_cooling_down", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

    public static final int MAX_AIM_TICKS = 60;
    public static final int MIN_AIM_TICKS = -20;

    public static void register(IEventBus modEventBus) {
        MEMORY_MODULE_TYPES.register(modEventBus);
    }
}
