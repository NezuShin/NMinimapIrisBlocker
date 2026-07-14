package su.nezushin.nminimapirisblocker.checker.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.checker.interfaces.Callback;
import su.nezushin.nminimapirisblocker.checker.interfaces.PacketHandler;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;
import su.nezushin.nminimapirisblocker.util.SignUtils;

import java.util.Arrays;
import java.util.List;

public class PacketeventsPacketHandler implements PacketHandler {

    private PacketListenerCommon packetListener;

    @Override
    public void openSign(Player player, Location location, List<Component> lines) {
        Vector3i position = new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        List<Component> padded = SignUtils.padToFour(lines);

        WrapperPlayServerBlockChange blockChange = new WrapperPlayServerBlockChange(
                position,
                StateTypes.OAK_SIGN.createBlockState()
        );

        WrapperPlayServerBlockEntityData tileEntity = new WrapperPlayServerBlockEntityData(
                position,
                BlockEntityTypes.SIGN,
                createSignNbt(padded)
        );

        WrapperPlayServerOpenSignEditor openEditor = new WrapperPlayServerOpenSignEditor(position, true);

        // writePacket keeps order; final sendPacket flushes the channel.
        PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();
        playerManager.writePacket(player, blockChange);
        playerManager.writePacket(player, tileEntity);
        playerManager.sendPacket(player, openEditor);
    }

    private static NBTCompound createSignNbt(List<Component> lines) {
        NBTCompound root = new NBTCompound();
        root.setTag("front_text", createSide(lines));
        root.setTag("back_text", createSide(SignUtils.padToFour(List.of())));
        root.setTag("is_waxed", new NBTByte((byte) 0));
        return root;
    }

    private static NBTCompound createSide(List<Component> lines) {
        NBTCompound side = new NBTCompound();
        side.setTag("has_glowing_text", new NBTByte((byte) 0));
        side.setTag("color", new NBTString("black"));

        NBTList<NBTCompound> messages = new NBTList<>(NBTType.COMPOUND);
        for (Component line : lines) {
            messages.addTag(toChatComponent(line));
        }
        side.setTag("messages", messages);
        return side;
    }

    private static NBTCompound toChatComponent(Component component) {
        NBTCompound compound = new NBTCompound();
        if (component instanceof TranslatableComponent translatable) {
            compound.setTag("translate", new NBTString(translatable.key()));
        } else {
            compound.setTag("text", new NBTString(PlainTextComponentSerializer.plainText().serialize(component)));
        }
        return compound;
    }

    @Override
    public void register(Callback<PacketData> callback) {
        packetListener = PacketEvents.getAPI().getEventManager().registerListener(new PacketListener() {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Client.UPDATE_SIGN))
                    return;

                WrapperPlayClientUpdateSign packet = new WrapperPlayClientUpdateSign(event);
                callback.run(new PacketData((Player) event.getPlayer(), Arrays.asList(packet.getTextLines())));
            }
        }, PacketListenerPriority.LOW);
    }

    @Override
    public void unregister() {
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
    }
}
