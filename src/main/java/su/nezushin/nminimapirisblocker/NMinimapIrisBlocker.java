package su.nezushin.nminimapirisblocker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import su.nezushin.nminimap.util.SchedulerUtil;
import su.nezushin.nminimapirisblocker.listeners.JoinQuitListener;
import su.nezushin.nminimapirisblocker.listeners.NMinimapRenderListener;
import su.nezushin.nminimapirisblocker.util.config.Config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class NMinimapIrisBlocker extends JavaPlugin {

    private static NMinimapIrisBlocker instance;

    private Set<Player> blockedPlayers = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        unload();
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        SchedulerUtil.getScheduler().cancelAllTasks();
        SignChecker.unregister();
    }

    public void load() {
        Config.init();

        if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            this.getLogger().severe("ProtocolLib plugin is not found. It is mandatory dependency. Please download it from https://www.spigotmc.org/resources/protocollib.1997/");
        }


        if (Bukkit.getPluginManager().isPluginEnabled("NMinimap")) {
            Bukkit.getPluginManager().registerEvents(new NMinimapRenderListener(), getInstance());
        }
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), getInstance());

        SignChecker.register();

    }

    public void reload() {
        unload();
        load();
    }

    public static NMinimapIrisBlocker getInstance() {
        return instance;
    }

    public Set<Player> getBlockedPlayers() {
        return blockedPlayers;
    }
}
