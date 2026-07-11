# NMinimapIrisBlocker
Sign-check mods detector. Designed to use with [NMinimap](https://github.com/NezuShin/NMinimap) but can also work separately.


### Dependencies

- [Packet events](https://www.spigotmc.org/resources/packetevents-api.80279/)
- [NMinimap](https://github.com/NezuShin/NMinimap) (Optional)

### Permissions

- `nminimap.skip-check` - access to bypass sign check
- `nminimap.admin` - access to admin features

### How does it work?

Minecraft client can resolve translations components (e.g. `iris.shaders.reloaded`, `item.minecraft.diamond`, etc.) on signs. 
Plugin utilizes it - opens and closes sign menu when player joining.