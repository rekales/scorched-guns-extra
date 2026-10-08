package net.zincstudios.scgextra.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.*;

public class MedalItem extends Item {

    private static final int TICK_INTERVAL = 10;
    private static final Map<UUID, Attribute> ALL_MODIFIER_ATTRIBUTES = new HashMap<>();

    private final Map<Attribute, AttributeModifier> modifiers;
    private final Map<MobEffect, Integer> effects;
    private final Set<MobEffect> resistances;

    public MedalItem(MedalItem.Traits traits) {
        this(new Item.Properties().stacksTo(1), traits);
    }

    public MedalItem(Item.Properties properties, MedalItem.Traits traits) {
        super(properties);
        this.modifiers = Map.copyOf(traits.modifiers);
        this.effects = Map.copyOf(traits.effects);
        this.resistances = Set.copyOf(traits.resistances);
        for (Map.Entry<Attribute, AttributeModifier> e : traits.modifiers.entrySet()) {
            ALL_MODIFIER_ATTRIBUTES.put(e.getValue().getId(), e.getKey());
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        ResourceLocation loc = ForgeRegistries.ITEMS.getKey(this);
        if (loc == null) return;

        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item." + loc.getNamespace() + "." + loc.getPath() + ".description").withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.scguns.hold_shift").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        Player player = event.player;

        if (event.player.tickCount % TICK_INTERVAL != 0) return;

        Map<UUID, AttributeModifier> desiredModifiers = new HashMap<>();

        List<ItemStack> stacks = new ArrayList<>();

        CuriosApi.getCuriosInventory(player).ifPresent(itemHandler -> {
            IDynamicStackHandler charms = itemHandler.getCurios().get("charm").getStacks();
            for (int i = 0; i < charms.getSlots(); i++) {
                ItemStack stack = charms.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
        });
        
        for (ItemStack stack : stacks) {
            if (!(stack.getItem() instanceof MedalItem medalItem)) continue;

            for (Map.Entry<Attribute, AttributeModifier> entry : medalItem.modifiers.entrySet()) {
                desiredModifiers.put(entry.getValue().getId(), entry.getValue());
            }
            for (Map.Entry<MobEffect, Integer> entry : medalItem.effects.entrySet()) {
                player.addEffect(new MobEffectInstance(entry.getKey(), 115, entry.getValue(), true, true));
            }
            for (MobEffect effect : medalItem.resistances) {
                player.removeEffect(effect);
            }
        }

        for (Map.Entry<UUID, Attribute> entry : ALL_MODIFIER_ATTRIBUTES.entrySet()) {
            UUID uuid = entry.getKey();
            AttributeInstance attr = player.getAttribute(entry.getValue());
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

    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) return;

        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof MedalItem medalItem)) continue;
            if (medalItem.resistances.contains(event.getEffectInstance().getEffect())) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    // for cleaner registration
    public static class Traits {
        private final Map<Attribute, AttributeModifier> modifiers = new HashMap<>();
        private final Map<MobEffect, Integer> effects = new HashMap<>();
        private final Set<MobEffect> resistances = new HashSet<>();


        public MedalItem.Traits modifier(Attribute attribute, AttributeModifier modifier) {
            this.modifiers.put(attribute, modifier);
            return this;
        }

        public MedalItem.Traits effect(MobEffect effect, int amplifier) {
            this.effects.put(effect, amplifier);
            return this;
        }

        public MedalItem.Traits resistance(MobEffect effect) {
            this.resistances.add(effect);
            return this;
        }
    }
}
