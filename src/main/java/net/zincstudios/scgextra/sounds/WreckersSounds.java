package net.zincstudios.scgextra.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
import net.zincstudios.scgextra.SCGExtra;

import java.util.List;

import static net.zincstudios.scgextra.sounds.ModSounds.SOUND_EVENTS;

public class WreckersSounds {

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(SCGExtra.asResource(name)));
    }

    // GANG (shared by red / green / turret which the doc doesn't indicate the voice set)
    public static final RegistryObject<SoundEvent> GANG_IDLE = register("wreckers.idle");
    public static final RegistryObject<SoundEvent> GANG_HURT = register("wreckers.hurt");
    public static final RegistryObject<SoundEvent> GANG_ATTACK = register("wreckers.attack");
    public static final RegistryObject<SoundEvent> GANG_DEATH = register("wreckers.death");

    // BLUE
    public static final RegistryObject<SoundEvent> BLUE_IDLE = register("wrecker_blue.idle");
    public static final RegistryObject<SoundEvent> BLUE_HURT = register("wrecker_blue.hurt");
    public static final RegistryObject<SoundEvent> BLUE_ATTACK = register("wrecker_blue.attack");
    public static final RegistryObject<SoundEvent> BLUE_DEATH = register("wrecker_blue.death");

    // JUMBO
    public static final RegistryObject<SoundEvent> JUMBO_IDLE = register("wrecker_jumbo.idle");
    public static final RegistryObject<SoundEvent> JUMBO_HURT = register("wrecker_jumbo.hurt");
    public static final RegistryObject<SoundEvent> JUMBO_ATTACK = register("wrecker_jumbo.attack");
    public static final RegistryObject<SoundEvent> JUMBO_DEATH = register("wrecker_jumbo.death");
    public static final RegistryObject<SoundEvent> JUMBO_LINE = register("wrecker_jumbo.line");

    public static final RegistryObject<SoundEvent> TOOL_USE = register("wrecking_tool.use");

    // HELICUBE
    public static final RegistryObject<SoundEvent> HELICUBE_IDLE = register("wrecker_helicube.idle");
    public static final RegistryObject<SoundEvent> HELICUBE_HURT = register("wrecker_helicube.hurt");

    // DOZER
    public static final RegistryObject<SoundEvent> DOZER_IDLE = register("wrecker_dozer.idle");
    public static final RegistryObject<SoundEvent> DOZER_HURT = register("wrecker_dozer.hurt");
    public static final RegistryObject<SoundEvent> DOZER_CHARGE = register("wrecker_dozer.charge");
    public static final RegistryObject<SoundEvent> DOZER_MOVE = register("wrecker_dozer.move");
    public static final RegistryObject<SoundEvent> DOZER_STUN_DEATH = register("wrecker_dozer.stun_death");

    // BIKER
    public static final RegistryObject<SoundEvent> WRECKER_BIKER_IDLE = register("scgextra.wreckers.wrecker_biker.idle");
    public static final RegistryObject<SoundEvent> WRECKER_BIKER_IDLE_WHILE_CIRCLING = register("scgextra.wreckers.wrecker_biker.idle_while_circling");
    public static final RegistryObject<SoundEvent> WRECKER_BIKER_HURT = register("scgextra.wreckers.wrecker_biker.hurt");
    public static final RegistryObject<SoundEvent> WRECKER_BIKER_HIT = register("scgextra.wreckers.wrecker_biker.hit");
    public static final RegistryObject<SoundEvent> WRECKER_BIKER_DEATH = register("scgextra.wreckers.wrecker_biker.death");

    public static List<SoundEvent> gangVoiceLines() {
        return List.of(GANG_IDLE.get(),
                GANG_HURT.get(),
                GANG_ATTACK.get());
    }

    public static List<SoundEvent> blueVoiceLines() {
        return List.of(BLUE_IDLE.get(), BLUE_HURT.get(),
                BLUE_ATTACK.get());
    }

    public static List<SoundEvent> jumboVoiceLines() {
        return List.of(JUMBO_IDLE.get(), JUMBO_HURT.get(), JUMBO_ATTACK.get(), JUMBO_LINE.get());
    }

    public static void init() {}
}
