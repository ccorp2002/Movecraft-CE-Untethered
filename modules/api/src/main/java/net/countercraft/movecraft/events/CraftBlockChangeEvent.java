package net.countercraft.movecraft.events;

import net.countercraft.movecraft.craft.Craft;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CraftBlockChangeEvent extends CraftEvent implements Cancellable {
    @NotNull private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private BlockState block;
    private boolean adding = false;
    public CraftBlockChangeEvent(@NotNull Craft c, BlockState block, boolean adding) {
        super(c);
        this.block = block;
        this.adding = adding;
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

    public boolean isAdding() {
        return adding;
    }

    public BlockState getState() {
        return this.block;
    }

    public void setState(BlockState state) {
        this.block = state;
    }
    public void setAdding(boolean adding) {
        this.adding = adding; 
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
