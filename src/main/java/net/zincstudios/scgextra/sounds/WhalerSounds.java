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
    
    public static final RegistryObject<SoundEvent> FISH_FOLK_ATTACK = SOUND_EVENTS.register("leviathan_syndicate.fish_folk.attack", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.fish_folk.attack")));
    
    public static final RegistryObject<SoundEvent> FISH_FOLK_DEATH = SOUND_EVENTS.register("leviathan_syndicate.fish_folk.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.fish_folk.death")));

    
    public static final RegistryObject<SoundEvent> TENTACLIATOR_IDLE = SOUND_EVENTS.register("leviathan_syndicate.tentacliator.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.tentacliator.idle")));

    public static final RegistryObject<SoundEvent> TENTACLIATOR_HURT = SOUND_EVENTS.register("leviathan_syndicate.tentacliator.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.tentacliator.hurt")));
    
    public static final RegistryObject<SoundEvent> TENTACLIATOR_DEATH = SOUND_EVENTS.register("leviathan_syndicate.tentacliator.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.tentacliator.death")));
    
    
    public static final RegistryObject<SoundEvent> SALMONSAUR_IDLE = SOUND_EVENTS.register("leviathan_syndicate.salmonsaur.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.salmonsaur.idle")));

    public static final RegistryObject<SoundEvent> SALMONSAUR_HURT = SOUND_EVENTS.register("leviathan_syndicate.salmonsaur.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.salmonsaur.hurt")));
    
    public static final RegistryObject<SoundEvent> SALMONSAUR_ATTACK = SOUND_EVENTS.register("leviathan_syndicate.salmonsaur.attack", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.salmonsaur.attack")));
    
    public static final RegistryObject<SoundEvent> SALMONSAUR_DEATH = SOUND_EVENTS.register("leviathan_syndicate.salmonsaur.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.salmonsaur.death")));
    
    
    public static final RegistryObject<SoundEvent> LION_FISH_IDLE = SOUND_EVENTS.register("leviathan_syndicate.lion_fish.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.lion_fish.idle")));

    public static final RegistryObject<SoundEvent> LION_FISH_HURT = SOUND_EVENTS.register("leviathan_syndicate.lion_fish.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.lion_fish.hurt")));
    
    public static final RegistryObject<SoundEvent> LION_FISH_ATTACK = SOUND_EVENTS.register("leviathan_syndicate.lion_fish.attack", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.lion_fish.attack")));
    
    public static final RegistryObject<SoundEvent> LION_FISH_DEATH = SOUND_EVENTS.register("leviathan_syndicate.lion_fish.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.lion_fish.death")));


    public static final RegistryObject<SoundEvent> ARMORED_WHALE_BITE = SOUND_EVENTS.register("leviathan_syndicate.armored_whale.bite", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.armored_whale.bite")));
    
    public static final RegistryObject<SoundEvent> ARMORED_WHALE_DEATH = SOUND_EVENTS.register("leviathan_syndicate.armored_whale.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.armored_whale.death")));
    
    public static final RegistryObject<SoundEvent> ARMORED_WHALE_FINSLAP = SOUND_EVENTS.register("leviathan_syndicate.armored_whale.finslap", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.armored_whale.finslap")));
    
    public static final RegistryObject<SoundEvent> ARMORED_WHALE_HURT = SOUND_EVENTS.register("leviathan_syndicate.armored_whale.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.armored_whale.hurt")));
    
    public static final RegistryObject<SoundEvent> ARMORED_WHALE_IDLE = SOUND_EVENTS.register("leviathan_syndicate.armored_whale.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.armored_whale.idle")));
    
    public static final RegistryObject<SoundEvent> ARMORED_WHALE_JUMP = SOUND_EVENTS.register("leviathan_syndicate.armored_whale.jump", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.armored_whale.jump")));
    
    public static final RegistryObject<SoundEvent> ARMORED_WHALE_SHOOT = SOUND_EVENTS.register("leviathan_syndicate.armored_whale.shoot", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.armored_whale.shoot")));
    
    public static final RegistryObject<SoundEvent> ARMORED_WHALE_SPLASH = SOUND_EVENTS.register("leviathan_syndicate.armored_whale.splash", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.armored_whale.splash")));


    public static final RegistryObject<SoundEvent> PUFFICUS_DEATH = SOUND_EVENTS.register("leviathan_syndicate.pufficus.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.pufficus.death")));

    public static final RegistryObject<SoundEvent> PUFFICUS_HURT = SOUND_EVENTS.register("leviathan_syndicate.pufficus.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.pufficus.hurt")));
    
    public static final RegistryObject<SoundEvent> PUFFICUS_IDLE = SOUND_EVENTS.register("leviathan_syndicate.pufficus.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.pufficus.idle")));
    
    public static final RegistryObject<SoundEvent> PUFFICUS_THROW_NET = SOUND_EVENTS.register("leviathan_syndicate.pufficus.throw_net", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("leviathan_syndicate.pufficus.throw_net")));


    public static final RegistryObject<SoundEvent> GUARDIAN_STATUE_CHARGE = SOUND_EVENTS.register("guardian_statue_charge", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("guardian_statue_charge")));

    public static void init() {}
}