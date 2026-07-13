package su.nezushin.nminimapirisblocker.checker.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.checker.interfaces.Callback;
import su.nezushin.nminimapirisblocker.checker.interfaces.PacketHandler;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;

import java.util.Arrays;

public class PacketeventsPacketHandler implements PacketHandler {

    private PacketListenerCommon packetListener;

    @Override
    public void openSign(Player p, Location loc) {
        sendPackets(p, new WrapperPlayServerOpenSignEditor(SpigotConversionUtil.fromBukkitLocation(loc).getPosition().toVector3i(), true));
    }

    @Override
    public void register(Callback<PacketData> callback) {
        packetListener = PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            public void onPacketReceive(PacketReceiveEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Client.UPDATE_SIGN))
                    return;

                var packet = new WrapperPlayClientUpdateSign(event);
                callback.run(new PacketData(event.getPlayer(), Arrays.asList(packet.getTextLines())));
            }
        }, PacketListenerPriority.LOW);

    }

    @Override
    public void unregister() {
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
    }

    public static void sendPackets(Player p, PacketWrapper<?>... wrappers) {
        for (var i : wrappers)
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, i);
    }
}
