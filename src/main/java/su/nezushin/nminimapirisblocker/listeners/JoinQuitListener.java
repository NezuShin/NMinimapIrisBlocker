package su.nezushin.nminimapirisblocker.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.api.ProbeCause;
import su.nezushin.nminimapirisblocker.api.events.AsyncPlayerCheckDoneEvent;
import su.nezushin.nminimapirisblocker.checker.CheckResult;
import su.nezushin.nminimapirisblocker.util.config.Config;

import java.util.HashMap;
import java.util.Map;

public class JoinQuitListener implements Listener {

    Map<Player, Integer> retries = new HashMap<>();

    @EventHandler
    public void join(PlayerJoinEvent e) {
        var p = e.getPlayer();


        if (p.hasPermission("nminimap.skip-check"))
            return;

        check(p, Config.checkDelay);
    }

    @EventHandler
    public void onCheckDone(AsyncPlayerCheckDoneEvent e) {
        if (!Config.timeoutRetryEnable)
            return;
        if (!e.getCause().equals(ProbeCause.JOIN))
            return;
        if (e.getResult().result() != CheckResult.TIMEOUT)
            return;
        check(e.getPlayer(), Config.timeoutRetryDelay);
    }

    private void check(Player p, int delay) {
        if (!p.isOnline())
            return;
        Runnable run = () -> {
            NMinimapIrisBlocker.getInstance().probe(p, ProbeCause.JOIN);
        };
        if (retries.getOrDefault(p, 0) > Config.maxRetries)
            return;//:(

        retries.put(p, retries.getOrDefault(p, 0) + 1);
        NMinimapIrisBlocker.getInstance().getScheduler().async(run, delay);
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        var p = e.getPlayer();
        retries.remove(p);
        NMinimapIrisBlocker.getInstance().getBlockedPlayers().remove(p);
        NMinimapIrisBlocker.getInstance().getChecker().clearPlayer(p);
    }
}
