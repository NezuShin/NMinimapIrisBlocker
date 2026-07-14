package su.nezushin.nminimapirisblocker.checker.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedRegistrable;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.checker.interfaces.Callback;
import su.nezushin.nminimapirisblocker.checker.interfaces.PacketHandler;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;
import su.nezushin.nminimapirisblocker.util.SignUtils;

import java.util.Arrays;
import java.util.List;

public class ProtocolLibPacketHandler implements PacketHandler {

    private static PacketAdapter adapter;

    @Override
    public void openSign(Player player, Location location, List<Component> lines) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        List<Component> padded = SignUtils.padToFour(lines);

        PacketContainer blockChange = manager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        blockChange.getBlockPositionModifier().write(0, position);
        blockChange.getBlockData().write(0, WrappedBlockData.createData(Material.OAK_SIGN));

        PacketContainer tileEntity = manager.createPacket(PacketType.Play.Server.TILE_ENTITY_DATA);
        tileEntity.getBlockPositionModifier().write(0, position);
        tileEntity.getBlockEntityTypeModifier().write(0, WrappedRegistrable.blockEntityType("sign"));
        tileEntity.getNbtModifier().write(0, createSignNbt(padded));

        PacketContainer openEditor = manager.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        openEditor.getBlockPositionModifier().write(0, position);
        openEditor.getBooleans().write(0, true);

        manager.sendServerPacket(player, blockChange);
        manager.sendServerPacket(player, tileEntity);
        manager.sendServerPacket(player, openEditor);
    }

    private static NbtCompound createSignNbt(List<Component> lines) {
        NbtCompound root = NbtFactory.ofCompound("");
        root.put("front_text", createSide(lines));
        root.put("back_text", createSide(SignUtils.padToFour(List.of())));
        root.put("is_waxed", (byte) 0);
        return root;
    }

    private static NbtCompound createSide(List<Component> lines) {
        NbtCompound side = NbtFactory.ofCompound("");
        side.put("has_glowing_text", (byte) 0);
        side.put("color", "black");

        side.put(NbtFactory.ofList(
                "messages",
                toChatComponent(lines.get(0)),
                toChatComponent(lines.get(1)),
                toChatComponent(lines.get(2)),
                toChatComponent(lines.get(3))
        ));
        return side;
    }

    private static NbtCompound toChatComponent(Component component) {
        NbtCompound compound = NbtFactory.ofCompound("");
        if (component instanceof TranslatableComponent translatable) {
            compound.put("translate", translatable.key());
        } else {
            compound.put("text", PlainTextComponentSerializer.plainText().serialize(component));
        }
        return compound;
    }

    @Override
    public void register(Callback<PacketData> callback) {
        adapter = new PacketAdapter(NMinimapIrisBlocker.getInstance(), PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                var packet = event.getPacket();
                callback.run(new PacketData(event.getPlayer(), Arrays.asList(packet.getStringArrays().read(0))));
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
    }

    @Override
    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
    }
}
