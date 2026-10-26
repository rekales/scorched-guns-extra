package net.zincstudios.scgextra.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.zincstudios.scgextra.attributes.SCGEAttributes;
import net.zincstudios.scgextra.item.armor.ArmorSet;
import net.zincstudios.scgextra.item.armor.RitualArmorSet;
import org.spongepowered.asm.mixin.Mixin;
import top.ribs.scguns.init.ModDamageTypes;

@Mixin(value = LivingEntity.class)
public class LivingEntityMixin {

    @SuppressWarnings("ConstantValue")
    @WrapMethod(method = "getDamageAfterMagicAbsorb")
    private float atGetDamageAfterMagicAbsorb(DamageSource damageSource, float damageAmount, Operation<Float> original) {
        if(damageSource.is(DamageTypes.MAGIC) && (Object)this instanceof LivingEntity le){
            AttributeInstance ai = le.getAttribute(SCGEAttributes.MAGIC_DAMAGE_REDUCTION.get());
            if(ai!=null){
                damageAmount = damageAmount*(float)ai.getValue();   
            }
        }
        if (damageSource.is(ModDamageTypes.BULLET)
                && !damageSource.is(DamageTypeTags.BYPASSES_EFFECTS)
                && (Object)this instanceof Player player) {
            float damageMult = (float) player.getAttributeValue(SCGEAttributes.BULLET_DAMAGE_TAKEN_MULT.get());
            return original.call(damageSource, damageAmount * damageMult);
        }

        if(damageSource.getDirectEntity() instanceof Player p && (Object)this instanceof Monster){
            float damageMult = (float) p.getAttributeValue(SCGEAttributes.UNDEAD_DAMAGE_MULT.get());
            return original.call(damageSource, damageAmount * damageMult);
        }

        return original.call(damageSource, damageAmount);
    }

    @WrapMethod(method = "checkTotemDeathProtection")
    private boolean atCheckTotemDeathProtection(DamageSource damageSource, Operation<Boolean> original) {
        LivingEntity self = (LivingEntity) (Object) this;
        Level level = self.level();

        if (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (ArmorSet.getArmorSet(self) instanceof RitualArmorSet) {
                int cooldownTicks = (int) (RitualArmorSet.getReviveCooldownEnd(self) - level.getGameTime());
                if (cooldownTicks <= 0) {
                    self.setHealth(1.0F);
                    self.removeAllEffects();
                    self.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                    self.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                    self.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
                    level.broadcastEntityEvent(self, (byte)35);

                    RitualArmorSet.setReviveCooldown(self, RitualArmorSet.COOLDOWN_DURATION);
                    return true;
                }
            }
        }

        return original.call(damageSource);
    }
}
