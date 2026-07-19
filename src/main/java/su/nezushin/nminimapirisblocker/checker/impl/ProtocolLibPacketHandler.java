package su.nezushin.nminimapirisblocker.checker.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedRegistrable;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.checker.interfaces.Callback;
import su.nezushin.nminimapirisblocker.checker.interfaces.PacketHandler;
import su.nezushin.nminimapirisblocker.checker.records.PacketData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class ProtocolLibPacketHandler implements PacketHandler {

    private static final Class<?> MENU_TYPE_CLASS = MinecraftReflection.getMinecraftClass("world.inventory.MenuType");
    private static final Class<?> DATA_COMPONENT_TYPE_CLASS =
            MinecraftReflection.getMinecraftClass("core.component.DataComponentType");
    private static final Class<?> DATA_COMPONENTS_CLASS =
            MinecraftReflection.getMinecraftClass("core.component.DataComponents");

    private static final Object CUSTOM_NAME_TYPE = resolveCustomNameType();

    private static final MethodAccessor SET_COMPONENT = Accessors.getMethodAccessor(
            FuzzyReflection.fromClass(MinecraftReflection.getItemStackClass(), true)
                    .getMethod(FuzzyMethodContract.newBuilder()
                            .parameterExactArray(DATA_COMPONENT_TYPE_CLASS, Object.class)
                            .parameterCount(2)
                            .requireModifier(Modifier.PUBLIC)
                            .banModifier(Modifier.STATIC)
                            .build()));

    private static PacketAdapter adapter;

    @Override
    public void openAnvil(Player player, Component translation) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        ItemStack item = createProbeItem(translation);

        PacketContainer openWindow = manager.createPacket(PacketType.Play.Server.OPEN_WINDOW);
        openWindow.getIntegers().write(0, WINDOW_ID);
        openWindow.getRegistrableModifier(MENU_TYPE_CLASS).write(0, WrappedRegistrable.fromClassAndKey(MENU_TYPE_CLASS, "anvil"));
        openWindow.getChatComponents().write(0, titleComponent(Component.translatable("container.repair")));

        PacketContainer setSlot = manager.createPacket(PacketType.Play.Server.SET_SLOT);
        setSlot.getIntegers().write(0, WINDOW_ID);
        setSlot.getIntegers().write(1, 1); // state id
        setSlot.getIntegers().write(2, 0); // slot
        setSlot.getItemModifier().write(0, item);

        manager.sendServerPacket(player, openWindow);
        manager.sendServerPacket(player, setSlot);
    }

    @Override
    public void closeAnvil(Player player) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer closeWindow = manager.createPacket(PacketType.Play.Server.CLOSE_WINDOW);
        closeWindow.getIntegers().write(0, WINDOW_ID);
        manager.sendServerPacket(player, closeWindow);
    }

    private static ItemStack createProbeItem(Component translation) {
        ItemStack stack = MinecraftReflection.getBukkitItemStack(new ItemStack(Material.STONE_SWORD));
        Object nmsItem = MinecraftReflection.getMinecraftItemStack(stack);

        SET_COMPONENT.invoke(nmsItem, CUSTOM_NAME_TYPE, chatComponentFromNbt(translation));

        return MinecraftReflection.getBukkitItemStack(nmsItem);
    }

    private static Object resolveCustomNameType() {
        FieldAccessor named = Accessors.getFieldAccessorOrNull(DATA_COMPONENTS_CLASS, "CUSTOM_NAME", Object.class);
        if (named != null) {
            return named.get(null);
        }

        for (Field field : DATA_COMPONENTS_CLASS.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            if (!DATA_COMPONENT_TYPE_CLASS.isAssignableFrom(field.getType()))
                continue;
            try {
                Object value = field.get(null);
                if (String.valueOf(value).contains("custom_name"))
                    return value;
            } catch (IllegalAccessException ignored) {
            }
        }

        throw new IllegalStateException("Unable to resolve DataComponents.CUSTOM_NAME");
    }

    private static Object chatComponentFromNbt(Component component) {
        NbtCompound nbt = NbtFactory.ofCompound("");
        if (component instanceof TranslatableComponent translatable) {
            nbt.put("translate", translatable.key());
        } else {
            nbt.put("text", PlainTextComponentSerializer.plainText().serialize(component));
        }

        String json = nbt.containsKey("translate")
                ? "{\"translate\":\"" + nbt.getString("translate") + "\"}"
                : "{\"text\":\"" + nbt.getString("text") + "\"}";
        return WrappedChatComponent.fromJson(json).getHandle();
    }

    private static WrappedChatComponent titleComponent(Component component) {
        return WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component));
    }

    @Override
    public void register(Callback<PacketData> callback) {
        adapter = new PacketAdapter(NMinimapIrisBlocker.getInstance(), PacketType.Play.Client.ITEM_NAME) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                var packet = event.getPacket();
                callback.run(new PacketData(event.getPlayer(), List.of(packet.getStrings().read(0))));
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
    }

    @Override
    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
    }
}
