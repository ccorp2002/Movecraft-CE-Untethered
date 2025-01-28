package net.countercraft.movecraft.util.pathfinding;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.*;


import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.events.CraftPathfindEvent;
import net.countercraft.movecraft.craft.PlayerCraftImpl;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.NPCCraftImpl;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.MovecraftChunk;
import net.countercraft.movecraft.WorldHandler;
import net.countercraft.movecraft.processing.CachedMovecraftWorld;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.processing.WorldManager;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.DirectionalUtils;
import net.countercraft.movecraft.config.Settings;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.BlockFace;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;


public final class RunPathfinder
{
  private static final Random random = new Random();
  protected static final WorldHandler handler = Movecraft.getInstance().getWorldHandler();
  
  public static World getWorldOffMain(Craft craft) {
    World badWorld = null;
    if (!Bukkit.isPrimaryThread()) {
      try {
        badWorld = craft.getMovecraftWorld().getWorld();
      } catch (Exception exc) {
        badWorld = craft.getWorld();
      }
    } else {
      badWorld = craft.getWorld();
    }
    if (badWorld == null) badWorld = craft.getWorld();
    return badWorld;
  }

  public static boolean directionalCraftMovement(final Craft craft, String direct, boolean inverse) {
    if (direct == null) return false;
    if (craft == null) return false;
    direct = direct.toLowerCase();
    int x = 0;
    int y = 0;
    int z = 0;
    if (direct.contains(",")) {
      for (String s : direct.split(",")) {
        if (s.contains("east")) x += 1*craft.getCurrentGear();
        else if (s.contains("west")) x -= 1*craft.getCurrentGear();
        if (s.contains("south")) z += 1*craft.getCurrentGear();
        else if (s.contains("north")) z -= 1*craft.getCurrentGear();
        if (s.contains("up")) y += 1*craft.getCurrentGear();
        else if (s.contains("down")) y -= 1*craft.getCurrentGear();
        else continue;

      }
    } else if (direct.contains("_")) {
      for (String s : direct.split("_")) {
        if (s.contains("east")) x += 1*craft.getCurrentGear();
        else if (s.contains("west")) x -= 1*craft.getCurrentGear();
        if (s.contains("south")) z += 1*craft.getCurrentGear();
        else if (s.contains("north")) z -= 1*craft.getCurrentGear();
        if (s.contains("up")) y += 1*craft.getCurrentGear();
        else if (s.contains("down")) y -= 1*craft.getCurrentGear();
        else continue;

      }
    } else {
      if (direct.contains("east")) x += 1*craft.getCurrentGear();
      if (direct.contains("west")) x -= 1*craft.getCurrentGear();
      if (direct.contains("south")) z += 1*craft.getCurrentGear();
      if (direct.contains("north")) z -= 1*craft.getCurrentGear();
      if (direct.contains("up")) y += 1*craft.getCurrentGear();
      if (direct.contains("down")) y -= 1*craft.getCurrentGear();
    }
    return directionalCraftMovement(craft,(new Vector(x,y,z)),inverse);
  }
  public static boolean directionalCraftMovement(final Craft craft, Vector direct, boolean inverse) {
    if (direct == null) return false;
    if (craft == null) return false;
    int x = (int)Math.floor(direct.getX())*craft.getCurrentGear();
    int y = (int)Math.floor(direct.getY())*craft.getCurrentGear();
    int z = (int)Math.floor(direct.getZ())*craft.getCurrentGear();
    if (inverse) {
      x *= -1;
      y *= -1;
      z *= -1;
    }
    //int length = MathUtils.getBoxLength(craft.getHitBox());
    if (craft.getBottomCenter().getY()+y >= 750) y = 0;
    else if (craft.getBottomCenter().getY()+y <= 250) y = 0;
    craft.translate(x,y,z);
    craft.setCurrentGear(1);
    return true;
  }

  public static void rotateCraftTowardsPoint(final Craft craft, MovecraftLocation point, boolean inverse) {
    if (craft == null) return;
    if (point == null) return;
    point.setY(craft.getBottomCenter().getY());
    if (craft.getBottomCenter().distance(point) >= 500) return;
    if (craft.getBottomCenter().distance(point) <= 50) return;
    if (!CraftManager.getInstance().isCraftActive(craft)) return;
    CruiseDirection cdir = craft.getCruiseDirection();
    BlockFace blockDir = cdir.toBlockFace();
    int acx = Math.abs(craft.getBottomCenter().getX());
    int acz = Math.abs(craft.getBottomCenter().getZ());
    int apx = Math.abs(point.getX());
    int apz = Math.abs(point.getX());
    int minDist = MathUtils.getBoxWidth(craft.getHitBox());
    if (minDist >= 75) minDist = 75; //Override Value
    if (acx - apx > minDist) {
      point.setX(acx);
    }
    if (acx - apx < -minDist) {
      point.setX(acx);
    }
    if (acz - apz > minDist) {
      point.setZ(acz);
    }
    if (acz - apz < -minDist) {
      point.setZ(acz);
    }
    //DEBUG
    if (Settings.Debug)  {
      System.out.println("X-Range "+minDist+" ? "+acx+" - "+apx+" : "+(acx - apx));
      System.out.println("Z-Range "+minDist+" ? "+acz+" - "+apz+" : "+(acz - apz));
    }
    Vector vector = getDirectionalVector(craft.getBottomCenter(), point, inverse);
    vector = vector.setY(0);
    //DEBUG    //System.out.println(DirectionalUtils.getDirection(vector, true).toString()+" : "+blockDir.toString()+" > "+vector.toString());
    if (blockDir == BlockFace.SELF || blockDir == BlockFace.UP || blockDir == BlockFace.DOWN) return;
    MovecraftRotation nextRotation = MovecraftRotation.NONE;
    blockDir = DirectionalUtils.getDirection(vector, true);
    if (blockDir.toString().contains("_")) {
      for (String s : blockDir.toString().split("_")) {
        CruiseDirection newDir = CruiseDirection.fromBlockFace(CruiseDirection.valueOf(s).toBlockFace().getOppositeFace());
        if (CruiseDirection.isClockwise(craft.getCruiseDirection(), newDir)) {
          nextRotation = MovecraftRotation.CLOCKWISE;
        } else if (CruiseDirection.isAntiClockwise(craft.getCruiseDirection(), newDir)) {
          nextRotation = MovecraftRotation.ANTICLOCKWISE;
        }
        if (newDir == cdir || newDir == craft.getCruiseDirection()) nextRotation = MovecraftRotation.NONE;
        //if (cdir.getOpposite() == newDir && (newDir == CruiseDirection.NORTH || newDir == CruiseDirection.SOUTH)) nextRotation = MovecraftRotation.ANTICLOCKWISE;
        //DEBUG
        if (Settings.Debug) System.out.println(craft.toString()+" ROTATING? : "+cdir+" --> "+newDir.toString()+" ("+nextRotation.toString()+")");
        if (nextRotation == MovecraftRotation.NONE) continue;
        //long ticksElapsed = (System.currentTimeMillis() - craft.getLastRotateTime());
        craft.rotate(nextRotation);
      }
    } else {
      CruiseDirection newDir = CruiseDirection.fromBlockFace(CruiseDirection.valueOf(blockDir.toString()).toBlockFace().getOppositeFace());
      if (CruiseDirection.isClockwise(craft.getCruiseDirection(), newDir)) {
        nextRotation = MovecraftRotation.CLOCKWISE;
      } else if (CruiseDirection.isAntiClockwise(craft.getCruiseDirection(), newDir)) {
        nextRotation = MovecraftRotation.ANTICLOCKWISE;
      }
      if (newDir == cdir || newDir == craft.getCruiseDirection()) nextRotation = MovecraftRotation.NONE;
      //if (cdir.getOpposite() == newDir && (newDir == CruiseDirection.NORTH || newDir == CruiseDirection.SOUTH)) nextRotation = MovecraftRotation.ANTICLOCKWISE;
      //DEBUG
      if (Settings.Debug) System.out.println(craft.toString()+" ROTATING? : "+cdir+" --> "+newDir.toString()+" ("+nextRotation.toString()+")");
      if (nextRotation == MovecraftRotation.NONE) return;

      //long ticksElapsed = (System.currentTimeMillis() - craft.getLastRotateTime());
      craft.rotate(nextRotation);
    }
  }

  public static Vector getDirectionalVector(Location p1, Location p2) {
    return getDirectionalVector(p1,p2,false);
  }

  public static Vector getDirectionalVector(Location p1, Location p2, boolean inverse) {
    return getDirectionalVector(MathUtils.bukkit2MovecraftLoc(p1),MathUtils.bukkit2MovecraftLoc(p2),inverse);
  }

  public static Vector getDirectionalVector(MovecraftLocation p1, MovecraftLocation p2, boolean inverse) {
    Vector r;
    if (p1 == null || p2 == null) {
      r = new Vector(0,0,0).normalize();
    } else {
      MovecraftLocation tmp = p2.subtract(p1);
      r = new Vector(tmp.getX(),tmp.getY(),tmp.getZ());
    }
    if (r.getX() > 0) {
      r = r.setX(1);
    }
    if (r.getX() < 0) {
      r = r.setX(-1);
    }
    if (r.getY() > 0) {
      r = r.setY(1);
    }
    if (r.getY() < 0) {
      r = r.setY(-1);
    }
    if (r.getZ() > 0) {
      r = r.setZ(1);
    }
    if (r.getZ() < 0) {
      r = r.setZ(-1);
    }
    if (inverse) r = r.multiply(-1);
    return r;
  }

  public static Set<Craft> getCraftListInRange(Craft craft, int dist) {
    if (dist <= 0) dist = 1000;
    final Set<Craft> output = new HashSet<>();
    if (craft == null) return output;
    final MovecraftLocation point = craft.getMidPoint();
    for (final Craft oth : CraftManager.getInstance().getCrafts()) {
      if (oth.equals(craft)) continue;
      if (!getWorldOffMain(craft).equals(getWorldOffMain(oth))) continue;
      final HitBox temp = oth.getHitBox();
      if (!MathUtils.locationNearHitBox(temp,point,dist)) continue;
      output.add(oth);
    }
    output.remove(craft);
    return output;
  }
  public static Set<Craft> getCraftListInRange(MovecraftLocation point, World world, int dist) {
    if (dist <= 0) dist = 1000;
    final Set<Craft> output = new HashSet<>();
    if (point == null) return output;
    if (world == null) return output;
    for (final Craft oth : CraftManager.getInstance().getCrafts()) {
      final HitBox temp = oth.getHitBox();
      if (!world.equals(getWorldOffMain(oth))) continue;
      if (!MathUtils.locationNearHitBox(temp,point,dist)) continue;
      output.add(oth);
    }
    return output;
  }

  public static Set<Craft> getCraftListInRange(MovecraftLocation point, Craft craft, int dist) {
    if (dist <= 0) dist = 1000;
    final Set<Craft> output = new HashSet<>();
    if (point == null) return output;
    for (final Craft oth : CraftManager.getInstance().getCrafts()) {
      if (oth.equals(craft)) continue;
      if (!getWorldOffMain(craft).equals(getWorldOffMain(oth))) continue;
      final HitBox temp = oth.getHitBox();
      if (!MathUtils.locationNearHitBox(temp,point,dist)) continue;
      output.add(oth);
    }
    output.remove(craft);
    return output;
  }

  public static boolean makeCraftMoveTowards(final Craft craft, final MovecraftLocation dest, int threshold, boolean reverse) {
    return makeCraftMoveTowards(craft,dest,threshold,reverse,0);
  }

  public static boolean makeCraftMoveTowards(final Craft craft, final MovecraftLocation dest, int threshold) {
    return makeCraftMoveTowards(craft,dest,threshold,false,0);
  }

  public static boolean makeCraftMoveTowards(final Craft craft, final MovecraftLocation dest, int threshold, int iters) {
    return makeCraftMoveTowards(craft,dest,threshold,false,iters);
  }
  
  public static boolean makeCraftMoveTowards(final Craft craft, final MovecraftLocation dest, int threshold, boolean reverse, int iters) {
    return false;
  }
}