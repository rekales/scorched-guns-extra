package net.zincstudios.scgextra.sounds;

import static net.zincstudios.scgextra.sounds.ModSounds.SOUND_EVENTS;

import net.zincstudios.scgextra.SCGExtra;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class PiglinSounds {

    public static final RegistryObject<SoundEvent> PIGLIN_NOB_ATTACK = SOUND_EVENTS.register("piglin.piglin_nob.attack",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_nob.attack")));

    public static final RegistryObject<SoundEvent> PIGLIN_NOB_DEATH = SOUND_EVENTS.register("piglin.piglin_nob.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_nob.death")));

    public static final RegistryObject<SoundEvent> PIGLIN_NOB_IDLE = SOUND_EVENTS.register("piglin.piglin_nob.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_nob.idle")));

    public static final RegistryObject<SoundEvent> PIGLIN_NOB_SCREAM = SOUND_EVENTS.register("piglin.piglin_nob.scream",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_nob.scream")));

    public static void init() {}

}