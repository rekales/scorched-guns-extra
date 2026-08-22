package net.zincstudios.scgextra.entity.cog.juggernaut;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.item.ItemStack;
import top.ribs.scguns.init.ModItems;
import top.ribs.scguns.item.GunItem;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DynamicWeaponSwitching extends Behavior<CogJuggernautEntity> {

    private static final ImmutableList<GunItem> ARSENAL = ImmutableList.of(
            ModItems.GATTALER.get(),
            ModItems.THUNDERHEAD.get(),
            ModItems.SPITFIRE.get()
    );

    private long cooldownEnd = 0;

    public DynamicWeaponSwitching() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CogJuggernautEntity owner) {
        return this.cooldownEnd < level.getGameTime();
    }

    @Override
    protected void start(ServerLevel level, CogJuggernautEntity entity, long gameTime) {
        this.cooldownEnd = gameTime + 300;

        ItemStack gunStack = new ItemStack(ARSENAL.get(entity.getRandom().nextInt(ARSENAL.size())));
        entity.setItemInHand(InteractionHand.MAIN_HAND, gunStack);
    }
}
