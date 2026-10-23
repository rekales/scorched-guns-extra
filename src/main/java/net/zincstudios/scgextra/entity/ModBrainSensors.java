package net.zincstudios.scgextra.entity;

import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.entity.common.brain.CustomGunSensor;
import net.zincstudios.scgextra.entity.common.brain.HeldGunSensor;
import net.zincstudios.scgextra.entity.common.brain.VarRangePlayerSensor;

import java.util.function.Supplier;

public class ModBrainSensors {

    private static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister
            .create(ForgeRegistries.SENSOR_TYPES, SCGExtra.MOD_ID);

    public static final Supplier<SensorType<HeldGunSensor>> HELD_GUN = SENSOR_TYPES
            .register("held_gun", () -> new SensorType<>(HeldGunSensor::new));
    public static final Supplier<SensorType<HeldGunSensor>> HELD_GUN_RAPID = SENSOR_TYPES
            .register("held_gun_rapid", () -> new SensorType<>(() -> new HeldGunSensor(2)));
    public static final Supplier<SensorType<CustomGunSensor>> CUSTOM_GUN = SENSOR_TYPES
            .register("custom_gun", () -> new SensorType<>(CustomGunSensor::new));

    // Improved versions of vanilla sensors
    public static final Supplier<SensorType<VarRangePlayerSensor>> SHORT_RANGE_PLAYER = SENSOR_TYPES
            .register("short_range_player", () -> new SensorType<>(() -> new VarRangePlayerSensor(20, 16)));
    public static final Supplier<SensorType<VarRangePlayerSensor>> MEDIUM_RANGE_PLAYER = SENSOR_TYPES
            .register("medium_range_player", () -> new SensorType<>(() -> new VarRangePlayerSensor(30, 24)));
    public static final Supplier<SensorType<VarRangePlayerSensor>> LONG_RANGE_PLAYER = SENSOR_TYPES
            .register("long_range_player", () -> new SensorType<>(() -> new VarRangePlayerSensor(40, 32)));
    public static final Supplier<SensorType<VarRangePlayerSensor>> VERY_LONG_RANGE_PLAYER = SENSOR_TYPES
            .register("very_long_range_player", () -> new SensorType<>(() -> new VarRangePlayerSensor(50, 48)));
    public static final Supplier<SensorType<VarRangePlayerSensor>> EXTREMELY_LONG_RANGE_PLAYER = SENSOR_TYPES
            .register("extremely_long_range_player", () -> new SensorType<>(() -> new VarRangePlayerSensor(60, 128)));

    public static void register(IEventBus modEventBus) {
        SENSOR_TYPES.register(modEventBus);
    }
}
