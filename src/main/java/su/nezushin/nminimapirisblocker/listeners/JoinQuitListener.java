package su.nezushin.nminimapirisblocker.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.SignChecker;
import su.nezushin.nminimapirisblocker.util.SchedulerUtil;
import su.nezushin.nminimapirisblocker.util.config.Config;

public class JoinQuitListener implements Listener {


    @EventHandler
    public void join(PlayerJoinEvent e) {
        var p = e.getPlayer();


        if (p.hasPermission("nminimap.skip-check"))
            return;

        Runnable run = () -> {
            SignChecker.probe(p, (result) -> {
                NMinimapIrisBlocker.getInstance().getLogger().info("Sign check done for " + p.getName() + ": " + result.result());
                if (result.result() == SignChecker.SignCheckResult.HAVE_RESTRICTED) {
                    if (Config.logResolvedTranslations)
                        NMinimapIrisBlocker.getInstance().getLogger().info(p.getName() + "'s resolved translations: " +
                                result.resolved().entrySet().stream()
                                        .map(i -> i.getValue() + "=" + i.getKey())
                                        .reduce("", (first, second) ->
                                                first + "\n" + second));
                    NMinimapIrisBlocker.getInstance().getBlockedPlayers().add(p);
                }
            }, Config.restrictedTranslations);
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
