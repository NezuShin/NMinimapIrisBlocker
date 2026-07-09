package su.nezushin.nminimapirisblocker.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import su.nezushin.nminimap.api.events.AsyncMapRenderEvent;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;

public class NMinimapRenderListener implements Listener {


    @EventHandler
    public void asyncRender(AsyncMapRenderEvent e) {
        var player = e.getPlayer();
        var p = player.getPlayer();
        if (!NMinimapIrisBlocker.getInstance().getBlockedPlayers().contains(p))
            return;

        player.setEnabled(false);
    }
}
