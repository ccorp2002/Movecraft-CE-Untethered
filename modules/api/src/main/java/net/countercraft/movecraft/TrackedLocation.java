package net.countercraft.movecraft;

import org.bukkit.World;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import net.countercraft.movecraft.util.MathUtils;

public class TrackedLocation {
    public int x, y, z;
    private MovecraftLocation origin;

    /**
     * Creates a new TrackedLocation instance which tracks a location about an origin.
     * @param origin The origin point at which the tracked location rotates about.
     * @param location The absolute location to track.
     */
    public TrackedLocation(@NotNull int x, int y, int z) {
        this.origin = new MovecraftLocation(0,0,0);
        this.x = x - 0;
        this.y = y - 0;
        this.z = z - 0;
    }

    public TrackedLocation(@NotNull MovecraftLocation origin, @NotNull MovecraftLocation location) {
        this.origin = origin;
        this.x = location.getX() - origin.getX();
        this.y = location.getY() - origin.getY();
        this.z = location.getZ() - origin.getZ();
    }

    /**
     * Moves the origin point of the tracked location
     */
    public MovecraftLocation translate(int dx, int dy, int dz) {
        return this.getLocation();
    }

    public MovecraftLocation translate(MovecraftLocation displ) {
        return this.getLocation();
    }
    public TrackedLocation translateTracked(int dx, int dy, int dz) {
        this.origin = this.origin.translate(dx,dy,dz);
        return this;
    }

    public TrackedLocation translateTracked(MovecraftLocation displ) {
        this.origin = this.origin.translate(displ);
        return this;
    }

    /**
     * Rotates the stored location about the origin point location.
     * @param rotation A clockwise or counter-clockwise direction to rotate.
     */

    public MovecraftLocation rotate(MovecraftLocation origin, MovecraftRotation rotation) {
        return MathUtils.rotateVec(rotation,this.getLocation().subtract(origin)).add(origin);
    }

    public TrackedLocation rotateTracked(MovecraftLocation origin, MovecraftRotation rotation) {
        return new TrackedLocation(origin,MathUtils.rotateVec(rotation,this.getLocation().subtract(origin)).add(origin));
    }

    public MovecraftLocation rotateVert(MovecraftLocation origin, MovecraftRotation rotation) {
        return MathUtils.rotateVecVert(rotation,this.getLocation().subtract(origin)).add(origin);
    }

    public TrackedLocation rotateTrackedVert(MovecraftLocation origin, MovecraftRotation rotation) {
        return new TrackedLocation(origin,MathUtils.rotateVecVert(rotation,this.getLocation().subtract(origin)).add(origin));
    }

    public int getX() {
        return this.x + origin.getX();
    }

    public int getY() {
        return this.y + origin.getY();
    }

    public int getZ() {
        return this.z + origin.getZ();
    }

    public String toString(){
        return "(" + x + "," + y + "," + z +")";
    }

    /**
     * Gets the stored location.
     * @return Returns the location.
     */
    public MovecraftLocation getLocation() {
        return new MovecraftLocation(this.x + origin.getX(), this.y + origin.getY(), this.z + origin.getZ());
    }
    public MovecraftLocation getOrigin() {
        return this.origin;
    }
    public TrackedLocation setOrigin(MovecraftLocation newOrigin) {
        this.origin = newOrigin;
        return this;
    }

    public Location toBukkit(World world) {
        return (this.getLocation()).toBukkit(world);
    }
}
