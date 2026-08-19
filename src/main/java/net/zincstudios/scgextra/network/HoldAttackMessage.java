package net.zincstudios.scgextra.network;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.zincstudios.scgextra.item.HoldAttackHandler;

public final class HoldAttackMessage extends PlayMessage<HoldAttackMessage> {

    private boolean held;

    public HoldAttackMessage(boolean held) {
        this.held = held;
    }

    @SuppressWarnings("unused")  // Empty constructor needed by framework for some reason
    public HoldAttackMessage() {
    }

    public void encode(HoldAttackMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.held);
    }

    public HoldAttackMessage decode(FriendlyByteBuf buf) {
        return new HoldAttackMessage(buf.readBoolean());
    }

    public void handle(HoldAttackMessage msg, MessageContext ctx) {
        ctx.execute(() -> {
            ServerPlayer player = ctx.getPlayer();
            if (player == null) return;
            HoldAttackHandler.setServerHeld(player, msg.held);
        });
        ctx.setHandled(true);
    }
}
