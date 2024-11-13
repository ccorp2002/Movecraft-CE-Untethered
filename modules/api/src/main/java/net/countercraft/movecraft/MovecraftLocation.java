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

package net.countercraft.movecraft;

import com.google.common.primitives.UnsignedInteger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import net.countercraft.movecraft.craft.Craft;
import java.util.HashMap;
import net.countercraft.movecraft.util.MathUtils;

import static net.countercraft.movecraft.util.BitMath.mask;
import static net.countercraft.movecraft.util.BitMath.unpackX;
import static net.countercraft.movecraft.util.BitMath.unpackY;
import static net.countercraft.movecraft.util.BitMath.unpackZ;

/**
 * Represents a Block aligned coordinate triplet.
 */
public class MovecraftLocation implements Comparable<MovecraftLocation>{
    public int x, y, z;

    public MovecraftLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int setX(int x) {
        this.x = x;
        return x;
    }

    public int setY(int z) {
        this.y = y;
        return y;
    }

    public int setZ(int z) {
        this.z = z;
        return z;
    }

    /**
     * Returns a MovecraftLocation that has undergone the given translation.
     * <p>
     * This does not change the MovecraftLocation that it is called upon and that should be accounted for in terms of Garbage Collection.
     *
     * @param dx - X translation
     * @param dy - Y translation
     * @param dz - Z translation
     * @return New MovecraftLocation shifted by specified amount
     */
    public MovecraftLocation add(int dx, int dy, int dz) {
        return this.translate(x + dx, y + dy, z + dz);
    }
    public MovecraftLocation add(int m) {
        return this.translate(x + m, y + m, z + m);
    }
    public MovecraftLocation subtract(int m) {
        return this.translate(x - m, y - m, z - m);
    }

    public MovecraftLocation translate(int dx, int dy, int dz) {
        return new MovecraftLocation(x + dx, y + dy, z + dz);
    }
    public MovecraftLocation translate(MovecraftLocation l) {
        return this.add(l);
    }
    @Override
    public boolean equals(Object o) {
        if (o instanceof MovecraftLocation) {
            MovecraftLocation location = (MovecraftLocation) o;
            return location.x==this.x && location.y==this.y && location.z == this.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 131 * 131 * x + 131 * z + y;
    }

    public MovecraftLocation add(MovecraftLocation l) {
        return new MovecraftLocation(getX() + l.getX(), getY() + l.getY(), getZ() + l.getZ());
    }

    public MovecraftLocation subtract(MovecraftLocation l) {
        return new MovecraftLocation(getX() - l.getX(), getY() - l.getY(), getZ() - l.getZ());
    }

    /**
     *
     * Gives the euclidean distance between this MovecraftLocation and another MovecraftLocation
     *
     * @param other the MovecraftLocation distant from this one
     * @return the euclidean distance between this and the other MovecraftLocation
     */
    public int distanceSquared(MovecraftLocation other) {
        if (other == null) return 127_000_000;
        int diffx = this.x - other.x;
        int diffy = this.y - other.y;
        int diffz = this.z - other.z;
        return diffx * diffx + diffy * diffy + diffz * diffz;
    }

    /**
     *
     * Gives the direct distance between this MovecraftLocation and another MovecraftLocation
     *
     * @param other the MovecraftLocation distant from this one
     * @return the direct distance between this and the other MovecraftLocation
     */
    public double distance(MovecraftLocation other) {
        if (other == null) return  127_000_000d;
        return Math.sqrt(distanceSquared(other));
    }
    public TrackedLocation toTracked(MovecraftLocation midPoint) {
        return new TrackedLocation(midPoint,this);
    }
    public Location toBukkit(World world){
        return new Location(world, this.x, this.y, this.z);
    }
    @Override
    public MovecraftLocation clone(){
        return new MovecraftLocation(this.x, this.y, this.z);
    }
    public boolean isChunkLoaded(World world){
        return world.isChunkLoaded((int)(this.x / 16),(int)(this.z / 16));
    }
    public MovecraftChunk toMovecraftChunk(World world){
        return new MovecraftChunk((int)(this.x / 16),(int)(this.z / 16), world);
    }

    public static Location toBukkit(World world, MovecraftLocation location){
        return new Location(world, location.x, location.y, location.z);
    }

    @Override
    public String toString(){
        return "(" + x + "," + y + "," + z +")";
    }

    public MovecraftLocation rotate(MovecraftLocation origin, MovecraftRotation rotation) {
        return MathUtils.rotateVec(rotation,this.subtract(origin)).add(origin);
    }

    public MovecraftLocation rotateVert(MovecraftLocation origin, MovecraftRotation rotation) {
        return MathUtils.rotateVecVert(rotation,this.subtract(origin)).add(origin);
    }



    private static final long BITS_26 = mask(26);
    private static final long BITS_12 = mask(12);

    public long pack(){
        return (x & BITS_26) | ((z & BITS_26) << 26) | (((y & (long) BITS_12) << (26 + 26)));
    }

    public static long pack(int x, int y, int z){
        return (x & BITS_26) | ((z & BITS_26) << 26) | (((y & (long) BITS_12) << (26 + 26)));
    }

    @NotNull
    public static MovecraftLocation unpack(long l){
        return new MovecraftLocation((int) (l << 38 >> 38),(int) (l >> 52),(int) (l << 12 >> 38));
    }
    public MovecraftLocation getRelative(BlockFace facing) {
        return this.translate(facing.getModX(), facing.getModY(), facing.getModZ());
    }

    @Override
    public int compareTo(@NotNull MovecraftLocation other) {
        if(this.x != other.x){
            return this.x - other.x;
        }
        if(this.y != other.y){
            return this.y - other.y ;
        }
        if(this.z != other.z){
            return this.z - other.z ;
        }
        return 0;
    }
}
