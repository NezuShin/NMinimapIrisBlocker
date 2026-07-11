package su.nezushin.nminimapirisblocker.checker;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.NBTComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.util.SchedulerUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SignChecker {


    private static Map<Player, SchedulerUtil.RunningTask> timeouts = new ConcurrentHashMap<>();

    private static Map<Player, Callback<List<String>>> callbacks = new ConcurrentHashMap<>();


    private interface Callback<T> {

        public void run(T translations);

    }

    private static PacketListenerCommon packetListener;

    public static void register() {
        packetListener = PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            public void onPacketReceive(PacketReceiveEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Client.UPDATE_SIGN))
                    return;

                var packet = new WrapperPlayClientUpdateSign(event);
                var callback = callbacks.get(event.getPlayer());
                if (callback == null)
                    return;
                SchedulerUtil.getScheduler().async(() -> callback.run(Arrays.asList(packet.getTextLines())), 0);
            }
        }, PacketListenerPriority.LOW);

    }

    public static void clearPlayer(Player p) {
        callbacks.remove(p);
    }

    public static void unregister() {
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
    }


    public static CompletableFuture<SignCheckData> probe(Player p, List<String> checks) {
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
            if(timeout != null) timeout.cancel();
            check(translations, currentTranslations, resolved);
            currentTranslations.clear();
            if (!remainingChecks.isEmpty()) {
                timeouts.put(p, SchedulerUtil.getScheduler().async(timeoutCallback, 40));
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

        timeouts.put(p, SchedulerUtil.getScheduler().async(timeoutCallback, 20));
        ;

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

    private static void openSignEditor(Player p, List<String> remainingChecks, List<String> currentTranslations) {
        Location loc = new Location(p.getWorld(), 0, 0, 0);

        Sign sign = (Sign) Bukkit.createBlockData(Material.OAK_SIGN).createBlockState();

        var side = sign.getSide(Side.FRONT);

        for (var i = 0; i < 4 && i < remainingChecks.size(); i++) {
            String s = remainingChecks.remove(0);
            //side.line(i, BukkitComponentSerializer.legacy().serialize(Component.translatable(s)));
            side.line(i, Component.translatable(s));
            currentTranslations.add(s);
        }

        p.sendBlockChange(loc, sign.getBlockData());
        p.sendBlockUpdate(loc, sign);

        openSign(p, loc);
        SchedulerUtil.getScheduler().async(() -> {
        p.sendBlockChange(loc, loc.getBlock().getBlockData());
        }, 1);

    }

    private static void openSign(Player p, Location loc) {
        sendPackets(p, new WrapperPlayServerOpenSignEditor(SpigotConversionUtil.fromBukkitLocation(loc).getPosition().toVector3i(), true));
    }

    public static void sendPackets(Player p, PacketWrapper<?>... wrappers) {
        for (var i : wrappers)
            PacketEvents.getAPI().getPlayerManager().sendPacket(p, i);
    }


}