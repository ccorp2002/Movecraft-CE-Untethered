package net.countercraft.movecraft.listener;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.NPCCraftImpl;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import org.bukkit.block.*;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import org.bukkit.event.world.AsyncStructureGenerateEvent;
import org.bukkit.util.*;

public class InternalCraftListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onCraftDetect(@NotNull CraftDetectEvent event) {
        // Walk through all signs and set a UUID in there
        final Craft craft = event.getCraft();
        if (craft.isAutomated()) return;
        // Now, find all signs on the craft...
        for (MovecraftLocation mLoc : craft.getHitBox()) {
            Block block = mLoc.toBukkit(craft.getWorld()).getBlock();
            // Only interested in signs, if no sign => continue
            // TODO: Just limit to signs?
            // Edit: That's useful for dispensers too to flag TNT and the like, but for that one could use a separate listener
            if (!(block.getState() instanceof Sign))
                continue;
            // Sign located!
            Sign tile = (Sign) block.getState();

            craft.markTileStateWithUUID(tile);
            tile.update();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDisassembly(@NotNull CraftReleaseEvent event) {
        // Walk through all signs and set a UUID in there
        final Craft craft = event.getCraft();
        if (craft == null) return;
        if (craft.isAutomated()) return;
        if (event.isCancelled()) return;

        // Now, find all signs on the craft...
        for (MovecraftLocation mLoc : craft.getHitBox()) {
            Block block = mLoc.toBukkit(craft.getWorld()).getBlock();
            // Only interested in signs, if no sign => continue
            if (!(block.getState() instanceof Sign))
                continue;
            // Sign located!
            Sign tile = (Sign) block.getState();

            craft.removeUUIDMarkFromTile(tile);

            tile.update();
        }
    }
}