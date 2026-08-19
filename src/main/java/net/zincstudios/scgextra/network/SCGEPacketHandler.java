package net.zincstudios.scgextra.network;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.framework.api.network.MessageDirection;
import com.mrcrayfish.framework.network.message.IMessage;
import net.zincstudios.scgextra.SCGExtra;

import java.util.function.Supplier;

public class SCGEPacketHandler {

    private static FrameworkNetwork playChannel;

    public static void init() {
        playChannel = FrameworkAPI.createNetworkBuilder(SCGExtra.asResource("play"), 1)
                .registerPlayMessage(GunFlashMessage.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(HoldAttackMessage.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(TurretTriggerMessage.class, MessageDirection.PLAY_SERVER_BOUND)
                .build();
    }

    public static FrameworkNetwork getPlayChannel() {
        return playChannel;
    }

    // For convenience and to minimize code footprint
    public static void sendToNearbyPlayers(Supplier<LevelLocation> levelLocation, IMessage<?> message) {
        playChannel.sendToNearbyPlayers(levelLocation, message);
    }

    public static void sendToServer(IMessage<?> message) {
        playChannel.sendToServer(message);
    }
}
