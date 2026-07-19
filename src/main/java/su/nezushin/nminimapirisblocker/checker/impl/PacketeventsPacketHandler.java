package su.nezushin.nminimapirisblocker.checker.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.checker.interfaces.Callback;
import su.nezushin.nminimapirisblocker.checker.interfaces.PacketHandler;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;

import java.util.List;

public class PacketeventsPacketHandler implements PacketHandler {

    private static final int ANVIL_MENU_TYPE = 8; // minecraft:anvil

    private PacketListenerCommon packetListener;

    @Override
    public void openAnvil(Player player, Component translation) {
        ItemStack item = createProbeItem(translation);
        PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();

        playerManager.writePacket(player,
                new WrapperPlayServerOpenWindow(WINDOW_ID, ANVIL_MENU_TYPE, Component.translatable("container.repair")));
        playerManager.sendPacket(player,
                new WrapperPlayServerSetSlot(WINDOW_ID, 1, 0, item));
    }

    @Override
    public void closeAnvil(Player player) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, new WrapperPlayServerCloseWindow(WINDOW_ID));
    }

    private static ItemStack createProbeItem(Component translation) {
        return ItemStack.builder()
                .type(ItemTypes.STONE_SWORD)
                .component(ComponentTypes.CUSTOM_NAME, translation)
                .build();
    }

    @Override
    public void register(Callback<PacketData> callback) {
        packetListener = PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Client.NAME_ITEM))
                    return;

                WrapperPlayClientNameItem packet = new WrapperPlayClientNameItem(event);
                callback.run(new PacketData((Player) event.getPlayer(), List.of(packet.getItemName())));
            }
        }, PacketListenerPriority.LOW);
    }

    @Override
    public void unregister() {
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
    }
}
