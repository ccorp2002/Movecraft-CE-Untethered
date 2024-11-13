package net.countercraft.movecraft.util.radar;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.*;

public class RadarUtils {



    public static String[][] generateRawMap(Chunk playerChunk, List<Chunk> chunkList, int zoomsetting, int gridScale){
        if (gridScale <= 10) gridScale = 10;
        String[][] gridArray = new String[gridScale+1][gridScale];

        int chunkMultiplier = zoomsetting; // Zoom settings
        int chunkSize = 16;

        chunkMultiplier = chunkMultiplier * chunkSize;

         // Fill array
        for(int x=0;x<gridArray.length;x++)
             for(int y=0;y<gridArray[x].length;y++)
                 gridArray[x][y] = "+";


        int playerCornerX = calculateCornerX(playerChunk, chunkMultiplier) / chunkMultiplier;
        int playerCornerZ = calculateCornerZ(playerChunk, chunkMultiplier) / chunkMultiplier;



        int calcChunkX;
        int calcChunkZ;

        // Get each chunk and divide the NorthWest corner by 16 in order to get each individual chunk in its representable form(Hard to explain ik)
        // After that make it relative to the player's position by subtracting the player's chunk by the calculated chunk
        // Then align it to the origin by adding 10 each.

	    for(int i = 0; i < chunkList.size(); i++){
           	calcChunkX = (((calculateCornerX(chunkList.get(i), chunkMultiplier) / chunkMultiplier)) - playerCornerX) + 10;
           	calcChunkZ = (((calculateCornerZ(chunkList.get(i), chunkMultiplier) / chunkMultiplier)) - playerCornerZ) + 10;

           	if(calcChunkX < gridScale+1 && calcChunkZ < gridScale && calcChunkX > -1 && calcChunkZ > -1) {
                gridArray[calcChunkX][calcChunkZ] = "x";
            }
        }

        // Setup the origin
        gridArray[(int)(gridScale/2)][(int)(gridScale/2)] = "^";

        // Once we have the entire map stored in the array send it
        return gridArray;
    }
  public static List<String> generateMap(Chunk playerChunk, List<Chunk> chunkList, int zoomsetting, int gridScale){
    if (gridScale <= 10) gridScale = 10;
    return outputGridMap(generateRawMap(playerChunk,chunkList,zoomsetting,gridScale),gridScale);
  }

  public static List<String> outputGridMap(String[][] gridArray, int gridScale){

      String gridRow;
      final List<String> strings = new ArrayList<>();
      // Z = Y in this case, but in order to be consistent I kept it at Z
      for(int z = 0; z < gridScale; z++) {
        // The row will be appended into a string then sent
        gridRow = "";
        StringBuilder sb = new StringBuilder(gridRow);
        for (int x = 0; x < gridScale+1; x++) {
            sb.append(gridArray[x][z]);
        }
        strings.add(sb.toString());
      }
      return strings;
  }

  public static int calculateCornerX(Chunk c, int chunkMultiplier){
      return c.getX() + (c.getX() % chunkMultiplier);
  }

  public static int calculateCornerZ(Chunk c, int chunkMultiplier){
      return c.getZ() + (c.getZ() % chunkMultiplier);
  }
}