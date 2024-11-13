package net.countercraft.movecraft.events;

import net.countercraft.movecraft.craft.Craft;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerCraftMovementEvent extends CraftEvent implements Cancellable {
    private int dx, dy, dz;
    @NotNull private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    public PlayerCraftMovementEvent(@NotNull Craft c, int dx, int dy, int dz) {
        super(c);
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }
    /**
     * Gets the translation change in X direction
     * @return translation change in X direction
     */
    public int getDx() {
        return dx;
    }

    /**
     * Sets the translation change in X direction
     * @param dx translation change in X direction
     */

    public void setDx(int dx) {
        this.dx = dx;
    }

    /**
     * Gets the translation change in Y direction
     * @return translation change in Y direction
     */
    public int getDy() {
        return dy;
    }

    /**
     * Sets the translation change in Y direction
     * @param dy translation change in Y direction
     */
    public void setDy(int dy) {
        this.dy = dy;
    }

    /**
     * Gets the translation change in Z direction
     * @return translation change in Z direction
     */
    public int getDz() {
        return dz;
    }

    /**
     * Sets the translation change in Z direction
     * @param dz translation change in Z direction
     */
    public void setDz(int dz) {
        this.dz = dz;
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
