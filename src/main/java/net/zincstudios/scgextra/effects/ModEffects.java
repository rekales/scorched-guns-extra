package net.zincstudios.scgextra.effects;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.zincstudios.scgextra.SCGExtra;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SCGExtra.MOD_ID);

    public static final RegistryObject<InkEffect> INK_EFFECT = MOB_EFFECTS.register("ink_effect", () -> new InkEffect(MobEffectCategory.HARMFUL, 0xFF0000));
    public static final RegistryObject<GlowingInkEffect> GLOWING_INK_EFFECT = MOB_EFFECTS.register("glowing_ink_effect", () -> new GlowingInkEffect(MobEffectCategory.HARMFUL, 0xFF0000));
    
    public static final RegistryObject<StunnedEffect> STUNNED_EFFECT = MOB_EFFECTS.register("stunned", () -> new StunnedEffect(MobEffectCategory.HARMFUL, 0x000000));

    public static final RegistryObject<MobEffect> MORTAL = MOB_EFFECTS.register("mortal", () -> new MortalMobEffect(MobEffectCategory.NEUTRAL, 0x2C366D));

    public static final RegistryObject<MobEffect> ENLIGHTENMENT = MOB_EFFECTS.register("enlightenment", () -> new EnlightenmentHealthBoostMobEffect(MobEffectCategory.BENEFICIAL, 0x000000)
            .addAttributeModifier(Attributes.MAX_HEALTH, EnlightenmentHealthBoostMobEffect.EFFECT_ID, 8.0D, AttributeModifier.Operation.ADDITION));

    public static void register(IEventBus eventBus){
        MOB_EFFECTS.register(eventBus);
    }
}
