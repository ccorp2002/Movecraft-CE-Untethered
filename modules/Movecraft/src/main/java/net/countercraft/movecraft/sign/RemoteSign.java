package net.countercraft.movecraft.sign;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;

public final class RemoteSign implements Listener{
    private static final String HEADER = "Remote Sign";
    private static final String NODE_HEADER = "[NODE]";
    private static final ItemStack AIR_STACK = new ItemStack(Material.AIR);

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public final void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) return;
        if (!event.getLine(0).equalsIgnoreCase(HEADER) && !event.getLine(0).equalsIgnoreCase(NODE_HEADER)) {
            return;
        }
        else if(event.getLine(1).equals("")) {
            event.getPlayer().sendMessage(ERROR_PREFIX + " Remote Sign - Cannot be blank; " + "Defaulting to '1'");
            event.setLine(1,"1");
            event.setLine(2,"");
            event.setLine(3,"");
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBookClick(@NotNull PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        String sid = "";
        if ((event.getPlayer().getInventory().getItemInMainHand()).getType() == Material.BOOK) {
            if ((event.getPlayer().getInventory().getItemInMainHand().hasItemMeta())) {
                if (((ItemMeta)event.getPlayer().getInventory().getItemInMainHand().getItemMeta()).hasDisplayName()) {
                    sid = (String)((ItemMeta)event.getPlayer().getInventory().getItemInMainHand().getItemMeta()).getDisplayName();
                }
            }
        } else if ((event.getPlayer().getInventory().getItemInOffHand()).getType() == Material.BOOK) {
            if ((event.getPlayer().getInventory().getItemInOffHand().hasItemMeta())) {
                if (((ItemMeta)event.getPlayer().getInventory().getItemInOffHand().getItemMeta()).hasDisplayName()) {
                    sid = (String)((ItemMeta)event.getPlayer().getInventory().getItemInOffHand().getItemMeta()).getDisplayName();
                }

            }
        }
        sid = ChatColor.stripColor(sid);
        if (sid.equalsIgnoreCase("")) {
            return;
        }
        BaseCraft foundCraft = null;
        for (PlayerCraft tcraft : CraftManager.getInstance().getPlayerCraftsInWorld(event.getPlayer().getWorld())) {
            if (MathUtils.locationNearHitBox(tcraft.getHitBox(), event.getPlayer().getLocation(), 15)) {
                // don't use a craft with a null player. This is
                // mostly to avoid trying to use subcrafts
                if (event.getPlayer() == null) continue;
                if (tcraft == null) continue;
                if (!((BaseCraft)tcraft).hasPassenger(event.getPlayer())) continue;
                foundCraft = (BaseCraft)tcraft;
                break;
            }
        }
        if (foundCraft != null) {
            World craftWorld = foundCraft.getWorld();
            event.setCancelled(true);
            for (Block b : (foundCraft).getBlockName("SIGN")) {
                if ((ChatColor.stripColor(((Sign)b.getState()).getLine(0)).equalsIgnoreCase(HEADER) || ChatColor.stripColor(((Sign)b.getState()).getLine(0)).equalsIgnoreCase(NODE_HEADER))) continue;
                if (isForbidden((Sign)b.getState())) continue;
                if (isEqualSignIgnoreHeader((Sign)b.getState(), sid)) {
                    PlayerInteractEvent newEvent = new PlayerInteractEvent(event.getPlayer(), event.getAction(), AIR_STACK, b, event.getBlockFace());
                    Bukkit.getServer().getPluginManager().callEvent(newEvent);
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignClick(@NotNull PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (event.getItem() == null) return;
        if (event.getItem().getType() == Material.AIR) return;
        if (event.getItem().getType() == Material.BOOK) return;
        BlockState state = event.getClickedBlock().getState();
        if (!(state instanceof Sign)) {
            return;
        }
        Sign sign = (Sign) state;
        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(HEADER) && !ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(NODE_HEADER)) {
            return;
        }
        BaseCraft foundCraft = null;
        for (PlayerCraft tcraft : CraftManager.getInstance().getPlayerCraftsInWorld(event.getClickedBlock().getWorld())) {
            if (MathUtils.locationInHitBox(tcraft.getHitBox(), event.getClickedBlock().getLocation())) {
                // don't use a craft with a null player. This is
                // mostly to avoid trying to use subcrafts
                foundCraft = (BaseCraft)tcraft;
                break;
            }
        }

        if (foundCraft == null) {
            event.getPlayer().sendMessage(ERROR_PREFIX+I18nSupport.getInternationalisedString("Remote Sign - Must be a part of a piloted craft"));
            return;
        }

        if (!foundCraft.getType().getBoolProperty(CraftType.ALLOW_REMOTE_SIGN)) {
            event.getPlayer().sendMessage(ERROR_PREFIX + I18nSupport.getInternationalisedString("Remote Sign - Not allowed on this craft"));
            return;
        }

        String targetText = ChatColor.stripColor(sign.getLine(1));
        if(targetText.equalsIgnoreCase(HEADER) || targetText.equalsIgnoreCase(NODE_HEADER) ) {
            event.getPlayer().sendMessage(ERROR_PREFIX+I18nSupport.getInternationalisedString("Remote Sign - Cannot remote another Remote Sign"));
            return;
        }

        if(targetText.equalsIgnoreCase("")) {
            event.getPlayer().sendMessage("Remote Sign - Cannot be blank");
            return;
        }
        int counter = 0;
        for (Block b : (foundCraft).getBlockName("SIGN")) {
            BlockState tstate = b.getState();
            if (!(tstate instanceof Sign)) {
                continue;
            }
            Sign ts = (Sign) tstate;
            if (counter >= Settings.MaxRemoteSigns && Settings.MaxRemoteSigns < 0) continue;
            if (isEqualSign(ts, targetText)) {
                if (!isForbidden(ts)) {
                    counter++;
                    if (counter > 0) Movecraft.getInstance().getLogger().info("Remote Sign Found : "+targetText);
                    final PlayerInteractEvent newEvent = new PlayerInteractEvent(event.getPlayer(), event.getAction(), AIR_STACK, b, event.getBlockFace());
                    Bukkit.getServer().getPluginManager().callEvent(newEvent);
                }
            }
        }

    }
    private boolean isEqualSign(Sign test, String target) {
        return (!ChatColor.stripColor(test.getLine(0)).equalsIgnoreCase(NODE_HEADER) && !ChatColor.stripColor(test.getLine(0)).equalsIgnoreCase(HEADER)) && (ChatColor.stripColor(test.getLine(0)).equalsIgnoreCase(target)
                || ChatColor.stripColor(test.getLine(1)).equalsIgnoreCase(target)
                || ChatColor.stripColor(test.getLine(2)).equalsIgnoreCase(target)
                || ChatColor.stripColor(test.getLine(3)).equalsIgnoreCase(target) );
    }
    public boolean isEqualSignIgnoreHeader(Sign test, String target) {
        return (ChatColor.stripColor(test.getLine(0)).contains(target)
                || ChatColor.stripColor(test.getLine(1)).contains(target)
                || ChatColor.stripColor(test.getLine(2)).contains(target)
                || ChatColor.stripColor(test.getLine(3)).contains(target));
    }
    public boolean isForbidden(Sign test) {
        for (int i = 0; i < 4; i++) {
            String t = ChatColor.stripColor(test.getLine(i)).toLowerCase();
            if(Settings.ForbiddenRemoteSigns.contains(t))
                return true;
        }
        return false;
    }
}
