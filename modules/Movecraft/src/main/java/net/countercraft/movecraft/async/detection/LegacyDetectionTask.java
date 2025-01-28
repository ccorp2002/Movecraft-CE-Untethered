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

package net.countercraft.movecraft.async.detection;


import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.async.AsyncTask;
import net.countercraft.movecraft.craft.*;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.hitboxes.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.*;
import java.util.*;

public class LegacyDetectionTask extends AsyncTask {
    @NotNull private Craft c;
    @NotNull private MovecraftLocation startLocation;
    private int minSize;
    private int maxSize;
    @NotNull private BitmapHitBox fluidBox = new BitmapHitBox();
    @NotNull private BitmapHitBox hitBox = new BitmapHitBox();
    @NotNull private World world;
    @Nullable private Player player;
    @NotNull private Player notificationPlayer;
    private ArrayList<Material> allowedBlocks;
    private ArrayList<Material> forbiddenBlocks;
    private int maxX;
    private int maxY;
    private int maxZ;
    private int minY;
    private boolean failed;
    private boolean waterContact;
    @NotNull private String failMessage = "";

    public LegacyDetectionTask(Craft c, @NotNull MovecraftLocation startLocation, @Nullable Player player) {
        super(c);
        this.startLocation = startLocation;
        this.minSize = c.getType().getIntProperty(CraftType.MIN_SIZE);
        this.maxSize = c.getType().getIntProperty(CraftType.MAX_SIZE);
        this.world = c.getWorld();
        this.player = player;
        this.notificationPlayer = player;
        this.allowedBlocks = this.getAllowedBlocks();
        this.forbiddenBlocks = this.getForbiddenBlocks();
    }

    @Override
    public void execute() {
        Set<Block> blocks = new HashSet<Block>(getBlocks(startLocation.toBukkit(this.world).getBlock(),this.maxSize));
        this.failed = false;
        if (player == null) {
          this.c.setAudience(Movecraft.getAdventure().console());
        } else {
          this.c.setAudience(Movecraft.getAdventure().player(player));
        }
        if (blocks.isEmpty()){
          fail(String.format(I18nSupport.getInternationalisedString("Detection - Forbidden block found")));
          return;
        }
        for (Block blk : blocks){
          hitBox.add(MathUtils.bukkit2MovecraftLoc(blk.getLocation()));
        }
        if (this.player == null) {
          this.c = new NPCCraftImpl(c.getType(), world, null);
        } else {
          this.c = new PlayerCraftImpl(c.getType(), world, player);
          ((PlayerCraftImpl)this.c).addPassenger(player);
        }
        c.setHitBox(hitBox);
        CraftDetectEvent detectionEvent = new CraftDetectEvent(this.c, startLocation);
        Bukkit.getPluginManager().callEvent(detectionEvent);
        if (!isWithinLimit(hitBox.size(), minSize, maxSize)) {
            return;
        }
        CraftManager.getInstance().addCraft(c);
        if (this.player != null) {
          CraftPilotEvent pilotEvent = new CraftPilotEvent(this.c, CraftPilotEvent.Reason.PLAYER);
          Bukkit.getPluginManager().callEvent(pilotEvent);
        }
    }
    


    public Collection<Block> getBlocks(Block start, int max) {
      HashSet<Block> blocks = new HashSet<>();
      HashSet<Block> last = new HashSet<>();
      last.add(start);
      boolean found = true;
      while (found) {
        found = false;
        HashSet<Block> toAdd = new HashSet<>();
        for (Block b : last) {
          HashSet<Block> faces = new HashSet<>();
          faces.add(b.getRelative(BlockFace.DOWN));
          faces.add(b.getRelative(BlockFace.EAST));
          faces.add(b.getRelative(BlockFace.NORTH));
          faces.add(b.getRelative(BlockFace.SOUTH));
          faces.add(b.getRelative(BlockFace.UP));
          faces.add(b.getRelative(BlockFace.WEST));
          for (Block block : faces) {
            if (allowedBlocks.contains(block.getType())) {
              toAdd.add(block);
              found = true;
            }
            if (forbiddenBlocks.contains(block.getType())) {
              return new HashSet<>();
            }
          }
        }
        blocks.addAll(toAdd);
        last = toAdd;
        if (blocks.size() > max)
          return new HashSet<>();
      }
      return blocks;
    }

    public static Collection<Block> getBlocks(Collection<Material> types, Block start, int max) {
      HashSet<Block> blocks = new HashSet<>();
      HashSet<Block> last = new HashSet<>();
      last.add(start);
      Set<Material> allowed = new HashSet();
      boolean found = true;
      Craft c = CraftManager.getInstance().getCraftFromBlock(start);
      HashSet<Block> faces = new HashSet<>();
      while (found) {
        found = false;
        HashSet<Block> toAdd = new HashSet<>();
        for (Block b : last) {
          faces.clear();
          faces.add(b.getRelative(BlockFace.DOWN));
          faces.add(b.getRelative(BlockFace.EAST));
          faces.add(b.getRelative(BlockFace.NORTH));
          faces.add(b.getRelative(BlockFace.SOUTH));
          faces.add(b.getRelative(BlockFace.UP));
          faces.add(b.getRelative(BlockFace.WEST));
          for (Block block : faces) {
            if (allowed.contains(block.getType())) {
              toAdd.add(block);
              found = true;
            }
          }
        }
        blocks.addAll(toAdd);
        last = toAdd;
        if (blocks.size() > max)
          return new HashSet<Block>();
      }
      return blocks;
    }

    public static Collection<Block> getBlocks(CraftType type, Block start, int max) {
      HashSet<Block> blocks = new HashSet<>();
      HashSet<Block> last = new HashSet<>();
      last.add(start);
      ArrayList<Material> allowed = new ArrayList();
      allowed.addAll(type.getAllowedBlocks());
      ArrayList<Material> denied = new ArrayList();
      denied.addAll(type.getForbiddenBlocks());
      boolean found = true;
      while (found) {
        found = false;
        HashSet<Block> toAdd = new HashSet<>();
        for (Block b : last) {
          HashSet<Block> faces = new HashSet<>();
          faces.add(b.getRelative(BlockFace.DOWN));
          faces.add(b.getRelative(BlockFace.EAST));
          faces.add(b.getRelative(BlockFace.NORTH));
          faces.add(b.getRelative(BlockFace.SOUTH));
          faces.add(b.getRelative(BlockFace.UP));
          faces.add(b.getRelative(BlockFace.WEST));
          for (Block block : faces) {
            if (allowed.contains(block.getType())) {
              toAdd.add(block);
              found = true;
            }
            if (denied.contains(block.getType())) {
              return new HashSet<>();
            }
          }
        }
        blocks.addAll(toAdd);
        last = toAdd;
        if (blocks.size() > max)
          return new HashSet<>();
      }
      return blocks;
    }

    private boolean isWithinLimit(int size, int min, int max) {
        if (size < min) {
            fail(String.format(I18nSupport.getInternationalisedString("Detection - Craft too small"), min));
            return false;
        } else if (size > max) {
            fail(String.format(I18nSupport.getInternationalisedString("Detection - Craft too large"), max));
            return false;
        } else {
            return true;
        }

    }

    private void fail(String message) {
        failed = true;
        failMessage = message;
    }

    @NotNull
    public World getWorld() {
        return world;
    }

    @NotNull
    public Player getNotificationPlayer() {
        return this.notificationPlayer;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    public ArrayList<Material> getAllowedBlocks() {
      ArrayList<Material> allowed = new ArrayList();
      allowed.addAll(c.getType().getMaterialSetProperty(CraftType.ALLOWED_BLOCKS));
      return allowed;
    }

    public ArrayList<Material> getForbiddenBlocks() {
      ArrayList<Material> denied = new ArrayList();
      denied.addAll(c.getType().getMaterialSetProperty(CraftType.FORBIDDEN_BLOCKS));
      return denied;
    }

    public boolean failed() {
        return failed;
    }

    @NotNull
    public HitBox getHitBox() {
        return hitBox;
    }

    public boolean isWaterContact() {
        return waterContact;
    }

    @NotNull
    public String getFailMessage() {
        return failMessage;
    }
}
