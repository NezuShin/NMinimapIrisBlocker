package su.nezushin.nminimapirisblocker.checker.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.checker.interfaces.Callback;
import su.nezushin.nminimapirisblocker.checker.interfaces.PacketHandler;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;

import java.util.Arrays;

public class ProtocolLibPacketHandler implements PacketHandler {

    private static PacketAdapter adapter;

    @Override
    public void openSign(Player p, Location loc) {
        var manager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = manager.createPacket(com.comphenix.protocol.PacketType.Play.Server.OPEN_SIGN_EDITOR, true);
        packet.getBlockPositionModifier().write(0, new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

        packet.getBooleans().write(0, true);

        manager.sendServerPacket(p, packet);
    }

    @Override
    public void register(Callback<PacketData> callback) {
        adapter = new PacketAdapter(NMinimapIrisBlocker.getInstance(), PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                var packet = event.getPacket();
                callback.run(new PacketData(event.getPlayer(), Arrays.asList(packet.getStringArrays().read(0))));
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
    }

    @Override
    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
    }
}
