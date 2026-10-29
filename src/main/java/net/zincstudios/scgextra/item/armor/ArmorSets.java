package net.zincstudios.scgextra.item.armor;

import net.zincstudios.scgextra.attributes.SCGEAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.zincstudios.scgextra.SCGExtra;
import top.ribs.scguns.init.ModEffects;

// Maybe use registries?
public final class ArmorSets {

    //REMINDER TO SELF: DON'T USE MODIFERS FROM HERE, THEY STAY, USE HANDLER
    public static final ArmorSet OPPRESSOR = new ArmorSet(SCGExtra.asResource("oppressor"), new ArmorSet.Traits());

    public static final ArmorSet COMMISSAR = new ArmorSet(SCGExtra.asResource("commissar"), new ArmorSet.Traits());

    public static final ArmorSet WRECKER = new ArmorSet(SCGExtra.asResource("wrecker"), new ArmorSet.Traits());

    public static final ArmorSet LEVIATHAN = new ArmorSet(SCGExtra.asResource("leviathan"), new ArmorSet.Traits()
            .effect(MobEffects.WATER_BREATHING, 0)
            .effect(MobEffects.DOLPHINS_GRACE, 0)
    );

    public static final ArmorSet GOLDEN_IDOL = new GoldenIdolArmorSet(SCGExtra.asResource("golden_idol"), new ArmorSet.Traits()
            .effect(MobEffects.FIRE_RESISTANCE, 0)
    );

    public static final ArmorSet ENLIGHTENED = new ArmorSet(SCGExtra.asResource("enlightened"), new ArmorSet.Traits());

    public static final ArmorSet JUGGERNAUT = new ArmorSet(SCGExtra.asResource("juggernaut"), new ArmorSet.Traits());

    public static final ArmorSet RITUAL = new RitualArmorSet(SCGExtra.asResource("ritual"), new ArmorSet.Traits());

    public static final ArmorSet PIONEER = new ArmorSet(SCGExtra.asResource("pioneer"), new ArmorSet.Traits());

    public static void onEntityHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.getAttribute(SCGEAttributes.LEVIATHAN.get())!=null && entity.getAttribute(SCGEAttributes.LEVIATHAN.get()).getValue()>=1.0 && entity.level().getGameTime()>entity.getPersistentData().getLong("RegenCooldownEnd")){
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60));
            CompoundTag tag = entity.getPersistentData();
            tag.putLong("RegenCooldownEnd", entity.level().getGameTime() + 80);
        }
        if(entity.getAttribute(SCGEAttributes.RITUAL.get())!=null && entity.getAttribute(SCGEAttributes.LEVIATHAN.get()).getValue()>=1.0 && entity.level().getGameTime()>entity.getPersistentData().getLong("RegenCooldownEnd")){
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60));
            CompoundTag tag = entity.getPersistentData();
            tag.putLong("RitualRegenCooldownEnd", entity.level().getGameTime() + 80);
        }
        if(entity.getAttribute(SCGEAttributes.ENLIGHTENED.get())!=null && entity.getAttribute(SCGEAttributes.LEVIATHAN.get()).getValue()>=2.0 && entity.level().getGameTime()>entity.getPersistentData().getLong("RegenCooldownEnd")){
            entity.addEffect(new MobEffectInstance(MobEffects.HEAL, 10));
            CompoundTag tag = entity.getPersistentData();
            tag.putLong("EnlightenedRegenCooldownEnd", entity.level().getGameTime() + 60);
        }
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity hurter)) return;

        if(hurter.getAttribute(SCGEAttributes.WRECEKR.get())!=null && hurter.getAttributeValue(SCGEAttributes.WRECEKR.get())>=1.0){
            entity.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 120));
        }
        if(entity.getAttribute(SCGEAttributes.WRECEKR.get())!=null && entity.getAttributeValue(SCGEAttributes.WRECEKR.get())>=1.0){
            hurter.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 120));
        }
    }
}
