package su.nezushin.nminimapirisblocker.checker;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.checker.impl.PacketeventsPacketHandler;
import su.nezushin.nminimapirisblocker.checker.impl.ProtocolLibPacketHandler;
import su.nezushin.nminimapirisblocker.checker.interfaces.Callback;
import su.nezushin.nminimapirisblocker.checker.interfaces.PacketHandler;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;
import su.nezushin.nminimapirisblocker.checker.records.SignCheckData;
import su.nezushin.nminimapirisblocker.util.RunningTask;

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

        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            packetHandler = new ProtocolLibPacketHandler();
            packetHandler.register(this::onSignPacketReceive);

            return true;
        } else if (Bukkit.getPluginManager().isPluginEnabled("packetevents")) {
            packetHandler = new PacketeventsPacketHandler();
            packetHandler.register(this::onSignPacketReceive);
            return true;
        }
        return false;
    }

    public void clearPlayer(Player p) {
        callbacks.remove(p);
    }

    public void unregister() {
        if (packetHandler != null)
            packetHandler.unregister();
    }

    private void onSignPacketReceive(PacketData data) {
        var callback = callbacks.get(data.player());
        if (callback != null)
            callback.run(data.translations());
    }


    public CompletableFuture<SignCheckData> probe(Player p, List<String> checks) {
        var callback = new CompletableFuture<SignCheckData>();

        List<String> remainingChecks = new ArrayList<>(checks);

        List<String> currentTranslations = new ArrayList<>();

        Map<String, String> resolved = new HashMap<>();

        Runnable timeoutCallback = () -> {
            callbacks.remove(p);
            timeouts.remove(p);
            callback.complete(new SignCheckData(SignCheckResult.TIMEOUT, resolved));
        };

        Callback<List<String>> packetCallback = (List<String> translations) -> {
            var timeout = timeouts.get(p);
            if (timeout != null) timeout.cancel();
            check(translations, currentTranslations, resolved);
            currentTranslations.clear();
            if (!remainingChecks.isEmpty()) {
                timeouts.put(p, NMinimapIrisBlocker.getInstance().getScheduler().async(timeoutCallback, 40));
                openSignEditor(p, remainingChecks, currentTranslations);
                return;
            }

            callbacks.remove(p);

            if (checks.stream().anyMatch(resolved::containsKey)) {
                callback.complete(new SignCheckData(SignCheckResult.HAVE_RESTRICTED, resolved));
                return;
            }


            callback.complete(new SignCheckData(SignCheckResult.SUCCESS, resolved));
        };

        timeouts.put(p, NMinimapIrisBlocker.getInstance().getScheduler().async(timeoutCallback, 20));

        callbacks.put(p, packetCallback);

        openSignEditor(p, remainingChecks, currentTranslations);
        return callback;
    }


    private static void check(List<String> translations, List<String> currentTranslations, Map<String, String> resolved) {
        for (var i = 0; i < 4 && i < currentTranslations.size(); i++) {
            String translation = translations.get(i);
            String current = currentTranslations.get(i);
            if (!translation.equalsIgnoreCase(current)) {
                resolved.put(current, translation);
            }
        }
    }

    private void openSignEditor(Player p, List<String> remainingChecks, List<String> currentTranslations) {
        Location loc = p.getLocation();

        List<Component> lines = new ArrayList<>(4);
        for (var i = 0; i < 4 && !remainingChecks.isEmpty(); i++) {
            String s = remainingChecks.remove(0);
            lines.add(Component.translatable(s));
            currentTranslations.add(s);
        }

        packetHandler.openSign(p, loc, lines);

        NMinimapIrisBlocker.getInstance().getScheduler().async(() -> {
            p.sendBlockChange(loc, loc.getBlock().getBlockData());
        }, 2);
    }
}
