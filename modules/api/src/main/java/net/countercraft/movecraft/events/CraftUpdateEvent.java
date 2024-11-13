package net.countercraft.movecraft.events;

import net.countercraft.movecraft.craft.Craft;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CraftUpdateEvent extends CraftEvent implements Cancellable {
    @NotNull private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    public CraftUpdateEvent(@NotNull Craft c) {
        super(c);
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
