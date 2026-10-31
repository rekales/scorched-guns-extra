package net.zincstudios.scgextra.item;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import top.ribs.scguns.init.ModEffects;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiUseHealingItem extends Item {

    int healingAmount;
    int cooldown;
    List<MobEffectInstance> potionEffects;
    Player wasInteractingWith = null;

    public MultiUseHealingItem(Item.Properties properties, int healingAmount, int cooldown, MobEffectInstance... potionEffects) {
        super(properties);
        this.potionEffects = Arrays.stream(potionEffects).filter(Objects::nonNull).toList();
        this.healingAmount = healingAmount;
        this.cooldown = cooldown;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(world, player, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
            InteractionHand usedHand) {
        if(!player.getCooldowns().isOnCooldown(this)){
            if(interactionTarget instanceof Player p){
                this.wasInteractingWith = p;
                return use(player.level(), player, usedHand).getResult();
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (entityLiving instanceof Player p) {
            Player player;
            if (!world.isClientSide) {
                if(this.wasInteractingWith!=null){
                    player = this.wasInteractingWith;
                }else{
                    player = p;
                }
                p.getCooldowns().addCooldown(this, this.cooldown);
                player.heal((float)this.healingAmount);
                if (player.hasEffect(ModEffects.LACERATED.get())) {
                    player.removeEffect(ModEffects.LACERATED.get());
                }

                for(MobEffectInstance effect : this.potionEffects) {
                    if (effect != null) {
                        player.addEffect(new MobEffectInstance(effect));
                    }
                }

                if (!player.getAbilities().instabuild) {
                    player.getCooldowns().addCooldown(stack.getItem(), this.cooldown);
                    stack.hurtAndBreak(1, player, entity ->
                            entity.broadcastBreakEvent(player.getUsedItemHand()));
                }
                this.wasInteractingWith = null;
            }
        }

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.scguns.healing_bandage.heal", this.healingAmount).withStyle(ChatFormatting.GREEN));
        if (!this.potionEffects.isEmpty()) {
            for(MobEffectInstance effect : this.potionEffects) {
                if (effect != null) {
                    effect.getEffect();
                    Component effectName = Component.translatable(effect.getEffect().getDescriptionId()).withStyle(ChatFormatting.BLUE);
                    int durationInSeconds = effect.getDuration() / 20;
                    int minutes = durationInSeconds / 60;
                    int seconds = durationInSeconds % 60;
                    String formattedDuration = String.format(" (%02d:%02d)", minutes, seconds);
                    Component effectDuration = Component.literal(formattedDuration).withStyle(ChatFormatting.BLUE);
                    tooltip.add(Component.empty().append(effectName).append(effectDuration));
                }
            }
        }

        super.appendHoverText(stack, world, tooltip, flag);
    }
}
