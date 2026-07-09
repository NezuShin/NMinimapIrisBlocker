package su.nezushin.nminimapirisblocker;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.common.collect.Lists;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;

import java.util.*;

public class SignChecker {


    private static Map<Player, Integer> timeouts = new HashMap<>();

    private static Map<Player, Callback<List<String>>> callbacks = new HashMap<>();

    public static enum SignCheckResult {
        HAVE_RESTRICTED,
        TIMEOUT,
        SUCCESS
    }

    public static interface Callback<T> {

        public void run(T translations);

    }

    public static record SignCheckData(SignCheckResult result, Map<String, String> resolved) {

    }

    private static PacketAdapter adapter = new PacketAdapter(NMinimapIrisBlocker.getInstance(), PacketType.Play.Client.UPDATE_SIGN) {
        @Override
        public void onPacketReceiving(PacketEvent event) {
            var packet = event.getPacket();
            var callback = callbacks.get(event.getPlayer());
            if (callback == null)
                return;
            callback.run(Arrays.asList(packet.getStringArrays().read(0)));
        }
    };

    public static void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(adapter);

    }

    public static void clearPlayer(Player p) {
        callbacks.remove(p);
    }

    public static void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
    }


    public static void probe(Player p, Callback<SignCheckData> callback, List<String> checks) {

        List<String> remainingChecks = new ArrayList<>(checks);

        List<String> currentTranslations = new ArrayList<>();

        Map<String, String> resolved = new HashMap<>();

        Runnable timeoutCallback = () -> {
            callbacks.remove(p);
            timeouts.remove(p);
            callback.run(new SignCheckData(SignCheckResult.TIMEOUT, resolved));
        };

        Callback<List<String>> packetCallback = (List<String> translations) -> {
            Bukkit.getScheduler().cancelTask(timeouts.get(p));
            check(translations, currentTranslations, resolved);
            currentTranslations.clear();
            if (!remainingChecks.isEmpty()) {
                timeouts.put(p, Bukkit.getScheduler().scheduleSyncDelayedTask(NMinimapIrisBlocker.getInstance(), timeoutCallback, 20));
                openSignEditor(p, remainingChecks, currentTranslations);
                return;
            }

            callbacks.remove(p);


            if (checks.stream().noneMatch(resolved::containsKey)) {
                callback.run(new SignCheckData(SignCheckResult.HAVE_RESTRICTED, resolved));
                return;
            }


            callback.run(new SignCheckData(SignCheckResult.SUCCESS, resolved));
        };

        timeouts.put(p, Bukkit.getScheduler().scheduleSyncDelayedTask(NMinimapIrisBlocker.getInstance(), timeoutCallback, 20));

        callbacks.put(p, packetCallback);

        openSignEditor(p, remainingChecks, currentTranslations);
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

    private static void openSignEditor(Player p, List<String> remainingChecks, List<String> currentTranslations) {
        Location loc = p.getLocation();
        Sign sign = (Sign) Bukkit.createBlockData(Material.OAK_SIGN).createBlockState();

        var side = sign.getSide(Side.FRONT);

        for (var i = 0; i < 4 && i < remainingChecks.size(); i++) {
            String s = remainingChecks.remove(0);
            side.setLine(i, BukkitComponentSerializer.legacy().serialize(Component.translatable(s)));
            currentTranslations.add(s);
        }
        p.sendBlockChange(loc, sign.getBlockData());
        p.sendBlockUpdate(loc, sign);
        openSign(p, loc);
        //Bukkit.getScheduler().scheduleSyncDelayedTask(Items.getInstance(), () -> {
        p.sendBlockChange(loc, loc.getBlock().getBlockData());
        //});

    }

    private static void openSign(Player p, Location loc) {
        var manager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR, true);
        packet.getBlockPositionModifier().write(0,
                new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

        packet.getBooleans().write(0, true);

        manager.sendServerPacket(p, packet);
    }

}