package net.zincstudios.scgextra.item.armor;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.zincstudios.scgextra.SCGExtra;
import net.zincstudios.scgextra.attributes.SCGEAttributes;
import top.ribs.scguns.init.ModEffects;

// Maybe use registries?
public final class ArmorSets {

    public static final ArmorSet OPPRESSOR = new ArmorSet(SCGExtra.asResource("oppressor"), new ArmorSet.Traits()
            .modifier(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier("oppressor_set_knockback_res", 1, AttributeModifier.Operation.ADDITION))
            .modifier(SCGEAttributes.UNDEAD_DAMAGE_MULT.get(), new AttributeModifier("oppressor_set_undead_damage", 0.1, AttributeModifier.Operation.ADDITION))
    );

    public static final ArmorSet COMMISSAR = new ArmorSet(SCGExtra.asResource("commissar"), new ArmorSet.Traits()
            .modifier(SCGEAttributes.BULLET_DAMAGE_TAKEN_MULT.get(), new AttributeModifier("commissar_set_bullet_damage_taken", -0.1, AttributeModifier.Operation.ADDITION))
    );

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
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity hurter)) return;

        if (ArmorSet.getArmorSet(hurter) == WRECKER) {
            entity.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 120));
        }
        
        if (ArmorSet.getArmorSet(hurter) == OPPRESSOR) {
            entity.hurt(event.getSource(), (float)(event.getAmount()*0.1));
        }

        if (ArmorSet.getArmorSet(entity) == WRECKER) {
            hurter.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 120));
        }
    }
}
