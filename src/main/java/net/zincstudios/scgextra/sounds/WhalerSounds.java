package net.zincstudios.scgextra.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;

import static net.zincstudios.scgextra.sounds.ModSounds.SOUND_EVENTS;

public class WhalerSounds {

    public static final RegistryObject<SoundEvent> FISH_FOLK_IDLE = SOUND_EVENTS.register("leviathan_syndicate.fish_folk.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.fish_folk.idle")));

    public static final RegistryObject<SoundEvent> FISH_FOLK_HURT = SOUND_EVENTS.register("leviathan_syndicate.fish_folk.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.fish_folk.hurt")));
    
    public static final RegistryObject<SoundEvent> FISH_FOLK_DEATH = SOUND_EVENTS.register("leviathan_syndicate.fish_folk.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.fish_folk.death")));

    
    public static final RegistryObject<SoundEvent> TENTACLIATOR_IDLE = SOUND_EVENTS.register("leviathan_syndicate.tentacliator.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.tentacliator.idle")));

    public static final RegistryObject<SoundEvent> TENTACLIATOR_HURT = SOUND_EVENTS.register("leviathan_syndicate.tentacliator.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.tentacliator.hurt")));
    
    public static final RegistryObject<SoundEvent> TENTACLIATOR_DEATH = SOUND_EVENTS.register("leviathan_syndicate.tentacliator.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.tentacliator.death")));



    public static final RegistryObject<SoundEvent> WHALE_SLAM = SOUND_EVENTS.register("armored_whale_slam", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("armored_whale_slam")));

    public static final RegistryObject<SoundEvent> WHALE_SPLASH = SOUND_EVENTS.register("armored_whale_splash", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("armored_whale_splash")));

    public static final RegistryObject<SoundEvent> GUARDIAN_STATUE_CHARGE = SOUND_EVENTS.register("guardian_statue_charge", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("guardian_statue_charge")));

    public static void init() {}
}