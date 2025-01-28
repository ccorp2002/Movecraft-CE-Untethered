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

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.NPCCraftImpl;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraftImpl;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.type.RequiredBlockEntry;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftPreTranslateEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.events.PlayerCraftMovementEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.util.hitboxes.MutableHitBox;
import net.countercraft.movecraft.util.hitboxes.BitmapHitBox;
import net.countercraft.movecraft.util.hitboxes.BitMapSetHitBox;
import net.countercraft.movecraft.util.hitboxes.SetHitBox;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
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
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
//import com.jeff_media.customblockdata.*;
//import com.jeff_media.morepersistentdatatypes.*;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;


public class PlayerListener implements Listener {
    private final Map<Craft, Long> timeToReleaseAfter = new WeakHashMap<>();
    public Set<Location> checkCraftBorders(Craft craft) {
        Set<Location> mergePoints = new HashSet<>();
        final EnumSet<Material> ALLOWED_BLOCKS = craft.getType().getMaterialSetProperty(CraftType.ALLOWED_BLOCKS);
        final EnumSet<Material> FORBIDDEN_BLOCKS = craft.getType().getMaterialSetProperty(CraftType.FORBIDDEN_BLOCKS);
        final MovecraftLocation[] SHIFTS = {
                //x
                new MovecraftLocation(-1, 0, 0),
                new MovecraftLocation(-1, -1, 0),
                new MovecraftLocation(-1,1,0),
                new MovecraftLocation(1, -1, 0),
                new MovecraftLocation(1, 1, 0),
                new MovecraftLocation(1, 0, 0),
                //z
                new MovecraftLocation(0, 1, 1),
                new MovecraftLocation(0, 0, 1),
                new MovecraftLocation(0, -1, 1),
                new MovecraftLocation(0, 1, -1),
                new MovecraftLocation(0, 0, -1),
                new MovecraftLocation(0, -1, -1),
                //y
                new MovecraftLocation(0, 1, 0),
                new MovecraftLocation(0, -1, 0)};
        //Check each location in the hitbox
        for (MovecraftLocation ml : craft.getHitBox()){
            //Check the surroundings of each location
            for (MovecraftLocation shift : SHIFTS){
                MovecraftLocation test = ml.add(shift);
                //Ignore locations contained in the craft's hitbox
                if (craft.getHitBox().contains(test)){
                    continue;
                }
                Block testBlock = test.toBukkit(craft.getWorld()).getBlock();
                Material testMaterial = testBlock.getType();
                //Break the loop if an allowed block is found adjacent = the craft's hitbox
                if (ALLOWED_BLOCKS.contains(testMaterial)){
                    mergePoints.add(testBlock.getLocation());
                }
                //Do the same if a forbidden block is found
                else if (FORBIDDEN_BLOCKS.contains(testMaterial)){
                    mergePoints.add(testBlock.getLocation());
                }
            }
        }
        //Return the string representation of the merging point and alert the pilot
        return mergePoints;
    }

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent e) {}
    
    @EventHandler
    public void onCraftRotate(CraftRotateEvent e) {}

    @EventHandler
    public void onCraftPreTranslate(CraftPreTranslateEvent e) {}

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCraftRelease(CraftReleaseEvent e) {
        if (e.isCancelled()) return;
        Craft crft = e.getCraft();
        if (crft == null) return;
        if (crft instanceof BaseCraft) {
          final BaseCraft craft = (BaseCraft)crft;
          if (craft.getOrigBlockCount() < 1000000) {
            for (Block block : craft.getBlockName("SIGN")) {
              Sign sign = (Sign)block.getState();
              if (ChatColor.stripColor(sign.getLine(0).toLowerCase()).contains(craft.getType().getName().toLowerCase())) continue;
              if (ChatColor.stripColor(sign.getLine(0).toLowerCase()).contains("pilot:")) continue;
              if (ChatColor.stripColor(sign.getLine(0).toLowerCase()).contains("[private]")) continue;
              if (ChatColor.stripColor(sign.getLine(0).toLowerCase()).contains("private:")) continue;
              if (ChatColor.stripColor(sign.getLine(0).toLowerCase()).contains("[node]")) continue;
              if (ChatColor.stripColor(sign.getLine(0).toLowerCase()).contains("remote sign")) continue;
              if (ChatColor.stripColor(sign.getLine(0).toLowerCase()).contains("subcraft")) continue;
              try {
                sign.setWaxed(false);
              } catch (Exception exc) {
                sign.setEditable(true);
              }
              sign.update();
            }
          }
          new BukkitRunnable() {
            @Override
            public void run() {
              craft.getRawTrackedMap().clear();
              craft.getCraftTags().clear();
            }
          }.runTaskLater(Movecraft.getInstance(), 5*20);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCraftDetectEvent(final CraftDetectEvent e) {
      final Craft craft = e.getCraft();
      craft.setLastBlockCheck(System.currentTimeMillis());
      if (craft.getOrigBlockCount() > craft.getType().getMaxSize() && !(e.isCancelled())) {
          e.setFailMessage(String.format(I18nSupport.getInternationalisedString("Detection - Craft too large"), craft.getType().getIntProperty(CraftType.MAX_SIZE))+" Current Size: "+craft.getOrigBlockCount());
          CraftManager.getInstance().forceRemoveCraft(craft);
          e.setCancelled(true);
          return;
      }
      if (craft.getOrigBlockCount() < craft.getType().getMinSize() && !(e.isCancelled())) {
          e.setFailMessage(String.format(I18nSupport.getInternationalisedString("Detection - Craft too small"), craft.getType().getIntProperty(CraftType.MIN_SIZE))+" Current Size: "+craft.getOrigBlockCount());
          CraftManager.getInstance().forceRemoveCraft(craft);
          e.setCancelled(true);
          return;
      }
      CraftManager.getInstance().detectCraftHealthBlocks(craft);
      if (CraftManager.getInstance().detect_lift_sinking(craft)) {
        e.setFailMessage("Too few Lift/Fly Blocks! \nCraft must be composed of "+CraftManager.getInstance().get_lift_required(craft)+"% Lift/Fly Blocks");
        CraftManager.getInstance().forceRemoveCraft(craft);
        craft.getAudience().sendMessage(Component.text(e.getFailMessage()));
        e.setCancelled(true);
      }
      if (e.isCancelled()) {
        CraftManager.getInstance().forceRemoveCraft(craft);
        return;
      }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCraftPilotEvent(final CraftPilotEvent e) {
      if (e.getCraft() instanceof SubCraft) return;
      if (e.getReason() == CraftPilotEvent.Reason.SUB_CRAFT) return;
      e.getCraft().setLastBlockCheck(System.currentTimeMillis());
      if (e.getCraft() instanceof final BaseCraft craft) {
        craft.setDataTag("radar_range",(Double)125d);
        craft.setDataTag("radar_profile",(Double)360d);
        craft.setProcessing(true);
        CraftManager.getInstance().detectCraftHealthBlocks(craft);
        if (craft instanceof NPCCraftImpl) return;
        final Movecraft instance = Movecraft.getInstance();
        final Player player = craft.getNotificationPlayer();
        if (player != null) craft.addPassenger(player);
        craft.setDataTag("idle",false);
        craft.updateLastMoveTime();
        for (ItemStack stack : CraftManager.getInstance().fuelTypeMap.keySet()) {
          CraftManager.getInstance().getAndTrackItemsOnCraft(craft,stack,null);
        }
        if (craft.getOrigBlockCount() < 950000*2) {
          final int waterline = craft.getWaterLine();
          final Location midpoint = craft.getHitBox().getMidPoint().toBukkit(craft.getWorld());
          craft.setProcessing(true);
          new BukkitRunnable() {
            @Override
            public void run() {
              HitBox interior = CraftManager.getInstance().detectCraftInterior(craft);
              CruiseDirection cdir = CruiseDirection.NONE;
              craft.setCruiseDirection(cdir);
              for (Block block : craft.getBlockName("SIGN")) {
                Sign sign = (Sign)block.getState();
                try {
                  sign.setEditable(false);
                } catch (Exception exc) {
                }
                if (ChatColor.stripColor(sign.getLine(0).toLowerCase()).contains("cruise:")) {
                  if (!(sign.getBlockData() instanceof WallSign)) continue;
                  craft.setCruiseDirection(CruiseDirection.fromBlockFace(((WallSign) sign.getBlockData()).getFacing()));
                }
                sign.update();
              }
              if (cdir == CruiseDirection.NONE) {
                  if (player != null) cdir = (CruiseDirection.fromBlockFace(player.getFacing().getOppositeFace()));
              }
              for (MovecraftLocation location : craft.getHitBox()) {
                  if (location.getY() <= waterline) {
                      craft.getPhaseBlocks().put(location.toBukkit(craft.getWorld()), Movecraft.getInstance().getWaterBlockData());
                  }
              }
              craft.updateLastMoveTime();
              instance.getLogger().info(" Craft ("+craft+")'s Waterlevel: "+waterline);
              if (player != null) instance.getLogger().info(player.getName()+"'s Craft ("+craft+") Interior Hitbox Size: "+interior.size());
              else instance.getLogger().info("NULL's Craft ("+craft+") Interior Hitbox Size: "+interior.size());
              craft.updateLastMoveTime();
              craft.setProcessing(false);
            }
          }.runTaskLater(instance, 20*1);
          instance.getWorldHandler().processLight(craft.getHitBox(),craft.getWorld());
          craft.updateLastMoveTime();
          Set<Entity> nearEntites = new HashSet<>();
          nearEntites.addAll(craft.getWorld().getNearbyEntities(midpoint,
                  craft.getHitBox().getXLength() / 2.0 + 1.5,
                  craft.getHitBox().getYLength() / 1.75 + 1.5,
                  craft.getHitBox().getZLength() / 2.0 + 1.5));
          nearEntites.addAll(((BaseCraft)craft).getPassengers());
          craft.updateLastMoveTime();
          for (Craft c2 : CraftManager.getInstance().getCraftsInWorld(craft.getWorld())) {
            if (c2.equals(craft))
              continue;
            nearEntites.removeAll(((BaseCraft)c2).getPassengers());
          }
          for (Entity entity : nearEntites) {
              if (entity == null) continue;
              if (!(MathUtils.locationNearHitBox(craft.getHitBox(),entity.getLocation(),2))) {
                if (entity.getType() == EntityType.PLAYER) (craft).removePassenger(entity);
                if (entity instanceof Display) (craft).removePassenger(entity);
                continue;
              }
              (craft).addPassenger(entity);
          }
          if (player != null) craft.addPassenger(player);
        }
      }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent e) {
        try {
          CraftManager.getInstance().forceRemoveCraft(CraftManager.getInstance().getCraftByPlayer(e.getPlayer()));
        } catch (Exception ex) {}
    }

    @EventHandler
    public void onPlayerDeath(EntityDamageByEntityEvent e) {}

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Player p = event.getPlayer();
    PlayerCraft c = CraftManager.getInstance().getCraftByPlayer(p);
    Craft craft = c;
    if (c == null)
      return;
    int dx, dy, dz;
    dx = dy = dz = 0;
    if (MathUtils.locationNearHitBox(c.getHitBox(), p.getLocation(),2.0D)) {
      //this.timeToReleaseAfter.remove(c);
      if (!(CraftManager.getInstance().getCraftsInWorld(p.getWorld()).contains(c))) {
        return;
      }
      if (c.getPilotLocked()) {
        CraftType type = c.getType();
        Vector from = event.getFrom().toVector();
        Vector to = event.getTo().toVector();
        Location loc = event.getTo();
        Location toloc = loc.clone();
        Location fromloc = event.getFrom();

        if (!p.hasPermission("movecraft." + craft.getType().getStringProperty(CraftType.NAME) + ".move")) {
            p.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return; // Player doesn't have permission = move this craft, so don't do anything
        }

        int tickCooldown = (int) craft.getType().getPerWorldProperty(
                CraftType.PER_WORLD_TICK_COOLDOWN, craft.getWorld());
        if (type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_DIRECT_MOVEMENT)
                && type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN))
            tickCooldown *= c.getCurrentGear(); // Account for gear shifts
        Long lastTime = craft.getLastMoveTime();
        Location newloc = new Location(fromloc.getWorld(), fromloc.getBlockX()+0.5,fromloc.getY(),fromloc.getBlockZ()+0.5);
        newloc.setYaw(toloc.getYaw());
        newloc.setPitch(toloc.getPitch());
        if (lastTime != null) {
            long ticksElapsed = (System.currentTimeMillis() - lastTime) / 50;

            // if the craft should go slower underwater, make time pass more slowly there
            if (craft.getType().getBoolProperty(CraftType.HALF_SPEED_UNDERWATER)
                    && craft.getHitBox().getMinY() < craft.getWorld().getSeaLevel())
                ticksElapsed /= 2;

              event.setTo(newloc);
            if (ticksElapsed < tickCooldown)
                return; // Not enough time has passed, so don't do anything
        }
        if (to.getX() - from.getX() != 0.0D || to.getZ() - from.getZ() != 0.0D || to.getY() - from.getY() != 0.0D) {
          dx = (int)Math.signum((Math.abs(to.getX() - from.getX()) > 0.07D) ? (to.getX() - from.getX()) : 0.0D);
          dz = (int)Math.signum((Math.abs(to.getZ() - from.getZ()) > 0.07D) ? (to.getZ() - from.getZ()) : 0.0D);
          int gearShifts = type.getIntProperty(CraftType.GEAR_SHIFTS);
          int currentGear = craft.getCurrentGear();
          if (((p.isSneaking() && c.getPilotLocked()) || (c.getPilotLocked() && c.getCruising())) && c.getHitBox().size() <= 65000) {
            int playerDir[] = InteractListener.getCardinalDirection(p);
            CruiseDirection mcdir = CruiseDirection.NONE;
            CruiseDirection mcdirb = CruiseDirection.NONE;
            if (dx != 0 && dx <= -1) {
              mcdir = CruiseDirection.WEST;
            } else if (dx != 0 && dx >= 1) {
              mcdir = CruiseDirection.EAST;
            }
            if (dz != 0 && dz <= -1) {
              mcdir = CruiseDirection.NORTH;
            } else if (dz != 0 && dz >= 1) {
              mcdir = CruiseDirection.SOUTH;
            }
            if (playerDir[0] != 0 && playerDir[0] <= -1) {
              mcdirb = CruiseDirection.WEST;
            } else if (playerDir[0] != 0 && playerDir[0] >= 1) {
              mcdirb = CruiseDirection.EAST;
            }
            if (playerDir[2] != 0 && playerDir[2] <= -1) {
              mcdirb = CruiseDirection.NORTH;
            } else if (playerDir[2] != 0 && playerDir[2] >= 1) {
              mcdirb = CruiseDirection.SOUTH;
            }
            if (CruiseDirection.isClockwise(mcdir,mcdirb)) {
              // Handle shift right clicks (when not in direct control mode)
              craft.rotate(MovecraftRotation.CLOCKWISE,craft.getHitBox().getMidPoint(),false);
              return;
            } else if (CruiseDirection.isAntiClockwise(mcdir,mcdirb)) {
              // Handle shift right clicks (when not in direct control mode)
              craft.rotate(MovecraftRotation.ANTICLOCKWISE,craft.getHitBox().getMidPoint(),false);
              return;
            }
          }
          if (c.getCurrentGear() > 1) {
            dx *= c.getCurrentGear();
            dz *= c.getCurrentGear();
          }
          if (dx != 0 || dz != 0) {
            PlayerCraftMovementEvent pcme = new PlayerCraftMovementEvent(c,dx,dy,dz);
            Bukkit.getPluginManager().callEvent(pcme);
            if (pcme.isCancelled()) return;
            newloc.setYaw(p.getLocation().getYaw());
            newloc.setPitch(p.getLocation().getPitch());
            //event.setTo(newloc);
            p.teleport(newloc,org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN,io.papermc.paper.entity.TeleportFlag.Relative.values());
            craft.setLastCruiseUpdate(System.currentTimeMillis());
            c.translate(dx, 0, dz);
          }
          return;
        }
      }
    }
    if (!MathUtils.locationNearHitBox((c.getHitBox()),p.getLocation(),15.0D)) {
        if (Settings.ManOverboardTimeout != 0) {
          p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("Manoverboard - Player has left craft"));
          CraftManager.getInstance().addOverboard(p);
        } else {
          p.sendMessage(I18nSupport.getInternationalisedString("Release - Player has left craft"));
          try {
            CraftManager.getInstance().forceRemoveCraft(CraftManager.getInstance().getCraftByPlayer(event.getPlayer()));
          } catch (Exception ex) {}
        }
        return;
    }
  }
}