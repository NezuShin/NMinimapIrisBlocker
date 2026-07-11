package su.nezushin.nminimapirisblocker.util.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import su.nezushin.nminimapirisblocker.NMinimapIrisBlocker;
import su.nezushin.nminimapirisblocker.util.config.updater.ConfigUpdater;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Config {

    public static FileConfiguration config;

    public static List<String> blockedCommands = new ArrayList<>();

    public static List<String> restrictedTranslations = new ArrayList<>();

    public static String langName;

    public static int checkDelay, timeoutRetryDelay, maxRetries;

    public static boolean logResolvedTranslations, timeoutRetryEnable;

    public static void init() {
        var plugin = NMinimapIrisBlocker.getInstance();
        var configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            plugin.getConfig().options().copyDefaults(true);
            plugin.saveDefaultConfig();


            config = YamlConfiguration.loadConfiguration(configFile);

        } else {
            config = YamlConfiguration.loadConfiguration(configFile);

            if (config.getBoolean("config.allow-config-updates", true))
                try {
                    ConfigUpdater.update(NMinimapIrisBlocker.getInstance(), "config.yml", configFile,
                            "static-markers", "underground-layers", "markers.sizes");

                    config = YamlConfiguration.loadConfiguration(configFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
        }


        langName = config.getString("language", "en_US");


        blockedCommands = config.getStringList("blocked-commands");
        restrictedTranslations = config.getStringList("restricted-translations");

        checkDelay = config.getInt("check-delay", 0);

        logResolvedTranslations = config.getBoolean("log-resolved-translations", true);

        timeoutRetryEnable = config.getBoolean("retries.enable", true);
        timeoutRetryDelay = config.getInt("retries.delay", 20);
        maxRetries = config.getInt("retries.max-retries");

        Message.load();
    }

    public static String getResourceAsString(String resourcePath) {
        try (InputStream in = NMinimapIrisBlocker.getInstance().getResource(resourcePath.replace('\\', '/'));) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void copyDefaults(String resourcePath, File dest, boolean force) {
        if (dest.exists()) {
            if (!force)
                return;
            dest.delete();
        }
        dest.getParentFile().mkdirs();
        try (InputStream in = NMinimapIrisBlocker.getInstance().getResource(resourcePath.replace('\\', '/')); OutputStream out = new FileOutputStream(dest);) {
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + resourcePath);
            }
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
