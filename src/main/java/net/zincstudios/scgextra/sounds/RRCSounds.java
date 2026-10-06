package net.zincstudios.scgextra.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;

import static net.zincstudios.scgextra.sounds.ModSounds.SOUND_EVENTS;

public class RRCSounds {

    //Drone
    public static final RegistryObject<SoundEvent> RRC_DRONE_DEATH = SOUND_EVENTS.register("drone.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("drone.death")));
    public static final RegistryObject<SoundEvent> RRC_DRONE_HURT = SOUND_EVENTS.register("drone.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("drone.hurt1")));
    public static final RegistryObject<SoundEvent> RRC_DRONE_IDLE = SOUND_EVENTS.register("drone.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("drone.idle")));
    public static final RegistryObject<SoundEvent> RRC_DRONE_WALK = SOUND_EVENTS.register("drone.walk", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("drone.walk")));
    
    //Spring Junkie
    public static final RegistryObject<SoundEvent> RRC_SPRING_JUNKIE_DEATH = SOUND_EVENTS.register("spring_junkie.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("spring_junkie.death")));
    public static final RegistryObject<SoundEvent> RRC_SPRING_JUNKIE_LAUGH = SOUND_EVENTS.register("spring_junkie.laugh", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("spring_junkie.laugh")));
    public static final RegistryObject<SoundEvent> RRC_SPRING_JUNKIE_RUN = SOUND_EVENTS.register("spring_junkie.run", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("spring_junkie.run")));
    public static final RegistryObject<SoundEvent> RRC_SPRING_JUNKIE_SCREAM = SOUND_EVENTS.register("spring_junkie.scream", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("spring_junkie.scream")));
    public static final RegistryObject<SoundEvent> RRC_SPRING_JUNKIE_WALK = SOUND_EVENTS.register("spring_junkie.walk", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("spring_junkie.walk")));
    
    //Scout
    public static final RegistryObject<SoundEvent> RRC_SCOUT_DEATH = SOUND_EVENTS.register("scout.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scout.death")));
    public static final RegistryObject<SoundEvent> RRC_SCOUT_HURT = SOUND_EVENTS.register("scout.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scout.hurt")));
    public static final RegistryObject<SoundEvent> RRC_SCOUT_IDLE = SOUND_EVENTS.register("scout.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scout.idle")));
    public static final RegistryObject<SoundEvent> RRC_SCOUT_WALK = SOUND_EVENTS.register("scout.walk", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scout.walk")));
    
    //Tallman
    public static final RegistryObject<SoundEvent> RRC_TALLMAN_DEATH = SOUND_EVENTS.register("tallman.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("tallman.death")));
    public static final RegistryObject<SoundEvent> RRC_TALLMAN_HURT = SOUND_EVENTS.register("tallman.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("tallman.hurt")));
    public static final RegistryObject<SoundEvent> RRC_TALLMAN_IDLE = SOUND_EVENTS.register("tallman.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("tallman.idle")));
    public static final RegistryObject<SoundEvent> RRC_TALLMAN_WALK = SOUND_EVENTS.register("tallman.walk", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("tallman.walk")));
    
    //Oppressor
    public static final RegistryObject<SoundEvent> RRC_OPPRESSOR_ALERT = SOUND_EVENTS.register("oppressor.alert", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("oppressor.alert")));
    public static final RegistryObject<SoundEvent> RRC_OPPRESSOR_DEATH = SOUND_EVENTS.register("oppressor.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("oppressor.death")));
    public static final RegistryObject<SoundEvent> RRC_OPPRESSOR_HURT = SOUND_EVENTS.register("oppressor.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("oppressor.hurt")));
    public static final RegistryObject<SoundEvent> RRC_OPPRESSOR_IDLE = SOUND_EVENTS.register("oppressor.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("oppressor.idle")));
    
    //Scrapguard
    public static final RegistryObject<SoundEvent> RRC_SCRAPGUARD_PUNCH = SOUND_EVENTS.register("scrapguard.punch", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scrapguard.punch")));

    //Copper Knight
    public static final RegistryObject<SoundEvent> RRC_COPPER_KNIGHT_DEAD = SOUND_EVENTS.register("knight.dead", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("knight.dead")));
    public static final RegistryObject<SoundEvent> RRC_COPPER_KNIGHT_HURT = SOUND_EVENTS.register("knight.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("knight.hurt")));
    public static final RegistryObject<SoundEvent> RRC_COPPER_KNIGHT_IDLE = SOUND_EVENTS.register("knight.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("knight.idle")));

    //Flaming Head
    public static final RegistryObject<SoundEvent> RRC_FLAMING_HEAD_IDLE = SOUND_EVENTS.register("flaming_head.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("flaming_head.idle")));
    public static final RegistryObject<SoundEvent> RRC_FLAMING_HEAD_DEAD = SOUND_EVENTS.register("flaming_head.dead", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("flaming_head.dead")));
    public static final RegistryObject<SoundEvent> RRC_FLAMING_HEAD_SPIN = SOUND_EVENTS.register("flaming_head.spin", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("flaming_head.spin")));
    public static final RegistryObject<SoundEvent> RRC_FLAMING_HEAD_STUN = SOUND_EVENTS.register("flaming_head.stun", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("flaming_head.stun")));
       
    //Arc Psycho
    public static final RegistryObject<SoundEvent> RRC_ARC_PSYCHO_DEAD = SOUND_EVENTS.register("arc_psycho.dead", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("arc_psycho.dead")));
    public static final RegistryObject<SoundEvent> RRC_ARC_PSYCHO_HURT = SOUND_EVENTS.register("arc_psycho.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("arc_psycho.hurt")));
    public static final RegistryObject<SoundEvent> RRC_ARC_PSYCHO_IDLE = SOUND_EVENTS.register("arc_psycho.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("arc_psycho.idle")));
       
    //Gargoyle
    public static final RegistryObject<SoundEvent> RRC_GARGOYLE_DEAD = SOUND_EVENTS.register("scgextra.rrc.gargoyle.death", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scgextra.rrc.gargoyle.death")));
    public static final RegistryObject<SoundEvent> RRC_GARGOYLE_HURT = SOUND_EVENTS.register("scgextra.rrc.gargoyle.hurt", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scgextra.rrc.gargoyle.hurt")));
    public static final RegistryObject<SoundEvent> RRC_GARGOYLE_IDLE = SOUND_EVENTS.register("scgextra.rrc.gargoyle.idle", 
        () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scgextra.rrc.gargoyle.idle")));

    public static void init() {}
}