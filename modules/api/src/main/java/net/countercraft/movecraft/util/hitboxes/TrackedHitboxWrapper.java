package net.countercraft.movecraft.util.hitboxes;

import org.bukkit.World;
import org.bukkit.Location;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.exception.EmptyHitBoxException;
import net.countercraft.movecraft.util.MathUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class TrackedHitboxWrapper {
    public MutableHitBox relBox;
    private MovecraftLocation origin;

    /**
     * Creates a new TrackedHitboxWrapper instance which tracks a location about an origin.
     * @param origin The origin point at which the tracked location rotates about.
     * @param relBox The absolute location to track.
     */
    public TrackedHitboxWrapper(@NotNull HitBox box) {
        this.relBox = new BitmapHitBox();
        this.origin = new MovecraftLocation(0,0,0);
        for (MovecraftLocation loc : box) {
            this.relBox.add(loc.subtract(this.origin));
        }
    }

    public TrackedHitboxWrapper(@NotNull MovecraftLocation origin, @NotNull HitBox box) {
        this.relBox = new BitmapHitBox();
        this.origin = origin;
        for (MovecraftLocation loc : box) {
            this.relBox.add(loc.subtract(this.origin));
        }
    }

    public TrackedHitboxWrapper(@NotNull TrackedHitboxWrapper origin) {
        this.relBox = new BitmapHitBox();
        this.origin = origin.getOrigin();
        this.relBox = origin.getRelativeBox();
    }

    public TrackedHitboxWrapper(@NotNull TrackedHitboxWrapper origin, int dx, int dy, int dz) {
        this.relBox = new BitmapHitBox();
        this.origin = origin.getOrigin().translate(dx,dy,dz);
        this.relBox = origin.getRelativeBox();
    }

    @NotNull
    public Iterator<MovecraftLocation> iterator() {
        return this.toHitBox().asSet().iterator();
    }
    @NotNull
    public Iterator<MovecraftLocation> iterator(@NotNull TrackedHitboxWrapper wrapper) {
        return wrapper.toHitBox().asSet().iterator();
    }

    public HitBox translateBox(int dx, int dy, int dz) {
        this.origin = this.origin.translate(dx,dy,dz);
        return this.toHitBox();
    }

    public HitBox translateBox(MovecraftLocation displ) {
        this.origin = this.origin.translate(displ);
        return this.toHitBox();
    }

    public TrackedHitboxWrapper translateSelf(int dx, int dy, int dz) {
        this.origin = this.origin.translate(dx,dy,dz);
        return this;
    }

    public TrackedHitboxWrapper translateSelf(MovecraftLocation displ) {
        this.origin = this.origin.translate(displ);
        return this;
    }

    /**
     * Rotates the stored location about the origin point location.
     * @param rotation A clockwise or counter-clockwise direction to rotate.
     */

    public HitBox rotateBox(MovecraftLocation origin, MovecraftRotation rotation) {
        final MutableHitBox rotated = new BitmapHitBox();
        for (MovecraftLocation loc : this.relBox) {
            MovecraftLocation rotLoc = MathUtils.rotateVec(rotation,loc.subtract(origin)).add(origin);
            rotated.add(rotLoc);
        }
        return rotated;
    }

    public TrackedHitboxWrapper rotateNewTracked(MovecraftLocation origin, MovecraftRotation rotation) {
        final MutableHitBox rotated = new BitmapHitBox();
        for (MovecraftLocation loc : this.relBox) {
            MovecraftLocation rotLoc = MathUtils.rotateVec(rotation,loc.subtract(origin)).add(origin);
            rotated.add(rotLoc);
        }
        return new TrackedHitboxWrapper(origin,rotated);
    }

    public TrackedHitboxWrapper rotateSelf(MovecraftLocation origin, MovecraftRotation rotation) {
        final MutableHitBox rotated = new BitmapHitBox();
        for (MovecraftLocation loc : this.relBox) {
            MovecraftLocation rotLoc = MathUtils.rotateVec(rotation,loc.subtract(origin)).add(origin);
            rotated.add(rotLoc);
        }
        this.relBox = rotated;
        return this;
    }

    public TrackedHitboxWrapper setHitBox(MutableHitBox box) {
        this.relBox.clear();
        for (MovecraftLocation loc : box) {
            this.relBox.add(loc.subtract(this.origin));
        }
        return this;
    }

    public int getCenterX() {
        return relBox.getMidPoint().getX() + origin.getX();
    }

    public int getCenterY() {
        return relBox.getMidPoint().getY() + origin.getY();
    }

    public int getCenterZ() {
        return relBox.getMidPoint().getZ() + origin.getZ();
    }

    public String toString(){
        return "(" + origin.getX() + "," + origin.getY() + "," + origin.getZ() +")";
    }

    /**
     * Gets the stored location.
     * @return Returns the location.
     */
    public MovecraftLocation getOrigin() {
        return this.origin;
    }
    public TrackedHitboxWrapper setOrigin(MovecraftLocation newOrigin) {
        this.origin = newOrigin;
        return this;
    }

    public MutableHitBox getRelativeBox() {
        return this.relBox;
    }

    public MutableHitBox toHitBox() {
        final MutableHitBox box = new BitmapHitBox();
        for (MovecraftLocation loc : this.relBox) {
            box.add(loc.add(origin));
        }
        return box;
    }

    public MutableHitBox toHitBox(MovecraftLocation center) {
        final MutableHitBox box = new BitmapHitBox();
        for (MovecraftLocation loc : this.relBox) {
            box.add(loc.add(center));
        }
        return box;
    }
}
