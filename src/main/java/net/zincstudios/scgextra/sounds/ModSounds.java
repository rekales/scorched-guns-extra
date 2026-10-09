package net.zincstudios.scgextra.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.zincstudios.scgextra.SCGExtra;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SCGExtra.MOD_ID);

    public static void register(IEventBus eventbus) {
        WhalerSounds.init();
        RRCSounds.init();
        FACSounds.init();
        WreckersSounds.init();
        AsgharianSounds.init();
        COGSounds.init();
        NeutralSounds.init();
        PiglinSounds.init();

        SOUND_EVENTS.register(eventbus);
    }
}
