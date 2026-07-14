package su.nezushin.nminimapirisblocker.checker.interfaces;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;

import java.util.List;

public interface PacketHandler {

    void openSign(Player player, Location location, List<Component> lines);

    void register(Callback<PacketData> callback);

    void unregister();
}
