# NMinimapIrisBlocker
Sign-check mods detector. Designed to use with [NMinimap](https://github.com/NezuShin/NMinimap) but can also work separately.


### Dependencies

- [Packet events](https://www.spigotmc.org/resources/packetevents-api.80279/) or [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) (works better)
- [NMinimap](https://github.com/NezuShin/NMinimap) (Optional)

### Permissions

- `nminimap.skip-check` - access to bypass sign check
- `nminimap.admin` - access to admin features

### How does it work?

Minecraft client can resolve translations components (e.g. `iris.shaders.reloaded`, `item.minecraft.diamond`, etc.) on signs. 
Plugin utilizes it - opens and closes sign menu when player joining.


### API

Force probe player
```java
public void myProbe(Player player) {
    NMinimapIrisBlocker.getInstance().probe(player, ProbeCause.CUSTOM)
            .thenAccept(result -> {
                System.out.println("CHECK RESULT: " + result.result());
                System.out.println("RESOLVED TRANSLATIONS");
                System.out.println(result.resolved());
            });
}
```
Handle on-join check (to open menu for example)
```java
@EventHandler
public void handleJoinCheck(AsyncPlayerCheckDoneEvent e) {
    if (!e.getCause().equals(ProbeCause.JOIN))
        return;
    var player = e.getPlayer();
    System.out.println("CHECK RESULT: " + e.getResult().result());
    System.out.println("RESOLVED TRANSLATIONS");
    System.out.println(e.getResult().resolved());

    player.openInventory(yourInventory);//etc
}
```
