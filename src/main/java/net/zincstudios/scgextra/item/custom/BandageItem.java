package net.zincstudios.scgextra.item.custom;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zincstudios.scgextra.item.ModItems;
import top.ribs.scguns.init.ModEffects;
import top.ribs.scguns.item.HealingBandageItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

// TODO: Fix the deprecation info
@SuppressWarnings("deprecation")
public class BandageItem extends HealingBandageItem{

    private Player willBeAffected = null;
    private boolean hasMedipack = false;
    int normalHealingAmount;
    int medipackHealingAmount;
    List<MobEffectInstance> normalPotionEffects;
    List<MobEffectInstance> medipackPotionEffects;

    public BandageItem(Properties properties, int healingAmount, int medipackHealingAmount, List<MobEffectInstance> medipackPotionEffects, MobEffectInstance... potionEffects) {
        super(properties, healingAmount, potionEffects);
        this.normalHealingAmount = healingAmount;
        this.medipackHealingAmount = medipackHealingAmount;
        this.normalPotionEffects = Arrays.stream(potionEffects).filter(Objects::nonNull).toList();
        this.medipackPotionEffects = medipackPotionEffects;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
            InteractionHand usedHand) {
        if(!player.level().isClientSide){
            if(!hasMedipack) return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
            if(interactionTarget instanceof Player target){
                player.startUsingItem(usedHand);
                this.willBeAffected = target;
                return InteractionResultHolder.consume(player.getItemInHand(usedHand)).getResult();
            }
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if(!level.isClientSide()){
            if(entity instanceof Player pl){
                this.hasMedipack = false;
                CuriosApi.getCuriosHelper().findCurios(pl, "back").forEach(res->{
                    if(res.stack().is(ModItems.MEDIPACK.get()) && !pl.getCooldowns().isOnCooldown(res.stack().getItem()))this.hasMedipack = true;
                });
                CuriosApi.getCuriosHelper().findCurios(pl, "body").forEach(res->{
                    if(res.stack().is(ModItems.MEDIPACK.get()) && !pl.getCooldowns().isOnCooldown(res.stack().getItem()))this.hasMedipack = true;
                });
            }else{
                this.hasMedipack = false;
            }
            if(!this.hasMedipack){
                this.willBeAffected = null;
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    public @NotNull ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (entityLiving instanceof Player player) {
            if (!world.isClientSide) {
                if(this.hasMedipack && this.willBeAffected !=null){
                    this.willBeAffected.heal((float)this.medipackHealingAmount);
                }else if(this.hasMedipack){
                    player.heal((float)this.medipackHealingAmount);
                }else{
                    player.heal((float)this.normalHealingAmount);
                }
                if(this.hasMedipack && this.willBeAffected !=null){
                    if (this.willBeAffected.hasEffect((MobEffect)ModEffects.LACERATED.get())) {
                        this.willBeAffected.removeEffect((MobEffect)ModEffects.LACERATED.get());
                    }
                }else{
                    if (player.hasEffect((MobEffect)ModEffects.LACERATED.get())) {
                        player.removeEffect((MobEffect)ModEffects.LACERATED.get());
                    }
                }

                if(this.hasMedipack && this.willBeAffected !=null){
                    for(MobEffectInstance effect : this.medipackPotionEffects) {
                        if (effect != null) {
                            this.willBeAffected.addEffect(new MobEffectInstance(effect));
                        }
                    }
                }else if(this.hasMedipack){
                    for(MobEffectInstance effect : this.medipackPotionEffects) {
                        if (effect != null) {
                            player.addEffect(new MobEffectInstance(effect));
                        }
                    }
                }else{
                    for(MobEffectInstance effect : this.normalPotionEffects) {
                        if (effect != null) {
                            player.addEffect(new MobEffectInstance(effect));
                        }
                    }
                }

                boolean hurt = false;
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    if(this.hasMedipack){
                        for(SlotResult res : CuriosApi.getCuriosHelper().findCurios(player, "back")){
                            if(res.stack().is(ModItems.MEDIPACK.get()) && !hurt){
                                res.stack().hurtAndBreak(1, player, p ->p.playSound(SoundEvents.GLASS_BREAK));
                                hurt = true;
                                player.getCooldowns().addCooldown(res.stack().getItem(), 140);
                            }
                        }
                        for(SlotResult res : CuriosApi.getCuriosHelper().findCurios(player, "body")){
                            if(res.stack().is(ModItems.MEDIPACK.get()) && !hurt){
                                res.stack().hurtAndBreak(1, player, p ->p.playSound(SoundEvents.GLASS_BREAK));
                                hurt = true;
                                player.getCooldowns().addCooldown(res.stack().getItem(), 140);
                            }
                        }
                    }
                }
                this.willBeAffected = null;
            }
        }

        return stack;
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if(!level.isClientSide){
            this.willBeAffected = null;
        }
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }
}
