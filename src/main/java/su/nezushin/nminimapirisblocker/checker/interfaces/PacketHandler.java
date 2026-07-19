package su.nezushin.nminimapirisblocker.checker.interfaces;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;

public interface PacketHandler {

    int WINDOW_ID = 50;

    void openAnvil(Player player, Component translation);

    void closeAnvil(Player player);

    void register(Callback<PacketData> callback);

    void unregister();
}
