package net.zincstudios.scgextra.item;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ForgeMod;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.init.ModTags;

public class NetheriteChainAxeItem extends AxeItem implements HurtEffects{
    private final float attackDamageBaseline;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   protected static final UUID BLOCK_REACH_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C0F433DB5CF");

    public NetheriteChainAxeItem(Tier tier, float attackDamageModifier, float attackSpeedModifier,
            Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
        this.attackDamageBaseline = attackDamageModifier + tier.getAttackDamageBonus();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.attackDamageBaseline, Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)attackSpeedModifier, Operation.ADDITION));
        builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(BLOCK_REACH_UUID, "Tool modifier", (double)-2.0, Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        this.hurtEffect(stack, target, attacker);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void hurtEffect(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide() && !target.getType().is(ModTags.Entities.CANNOT_BE_LACERATED)) {
            target.addEffect(new MobEffectInstance(ModEffects.LACERATED.get(), 200));
        }
        if (!target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200));
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentSlot);
    }
    
}
