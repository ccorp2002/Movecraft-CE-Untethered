package net.countercraft.movecraft.util.radar;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.Movecraft;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.lang.Math;


public class RadarMap {

  public static final String RADAR_HEADER = "&r&6[&r&lRADAR MAP&r&6] &r> &r";


  public static List<String> getContactsMapForCraft(Craft craft, int zoomSettings){
      List<Craft> contacts = new ArrayList<>();
      List<Chunk> chunks = new ArrayList<>();
      contacts.addAll(getRadarContacts(craft));

      for(int i = 0; i < contacts.size(); i++){
          chunks.add(i, getCraftChunk(contacts.get(i)));
      }


      List<String> map = RadarUtils.generateMap(getCraftChunk(craft), chunks, zoomSettings, 10);
      contacts.clear();
      chunks.clear();
      return map;
  }


  public static Set<Craft> getRadarContacts(Craft craft) {
    double range = 0;
    Set<Craft> crafts = new HashSet<>();
    if (craft.getDataTag("radar_range") != null) {
      range = (double)craft.getDataTag("radar_range");
    }
    for (Craft oth : CraftManager.getInstance().getCraftsInWorld(craft.getWorld())) {
      double radar = 0;
      if (oth.getDataTag("radar_profile") != null) {
        radar = (double)oth.getDataTag("radar_profile");
      }
      radar += range;
      double dist = (double)craft.getMidPoint().distance(oth.getMidPoint());
      if (dist > range) continue;
      crafts.add(oth);
    }
    return crafts;
  }

  public static Chunk getCraftChunk(Craft craft){
      return Movecraft.getInstance().getWorldHandler().getChunkFastest(craft.getMidPoint().toBukkit(craft.getWorld())); //craft.getWorld().getChunkAt((int) getMidCoordinatesX(craft), (int) getMidCoordinatesZ(craft));
  }
  public static Chunk getPlayerChunk(Player player){
      return Movecraft.getInstance().getWorldHandler().getChunkFastest(player.getLocation()); //player.getWorld().getChunkAt((int) player.getLocation().getBlockX(), (int)  player.getLocation().getBlockZ());
  }

  public static long getMidCoordinatesX(Craft tcraft){
      long tpos = (long) tcraft.getMidPoint().getX()/16;
      return tpos;

  }
  public static long getMidCoordinatesY(Craft tcraft){
      long tpos = (long) tcraft.getMidPoint().getY();
      return tpos;
  }

  public static long getMidCoordinatesZ(Craft tcraft){
      long tpos = (long) tcraft.getMidPoint().getZ()/16;
      return tpos;
  }

}