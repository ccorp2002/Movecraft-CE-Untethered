package net.countercraft.movecraft.events;

import net.countercraft.movecraft.craft.Craft;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
public class CraftMergeEvent extends CraftEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @NotNull private final World world;
    @NotNull private final Set<Location> locations;
    private boolean cancelled;

    public CraftMergeEvent(@NotNull Craft craft, @NotNull Set<Location> locations, @NotNull World world) {
        super(craft);
        this.world = world;
        this.locations = locations;
        cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    public Set<Location> getLocations() {
        return locations;
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
