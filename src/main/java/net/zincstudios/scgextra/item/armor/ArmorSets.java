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

    public static final ArmorSet ENLIGHTENED = new ArmorSet(SCGExtra.asResource("enlightened"), new ArmorSet.Traits()
            .effect(MobEffects.REGENERATION, 0)
    );

    public static final ArmorSet JUGGERNAUT = new ArmorSet(SCGExtra.asResource("juggernaut"), new ArmorSet.Traits()
            .resistance(ModEffects.LACERATED.get())
    );

    public static final ArmorSet RITUAL = new RitualArmorSet(SCGExtra.asResource("ritual"), new ArmorSet.Traits());

    public static final ArmorSet PIONEER = new ArmorSet(SCGExtra.asResource("pioneer"), new ArmorSet.Traits()
            .resistance(MobEffects.LEVITATION)
    );

    public static void onEntityHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.getAttribute(SCGEAttributes.CAN_REGEN.get())!=null && entity.getAttribute(SCGEAttributes.CAN_REGEN.get()).getValue()>=1.0 && entity.level().getGameTime()>entity.getPersistentData().getLong("RegenCooldownEnd")){
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60));
            CompoundTag tag = entity.getPersistentData();
            tag.putLong("RegenCooldownEnd", entity.level().getGameTime() + 80);
        }
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity hurter)) return;

        if (ArmorSet.getArmorSet(hurter) == WRECKER) {
            entity.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 120));
        }
        
        //note: pretty sure something's wrong here, I forgot what I added this for
        if (ArmorSet.getArmorSet(hurter) == OPPRESSOR) {
            entity.hurt(event.getSource(), (float)(event.getAmount()*0.1));
        }

        if (ArmorSet.getArmorSet(entity) == WRECKER) {
            hurter.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 120));
        }
    }
}
