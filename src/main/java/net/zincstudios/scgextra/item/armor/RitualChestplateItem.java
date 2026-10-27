package net.zincstudios.scgextra.item.armor;

import java.util.Map;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RitualChestplateItem extends ArmorSetPartWithExtrasItem {

    public RitualChestplateItem(ArmorMaterial material, Type type, ArmorSet armorSet, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet, modifiers);
    }

    public RitualChestplateItem(ArmorMaterial material, Type type, ArmorSet armorSet, Properties properties, Map<Attribute, AttributeModifier> modifiers) {
        super(material, type, armorSet, properties, modifiers);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
        if (!level.isClientSide || player.isDeadOrDying() || stack != player.getItemBySlot(EquipmentSlot.CHEST)) return;

        ClientLevel clientLevel = (ClientLevel) level;  // Dedicated Server doesn't like doing instanceof ClientLevel
        Vec3 pos;
        if (player.tickCount%12 == 0) {
            pos = new Vec3(0.4,1.75, 0);
        } else if (player.tickCount%12 == 6) {
            pos = new Vec3(-0.4,1.75, 0);
        } else {
            return;
        }

        if (player.isCrouching()) {
            pos = pos.subtract(0, 0.35, 0);
        }

        pos = pos.add(
                        (level.getRandom().nextDouble() - 0.5) * 0.15,
                        (level.getRandom().nextDouble() - 0.5) * 0.15,
                        (level.getRandom().nextDouble() - 0.5) * 0.15
                ).yRot(-player.yBodyRot * Mth.DEG_TO_RAD)
                .add(player.position());

        clientLevel.addParticle(
                ParticleTypes.SOUL_FIRE_FLAME,
                pos.x, pos.y, pos.z,
                0, (level.getRandom().nextDouble()) * 0.02, 0
        );

        clientLevel.addParticle(
                ParticleTypes.SMOKE,
                pos.x, pos.y, pos.z,
                (level.getRandom().nextDouble() - 0.5) * 0.02,
                0,
                (level.getRandom().nextDouble() - 0.5) * 0.02
        );
    }
}
