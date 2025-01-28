package net.countercraft.movecraft.sign;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.SubCraftImpl;
import net.countercraft.movecraft.craft.SubcraftRotateCraft;
import net.countercraft.movecraft.craft.NPCCraftImpl;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.functions.Result;
import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.util.hitboxes.MutableHitBox;
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

public final class SubcraftRotateSign implements Listener {
    private static final String HEADER = "Subcraft Rotate";
    private final Set<MovecraftLocation> rotating = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onSignClick(@NotNull PlayerInteractEvent event) {
        MovecraftRotation rotation;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            rotation = MovecraftRotation.CLOCKWISE;
        else if (event.getAction() == Action.LEFT_CLICK_BLOCK)
            rotation = MovecraftRotation.ANTICLOCKWISE;
        else
            return;
        if (event.getPlayer().isSneaking())
            return;
        BlockState state = event.getClickedBlock().getState();
        if (!(state instanceof Sign))
            return;
        Sign sign = (Sign) state;
        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(HEADER))
            return;

        Location loc = event.getClickedBlock().getLocation();
        final MovecraftLocation startPoint = new MovecraftLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (rotating.contains(startPoint)) {
            event.getPlayer().sendMessage("You are already Rotating");
            event.setCancelled(true);
            return;
        }

        // rotate subcraft
        String craftTypeStr = ChatColor.stripColor(sign.getLine(1));
        CraftType craftType = CraftManager.getInstance().getCraftTypeFromString(craftTypeStr);
        if (craftType == null)
            return;
        if (ChatColor.stripColor(sign.getLine(2)).equals("")
                && ChatColor.stripColor(sign.getLine(3)).equals("")) {
            sign.setLine(2, "_\\ /_");
            sign.setLine(3, "/ \\");
            sign.update(false, false);
        }

        if (!event.getPlayer().hasPermission("movecraft." + craftTypeStr + ".pilot") || !event.getPlayer().hasPermission("movecraft." + craftTypeStr + ".rotate")) {
            event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return;
        }

        Craft pcraft = CraftManager.getInstance().getCraftByPlayer(event.getPlayer());
        if (pcraft != null) {
            if (!pcraft.isNotProcessing()) {
                event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Detection - Parent Craft is busy"));
                return;
            }
        }


        final Player player = event.getPlayer();
        final World world = event.getClickedBlock().getWorld();
        if (CraftManager.getInstance().getCraftFromBlock(event.getClickedBlock()) != null) {
            if (CraftManager.getInstance().getCraftFromBlock(event.getClickedBlock()) instanceof SubCraftImpl) {
                event.getPlayer().sendMessage("You are already rotating.");
                event.setCancelled(true);
                return;
            }
            if (pcraft == null) {
                pcraft = CraftManager.getInstance().getCraftFromBlock(event.getClickedBlock());
            }
            //if (!CraftManager.getInstance().getCraftFromBlock(event.getClickedBlock()).isNotProcessing()) {
            //    event.getPlayer().sendMessage("You are already Moving");
            //    event.setCancelled(true);
            //    return;
            //}
        }
        final Craft playerCraft = pcraft;
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
                    craft.setPassengers(parent.getPassengers());
                    craft.setParent(parent);
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
                    Movecraft.getInstance().getAsyncManager().submitTask(new RotationTask(craft, startPoint, rotation, craft.getWorld(), true), craft);
                    //craft.rotate(rotation, startPoint, true);

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
