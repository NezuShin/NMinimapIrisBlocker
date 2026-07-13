package su.nezushin.nminimapirisblocker.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import su.nezushin.nminimapirisblocker.api.ProbeCause;
import su.nezushin.nminimapirisblocker.checker.records.SignCheckData;

public class AsyncPlayerCheckDoneEvent extends PlayerEvent {
    private static final HandlerList handlerList = new HandlerList();

    private SignCheckData result;

    private ProbeCause cause;

    public AsyncPlayerCheckDoneEvent(@NotNull Player player, SignCheckData result, ProbeCause cause) {
        super(player, true);
        this.result = result;
        this.cause = cause;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public SignCheckData getResult() {
        return result;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public ProbeCause getCause() {
        return cause;
    }
}
