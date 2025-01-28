/*
 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.countercraft.movecraft.listener;

import com.google.common.collect.*;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.*;
import net.countercraft.movecraft.events.CraftBlockChangeEvent;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.Tags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.material.Attachable;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onBlockChange(@NotNull CraftBlockChangeEvent e) {
        if (e.isCancelled()) {
            if (!e.getState().getType().isSolid()) {
                e.setAdding(false);
            } else {
                e.setAdding(true);
            }
        }
        if (e.isAdding()) {
            e.getState().update(true,false);
            e.getCraft().addBlock(e.getState().getBlock());
        } else {
            e.getState().getBlock().setType(Material.AIR,false);
            e.getCraft().removeBlock(e.getState().getBlock());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent e) {
        if (!Settings.ProtectPilotedCrafts)
            return;

        if (e.getBlock().getType() == Material.FIRE)
            return; // allow players to punch out fire

        MovecraftLocation movecraftLocation = MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation());
        for (Craft craft : CraftManager.getInstance().getCraftsInWorld(e.getBlock().getWorld())) {
            if (craft == null || craft.getDisabled())
                continue;

            if (craft.getHitBox().contains(movecraftLocation)) {
                // TODO: for some reason before when this check runs the location is no longer in the hitbox
                //if (craft.getPassengers().contains(e.getPlayer())) {
                //  return;
                //}
                e.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(@NotNull BlockPlaceEvent e) {
        if (!Settings.ProtectPilotedCrafts)
            return;

        MovecraftLocation movecraftLocation = MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation());
        for (Craft craft : CraftManager.getInstance().getCraftsInWorld(e.getBlock().getWorld())) {
            if (craft == null || craft.getDisabled())
                continue;

            if (MathUtils.locIsNearCraftFast(craft,movecraftLocation)) {
                // TODO: for some reason before when this check runs the location is no longer in the hitbox
                //if (craft.getPassengers().contains(e.getPlayer())) {
                //  return;
                //}
                e.setCancelled(true);
                return;
            }
        }
    }


    public static boolean willDoBlockDamage(final Block block, int armorBonus, int hullBonus) {
        final java.util.Random rand = CraftManager.getInstance().rand();
        int survChance = (int)CraftManager.getInstance().getArmorChance(block);
        int roll = rand.nextInt(100)+1;
        if ((block.getType().getBlastResistance()) >= 75) return false;
        if (CraftManager.getInstance().isArmorBlock(block)) {
            survChance += armorBonus;
        } else {
            survChance += 25;
            survChance += hullBonus;
        }
        return (roll > survChance);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(@NotNull BlockExplodeEvent e) {
        final ArrayList<Block> blocks = Lists.newArrayList(e.blockList());
        Craft craft = null;
        for (Block block : blocks) {
            craft = CraftManager.getInstance().getCraftFromBlock(block);
            if (craft == null) continue;
            if (craft instanceof SinkingCraft) continue;
            craft.removeBlock(block);
            craft.updateLastMoveTime();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent e) {
        final ArrayList<Block> blocks = Lists.newArrayList(e.blockList());
        Craft craft = null;
        for (Block block : blocks) {
            craft = CraftManager.getInstance().getCraftFromBlock(block);
            if (craft == null) continue;
            if (craft instanceof SinkingCraft) continue;

            craft.removeBlock(block);
            craft.updateLastMoveTime();
        }
    }

    public static void tryRemoveBlock(final MovecraftLocation movecraftLocation, final Craft craft) {
        final Block block = movecraftLocation.toBukkit(craft.getWorld()).getBlock();
        if (CraftManager.getInstance().checkArmorBlock(block)) return;
        final Material type = block.getType();
        craft.updateLastMoveTime();
        if (craft.getHitBox().boundingHitBox().contains(movecraftLocation)) {
            Location location = movecraftLocation.toBukkit(block.getWorld());
            BlockData phaseBlock = craft.getPhaseBlocks().getOrDefault(location, null);
            if (phaseBlock == null) phaseBlock = Movecraft.getInstance().AirBlockData;
            if (location.getBlock().getType() != type) {
                Movecraft.getInstance().getWorldHandler().setBlockFast(location, phaseBlock);
                craft.removeBlock(location.getBlock());
            }
            if (Tags.FLUID.contains(location.getBlock().getType())) {
                Movecraft.getInstance().getWorldHandler().setBlockFast(location, phaseBlock);
                craft.removeBlock(location.getBlock());
            }

        }
    }

    public static void tryRemoveBlock(final Block block, final Craft craft) {
        final MovecraftLocation movecraftLocation = MathUtils.bukkit2MovecraftLoc(block.getLocation());
        if (CraftManager.getInstance().checkArmorBlock(block)) return;
        final Material type = block.getType();
        craft.updateLastMoveTime();
        if (craft.getHitBox().boundingHitBox().contains(movecraftLocation)) {
            Location location = movecraftLocation.toBukkit(block.getWorld());
            BlockData phaseBlock = craft.getPhaseBlocks().getOrDefault(location, null);
            if (phaseBlock == null) phaseBlock = Movecraft.getInstance().AirBlockData;
            if (location.getBlock().getType() != type) {
                Movecraft.getInstance().getWorldHandler().setBlockFast(location, phaseBlock);
                craft.removeBlock(location.getBlock());
            }
            if (Tags.FLUID.contains(location.getBlock().getType())) {
                Movecraft.getInstance().getWorldHandler().setBlockFast(location, phaseBlock);
                craft.removeBlock(location.getBlock());
            }

        }
    }

    // prevent items from dropping from moving crafts
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemSpawn(final ItemSpawnEvent e) {
        if (e.isCancelled()) {
            return;
        }
        for (Craft tcraft : CraftManager.getInstance().getCraftsInWorld(e.getLocation().getWorld())) {
            if ((!tcraft.isNotProcessing()) && MathUtils.locationInHitBox(tcraft.getHitBox(), e.getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    // prevent water and lava from spreading on moving crafts
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Block block = e.getToBlock();
        if (block.getType() != Material.WATER && block.getType() != Material.LAVA) {
            return;
        }
        for (Craft tcraft : CraftManager.getInstance().getCraftsInWorld(block.getWorld())) {
            if ((!tcraft.isNotProcessing()) && MathUtils.locIsNearCraftFast(tcraft, MathUtils.bukkit2MovecraftLoc(block.getLocation()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    // process certain redstone on cruising crafts
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRedstoneEvent(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        CraftManager.getInstance().getCraftsInWorld(block.getWorld());
        for (Craft tcraft : CraftManager.getInstance().getCraftsInWorld(block.getWorld())) {
            MovecraftLocation mloc = new MovecraftLocation(block.getX(), block.getY(), block.getZ());
            if (MathUtils.locIsNearCraftFast(tcraft, mloc) &&
                    tcraft.getCruising() && (block.getType() == Material.STICKY_PISTON ||
                    block.getType() == Material.PISTON || block.getType() == Material.DISPENSER &&
                    !tcraft.isNotProcessing())) {
                event.setNewCurrent(event.getOldCurrent()); // don't allow piston movement on cruising crafts
                return;
            }
        }
    }

    // prevent pistons on cruising crafts
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonEvent(BlockPistonExtendEvent event) {
        Block block = event.getBlock();
        Craft tcraft = CraftManager.getInstance().getCraftFromBlock(block);
        MovecraftLocation mloc = MathUtils.bukkit2MovecraftLoc(block.getLocation());
        if (MathUtils.locIsNearCraftFast(tcraft, mloc) && tcraft.getCruising() && !tcraft.isNotProcessing()) {
            event.setCancelled(true);
            return;
        }
    }

    // prevent hoppers on cruising crafts
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHopperEvent(InventoryMoveItemEvent event) {
        if (!(event.getSource().getHolder() instanceof Hopper)) {
            return;
        }
        Hopper block = (Hopper) event.getSource().getHolder();
        Craft tcraft = CraftManager.getInstance().getCraftFromBlock(block.getBlock());
        MovecraftLocation mloc = MathUtils.bukkit2MovecraftLoc(block.getLocation());
        if (MathUtils.locIsNearCraftFast(tcraft, mloc) && tcraft.getCruising() && !tcraft.isNotProcessing()) {
            event.setCancelled(true);
            return;
        }
    }

    // prevent fragile items from dropping on cruising crafts
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Block block = event.getBlock();

        CraftManager.getInstance().getCraftsInWorld(block.getWorld());
        for (Craft tcraft : CraftManager.getInstance().getCraftsInWorld(block.getWorld())) {
            MovecraftLocation mloc = new MovecraftLocation(block.getX(), block.getY(), block.getZ());
            if (!MathUtils.locIsNearCraftFast(tcraft, mloc)) {
                continue;
            }
            if(Tags.FRAGILE_MATERIALS.contains(event.getBlock().getType())) {
                BlockData m = block.getBlockData();
                BlockFace face = BlockFace.DOWN;
                boolean faceAlwaysDown = block.getType() == Material.COMPARATOR || block.getType() == Material.REPEATER;
                if (m instanceof Attachable && !faceAlwaysDown)
                    face = ((Attachable) m).getAttachedFace();
                if (!event.getBlock().getRelative(face).getType().isSolid()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDispense(BlockDispenseEvent e) {
        CraftManager.getInstance().getCraftsInWorld(e.getBlock().getWorld());
        for (Craft craft : CraftManager.getInstance().getCraftsInWorld(e.getBlock().getWorld())) {
            if (craft != null &&
                    !craft.isNotProcessing() &&
                    MathUtils.locIsNearCraftFast(craft, MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onFlow(BlockFromToEvent e){
        if(Settings.DisableSpillProtection)
            return;
        if(!e.getBlock().isLiquid())
            return;
        MovecraftLocation loc = MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation());
        MovecraftLocation toLoc = MathUtils.bukkit2MovecraftLoc(e.getToBlock().getLocation());
        for(Craft craft : CraftManager.getInstance().getCraftsInWorld(e.getBlock().getWorld())){
            if(craft.getHitBox().contains((loc)) && !craft.getFluidLocations().contains(toLoc)) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onIceForm(BlockFormEvent e) {
        if (e.isCancelled() || !Settings.DisableIceForm) {
            return;
        }
        if(e.getBlock().getType() != Material.WATER)
            return;
        MovecraftLocation loc = MathUtils.bukkit2MovecraftLoc(e.getBlock().getLocation());
        Craft craft = CraftManager.getInstance().fastNearestCraftToLoc(e.getBlock().getLocation());
        if (craft != null && craft.getHitBox().contains((loc))) {
            e.setCancelled(true);
        }
    }
}
