package su.nezushin.nminimapirisblocker.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.util.config.Config;
import su.nezushin.nminimapirisblocker.util.config.Message;

public class CommandListener implements Listener {


    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        var p = e.getPlayer();
        if (!NMinimapIrisBlocker.getInstance().getBlockedPlayers().contains(p))
            return;

        var msg = e.getMessage();
        msg = msg.startsWith("/") ? msg.substring(1) : msg; // /command arg1 arg2 -> command arg1 arg1
        msg = msg.contains(" ") ? msg.split(" ")[0] : msg;// command arg1 arg2 -> command
        msg = msg.contains(":") ? msg.substring(msg.indexOf(":") + 1) : msg; // namespace:command -> command

        if (!Config.blockedCommands.contains(msg.toLowerCase()))
            return;

        Message.delete_mods_to_access_the_command.send(p);
        e.setCancelled(true);
    }
}
