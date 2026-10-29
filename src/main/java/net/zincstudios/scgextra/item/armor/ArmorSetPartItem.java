package net.zincstudios.scgextra.item.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArmorSetPartItem extends ArmorItem implements ArmorSetPart {

    private final ArmorSet armorSet;

    public ArmorSetPartItem(ArmorMaterial material, Type type, ArmorSet armorSet) {
        this(material, type, armorSet, new Properties());
    }

    public ArmorSetPartItem(ArmorMaterial material, Type type, ArmorSet armorSet, Properties properties) {
        super(material, type, properties);
        this.armorSet = armorSet;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        ResourceLocation loc = this.armorSet.getResourceLocation();

        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("armorset.full_set_bonus.description").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("armorset." + loc.getNamespace() + "." + loc.getPath() +".description").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("armorset.half_set_bonus.description").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("armorset." + loc.getNamespace() + "." + loc.getPath() +".half_set.description").withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.scguns.hold_shift").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    @Override
    public @NotNull ArmorSet getArmorSet() {
        return this.armorSet;
    }
}
