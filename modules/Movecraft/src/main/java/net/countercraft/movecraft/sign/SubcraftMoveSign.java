package net.countercraft.movecraft.sign;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.async.translation.TranslationTask;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.SubCraftImpl;
import net.countercraft.movecraft.craft.SubcraftRotateCraft;
import net.countercraft.movecraft.craft.NPCCraftImpl;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.functions.Result;
import net.countercraft.movecraft.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public final class SubcraftMoveSign implements Listener {
    private static final String HEADER = "Subcraft Move";
    private final Set<MovecraftLocation> rotating = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onSignClick(@NotNull final PlayerInteractEvent event) {
        int multi = 0;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            multi = 1;
        else if (event.getAction() == Action.LEFT_CLICK_BLOCK)
            multi = -1;
        else
            return;
        if (event.getPlayer().isSneaking())
            return;
        MovecraftLocation vector = new MovecraftLocation(0,0,0);

        BlockState state = event.getClickedBlock().getState();
        if (!(state instanceof Sign))
            return;
        Sign sign = (Sign) state;
        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(HEADER)) {
            return;
        }
        if ((ChatColor.stripColor(sign.getLine(2))).length() == 0) {
            return;
        }

        Location loc = event.getClickedBlock().getLocation();
        final MovecraftLocation startPoint = new MovecraftLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (rotating.contains(startPoint)) {
            event.getPlayer().sendMessage("You are already Moving");
            event.setCancelled(true);
            return;
        }

        // rotate subcraft
        String craftTypeStr = ChatColor.stripColor(sign.getLine(1));
        CraftType craftType = CraftManager.getInstance().getCraftTypeFromString(craftTypeStr);
        if (craftType == null) {
            return;
        }
        String[] coords = (ChatColor.stripColor(sign.getLine(2))).split(",");
        int maxMove = craftType.getIntProperty(CraftType.MAX_STATIC_MOVE);
        if (coords.length == 0 || coords[0].length() <= 0) {
            return;
        }
        if (coords.length == 1) {
            vector.y = Integer.parseInt(coords[0]);
        }
        if (coords.length == 2) {
            vector.x = Integer.parseInt(coords[0]);
            vector.z = Integer.parseInt(coords[1]);
        }
        if (coords.length >= 3) {
            vector.x = Integer.parseInt(coords[0]);
            vector.y = Integer.parseInt(coords[1]);
            vector.z = Integer.parseInt(coords[2]);
        }
        int dLeftRight = vector.x;
        int dBackwardForward = vector.z;
        if (dLeftRight > maxMove)
            dLeftRight = maxMove;
        if (dLeftRight < -maxMove)
            dLeftRight = -maxMove;
        if (vector.y > maxMove)
            vector.y = maxMove;
        if (vector.y < -maxMove)
            vector.y = -maxMove;
        if (dBackwardForward > maxMove)
            dBackwardForward = maxMove;
        if (dBackwardForward < -maxMove)
            dBackwardForward = -maxMove;
        switch (sign.getRawData()) {
            case 0x3:
                // North
                vector.x = dLeftRight;
                vector.z = -dBackwardForward;
                break;
            case 0x2:
                // South
                vector.x = -dLeftRight;
                vector.z = dBackwardForward;
                break;
            case 0x4:
                // East
                vector.x = dBackwardForward;
                vector.z = dLeftRight;
                break;
            case 0x5:
                // West
                vector.x = -dBackwardForward;
                vector.z = -dLeftRight;
                break;
        }

        if (!event.getPlayer().hasPermission("movecraft." + craftTypeStr + ".pilot") || !event.getPlayer().hasPermission("movecraft." + craftTypeStr + ".translate")) {
            event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return;
        }

        final Craft playerCraft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
        if (playerCraft != null) {
            if (!playerCraft.isNotProcessing()) {
                event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Detection - Parent Craft is busy"));
                return;
            }
        }

        final int x = vector.x;
        final int y = vector.y;
        final int z = vector.z;
        final int mult = multi;
        final Player player = event.getPlayer();
        World world = event.getClickedBlock().getWorld();
        if (CraftManager.getInstance().getCraftFromBlock(event.getClickedBlock()) != null) {
            if (CraftManager.getInstance().getCraftFromBlock(event.getClickedBlock()) instanceof SubCraftImpl) {
                event.getPlayer().sendMessage("You are already moving.");
                event.setCancelled(true);
                return;
            }
            //if (!CraftManager.getInstance().getCraftFromBlock(event.getClickedBlock()).isNotProcessing()) {
            //    event.getPlayer().sendMessage("You are already Moving");
            //    event.setCancelled(true);
            //    return;
            //}
        }
        rotating.add(startPoint);
        if (playerCraft != null) playerCraft.setProcessing(true);
        CraftManager.getInstance().detect(
                startPoint,
                craftType, (type, w, p, parents) -> {
                    if (parents.size() > 1)
                        return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                "Detection - Failed - Already commanding a craft")), null);
                    if (parents.size() < 1)
                        return new Pair<>(Result.succeed(), new NPCCraftImpl(type, w, null));

                    Craft parent = parents.iterator().next();
                    NPCCraftImpl craft = new NPCCraftImpl(type, w, null);
                    craft.setParent(parent);
                    craft.setPassengers(parent.getPassengers());
                    return new Pair<>(Result.succeed(), craft);
                },
                world, null, Movecraft.getAdventure().console(),
                craft -> () -> {
                    Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.SUB_CRAFT));
                    if (craft instanceof NPCCraftImpl) { // Subtract craft from the parent
                        Craft parent = ((NPCCraftImpl) craft).getParent();
                        if (parent != null) {
                            var newHitbox = parent.getHitBox().difference(craft.getHitBox());
                            parent.setHitBox(newHitbox);
                        }
                    }
                    Movecraft.getInstance().getAsyncManager().submitTask(new TranslationTask(craft, world, mult*x,mult*y,mult*z), craft);
                    //craft.translate(mult*x,mult*y,mult*z);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (craft instanceof NPCCraftImpl) {
                                Craft parent = ((NPCCraftImpl) craft).getParent();
                                if (parent != null) {
                                    var newHitbox = parent.getHitBox().union(craft.getHitBox());
                                    parent.setHitBox(newHitbox);
                                }
                            }
                            CraftManager.getInstance().release(craft, CraftReleaseEvent.Reason.SUB_CRAFT, false);
                        }
                    }.runTaskLater(Movecraft.getInstance(), 2);
                }
        );
        event.setCancelled(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                rotating.remove(startPoint);
                if (playerCraft != null) playerCraft.setProcessing(false);
            }
        }.runTaskLater(Movecraft.getInstance(), 3);
    }
}
