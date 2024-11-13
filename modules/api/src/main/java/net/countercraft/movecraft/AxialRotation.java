
package net.countercraft.movecraft;


import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dropper;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.plugin.Plugin;
import org.bukkit.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

public enum AxialRotation {

	LYAW, RYAW, LROLL, RROLL, LPITCH, RPITCH;

	public boolean isRight() {
		return this == RYAW || this == RROLL || this == RPITCH;
	}

	public boolean isYaw() {
		return this == RYAW || this == LYAW;
	}

	public boolean isRoll() {
		return this == RROLL || this == LROLL;
	}

	public boolean isPitch() {
		return this == RPITCH || this == LPITCH;
	}

	public AxialRotation getOpposite() {
		if (this == RYAW) return LYAW;
		if (this == RROLL) return LROLL;
		if (this == RPITCH) return LPITCH;
		return null;
	}

	public double getDistance(Block axis, Location location) {
		Location loc = location.clone();
		if (this.isYaw()) loc.setY(axis.getY());
		else if (this.isRoll()) loc.setZ(axis.getZ());
		else if (this.isPitch()) loc.setX(axis.getX());
		return axis.getLocation().distance(loc);
	}

	public Vector getHeight(Block axis, Location locaction) {
		if (this.isYaw()) return new Vector(0d, locaction.getY() - axis.getY(), 0d);
		else if (this.isRoll()) return new Vector(0d, 0d, locaction.getZ() - axis.getZ());
		else if (this.isPitch()) return new Vector(locaction.getX() - axis.getX(), 0d, 0d);
		else return new Vector();
	}

	private double getArc(double x, double y) {
		if (x > 1) x = 1;
		else if (x < -1) x = -1;
		return Math.acos(x) * (y < 0 ? -1 : 1);
	}

	public double getArc(Vector vec) {
		if (isYaw()) return getArc(vec.getX(), vec.getZ());
		else if (isRoll()) return getArc(vec.getY(), vec.getX());
		else if (isPitch()) return getArc(vec.getZ(), vec.getY());
		throw new NullPointerException();
	}

	public double getModX(double rad) {
		if (isYaw()) return Math.cos(rad);
		else if (isRoll()) return Math.sin(rad);
		else return 0;
	}

	public double getModY(double rad) {
		if (isRoll()) return Math.cos(rad);
		else if (isPitch()) return Math.sin(rad);
		else return 0;
	}

	public double getModZ(double rad) {
		if (isYaw()) return Math.sin(rad);
		else if (isPitch()) return Math.cos(rad);
		else return 0;
	}

	public static AxialRotation getRotation(BlockFace blockFace, boolean right) {
		if (blockFace == BlockFace.NORTH) {
			if (right) return RROLL;
			else return LROLL;
		} else if (blockFace == BlockFace.SOUTH) {
			if (right) return LROLL;
			else return RROLL;
		} else if (blockFace == BlockFace.EAST) {
			if (right) return RPITCH;
			else return LPITCH;
		} else if (blockFace == BlockFace.WEST) {
			if (right) return LPITCH;
			else return RPITCH;
		} else if (blockFace == BlockFace.UP) {
			if (right) return RYAW;
			else return LYAW;
		} else if (blockFace == BlockFace.DOWN) {
			if (right) return LYAW;
			else return RYAW;
		} else return null;
	}
  public static BlockData rotateBlockData(AxialRotation rotation, BlockData data) {
    if (rotation == null)
      return data;
    if (data instanceof Orientable) {
      Orientable orientable = (Orientable)data;
      Set<Axis> axes = orientable.getAxes();
      Axis axis = orientable.getAxis();
      if (rotation.isYaw()) {
        if (axes.contains(Axis.Z) && axes.contains(Axis.X)) {
          if (axis == Axis.Z)
            orientable.setAxis(Axis.X);
          if (axis == Axis.X)
            orientable.setAxis(Axis.Z);
        }
      } else if (rotation.isRoll()) {
        if (axes.contains(Axis.X) && axes.contains(Axis.Y)) {
          if (axis == Axis.X)
            orientable.setAxis(Axis.Y);
          if (axis == Axis.Y)
            orientable.setAxis(Axis.X);
        }
      } else if (rotation.isPitch() &&
        axes.contains(Axis.Y) && axes.contains(Axis.Z)) {
        if (axis == Axis.Y)
          orientable.setAxis(Axis.Z);
        if (axis == Axis.Z)
          orientable.setAxis(Axis.Y);
      }
      data = (BlockData)orientable;
    }
    if (data instanceof Directional) {
      Directional directional = (Directional)data;
      BlockFace facing = directional.getFacing();
      try {
        if (rotation.isYaw()) {
          if (rotation.isRight()) {
            switch (facing) {
              case NORTH:
                directional.setFacing(BlockFace.EAST);
                break;
              case EAST:
                directional.setFacing(BlockFace.SOUTH);
                break;
              case SOUTH:
                directional.setFacing(BlockFace.WEST);
                break;
              case WEST:
                directional.setFacing(BlockFace.NORTH);
                break;
            }
          } else {
            switch (facing) {
              case NORTH:
                directional.setFacing(BlockFace.WEST);
                break;
              case WEST:
                directional.setFacing(BlockFace.SOUTH);
                break;
              case SOUTH:
                directional.setFacing(BlockFace.EAST);
                break;
              case EAST:
                directional.setFacing(BlockFace.NORTH);
                break;
            }
          }
        } else if (rotation.isRoll()) {
          if (rotation.isRight()) {
            switch (facing) {
              case EAST:
                directional.setFacing(BlockFace.DOWN);
                break;
              case DOWN:
                directional.setFacing(BlockFace.WEST);
                break;
              case WEST:
                directional.setFacing(BlockFace.UP);
                break;
              case UP:
                directional.setFacing(BlockFace.EAST);
                break;
            }
          } else {
            switch (facing) {
              case EAST:
                directional.setFacing(BlockFace.UP);
                break;
              case UP:
                directional.setFacing(BlockFace.WEST);
                break;
              case WEST:
                directional.setFacing(BlockFace.DOWN);
                break;
              case DOWN:
                directional.setFacing(BlockFace.EAST);
                break;
            }
          }
        } else if (rotation.isPitch()) {
          if (rotation.isRight()) {
            switch (facing) {
              case NORTH:
                directional.setFacing(BlockFace.DOWN);
                break;
              case DOWN:
                directional.setFacing(BlockFace.SOUTH);
                break;
              case SOUTH:
                directional.setFacing(BlockFace.UP);
                break;
              case UP:
                directional.setFacing(BlockFace.NORTH);
                break;
            }
          } else {
            switch (facing) {
              case NORTH:
                directional.setFacing(BlockFace.UP);
                break;
              case UP:
                directional.setFacing(BlockFace.SOUTH);
                break;
              case SOUTH:
                directional.setFacing(BlockFace.DOWN);
                break;
              case DOWN:
                directional.setFacing(BlockFace.NORTH);
                break;
            }
          }
        }
        data = (BlockData)directional;
      }
     catch (IllegalArgumentException illegalArgumentException) {}
    }
    return data;
  }
}
