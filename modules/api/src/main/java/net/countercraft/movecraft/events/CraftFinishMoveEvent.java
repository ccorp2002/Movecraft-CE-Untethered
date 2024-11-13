package net.countercraft.movecraft.events;

import net.countercraft.movecraft.craft.Craft;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called whenever a craft is piloted
 * @see Craft
 */
@SuppressWarnings("unused")
public class CraftFinishMoveEvent extends CraftEvent{
    private static final HandlerList HANDLERS = new HandlerList();

    public CraftFinishMoveEvent(@NotNull Craft craft){
        super(craft);
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
