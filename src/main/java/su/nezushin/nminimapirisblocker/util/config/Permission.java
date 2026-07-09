package su.nezushin.nminimapirisblocker.util.config;

import org.bukkit.command.CommandSender;

public enum Permission {

    admin;


    public boolean has(CommandSender p) {
        return p.hasPermission("nminimap." + this.name());
    }
}
