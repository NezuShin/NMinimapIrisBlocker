package su.nezushin.nminimapirisblocker.checker;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.checker.impl.PacketeventsPacketHandler;
import su.nezushin.nminimapirisblocker.checker.impl.ProtocolLibPacketHandler;
import su.nezushin.nminimapirisblocker.checker.interfaces.Callback;
import su.nezushin.nminimapirisblocker.checker.interfaces.PacketHandler;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;
import su.nezushin.nminimapirisblocker.checker.records.CheckData;
import su.nezushin.nminimapirisblocker.util.RunningTask;
import su.nezushin.nminimapirisblocker.util.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SignChecker {

    private Map<Player, RunningTask> timeouts = new ConcurrentHashMap<>();

    private Map<Player, Callback<List<String>>> callbacks = new ConcurrentHashMap<>();

    private PacketHandler packetHandler;

    public boolean register() {
        boolean protocolLibInstalled = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
        boolean packetEventsInstalled = Bukkit.getPluginManager().isPluginEnabled("packetevents");

        boolean preferPacketEvents = Config.backend != null
                && Config.backend.equalsIgnoreCase("PacketEvents");

        if (protocolLibInstalled && packetEventsInstalled) {
            packetHandler = preferPacketEvents
                    ? new PacketeventsPacketHandler()
                    : new ProtocolLibPacketHandler();
        } else if (protocolLibInstalled) {
            packetHandler = new ProtocolLibPacketHandler();
        } else if (packetEventsInstalled) {
            packetHandler = new PacketeventsPacketHandler();
        } else {
            return false;
        }

        String backendName = packetHandler instanceof PacketeventsPacketHandler ? "PacketEvents" : "ProtocolLib";
        NMinimapIrisBlocker.getInstance().getLogger().info(backendName + " selected as packet backend");

        packetHandler.register(this::onAnvilPacketReceive);
        return true;
    }

    public void clearPlayer(Player p) {
        callbacks.remove(p);
    }

    public void unregister() {
        if (packetHandler != null)
            packetHandler.unregister();
    }

    private void onAnvilPacketReceive(PacketData data) {
        var callback = callbacks.get(data.player());
        if (callback != null)
            callback.run(data.translations());
    }


    public CompletableFuture<CheckData> probe(Player p, List<String> checks) {
        var callback = new CompletableFuture<CheckData>();

        List<String> remainingChecks = new ArrayList<>(checks);

        List<String> currentTranslations = new ArrayList<>();

        Map<String, String> resolved = new HashMap<>();

        Runnable timeoutCallback = () -> {
            callbacks.remove(p);
            timeouts.remove(p);
            packetHandler.closeAnvil(p);
            callback.complete(new CheckData(CheckResult.TIMEOUT, resolved));
        };

        Callback<List<String>> packetCallback = (List<String> translations) -> {
            var timeout = timeouts.get(p);
            if (timeout != null) timeout.cancel();
            check(translations, currentTranslations, resolved);
            currentTranslations.clear();
            packetHandler.closeAnvil(p);
            if (!remainingChecks.isEmpty()) {
                timeouts.put(p, NMinimapIrisBlocker.getInstance().getScheduler().async(timeoutCallback, 40));
                openAnvilEditor(p, remainingChecks, currentTranslations);
                return;
            }

            callbacks.remove(p);

            if (checks.stream().anyMatch(resolved::containsKey)) {
                callback.complete(new CheckData(CheckResult.HAVE_RESTRICTED, resolved));
                return;
            }


            callback.complete(new CheckData(CheckResult.SUCCESS, resolved));
        };

        timeouts.put(p, NMinimapIrisBlocker.getInstance().getScheduler().async(timeoutCallback, 20));

        callbacks.put(p, packetCallback);

        openAnvilEditor(p, remainingChecks, currentTranslations);
        return callback;
    }


    private static void check(List<String> translations, List<String> currentTranslations, Map<String, String> resolved) {
        if (translations.isEmpty() || currentTranslations.isEmpty())
            return;

        String translation = translations.get(0);
        String current = currentTranslations.get(0);
        if (!translation.equalsIgnoreCase(current)) {
            resolved.put(current, translation);
        }
    }

    private void openAnvilEditor(Player p, List<String> remainingChecks, List<String> currentTranslations) {
        String s = remainingChecks.remove(0);
        currentTranslations.add(s);

        packetHandler.openAnvil(p, Component.translatable(s));
    }
}
