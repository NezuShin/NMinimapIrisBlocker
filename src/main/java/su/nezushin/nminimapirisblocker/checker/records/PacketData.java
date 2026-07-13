package su.nezushin.nminimapirisblocker.checker.records;

import org.bukkit.entity.Player;

import java.util.List;

public record PacketData(Player player, List<String> translations) {
}
