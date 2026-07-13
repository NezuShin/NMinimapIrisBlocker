package su.nezushin.nminimapirisblocker.cmd;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.api.ProbeCause;
import su.nezushin.nminimapirisblocker.util.config.Message;

import java.util.List;
import java.util.logging.Level;

public class MibCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!sender.hasPermission("nminimap.admin")) {
            Message.insufficient_permissions.send(sender);
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("players")) {
            Message.list_players_header.send(sender);
            for (var i : NMinimapIrisBlocker.getInstance().getBlockedPlayers())
                Message.list_players_entry.replace("{player}", i.getName()).send(sender);

            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("check")) {

            var target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                Message.player_not_found.replace("{player}", args[1]).send(sender);
                return true;
            }

            Message.probe_start.send(sender);
            NMinimapIrisBlocker.getInstance().probe(target, ProbeCause.COMMAND)
                    .thenAccept(result -> {
                        try {

                            Message.probe_result_header.replace("{result}", Message.valueOf(result.result().name().toLowerCase()).asString()).send(sender);
                            if (!result.resolved().isEmpty())
                                Message.probe_resolved_translations.send(sender);

                            result.resolved().forEach((a, b) -> {
                                Message.probe_resolved_translations_entry.replace("{translation}", a, "{resolved}", b).send(sender);
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
            return true;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            Message.reload_start.send(sender);
            try {
                var nminimap = NMinimapIrisBlocker.getInstance();

                nminimap.reload();
                Message.reload_complete.send(sender);
            } catch (Exception ex) {
                Message.reload_failed.send(sender);
                NMinimapIrisBlocker.getInstance().getLogger().log(Level.SEVERE, "Failed to reload plugin: ", ex);
            }
            return true;
        }

        Message.help.send(sender);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Lists.newArrayList("reload", "players", "check")
                    .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[0])).toList();
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check"))
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();
        }
        return List.of();
    }
}
