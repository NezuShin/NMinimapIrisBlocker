package su.nezushin.nminimapirisblocker.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.checker.SignChecker;
import su.nezushin.nminimapirisblocker.util.SchedulerUtil;
import su.nezushin.nminimapirisblocker.util.config.Config;

public class JoinQuitListener implements Listener {


    @EventHandler
    public void join(PlayerJoinEvent e) {
        var p = e.getPlayer();


        if (p.hasPermission("nminimap.skip-check"))
            return;

        Runnable run = () -> {
            NMinimapIrisBlocker.getInstance().probe(p);
        };


        if (Config.checkDelay != 0)
            SchedulerUtil.getScheduler().async(run, Config.checkDelay);
        else
            run.run();
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        var p = e.getPlayer();
        NMinimapIrisBlocker.getInstance().getBlockedPlayers().remove(p);
        SignChecker.clearPlayer(p);
    }
}
