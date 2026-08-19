package net.zincstudios.scgextra.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;

import static net.zincstudios.scgextra.sounds.ModSounds.SOUND_EVENTS;

public class AsgharianSounds {

    // Asghar Surgeon
    public static final RegistryObject<SoundEvent> ASGHAR_SURGEON_ATTACK = SOUND_EVENTS.register("asghar_surgeon.attack",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_surgeon.attack")));
    public static final RegistryObject<SoundEvent> ASGHAR_SURGEON_DEATH = SOUND_EVENTS.register("asghar_surgeon.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_surgeon.death")));
    public static final RegistryObject<SoundEvent> ASGHAR_SURGEON_HURT = SOUND_EVENTS.register("asghar_surgeon.hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_surgeon.hurt")));
    public static final RegistryObject<SoundEvent> ASGHAR_SURGEON_IDLE = SOUND_EVENTS.register("asghar_surgeon.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_surgeon.idle")));

    // Asghar Worker
    public static final RegistryObject<SoundEvent> ASGHAR_WORKER_CLAW = SOUND_EVENTS.register("asghar_worker.claw",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_worker.claw")));
    public static final RegistryObject<SoundEvent> ASGHAR_WORKER_DEATH = SOUND_EVENTS.register("asghar_worker.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_worker.death")));
    public static final RegistryObject<SoundEvent> ASGHAR_WORKER_HURT = SOUND_EVENTS.register("asghar_worker.hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_worker.hurt")));
    public static final RegistryObject<SoundEvent> ASGHAR_WORKER_SAW = SOUND_EVENTS.register("asghar_worker.saw",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_worker.saw")));
    public static final RegistryObject<SoundEvent> ASGHAR_WORKER_WALK = SOUND_EVENTS.register("asghar_worker.walk",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_worker.walk")));

    // Asghar Flamer
    public static final RegistryObject<SoundEvent> ASGHAR_FLAMER_ATTACK = SOUND_EVENTS.register("asghar_flamer.attack",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_flamer.attack")));
    public static final RegistryObject<SoundEvent> ASGHAR_FLAMER_DEATH = SOUND_EVENTS.register("asghar_flamer.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_flamer.death")));
    public static final RegistryObject<SoundEvent> ASGHAR_FLAMER_HURT = SOUND_EVENTS.register("asghar_flamer.hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_flamer.hurt")));
    public static final RegistryObject<SoundEvent> ASGHAR_FLAMER_IDLE = SOUND_EVENTS.register("asghar_flamer.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("asghar_flamer.idle")));

    // Candle Fiend
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_DEATH = SOUND_EVENTS.register("candle_fiend.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.death")));
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_HURT = SOUND_EVENTS.register("candle_fiend.hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.hurt")));
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_IDLE = SOUND_EVENTS.register("candle_fiend.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.idle")));
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_REVIVE = SOUND_EVENTS.register("candle_fiend.revive",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.revive")));
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_RUN = SOUND_EVENTS.register("candle_fiend.run",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.run")));
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_SCREAM = SOUND_EVENTS.register("candle_fiend.scream",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.scream")));
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_SLASH = SOUND_EVENTS.register("candle_fiend.slash",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.slash")));
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_SLAM = SOUND_EVENTS.register("candle_fiend.smash",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.smash")));
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_WALK = SOUND_EVENTS.register("candle_fiend.walk",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.walk")));
    public static final RegistryObject<SoundEvent> CANDLE_FIEND_WARNING = SOUND_EVENTS.register("candle_fiend.warning",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("candle_fiend.warning")));

    // Soul Ripper
    public static final RegistryObject<SoundEvent> SOUL_RIPPER_ATTACK = SOUND_EVENTS.register("soul_ripper.attack",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("soul_ripper.attack")));
    public static final RegistryObject<SoundEvent> SOUL_RIPPER_DEATH = SOUND_EVENTS.register("soul_ripper.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("soul_ripper.death")));
    public static final RegistryObject<SoundEvent> SOUL_RIPPER_HURT = SOUND_EVENTS.register("soul_ripper.hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("soul_ripper.hurt")));
    public static final RegistryObject<SoundEvent> SOUL_RIPPER_IDLE = SOUND_EVENTS.register("soul_ripper.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("soul_ripper.idle")));
    public static final RegistryObject<SoundEvent> SOUL_RIPPER_LANTERN_SHATTER = SOUND_EVENTS.register("soul_ripper.lantern_shatter",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("soul_ripper.lantern_shatter")));
    public static final RegistryObject<SoundEvent> SOUL_RIPPER_REVIVE = SOUND_EVENTS.register("soul_ripper.revive",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("soul_ripper.revive")));

    public static void init() {}
}
