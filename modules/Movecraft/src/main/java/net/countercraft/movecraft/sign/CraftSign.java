package net.countercraft.movecraft.sign;

import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CruiseOnPilotCraft;
import net.countercraft.movecraft.craft.CruiseOnPilotSubCraft;
import net.countercraft.movecraft.craft.PlayerCraftImpl;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.functions.Result;
import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.util.hitboxes.MutableHitBox;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

import java.util.HashSet;
import java.util.Set;

public final class CraftSign implements Listener {
    private final Set<MovecraftLocation> piloting = new HashSet<>();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignChange(@NotNull SignChangeEvent event) {
        if (CraftManager.getInstance().getCraftTypeFromString(event.getLine(0)) == null)
            return;

        if (!Settings.RequireCreatePerm)
            return;

        if (!event.getPlayer().hasPermission("movecraft." + ChatColor.stripColor(event.getLine(0)) + ".create")) {
            event.getPlayer().sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            //event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignClick(@NotNull PlayerInteractEvent event) {
        if (event.isCancelled())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null)
            return;

        BlockState state = event.getClickedBlock().getState();
        if (!(state instanceof Sign))
            return;

        Sign sign = (Sign) state;
        CraftType craftType = CraftManager.getInstance().getCraftTypeFromString(ChatColor.stripColor(sign.getLine(0)));
        if (craftType == null)
            return;
        try {
        sign.setWaxed(true);
        } catch (Exception exc) {
        sign.setEditable(false);
        }
        sign.update(true,false);

        // Valid sign prompt for ship command.
        Player player = event.getPlayer();
        if (!player.hasPermission("movecraft." + ChatColor.stripColor(sign.getLine(0)) + ".pilot")) {
            player.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return;
        }

        Location loc = event.getClickedBlock().getLocation();
        MovecraftLocation startPoint = new MovecraftLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (piloting.contains(startPoint)) {
            //event.setCancelled(true);
            return;
        }

        final Craft pCraft = CraftManager.getInstance().getCraftByPlayerName(player.getName());
        if (pCraft != null) {
            if (!craftType.getBoolProperty(CraftType.CRUISE_ON_PILOT) && !(ChatColor.stripColor(sign.getLine(0)).toLowerCase().contains("torpedo"))) {
                player.sendMessage(MOVECRAFT_COMMAND_PREFIX + "You must release your previous Craft");
                return;
                //player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Player- Error - You do not have a craft to release!"));
            }
        }
        // Attempt to run detection
        World world = event.getClickedBlock().getWorld();
        CraftManager.getInstance().detect(
                startPoint,
                craftType, (type, w, p, parents) -> {
                    if (type.getBoolProperty(CraftType.CRUISE_ON_PILOT)) {
                        if (parents.size() > 1)
                            return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                    "Detection - Failed - Already commanding a craft")), null);
                        if (parents.size() == 1) {
                            Craft parent = parents.iterator().next();
                            return new Pair<>(Result.succeed(),
                                    new CruiseOnPilotSubCraft(type, world, p, parent));
                        }

                        return new Pair<>(Result.succeed(),
                                new CruiseOnPilotCraft(type, world, p));
                    }
                    else {
                        if (parents.size() > 0)
                            return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                    "Detection - Failed - Already commanding a craft")), null);

                        return new Pair<>(Result.succeed(),
                                new PlayerCraftImpl(type, w, p));
                    }
                },
                world, player,
                Movecraft.getAdventure().player(player),
                craft -> () -> {
                    //Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
                    if (craft instanceof SubCraft) { // Subtract craft from the parent
                        Craft parent = ((SubCraft) craft).getParent();
                        var newHitbox = parent.getHitBox().difference(craft.getHitBox());;
                        parent.setHitBox(newHitbox);
                        parent.setOrigBlockCount(parent.getOrigBlockCount() - craft.getHitBox().size());
                    }

                    if (craft.getType().getBoolProperty(CraftType.CRUISE_ON_PILOT)) {
                        // Setup cruise direction
                        if (sign.getBlockData() instanceof WallSign)
                            craft.setCruiseDirection(CruiseDirection.fromBlockFace(((WallSign) sign.getBlockData()).getFacing()));
                        else
                            craft.setCruiseDirection(CruiseDirection.NONE);

                        // Start craft cruising
                        craft.setLastCruiseUpdate(System.currentTimeMillis());
                        craft.setCruising(true);

                        // Stop craft cruising and sink it in 15 seconds
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                craft.setCruising(false);
                                CraftManager.getInstance().sink(craft);
                            }
                        }.runTaskLater(Movecraft.getInstance(), (20 * 60));
                    }
                    else {
                        // Release old craft if it exists
                        Craft oldCraft = CraftManager.getInstance().getCraftByPlayer(player);
                        if (oldCraft != null)
                            CraftManager.getInstance().forceRemoveCraft(oldCraft);
                    }
                }
        );
        event.setCancelled(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                piloting.remove(startPoint);
            }
        }.runTaskLater(Movecraft.getInstance(), 4);
    }
}
