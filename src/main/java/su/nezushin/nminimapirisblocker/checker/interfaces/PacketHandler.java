package su.nezushin.nminimapirisblocker.checker.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;

public interface PacketHandler {

    public void openSign(Player p, Location loc);

    public void register(Callback<PacketData> callback);

    public void unregister();
}
