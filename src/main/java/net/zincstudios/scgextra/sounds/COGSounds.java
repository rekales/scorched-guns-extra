package net.zincstudios.scgextra.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;

import static net.zincstudios.scgextra.sounds.ModSounds.SOUND_EVENTS;

public final class COGSounds {

    public static final RegistryObject<SoundEvent> GENERAL_HEAVY_HURT = SOUND_EVENTS.register("cog_general.heavy_hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_general.heavy_hurt")));
    public static final RegistryObject<SoundEvent> GENERAL_LIGHT_HURT = SOUND_EVENTS.register("cog_general.light_hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_general.light_hurt")));
    public static final RegistryObject<SoundEvent> GENERAL_IDLE = SOUND_EVENTS.register("cog_general.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_general.idle")));

    public static final RegistryObject<SoundEvent> COG_DEVASTATOR_IDLE = SOUND_EVENTS.register("cog_devastator.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_devastator.idle")));

    public static final RegistryObject<SoundEvent> COG_BOMBARDIER_WALK = SOUND_EVENTS.register("cog_bombardier.walk",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_bombardier.walk")));
    public static final RegistryObject<SoundEvent> COG_BOMBARDIER_SCAN = SOUND_EVENTS.register("cog_bombardier.scan",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_bombardier.scan")));

    public static final RegistryObject<SoundEvent> COG_GIGANTES_FLY = SOUND_EVENTS.register("cog_gigantes.fly",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_gigantes.fly")));
    public static final RegistryObject<SoundEvent> COG_GIGANTES_IDLE = SOUND_EVENTS.register("cog_gigantes.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_gigantes.idle")));

    public static final RegistryObject<SoundEvent> COG_VENATOR_ATTACK = SOUND_EVENTS.register("cog_venator.attack",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_venator.attack")));
    public static final RegistryObject<SoundEvent> COG_VENATOR_RUN = SOUND_EVENTS.register("cog_venator.run",  // TODO: extend flee goal or something
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_venator.run")));

    public static final RegistryObject<SoundEvent> COG_CENTIPEDE_IDLE = SOUND_EVENTS.register("cog_centipede.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_centipede.idle")));

    public static final RegistryObject<SoundEvent> COG_JUGGERNAUT_DEAD = SOUND_EVENTS.register("cog_juggernaut.dead",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_juggernaut.dead")));
    public static final RegistryObject<SoundEvent> COG_JUGGERNAUT_HURT = SOUND_EVENTS.register("cog_juggernaut.hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("cog_juggernaut.hurt")));

    public static final RegistryObject<SoundEvent> COG_TRENCHBLEEDER_IDLE = SOUND_EVENTS.register("scgextra.cog.cog_trenchbleeder.idle",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scgextra.cog.cog_trenchbleeder.idle")));
    public static final RegistryObject<SoundEvent> COG_TRENCHBLEEDER_STEP = SOUND_EVENTS.register("scgextra.cog.cog_trenchbleeder.step",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scgextra.cog.cog_trenchbleeder.step")));
    public static final RegistryObject<SoundEvent> COG_TRENCHBLEEDER_STOMP = SOUND_EVENTS.register("scgextra.cog.cog_trenchbleeder.stomp",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scgextra.cog.cog_trenchbleeder.stomp")));
    public static final RegistryObject<SoundEvent> COG_TRENCHBLEEDER_HURT= SOUND_EVENTS.register("scgextra.cog.cog_trenchbleeder.hurt",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scgextra.cog.cog_trenchbleeder.hurt")));
    public static final RegistryObject<SoundEvent> COG_TRENCHBLEEDER_DEATH = SOUND_EVENTS.register("scgextra.cog.cog_trenchbleeder.death",
            () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource("scgextra.cog.cog_trenchbleeder.death")));

    public static void init() {}

}
