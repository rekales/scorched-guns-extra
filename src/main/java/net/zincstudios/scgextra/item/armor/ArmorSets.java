package net.zincstudios.scgextra.item.armor;

import net.zincstudios.scgextra.attributes.SCGEAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.zincstudios.scgextra.SCGExtra;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.init.ModEffects;

// Maybe use registries?
public final class ArmorSets {

    public static final ArmorSet OPPRESSOR = new ArmorSet(SCGExtra.asResource("oppressor"), new ArmorSet.Traits()
        .modifier(SCGEAttributes.UNDEAD_DAMAGE_MULT.get(), new AttributeModifier("oppressor_full_armor_undead_damage_mult", 0.1, AttributeModifier.Operation.ADDITION))
    );

    public static final ArmorSet COMMISSAR = new ArmorSet(SCGExtra.asResource("commissar"), new ArmorSet.Traits()
        .modifier(SCAttributes.RELOAD_SPEED.get(), new AttributeModifier("commissar_full_armor_reload_speed", 0.2, AttributeModifier.Operation.ADDITION))
    );

    public static final ArmorSet WRECKER = new ArmorSet(SCGExtra.asResource("wrecker"), new ArmorSet.Traits()
        .modifier(SCAttributes.ADDITIONAL_BULLET_DAMAGE.get(), new AttributeModifier("wrecker_full_armor_additional_bullet_damage", 0.2, AttributeModifier.Operation.ADDITION))
        .modifier(SCGEAttributes.BULLET_DAMAGE_TAKEN_MULT.get(), new AttributeModifier("wrecker_full_armor_additional_bullet_damage_taken", 0.2, AttributeModifier.Operation.ADDITION))
    );

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

    public static final ArmorSet PIONEER = new ArmorSet(SCGExtra.asResource("pioneer"), new ArmorSet.Traits());

    public static void onEntityHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity.getAttribute(SCGEAttributes.CAN_REGEN.get())!=null && entity.getAttribute(SCGEAttributes.CAN_REGEN.get()).getValue()>=1.0 && entity.level().getGameTime()>entity.getPersistentData().getLong("RegenCooldownEnd")){
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60));
            CompoundTag tag = entity.getPersistentData();
            tag.putLong("RegenCooldownEnd", entity.level().getGameTime() + 80);
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
