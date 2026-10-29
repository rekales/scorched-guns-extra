package net.zincstudios.scgextra.item.armor;

import net.zincstudios.scgextra.attributes.SCGEAttributes;

import java.util.List;
import java.util.Map;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import top.ribs.scguns.attributes.SCAttributes;
import top.ribs.scguns.init.ModEffects;

public class ArmorExtraModiferHandler {
    private static final Map<Attribute, Map<Attribute, AttributeModifier>> FULL_SET_BONUSES = Map.of(
        SCGEAttributes.OPPRESSOR.get(), Map.of(SCGEAttributes.UNDEAD_DAMAGE_MULT.get(), new AttributeModifier("oppressor_full_armor_add_undead_damage_mult", 0.1, AttributeModifier.Operation.ADDITION)),
        SCGEAttributes.COMMISSAR.get(), Map.of(SCAttributes.RELOAD_SPEED.get(), new AttributeModifier("commissar_full_armor_reload_speed", 0.2, AttributeModifier.Operation.ADDITION)),
        SCGEAttributes.WRECEKR.get(), Map.of(
            SCAttributes.ADDITIONAL_BULLET_DAMAGE.get(), new AttributeModifier("wrecker_full_armor_additional_bullet_damage", 0.2, AttributeModifier.Operation.ADDITION),
            SCGEAttributes.BULLET_DAMAGE_TAKEN_MULT.get(), new AttributeModifier("wrecker_full_armor_additional_bullet_damage_taken", 0.2, AttributeModifier.Operation.ADDITION)
        ),
        SCGEAttributes.GOLDEN_IDOL.get(), Map.of(SCGEAttributes.DEBUFF_ZONE.get(), new AttributeModifier("golden_idol_full_armor_add_debuff_zone", 1.0, AttributeModifier.Operation.ADDITION)),
        SCGEAttributes.JUGGERNAUT.get(), Map.of(
            SCGEAttributes.RECOIL_MULT.get(), new AttributeModifier("juggernaut_full_armor_reduce_recoil", -0.1, AttributeModifier.Operation.ADDITION),
            Attributes.MOVEMENT_SPEED, new AttributeModifier("juggernaut_full_armor_reduce_speed", -0.1, AttributeModifier.Operation.MULTIPLY_BASE)
        ),
        SCGEAttributes.PIONEER.get(), 
        Map.of(
            SCGEAttributes.BULLET_ADDITIONAL_CRIT_CHANCE.get(), new AttributeModifier("pioneer_full_armor_additional_crit_chance_mult", 0.1, AttributeModifier.Operation.ADDITION),
            SCGEAttributes.BULLET_ADDITIONAL_CRIT_DAMAGE_MULT.get(), new AttributeModifier("pioneer_full_armor_additional_crit_damage_mult", 0.1, AttributeModifier.Operation.ADDITION)
        )
    );
    private static final Map<Attribute, Map<Attribute, AttributeModifier>> HALF_SET_BONUSES = Map.of(
        SCGEAttributes.OPPRESSOR.get(), Map.of(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier("oppressor_half_armor_add_knockback_resistance", 1, AttributeModifier.Operation.ADDITION)),
        SCGEAttributes.COMMISSAR.get(), Map.of(SCGEAttributes.BULLET_DAMAGE_TAKEN_MULT.get(), new AttributeModifier("comissar_half_armor_reduce_bullet_damage", 0.1, AttributeModifier.Operation.ADDITION))
    );

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        Player player = event.player;

        if(player.hasEffect(MobEffects.LEVITATION) && player.getAttribute(SCGEAttributes.PIONEER.get())!=null && player.getAttribute(SCGEAttributes.PIONEER.get()).getValue()>=1.0){
            player.removeEffect(MobEffects.LEVITATION);
        }

        if(player.hasEffect(ModEffects.LACERATED.get()) && player.getAttribute(SCGEAttributes.JUGGERNAUT.get())!=null && player.getAttribute(SCGEAttributes.JUGGERNAUT.get()).getValue()>=1.0){
            player.removeEffect(ModEffects.LACERATED.get());
        }

        if(player.isOnFire() && player.getAttribute(SCGEAttributes.GOLDEN_IDOL.get())!=null && player.getAttribute(SCGEAttributes.GOLDEN_IDOL.get()).getValue()>=1.0){
            player.setRemainingFireTicks(0);
        }

        if (event.player.tickCount % 10 != 0) return;

        if(player.getAttributeValue(SCGEAttributes.ENLIGHTENED.get())>=1.0){
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 0, 40));
        }

        for(Attribute main : FULL_SET_BONUSES.keySet()){
            if(player.getAttributeValue(main)>=2){
                for(Attribute key : FULL_SET_BONUSES.get(main).keySet()){
                    if(!player.getAttribute(key).hasModifier(FULL_SET_BONUSES.get(main).get(key))){
                        player.getAttribute(key).addTransientModifier(FULL_SET_BONUSES.get(main).get(key));
                    }
                }
            }else{
                for(Attribute key : FULL_SET_BONUSES.get(main).keySet()){
                    if(player.getAttribute(key).hasModifier(FULL_SET_BONUSES.get(main).get(key))){
                        player.getAttribute(key).removeModifier(FULL_SET_BONUSES.get(main).get(key));
                    }
                }
            }
        }
        for(Attribute main : HALF_SET_BONUSES.keySet()){
            if(player.getAttributeValue(main)>=1){
                for(Attribute key : HALF_SET_BONUSES.get(main).keySet()){
                    if(!player.getAttribute(key).hasModifier(HALF_SET_BONUSES.get(main).get(key))){
                        player.getAttribute(key).addTransientModifier(HALF_SET_BONUSES.get(main).get(key));
                    }
                }
            }else{
                for(Attribute key : HALF_SET_BONUSES.get(main).keySet()){
                    if(player.getAttribute(key).hasModifier(HALF_SET_BONUSES.get(main).get(key))){
                        player.getAttribute(key).removeModifier(HALF_SET_BONUSES.get(main).get(key));
                    }
                }
            }
        }

        if(player.getAttribute(SCGEAttributes.GOLDEN_IDOL.get())!=null && player.getAttribute(SCGEAttributes.GOLDEN_IDOL.get()).getValue()>=1.0){
            List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5),  e->!(e instanceof Enemy));
            for(LivingEntity le : list){
                le.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100));
                le.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100));
            }
        }
        if(player.getAttribute(SCGEAttributes.DEBUFF_ZONE.get())!=null && player.getAttribute(SCGEAttributes.DEBUFF_ZONE.get()).getValue()>=1.0){
            List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10), e->e instanceof Enemy);
            for(LivingEntity le : list){
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            }
        }
    }
}
