package su.nezushin.nminimapirisblocker.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nezushin.nminimapirisblocker.api.ProbeCause;
import su.nezushin.nminimapirisblocker.checker.records.CheckData;

public class AsyncPlayerCheckDoneEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private CheckData result;

    private ProbeCause cause;

    private Player player;

    public AsyncPlayerCheckDoneEvent(@NotNull Player player, CheckData result, ProbeCause cause) {
        super(true);
        this.player = player;
        this.result = result;
        this.cause = cause;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public CheckData getResult() {
        return result;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public ProbeCause getCause() {
        return cause;
    }

    public Player getPlayer() {
        return player;
    }
}
