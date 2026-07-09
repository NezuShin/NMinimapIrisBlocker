package su.nezushin.nminimapirisblocker.util.config;

import com.google.common.collect.Lists;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.util.config.updater.ConfigUpdater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum Message {

    delete_mods_to_access_the_command;

    private static BukkitAudiences adventure;

    public static BukkitAudiences getAdventure() {
        if (adventure == null) {
            adventure = BukkitAudiences.create(NMinimapIrisBlocker.getInstance());
        }
        return adventure;
    }

    private List<String> message;


    public static void load() {
        getAdventure();

        var file = getLangFile(Config.langName);

        if (file == null) {
            NMinimapIrisBlocker.getInstance().getLogger().severe("Language file for lang " + Config.langName + " is not found. Using en_US instead");
            file = getLangFile("en_US");
        }

        loadFromFile(file);
    }

    private static File getLangFile(String lang) {
        var file = new File(NMinimapIrisBlocker.getInstance().getDataFolder(), "lang/" + lang + ".yml");
        var resourcePath = "defaults/lang/" + lang + ".yml";
        if (!file.exists()) {
            try {
                Config.copyDefaults(resourcePath, file, false);
            } catch (IllegalArgumentException ex) {
                return null;

            }
        } else {
            try {
                ConfigUpdater.update(NMinimapIrisBlocker.getInstance(), resourcePath, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return file;
    }

    private static void loadFromFile(File file) {
        var config = YamlConfiguration.loadConfiguration(file);
        for (var msg : Message.values())
            msg.load(config);
    }

    public void load(FileConfiguration messages) {
        var path = this.name();
        if (messages.isList(path)) {
            message = messages.getStringList(path);
        } else if (messages.isString(path)) {
            message = Lists.newArrayList(messages.getString(path));
        } else {
            message = Lists.newArrayList();
        }
    }

    public void send(CommandSender p) {
        new Sender(this.message).send(p);
    }

    public Sender replace(String... strings) {
        return new Sender(this.message).replace(strings);
    }

    public String asString() {
        return String.join("\n", message);
    }

    public static class Sender {

        private List<String> message;

        public Sender(List<String> message) {
            this.message = new ArrayList<>(message);
        }

        public Sender replace(String... strings) {
            var flag = false;
            var replace = "";
            for (var str : strings) {
                if (!flag) {
                    replace = str;
                } else {
                    for (var i = 0; i < this.message.size(); i++)
                        this.message.set(i, this.message.get(i).replace(replace, str));
                }
                flag = !flag;
            }
            return this;
        }

        public Sender send(CommandSender p) {
            var adventureSender = adventure.sender(p);


            for (var msg : message)
                adventureSender.sendMessage(MiniMessage.miniMessage().deserialize(msg));

            //for (var msg : message)
            //p.sendMessage(MiniMessage.miniMessage().deserialize(msg));
            return this;
        }

    }
}
