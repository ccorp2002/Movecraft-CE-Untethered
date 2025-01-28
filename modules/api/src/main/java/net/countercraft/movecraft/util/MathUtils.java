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

package net.countercraft.movecraft.util;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.AxialRotation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.bukkit.util.BlockVector;
import org.bukkit.persistence.PersistentDataType;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import java.util.Comparator;
import java.util.UUID;

import java.util.OptionalInt;

public class MathUtils {
    public static final NamespacedKey KEY_CRAFT_UUID = new NamespacedKey("movecraft", "craft-uuid");
    public static Craft getCraftByPersistentBlockData(@NotNull Location loc) {
        Block block = loc.getBlock();
        if (!(block.getState() instanceof TileState))
            return null;

        TileState blockEntity = (TileState)block.getState();

        if (blockEntity.getPersistentDataContainer().has(KEY_CRAFT_UUID, PersistentDataType.STRING)) {
            String value = blockEntity.getPersistentDataContainer().get(KEY_CRAFT_UUID, PersistentDataType.STRING);
            try {
                UUID uuid = UUID.fromString(value);
                Craft result = Craft.getCraftByUUID(uuid);
                if (result == null) {
                    // Remove invalid entry!
                    blockEntity.getPersistentDataContainer().remove(KEY_CRAFT_UUID);
                    blockEntity.update();
                } else if (!result.getHitBox().inBounds(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                    // Remove invalid entry!
                    blockEntity.getPersistentDataContainer().remove(KEY_CRAFT_UUID);
                    blockEntity.update();
                    result = null;
                }
                return result;
            } catch(IllegalArgumentException iae) {
                // Remove invalid entry!
                blockEntity.getPersistentDataContainer().remove(KEY_CRAFT_UUID);
                blockEntity.update();
                return null;
            }

        }
        return null;
    }
    public static Player getNearestPlayer(int range, final Location point) {
        int radius = range * range;
        if(Bukkit.getOnlinePlayers().size() > 0 && point != null) {
            if (point.getWorld().getPlayers().size() <= 0) return null; 
            return point.getWorld().getPlayers().stream().min(Comparator.comparingDouble((p) -> p.getLocation().distanceSquared(point))).orElse(null);
        } else {
            return null;
        }
    }


    /**
     * checks if <code>location</code> is within the bounding box <code>box</code> restricted by minimum values on x and z
     * @param box the bounding box to check within
     * @param minX the minimum x coordinate to search
     * @param minZ the minimum z coordinate to search
     * @param location the location to check
     * @return True if the player is within the given bounding box
     */
    @Contract(pure=true)
    public static boolean playerIsWithinBoundingPolygon(@NotNull final int[][][] box, final int minX, final int minZ, @NotNull final MovecraftLocation location) {
        if (location.getX() >= minX && location.getX() < (minX + box.length)) {
            // PLayer is within correct X boundary
            if (location.getZ() >= minZ && location.getZ() < (minZ + box[location.getX() - minX].length)) {
                // Player is within valid Z boundary
                int minY, maxY;
                try {
                    minY = box[location.getX() - minX][location.getZ() - minZ][0];
                    maxY = box[location.getX() - minX][location.getZ() - minZ][1];
                } catch (NullPointerException e) {
                    return false;
                }
                return location.getY() >= minY && location.getY() <= (maxY + 2);
            }
        }
        return false;
    }

    /**
     * checks if the given bukkit <code>location</code> is within <code>hitbox</code>
     * @param hitBox the bounding box to check within
     * @param location the location to check
     * @return True if the player is within the given bounding box
     */
    @Contract(pure=true)
    public static boolean locationInHitBox(@NotNull final HitBox hitBox, @NotNull final Location location) {
        if (location == null)
          return false;
        return hitBox.inBounds(location.getX(),location.getY(),location.getZ());
    }

    /**
     * Checks if a given <code>Location</code> is within some distance, <code>distance</code>, from a given <code>HitBox</code>
     * @param hitBox the hitbox to check
     * @param location the location to check
     * @return True if <code>location</code> is less or equal to 3 blocks from <code>craft</code>
     */
    @Contract(pure=true)
    public static boolean locationNearHitBox(@NotNull final HitBox hitBox, @NotNull final Location location, double distance) {
        if (hitBox == null)
          return false;
        if (location == null)
          return false;
        return !hitBox.isEmpty() &&
                location.getX() >= hitBox.getMinX() - distance &&
                location.getZ() >= hitBox.getMinZ() - distance &&
                location.getX() <= hitBox.getMaxX() + distance &&
                location.getZ() <= hitBox.getMaxZ() + distance &&
                location.getY() >= hitBox.getMinY() - distance &&
                location.getY() <= hitBox.getMaxY() + distance;
    }
    @Contract(pure=true)
    public static boolean locationNearHitBox(@NotNull final HitBox hitBox, @NotNull final MovecraftLocation location, double distance) {
        if (hitBox == null)
          return false;
        if (location == null)
          return false;
        return !hitBox.isEmpty() &&
                location.getX() >= hitBox.getMinX() - distance &&
                location.getZ() >= hitBox.getMinZ() - distance &&
                location.getX() <= hitBox.getMaxX() + distance &&
                location.getZ() <= hitBox.getMaxZ() + distance &&
                location.getY() >= hitBox.getMinY() - distance &&
                location.getY() <= hitBox.getMaxY() + distance;
    }

    @Contract(pure=true)
    public static int getBoxWidth(@NotNull final HitBox hitBox) {
        int xlen = Math.abs(hitBox.getMaxX() - hitBox.getMinX());
        int zlen = Math.abs(hitBox.getMaxZ() - hitBox.getMinZ());
        if (xlen > zlen) return zlen;
        return xlen;
    }

    @Contract(pure=true)
    public static int getBoxLength(@NotNull final HitBox hitBox) {
        int xlen = Math.abs(hitBox.getMaxX() - hitBox.getMinX());
        int zlen = Math.abs(hitBox.getMaxZ() - hitBox.getMinZ());
        if (xlen > zlen) return xlen;
        return zlen;
    }

    /**
     * Checks if a given <code>Location</code> is within 3 blocks from a given <code>Craft</code>
     * @param craft the craft to check
     * @param location the location to check
     * @return True if <code>location</code> is less or equal to 3 blocks from <code>craft</code>
     */
    @Contract(pure=true)
    public static boolean locIsNearCraftFast(@NotNull final Craft craft, @NotNull final MovecraftLocation location) {
        // optimized to be as fast as possible, it checks the easy ones first, then the more computationally intensive later
        if (craft == null) {
          return false;
        }
        if (location == null)
          return false;
        return locationNearHitBox(craft.getHitBox(), location.toBukkit(craft.getWorld()), 2);
    }

    @Contract(pure=true)
    public static Craft fastNearestCraftToLoc(Set<Craft> craftsList, Location loc) {
        Craft ret = null;
        if (loc == null)
          return ret;
        long closestDistSquared = Long.MAX_VALUE;
        for (Craft i : craftsList) {
            if (i.getHitBox().isEmpty())
                continue;
            int midX = (i.getHitBox().getMaxX() + i.getHitBox().getMinX()) >> 1;
//				int midY=(i.getMaxY()+i.getMinY())>>1; don't check Y because it is slow
            int midZ = (i.getHitBox().getMaxZ() + i.getHitBox().getMinZ()) >> 1;
            long distSquared = (long) (Math.pow(midX -  loc.getX(), 2) + Math.pow(midZ - (int) loc.getZ(), 2));
            if (distSquared < closestDistSquared) {
                closestDistSquared = distSquared;
                ret = i;
            }
        }
        return ret;
    }
    /**
     * Creates a <code>MovecraftLocation</code> representation of a bukkit <code>Location</code> object aligned to the block grid
     * @param bukkitLocation the location to convert
     * @return a new <code>MovecraftLocation</code> representing the given location
     */
    @Nullable
    @Contract(pure=true)
    public static MovecraftLocation bukkit2MovecraftLoc(@NotNull final Location bukkitLocation) {
        if (bukkitLocation == null) return null;
        return new MovecraftLocation(bukkitLocation.getBlockX(), bukkitLocation.getBlockY(), bukkitLocation.getBlockZ());
    }
    /**
     * Creates a <code>MovecraftLocation</code> representation of a bukkit <code>Location</code> object aligned to the block grid
     * @param bukkitLocation the location to convert
     * @return a new <code>MovecraftLocation</code> representing the given location
     */
    @Nullable
    @Contract(pure=true)
    public static MovecraftLocation bukkit2MovecraftLoc(@NotNull final Block block) {
        if (block == null) return null;
        return bukkit2MovecraftLoc(block.getLocation());
    }
    @Nullable
    @Contract(pure=true)
    public static MovecraftLocation bukkit2MovecraftLoc(@NotNull final Vector vector) {
        if (vector == null) return null;
        return new MovecraftLocation(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }
    @Nullable
    @Contract(pure=true)
    public static MovecraftLocation bukkit2MovecraftLoc(@NotNull final Entity entity) {
        if (entity == null) return null;
        return bukkit2MovecraftLoc(entity.getLocation());
    }
    @Nullable
    @Contract(pure=true)
    public static MovecraftLocation bukkit2MovecraftLoc(@NotNull final MovecraftLocation location) {
        if (location == null) return null;
        return location;
    }

    /**
     * Rotates a MovecraftLocation towards a supplied <code>Rotation</code>.
     * The resulting MovecraftRotation is based on a center of (0,0,0).
     * @param rotation the direction to rotate
     * @param movecraftLocation the location to rotate
     * @return a rotated Movecraft location
     */
    @NotNull
    @Contract(pure=true)
    public static MovecraftLocation rotateVec(@NotNull final MovecraftRotation rotation, @NotNull final MovecraftLocation movecraftLocation) {
        double theta;
        int x = 0;
        int y = 0;
        int z = 0;
        if (rotation == MovecraftRotation.CLOCKWISE) {
            theta = 0.5 * Math.PI;
            x = (int) Math.round((movecraftLocation.getX() * Math.cos(theta)) + (movecraftLocation.getZ() * (-1 * Math.sin(theta))));
            y = movecraftLocation.getY();
            z = (int) Math.round((movecraftLocation.getX() * Math.sin(theta)) + (movecraftLocation.getZ() * Math.cos(theta)));
        } else if (rotation == MovecraftRotation.ANTICLOCKWISE) {
            theta = -1 * 0.5 * Math.PI;
            x = (int) Math.round((movecraftLocation.getX() * Math.cos(theta)) + (movecraftLocation.getZ() * (-1 * Math.sin(theta))));
            y = movecraftLocation.getY();
            z = (int) Math.round((movecraftLocation.getX() * Math.sin(theta)) + (movecraftLocation.getZ() * Math.cos(theta)));
        } else {
            x = movecraftLocation.getX();
            y = movecraftLocation.getY();
            z = movecraftLocation.getZ();
        }


        return new MovecraftLocation(x, y, z);
    }

    /**
     * Rotates a MovecraftLocation towards a supplied <code>Rotation</code>.
     * The resulting MovecraftRotation is based on an axis.
     * @param rotation the direction to rotate
     * @param movecraftLocation the location to rotate
     * @return a rotated Movecraft location
     */
    @NotNull
    @Contract(pure=true)
    public static MovecraftLocation rotateVecAxis(@NotNull MovecraftRotation rotation, @NotNull final MovecraftLocation point, @NotNull final MovecraftLocation axis) {
        double angle = 0.0d;
        final Vector vector = new Vector(point.getX(), point.getY(), point.getZ());
        final Vector vectorAxis = new Vector(axis.getX(), axis.getY(), axis.getZ());
        if (rotation == MovecraftRotation.CLOCKWISE) {
            angle = 90d;
            
        } //UP
        else if (rotation == MovecraftRotation.ANTICLOCKWISE) {
            angle = -90d;
        } //DOWN
        else {
            return point;
        } //NONE
        Vector vxp = vectorAxis.clone().normalize().getCrossProduct(vector.clone().normalize());
        Vector vxvxp = vectorAxis.clone().normalize().getCrossProduct(vxp.clone().normalize());
        Vector resultVec = DirectionalUtils.addSub(vector,(int)Math.sin(angle)).multiply(vxp);
        resultVec = DirectionalUtils.addSub(resultVec,(int)(1 - Math.cos(angle)));
        resultVec = resultVec.multiply(vxvxp);
        MovecraftLocation result = new MovecraftLocation(resultVec.getBlockX(),resultVec.getBlockY(),resultVec.getBlockZ());
        return result;
    }
    @NotNull
    @Contract(pure=true)
    public static MovecraftLocation rotateVecAxis(@NotNull MovecraftRotation rotation, @NotNull final MovecraftLocation point, @NotNull final CruiseDirection axis) {
        double angle = 0.0d;
        final Vector vector = new Vector(point.getX(), point.getY(), point.getZ());
        final Vector vectorAxis = new Vector(axis.toBlockFace().getModX(), axis.toBlockFace().getModY(), axis.toBlockFace().getModZ());
        if (rotation == MovecraftRotation.CLOCKWISE) {
            angle = 90d;
            
        } //UP
        else if (rotation == MovecraftRotation.ANTICLOCKWISE) {
            angle = -90d;
        } //DOWN
        else {
            return point;
        } //NONE
        Vector vxp = vectorAxis.clone().normalize().getCrossProduct(vector.clone().normalize());
        Vector vxvxp = vectorAxis.clone().normalize().getCrossProduct(vxp.clone().normalize());
        Vector resultVec = DirectionalUtils.addSub(vector,(int)Math.sin(angle)).multiply(vxp);
        resultVec = DirectionalUtils.addSub(resultVec,(int)(1 - Math.cos(angle)));
        resultVec = resultVec.multiply(vxvxp);
        MovecraftLocation result = new MovecraftLocation(resultVec.getBlockX(),resultVec.getBlockY(),resultVec.getBlockZ());
        return result;
    }


    @NotNull
    @Contract(pure=true)
    public static MovecraftLocation rotateVecVert(@NotNull final MovecraftRotation rotation, @NotNull final MovecraftLocation movecraftLocation) {
        double theta;
        int x = 0;
        int y = 0;
        int z = 0;
        if (rotation == MovecraftRotation.CLOCKWISE) { //LEFT
            theta = 90d;
            x = (int) Math.round((movecraftLocation.getX() * Math.cos(theta)) + (movecraftLocation.getY() * (-1 * Math.sin(theta))));
            y = (int) Math.round((movecraftLocation.getX() * Math.sin(theta)) + (movecraftLocation.getY() * (Math.cos(theta))));
            z = movecraftLocation.getZ();
        } else if (rotation == MovecraftRotation.ANTICLOCKWISE) { //RIGHT
            theta = -90d;
            x = (int) Math.round((movecraftLocation.getX() * Math.cos(theta)) + (movecraftLocation.getZ() * (-1 * Math.sin(theta))));
            y = (int) Math.round((movecraftLocation.getX() * Math.sin(theta)) + (movecraftLocation.getY() * (Math.cos(theta))));
            z = movecraftLocation.getZ();
        } else {
            x = movecraftLocation.getX();
            y = movecraftLocation.getY();
            z = movecraftLocation.getZ();
        }
        return new MovecraftLocation(x, y, z);
    }
    @NotNull
    @Contract(pure=true)
    public static double[] rotateVecVertNoRound(@NotNull final MovecraftRotation rotation, @NotNull final MovecraftLocation movecraftLocation) {
        double theta;
        double x = 0;
        double y = 0;
        double z = 0;
        if (rotation == MovecraftRotation.CLOCKWISE) { //UP
            theta = 90d;
            x = (int) ((movecraftLocation.getX() * Math.cos(theta)) + (movecraftLocation.getY() * (-1 * Math.sin(theta))));
            y = (int) ((movecraftLocation.getX() * Math.sin(theta)) + (movecraftLocation.getY() * (Math.cos(theta))));
            z = movecraftLocation.getZ();
        } else if (rotation == MovecraftRotation.ANTICLOCKWISE) { //DOWN
            theta = -90d;
            x = (int) ((movecraftLocation.getX() * Math.cos(theta)) + (movecraftLocation.getZ() * (-1 * Math.sin(theta))));
            y = (int) ((movecraftLocation.getX() * Math.sin(theta)) + (movecraftLocation.getY() * (Math.cos(theta))));
            z = movecraftLocation.getZ();
        } else {
            x = movecraftLocation.getX();
            y = movecraftLocation.getY();
            z = movecraftLocation.getZ();
        }
        return new double[]{x, y, z};
    }
    @NotNull
    @Contract(pure=true)
    public static double[] rotateVecVertNoRound(@NotNull final MovecraftRotation rotation, double x, double y, double z) {
        double theta;
        double nx = 0;
        double nz = 0;
        double ny = 0;
        if (rotation == MovecraftRotation.CLOCKWISE) { //UP
            theta = 90d;
            x = (int) ((x * Math.cos(theta)) + (y * (-1 * Math.sin(theta))));
            y = (int) ((x * Math.sin(theta)) + (y * (Math.cos(theta))));
        } else if (rotation == MovecraftRotation.ANTICLOCKWISE) { //DOWN
            theta = -90d;
            x = (int) ((x * Math.cos(theta)) + (z * (-1 * Math.sin(theta))));
            y = (int) ((x * Math.sin(theta)) + (y * (Math.cos(theta))));
        }
        return new double[]{nx, ny, nz};
    }

    @NotNull
    @Contract(pure=true)
    public static MovecraftLocation rotateVecVert(@NotNull final boolean upDown, @NotNull final MovecraftLocation movecraftLocation) {
        double theta;
        int x = 0;
        int y = 0;
        int z = 0;
        if (upDown) {
            theta = 90d;
            x = (int) Math.round((movecraftLocation.getX() * Math.cos(theta)) + (movecraftLocation.getY() * (-1 * Math.sin(theta))));
            y = (int) Math.round((movecraftLocation.getX() * Math.sin(theta)) + (movecraftLocation.getY() * (Math.cos(theta))));
            z = movecraftLocation.getZ();
        } else {
            theta = -90d;
            x = (int) Math.round((movecraftLocation.getX() * Math.cos(theta)) + (movecraftLocation.getZ() * (-1 * Math.sin(theta))));
            y = (int) Math.round((movecraftLocation.getX() * Math.sin(theta)) + (movecraftLocation.getY() * (Math.cos(theta))));
            z = movecraftLocation.getZ();
        }
        return new MovecraftLocation(x, y, z);
    }

    @NotNull
    @Deprecated
    public static double[] rotateVec(@NotNull MovecraftRotation rotation, double x, double z) {
        double theta;
        if (rotation == MovecraftRotation.CLOCKWISE) {
            theta = 0.5 * Math.PI;
        } else {
            theta = -1 * 0.5 * Math.PI;
        }

        double newX = Math.round((x * Math.cos(theta)) + (z * (-1 * Math.sin(theta))));
        double newZ = Math.round((x * Math.sin(theta)) + (z * Math.cos(theta)));

        return new double[]{newX, newZ};
    }

    @NotNull
    @Deprecated
    public static double[] rotateVecNoRound(@NotNull MovecraftRotation r, double x, double z) {
        double theta;
        if (r == MovecraftRotation.CLOCKWISE) {
            theta = 0.5 * Math.PI;
        } else {
            theta = -1 * 0.5 * Math.PI;
        }

        double newX = (x * Math.cos(theta)) + (z * (-1 * Math.sin(theta)));
        double newZ = (x * Math.sin(theta)) + (z * Math.cos(theta));

        return new double[]{newX, newZ};
    }

    @Deprecated
    public static int positiveMod(int mod, int divisor) {
        if (mod < 0) {
            mod += divisor;
        }
        return mod;
    }

    /**
     * Checks if a <link>MovecraftLocation</link> is within the border of the given <link>World</link>
     * @param world the world to check in
     * @param location the location in the given <link>World</link>
     * @return true if location is within the world border, false otherwise
     */
    @Contract(pure = true)
    public static boolean withinWorldBorder(@NotNull World world, @NotNull MovecraftLocation location) {
        WorldBorder border = world.getWorldBorder();
        int radius = (int) (border.getSize() / 2.0);
        //The visible border will always end at 29,999,984 blocks, despite being larger
        int minX = border.getCenter().getBlockX() - radius;
        int maxX = border.getCenter().getBlockX() + radius;
        int minZ = border.getCenter().getBlockZ() - radius;
        int maxZ = border.getCenter().getBlockZ() + radius;
        return Math.abs(location.getX()) < 29999984 &&
                Math.abs(location.getZ()) < 29999984 &&
                location.getX() >= minX &&
                location.getX() <= maxX &&
                location.getZ() >= minZ &&
                location.getZ() <= maxZ;
    }

    @NotNull
    public static OptionalInt parseInt(@NotNull String encoded){
        try {
            return OptionalInt.of(Integer.parseInt(encoded));
        }catch(NumberFormatException e){
            return OptionalInt.empty();
        }
    }
}
