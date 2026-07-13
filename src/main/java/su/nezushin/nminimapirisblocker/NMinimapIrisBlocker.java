package su.nezushin.nminimapirisblocker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import su.nezushin.nminimapirisblocker.api.ProbeCause;
import su.nezushin.nminimapirisblocker.api.events.AsyncPlayerCheckDoneEvent;
import su.nezushin.nminimapirisblocker.checker.records.SignCheckData;
import su.nezushin.nminimapirisblocker.checker.SignCheckResult;
import su.nezushin.nminimapirisblocker.checker.SignChecker;
import su.nezushin.nminimapirisblocker.cmd.MibCommand;
import su.nezushin.nminimapirisblocker.listeners.CommandListener;
import su.nezushin.nminimapirisblocker.listeners.JoinQuitListener;
import su.nezushin.nminimapirisblocker.listeners.NMinimapRenderListener;
import su.nezushin.nminimapirisblocker.util.SchedulerUtil;
import su.nezushin.nminimapirisblocker.util.config.Config;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class NMinimapIrisBlocker extends JavaPlugin {

    private static NMinimapIrisBlocker instance;

    private SignChecker checker;

    private final Set<Player> blockedPlayers = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        instance = this;
        load();
    }

    @Override
    public void onDisable() {
        unload();
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        SchedulerUtil.getScheduler().cancelAllTasks();
        checker.unregister();
    }

    public void load() {
        getCommand("mib").setExecutor(new MibCommand());
        Config.init();


        checker = new SignChecker();
        ;



        if (!checker.register()) {
            this.getLogger().severe("This plugin requires Packetevents or ProtocolLib installed on server." +
                    " Neither were found. (ProtocolLib performs better) Please download it from https://www.spigotmc.org/resources/protocollib.1997/");
            setEnabled(false);
            return;
        }


        if (Bukkit.getPluginManager().isPluginEnabled("NMinimap")) {
            Bukkit.getPluginManager().registerEvents(new NMinimapRenderListener(), getInstance());
        }
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), getInstance());
        Bukkit.getPluginManager().registerEvents(new CommandListener(), getInstance());
    }

    public void reload() {
        unload();
        load();
    }

    /**
     * Check if player has restricted mods
     *
     * @param p player
     */
    public CompletableFuture<SignCheckData> probe(Player p, ProbeCause cause) {
        var future = new CompletableFuture<SignCheckData>();
        checker.probe(p, Config.restrictedTranslations)
                .thenAccept(result -> {
                    try {
                        NMinimapIrisBlocker.getInstance().getLogger().info("Sign check done for " + p.getName() + ": " + result.result());
                        if (result.result() == SignCheckResult.HAVE_RESTRICTED) {
                            if (Config.logResolvedTranslations)
                                NMinimapIrisBlocker.getInstance().getLogger().info(p.getName() + "'s resolved translations: " +
                                        result.resolved().entrySet().stream()
                                                .map(i -> i.getKey() + "=" + i.getValue())
                                                .reduce("", (first, second) ->
                                                        first + "\n" + second));
                            if (!p.hasPermission("nminimap.skip-check"))
                                NMinimapIrisBlocker.getInstance().getBlockedPlayers().add(p);
                        }
                        Bukkit.getPluginManager().callEvent(new AsyncPlayerCheckDoneEvent(p, result, cause));
                        future.complete(result);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                });

        return future;
    }

    public static NMinimapIrisBlocker getInstance() {
        return instance;
    }

    public Set<Player> getBlockedPlayers() {
        return blockedPlayers;
    }

    public SignChecker getChecker() {
        return checker;
    }
}
