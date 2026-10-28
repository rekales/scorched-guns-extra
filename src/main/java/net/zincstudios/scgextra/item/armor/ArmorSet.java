package net.zincstudios.scgextra.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.zincstudios.scgextra.attributes.SCGEAttributes;
import top.ribs.scguns.init.ModEffects;

import javax.annotation.Nullable;
import java.util.*;

public class ArmorSet {

    public static final int TICK_INTERVAL = 10;
    private static final Map<UUID, Attribute> ALL_MODIFIER_ATTRIBUTES = new HashMap<>();

    private final ResourceLocation resLoc;
    private final Map<Attribute, AttributeModifier> modifiers;
    private final Map<MobEffect, Integer> effects;
    private final Set<MobEffect> resistances;

    public ArmorSet(ResourceLocation resLoc, ArmorSet.Traits traits) {
        this.resLoc = resLoc;
        this.modifiers = Map.copyOf(traits.modifiers);
        this.effects = Map.copyOf(traits.effects);
        this.resistances = Set.copyOf(traits.resistances);
        for (Map.Entry<Attribute, AttributeModifier> e : traits.modifiers.entrySet()) {
            ALL_MODIFIER_ATTRIBUTES.put(e.getValue().getId(), e.getKey());
        }
    }

    // Only ticks every interval and on server side
    public void onTick(LivingEntity entity) {
        Map<UUID, AttributeModifier> desiredModifiers = new HashMap<>();

        ArmorSet armorSet = getArmorSet(entity);
        if (armorSet != null) {
            for (Map.Entry<Attribute, AttributeModifier> entry : armorSet.modifiers.entrySet()) {
                desiredModifiers.put(entry.getValue().getId(), entry.getValue());
            }
            for (Map.Entry<MobEffect, Integer> entry : armorSet.effects.entrySet()) {
                entity.addEffect(new MobEffectInstance(entry.getKey(), 115, entry.getValue(), true, true));
            }
            for (MobEffect effect : armorSet.resistances) {
                entity.removeEffect(effect);
            }
        }

        for (Map.Entry<UUID, Attribute> entry : ALL_MODIFIER_ATTRIBUTES.entrySet()) {
            UUID uuid = entry.getKey();
            AttributeInstance attr = entity.getAttribute(entry.getValue());
            if (attr == null) continue;

            boolean shouldHave = desiredModifiers.containsKey(uuid);
            boolean hasModifier = attr.getModifier(uuid) != null;

            if (shouldHave && !hasModifier) {
                attr.addTransientModifier(desiredModifiers.get(uuid));
            } else if (!shouldHave && hasModifier) {
                attr.removeModifier(uuid);
            }
        }
    }

    public ResourceLocation getResourceLocation() {
        return this.resLoc;
    }

    @Nullable
    public static ArmorSet getArmorSet(LivingEntity entity) {
        if (!(entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArmorSetPart headPart)) return null;
        if (!(entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorSetPart chestPart)) return null;
        if (!(entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmorSetPart legPart)) return null;
        if (!(entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ArmorSetPart feetPart)) return null;

        ArmorSet set = headPart.getArmorSet();
        if (set == chestPart.getArmorSet() && set == legPart.getArmorSet() && set == feetPart.getArmorSet()) return set;
        return null;
    }

    // TODO: no interval check, maybe cache result.
    // change to LivingEntity tick if needed
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        Player player = event.player;

        if(player.hasEffect(MobEffects.LEVITATION) && player.getAttribute(SCGEAttributes.PIONEER_FLY.get())!=null && player.getAttribute(SCGEAttributes.PIONEER_FLY.get()).getValue()>=1.0){
            player.removeEffect(MobEffects.LEVITATION);
        }

        if(player.hasEffect(ModEffects.LACERATED.get()) && player.getAttribute(SCGEAttributes.REDUCE_LACERATION.get())!=null && player.getAttribute(SCGEAttributes.REDUCE_LACERATION.get()).getValue()>=1.0){
            player.removeEffect(ModEffects.LACERATED.get());
        }

        if (event.player.tickCount % TICK_INTERVAL != 0) return;

        if(player.getAttribute(SCGEAttributes.BUFF_ZONE.get())!=null && player.getAttribute(SCGEAttributes.BUFF_ZONE.get()).getValue()!=0.0){
            double area = player.getAttribute(SCGEAttributes.BUFF_ZONE.get()).getValue();
            List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(area),  e->!(e instanceof Enemy));
            for(LivingEntity le : list){
                le.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100));
                le.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100));
            }
        }
        if(player.getAttribute(SCGEAttributes.DEBUFF_ZONE.get())!=null && player.getAttribute(SCGEAttributes.DEBUFF_ZONE.get()).getValue()!=0.0){
            double area = player.getAttribute(SCGEAttributes.DEBUFF_ZONE.get()).getValue();
            List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(area), e->e instanceof Enemy);
            for(LivingEntity le : list){
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            }
        }

        ArmorSet armorSet = getArmorSet(player);
        if (armorSet == null) return;

        armorSet.onTick(player);
    }

    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ArmorSet armorSet = getArmorSet(player);
        if (armorSet == null) return;

        if (armorSet.resistances.contains(event.getEffectInstance().getEffect())) {
            event.setResult(Event.Result.DENY);
        }
    }

    // TODO: Consider combining with MedalItem.Traits to a separate class along with handling
    public static class Traits {
        private final Map<Attribute, AttributeModifier> modifiers = new HashMap<>();
        private final Map<MobEffect, Integer> effects = new HashMap<>();
        private final Set<MobEffect> resistances = new HashSet<>();

        public ArmorSet.Traits modifier(Attribute attribute, AttributeModifier modifier) {
            this.modifiers.put(attribute, modifier);
            return this;
        }

        public ArmorSet.Traits effect(MobEffect effect, int amplifier) {
            this.effects.put(effect, amplifier);
            return this;
        }

        public ArmorSet.Traits resistance(MobEffect effect) {
            this.resistances.add(effect);
            return this;
        }
    }
}
