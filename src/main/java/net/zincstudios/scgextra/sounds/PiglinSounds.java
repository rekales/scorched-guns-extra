package net.zincstudios.scgextra.sounds;

import static net.zincstudios.scgextra.sounds.ModSounds.SOUND_EVENTS;

import net.zincstudios.scgextra.SCGExtra;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class PiglinSounds {

    //PIGLIN NOB
    public static final RegistryObject<SoundEvent> PIGLIN_NOB_ATTACK = SOUND_EVENTS.register("piglin.piglin_nob.attack",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_nob.attack")));

    public static final RegistryObject<SoundEvent> PIGLIN_NOB_DEATH = SOUND_EVENTS.register("piglin.piglin_nob.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_nob.death")));

    public static final RegistryObject<SoundEvent> PIGLIN_NOB_IDLE = SOUND_EVENTS.register("piglin.piglin_nob.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_nob.idle")));

    public static final RegistryObject<SoundEvent> PIGLIN_NOB_SCREAM = SOUND_EVENTS.register("piglin.piglin_nob.scream",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_nob.scream")));
    
    //PIGLIN HOG RIDER
    public static final RegistryObject<SoundEvent> PIGLIN_HOG_RIDER_ATTACK = SOUND_EVENTS.register("piglin.piglin_hog_rider.attack",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_hog_rider.attack")));

    public static final RegistryObject<SoundEvent> PIGLIN_HOG_RIDER_IDLE = SOUND_EVENTS.register("piglin.piglin_hog_rider.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_hog_rider.idle")));

    public static final RegistryObject<SoundEvent> PIGLIN_HOG_RIDER_STUN = SOUND_EVENTS.register("piglin.piglin_hog_rider.stun",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_hog_rider.stun")));
    
    //PIGLIN STRIDER RIDER
    public static final RegistryObject<SoundEvent> PIGLIN_STRIDER_RIDER_ATTACK = SOUND_EVENTS.register("piglin.piglin_strider_rider.attack",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_strider_rider.attack")));

    public static final RegistryObject<SoundEvent> PIGLIN_STRIDER_RIDER_IDLE = SOUND_EVENTS.register("piglin.piglin_strider_rider.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_strider_rider.idle")));

    public static final RegistryObject<SoundEvent> PIGLIN_STRIDER_RIDER_DEATH = SOUND_EVENTS.register("piglin.piglin_strider_rider.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_strider_rider.death")));
    
    //PIGLIN BLAZE KOPTER
    public static final RegistryObject<SoundEvent> PIGLIN_BLAZE_KOPTER_GUN = SOUND_EVENTS.register("piglin.piglin_blaze_kopter.gun",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_blaze_kopter.gun")));

    public static final RegistryObject<SoundEvent> PIGLIN_BLAZE_KOPTER_IDLE = SOUND_EVENTS.register("piglin.piglin_blaze_kopter.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_blaze_kopter.idle")));

    public static final RegistryObject<SoundEvent> PIGLIN_BLAZE_KOPTER_DEATH = SOUND_EVENTS.register("piglin.piglin_blaze_kopter.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_blaze_kopter.death")));

    //PIGKAN
    public static final RegistryObject<SoundEvent> PIGKAN_IDLE = SOUND_EVENTS.register("piglin.pigkan.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.pigkan.idle")));

    //PIGDREAD
    public static final RegistryObject<SoundEvent> PIGDREAD_IDLE = SOUND_EVENTS.register("piglin.pigdread.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.pigdread.idle")));
    public static final RegistryObject<SoundEvent> PIGDREAD_DEATH = SOUND_EVENTS.register("piglin.pigdread.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.pigdread.death")));
    public static final RegistryObject<SoundEvent> PIGDREAD_WALK = SOUND_EVENTS.register("piglin.pigdread.walk",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.pigdread.walk")));

    //GENERAL FOR ALL PIGLIN MOBS
    public static final RegistryObject<SoundEvent> GENERAL_CLAW = SOUND_EVENTS.register("piglin.general.claw",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.general.claw")));
    public static final RegistryObject<SoundEvent> GENERAL_HURT = SOUND_EVENTS.register("piglin.general.hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.general.hurt")));
    public static final RegistryObject<SoundEvent> GENERAL_RUN = SOUND_EVENTS.register("piglin.general.run",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.general.run")));
    public static final RegistryObject<SoundEvent> GENERAL_SAW = SOUND_EVENTS.register("piglin.general.saw",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.general.saw")));
    public static final RegistryObject<SoundEvent> GENERAL_WALK = SOUND_EVENTS.register("piglin.general.walk",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.general.walk")));

    //PIGLIN KILN HOG
    public static final RegistryObject<SoundEvent> PIGLIN_KILN_HOG_IDLE = SOUND_EVENTS.register("piglin.piglin_kiln_hog.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_kiln_hog.idle")));
    public static final RegistryObject<SoundEvent> PIGLIN_KILN_HOG_GROWL = SOUND_EVENTS.register("piglin.piglin_kiln_hog.growl",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_kiln_hog.growl")));
    public static final RegistryObject<SoundEvent> PIGLIN_KILN_HOG_DEFEAT = SOUND_EVENTS.register("piglin.piglin_kiln_hog.defeat",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_kiln_hog.defeat")));

    //PIGLIN WARBOSS
    public static final RegistryObject<SoundEvent> PIGLIN_WARBOSS_IDLE = SOUND_EVENTS.register("piglin.piglin_warboss.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_warboss.idle")));
    public static final RegistryObject<SoundEvent> PIGLIN_WARBOSS_SCREAM = SOUND_EVENTS.register("piglin.piglin_warboss.scream",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_warboss.scream")));
    public static final RegistryObject<SoundEvent> PIGLIN_WARBOSS_DEATH = SOUND_EVENTS.register("piglin.piglin_warboss.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_warboss.death")));
    public static final RegistryObject<SoundEvent> PIGLIN_WARBOSS_STUN = SOUND_EVENTS.register("piglin.piglin_warboss.stun",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_warboss.stun")));
    public static final RegistryObject<SoundEvent> PIGLIN_WARBOSS_MELEE = SOUND_EVENTS.register("piglin.piglin_warboss.melee",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("piglin.piglin_warboss.melee")));

    public static void init() {}

}