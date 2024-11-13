package net.countercraft.movecraft.util;

import java.util.EnumMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;


public class DirectionalUtils {


	// TAKEN FROM CRAFTBUKKIT TRIGMATH (Sorry!!!) 

	static final double sq2p1 = 2.414213562373095048802e0;
	static final double sq2m1 = .414213562373095048802e0;
	static final double p4 = .161536412982230228262e2;
	static final double p3 = .26842548195503973794141e3;
	static final double p2 = .11530293515404850115428136e4;
	static final double p1 = .178040631643319697105464587e4;
	static final double p0 = .89678597403663861959987488e3;
	static final double q4 = .5895697050844462222791e2;
	static final double q3 = .536265374031215315104235e3;
	static final double q2 = .16667838148816337184521798e4;
	static final double q1 = .207933497444540981287275926e4;
	static final double q0 = .89678597403663861962481162e3;
	static final double PIO2 = 1.5707963267948966135E0;

	private static double TrigMath_mxatan(double arg) {
			double argsq = arg * arg, value;

			value = ((((p4 * argsq + p3) * argsq + p2) * argsq + p1) * argsq + p0);
			value = value / (((((argsq + q4) * argsq + q3) * argsq + q2) * argsq + q1) * argsq + q0);
			return value * arg;
	}

	private static double TrigMath_msatan(double arg) {
			return arg < sq2m1 ? TrigMath_mxatan(arg)
						: arg > sq2p1 ? PIO2 - TrigMath_mxatan(1 / arg)
						: PIO2 / 2 + TrigMath_mxatan((arg - 1) / (arg + 1));
	}

	private static double TrigMath_atan(double arg) {
			return arg > 0 ? TrigMath_msatan(arg) : -TrigMath_msatan(-arg);
	}

	private static double TrigMath_atan2(double arg1, double arg2) {
			if (arg1 + arg2 == arg1)
					return arg1 >= 0 ? PIO2 : -PIO2;
			arg1 = TrigMath_atan(arg1 / arg2);
			return arg2 < 0 ? arg1 <= 0 ? arg1 + Math.PI : arg1 - Math.PI : arg1;
	}

	// TAKEN FROM BKCOMMONLIB UTILS (Also Sorry!!!)


	private static final int CHUNK_BITS = 4;
	private static final int CHUNK_VALUES = 16;
	public static final float DEGTORAD = 0.017453293F;
	public static final float RADTODEG = 57.29577951F;
	public static final double HALFROOTOFTWO = 0.707106781;

	public static final BlockFace[] AXIS = new BlockFace[4];
	public static final BlockFace[] RADIAL = {BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST};
	public static final BlockFace[] BLOCK_SIDES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
	public static final BlockFace[] ATTACHEDFACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP};
	public static final BlockFace[] ATTACHEDFACESDOWN = BLOCK_SIDES;
	private static final EnumMap<BlockFace, Integer> notches = new EnumMap<BlockFace, Integer>(BlockFace.class);

	static {
		for (int i = 0; i < RADIAL.length; i++) {
			notches.put(RADIAL[i], i);
		}
		for (int i = 0; i < AXIS.length; i++) {
			AXIS[i] = RADIAL[i << 1];
		}
	}

	/**
	 * Gets the inverse tangent of the value in degrees
	 * 
	 * @param value
	 * @return inverse tangent angle in degrees
	 */
	public static float atan(double value) {
		return RADTODEG * (float) atan(value);
	}

	/**
	 * Gets the inverse tangent angle in degrees of the rectangle vector
	 * 
	 * @param y axis
	 * @param x axis
	 * @return inverse tangent 2 angle in degrees
	 */
	public static float atan2(double y, double x) {
		return RADTODEG * (float) TrigMath_atan2(y, x);
	}

	/**
	 * Gets the floor integer value from a double value
	 * 
	 * @param value to get the floor of
	 * @return floor value
	 */
	public static int floor(double value) {
		int i = (int) value;
		return value < (double) i ? i - 1 : i;
	}

	/**
	 * Gets the floor integer value from a float value
	 * 
	 * @param value to get the floor of
	 * @return floor value
	 */
	public static int floor(float value) {
		int i = (int) value;
		return value < (float) i ? i - 1 : i;
	}

	/**
	 * Gets the ceiling integer value from a double value
	 * 
	 * @param value to get the ceiling of
	 * @return ceiling value
	 */
	public static int ceil(double value) {
		return -floor(-value);
	}

	/**
	 * Gets the ceiling integer value from a float value
	 * 
	 * @param value to get the ceiling of
	 * @return ceiling value
	 */
	public static int ceil(float value) {
		return -floor(-value);
	}
	/**
	 * Gets the Notch integer representation of a BlockFace<br>
	 * <b>These are the horizontal faces, which exclude up and down</b>
	 * 
	 * @param face to get
	 * @return Notch of the face
	 */
	public static int faceToNotch(BlockFace face) {
		Integer notch = notches.get(face);
		return notch == null ? 0 : notch.intValue();
	}

	/**
	 * Checks whether a given face is an offset along the X-axis
	 * 
	 * @param face to check
	 * @return True if it is along the X-axis, False if not
	 */
	public static boolean isAlongX(BlockFace face) {
		return face.getModX() != 0 && face.getModZ() == 0;
	}

	/**
	 * Checks whether a given face is an offset along the Y-axis
	 * 
	 * @param face to check
	 * @return True if it is along the Y-axis, False if not
	 */
	public static boolean isAlongY(BlockFace face) {
		return isVertical(face);
	}

	/**
	 * Checks whether a given face is an offset along the Z-axis
	 * 
	 * @param face to check
	 * @return True if it is along the Z-axis, False if not
	 */
	public static boolean isAlongZ(BlockFace face) {
		return face.getModZ() != 0 && face.getModX() == 0;
	}

	/**
	 * Gets the Block Face at the notch index specified<br>
	 * <b>These are the horizontal faces, which exclude up and down</b>
	 * 
	 * @param notch to get
	 * @return BlockFace of the notch
	 */
	public static BlockFace notchToFace(int notch) {
		return RADIAL[notch & 0x7];
	}

	/**
	 * Rotates a given Block Face horizontally
	 * 
	 * @param from face
	 * @param notchCount to rotate at
	 * @return rotated face
	 */
	public static BlockFace rotate(BlockFace from, int notchCount) {
		return notchToFace(faceToNotch(from) + notchCount);
	}

	/**
	 * Combines two non-subcardinal faces into one face<br>
	 * - NORTH and WEST returns NORTH_WEST<br>
	 * - NORTH and SOUTH returns NORTH (not possible to combine)
	 * 
	 * @param from face to combined
	 * @param to face to combined
	 * @return the combined face
	 */
	public static BlockFace combine(BlockFace from, BlockFace to) {
		if (from == BlockFace.NORTH) {
			if (to == BlockFace.WEST) {
				return BlockFace.NORTH_WEST;
			} else if (to == BlockFace.EAST) {
				return BlockFace.NORTH_EAST;
			}
		} else if (from == BlockFace.EAST) {
			if (to == BlockFace.NORTH) {
				return BlockFace.NORTH_EAST;
			} else if (to == BlockFace.SOUTH) {
				return BlockFace.SOUTH_EAST;
			}
		} else if (from == BlockFace.SOUTH) {
			if (to == BlockFace.WEST) {
				return BlockFace.SOUTH_WEST;
			} else if (to == BlockFace.EAST) {
				return BlockFace.SOUTH_EAST;
			}
		} else if (from == BlockFace.WEST) {
			if (to == BlockFace.NORTH) {
				return BlockFace.NORTH_WEST;
			} else if (to == BlockFace.SOUTH) {
				return BlockFace.SOUTH_WEST;
			}
		}
		return from;
	}

	/**
	 * Subtracts two faces
	 * 
	 * @param face1
	 * @param face2 to subtract from face1
	 * @return Block Face result ofthe subtraction
	 */
	public static BlockFace subtract(BlockFace face1, BlockFace face2) {
		return notchToFace(faceToNotch(face1) - faceToNotch(face2));
	}

	/**
	 * Adds two faces together
	 * 
	 * @param face1
	 * @param face2 to add to face1
	 * @return Block Face result of the addition
	 */
	public static BlockFace add(BlockFace face1, BlockFace face2) {
		return notchToFace(faceToNotch(face1) + faceToNotch(face2));
	}

	/**
	 * Gets all the individual faces represented by a Block Face<br>
	 * - NORTH_WEST returns NORTH and WEST<br>
	 * - NORTH returns NORTH and SOUTH<br>
	 * 
	 * @param main face to get the faces for
	 * @return an array of length 2 containing all the faces
	 */
	public static BlockFace[] getFaces(BlockFace main) {
		switch (main) {
			case SOUTH_EAST:
				return new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST};
			case SOUTH_WEST:
				return new BlockFace[] {BlockFace.SOUTH, BlockFace.WEST};
			case NORTH_EAST:
				return new BlockFace[] {BlockFace.NORTH, BlockFace.EAST};
			case NORTH_WEST:
				return new BlockFace[] {BlockFace.NORTH, BlockFace.WEST};
			default:
				return new BlockFace[] {main, main.getOppositeFace()};
		}
	}

	/**
	 * Gets the direction a minecart faces when on a given track
	 * 
	 * @param raildirection of the rails
	 * @return minecart direction
	 */
	public static BlockFace getRailsCartDirection(final BlockFace raildirection) {
		switch (raildirection) {
			case NORTH_EAST:
			case SOUTH_WEST:
				return BlockFace.NORTH_WEST;
			case NORTH_WEST:
			case SOUTH_EAST:
				return BlockFace.SOUTH_WEST;
			default:
				return raildirection;
		}
	}

	/**
	 * Gets the rail direction from a Direction<br>
	 * NORTH becomes SOUTH and WEST becomes EAST
	 * 
	 * @param direction to convert
	 * @return rail direction
	 */
	public static BlockFace toRailsDirection(BlockFace direction) {
		switch (direction) {
			case NORTH:
				return BlockFace.SOUTH;
			case WEST:
				return BlockFace.EAST;
			default:
				return direction;
		}
	}

	/**
	 * Gets whether a given Block Face is sub-cardinal (such as NORTH_WEST)
	 * 
	 * @param face to check
	 * @return True if sub-cardinal, False if not
	 */
	public static boolean isSubCardinal(final BlockFace face) {
		switch (face) {
			case NORTH_EAST:
			case SOUTH_EAST:
			case SOUTH_WEST:
			case NORTH_WEST:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a face is up or down
	 * 
	 * @param face to check
	 * @return True if it is UP or DOWN
	 */
	public static boolean isVertical(BlockFace face) {
		return face == BlockFace.UP || face == BlockFace.DOWN;
	}

	/**
	 * Gets the BlockFace.UP or BlockFace.DOWN constant based on the up parameter
	 * 
	 * @param up parameter
	 * @return UP if up is true, DOWN if up is false
	 */
	public static BlockFace getVertical(boolean up) {
		return up ? BlockFace.UP : BlockFace.DOWN;
	}

	/**
	 * Gets the BlockFace.UP or BlockFace.DOWN based on the delta-y parameter
	 * 
	 * @param dy parameter
	 * @return UP if dy >= 0, DOWN if dy < 0
	 */
	public static BlockFace getVertical(double dy) {
		return getVertical(dy >= 0.0);
	}

	/**
	 * Gets whether two faces have a sub-cardinal difference or less
	 * 
	 * @param face1 to check
	 * @param face2 to check
	 * @return True if the difference <= 45 degrees
	 */
	public static boolean hasSubDifference(final BlockFace face1, final BlockFace face2) {
		return getFaceYawDifference(face1, face2) <= 45;
	}

	/**
	 * Gets the Vector direction from a Block Face
	 * 
	 * @param face to use
	 * @param length of the vector
	 * @return Vector of the direction and length
	 */
	public static Vector faceToVector(BlockFace face, double length) {
		return faceToVector(face).multiply(length);
	}

	/**
	 * Gets the Vector direction from a Block Face
	 * 
	 * @param face to use
	 * @return Vector of the direction and length 1
	 */
	public static Vector faceToVector(BlockFace face) {
		return new Vector(face.getModX(), face.getModY(), face.getModZ());
	}

	public static Vector addSub(Vector face, int i) {
		return new Vector(face.getBlockX()+i, face.getBlockY()+i, face.getBlockZ()+i);
	}


	/**
	 * Gets the Block Face direction to go from one point to another
	 * 
	 * @param from point
	 * @param to point
	 * @param useSubCardinalDirections setting
	 * @return the Block Face of the direction
	 */
	public static BlockFace getDirection(Location from, Location to, boolean useSubCardinalDirections) {
		return getDirection(to.getX() - from.getX(), to.getZ() - from.getZ(), useSubCardinalDirections);
	}

	/**
	 * Gets the Block Face direction to go from one block to another
	 * 
	 * @param from block
	 * @param to block
	 * @param useSubCardinalDirections setting
	 * @return the Block Face of the direction
	 */
	public static BlockFace getDirection(Block from, Block to, boolean useSubCardinalDirections) {
		return getDirection(to.getX() - from.getX(), to.getZ() - from.getZ(), useSubCardinalDirections);
	}

	/**
	 * Gets the Block Face direction to go into the movement vector direction
	 * 
	 * @param movement vector
	 * @return the Block Face of the direction
	 */
	public static BlockFace getDirection(Vector movement) {
		return getDirection(movement, true);
	}

	/**
	 * Gets the Block Face direction to go into the movement vector direction
	 * 
	 * @param movement vector
	 * @param useSubCardinalDirections setting
	 * @return the Block Face of the direction
	 */
	public static BlockFace getDirection(Vector movement, boolean useSubCardinalDirections) {
		return getDirection(movement.getX(), movement.getZ(), useSubCardinalDirections);
	}

	/**
	 * Gets the Block Face direction to go into the movement vector direction
	 * 
	 * @param dx vector axis
	 * @param dz vector axis
	 * @param useSubCardinalDirections setting
	 * @return the Block Face of the direction
	 */
	public static BlockFace getDirection(final double dx, final double dz, boolean useSubCardinalDirections) {
		return yawToFace(getLookAtYaw(dx, dz), useSubCardinalDirections);
	}

	/**
	 * Gets the yaw angle in degrees difference between two Block Faces
	 * @param face1
	 * @param face2
	 * @return angle in degrees
	 */
	public static int getFaceYawDifference(BlockFace face1, BlockFace face2) {
		return getAngleDifference(faceToYaw(face1), faceToYaw(face2));
	}



	public static float getLookAtYaw(Location loc, Location lookat) {
		return getLookAtYaw(lookat.getX() - loc.getX(), lookat.getZ() - loc.getZ());
	}

	public static float getLookAtYaw(Vector motion) {
		return getLookAtYaw(motion.getX(), motion.getZ());
	}

	/**
	 * Gets the angle difference between two angles
	 * 
	 * @param angle1
	 * @param angle2
	 * @return angle difference
	 */
	public static int getAngleDifference(int angle1, int angle2) {
		return (int)Math.abs(wrapAngle(angle1 - angle2));
	}

	/**
	 * Gets the angle difference between two angles
	 * 
	 * @param angle1
	 * @param angle2
	 * @return angle difference
	 */
	public static float getAngleDifference(float angle1, float angle2) {
		return Math.abs(wrapAngle(angle1 - angle2));
	}

	/**
	 * Gets the horizontal look-at angle in degrees to look into the 2D-direction specified
	 * 
	 * @param dx axis of the direction
	 * @param dz axis of the direction
	 * @return the angle in degrees
	 */
	public static float getLookAtYaw(double dx, double dz) {
		return atan2(dz, dx) - 180f;
	}

	/**
	 * Gets the co-sinus value from a Block Face treated as an Angle
	 * 
	 * @param face to get the co-sinus value from
	 * @return co-sinus value
	 */
	public static double cos(final BlockFace face) {
		switch (face) {
			case SOUTH_WEST:
			case NORTH_WEST:
				return -HALFROOTOFTWO;
			case SOUTH_EAST:
			case NORTH_EAST:
				return HALFROOTOFTWO;
			case EAST:
				return 1;
			case WEST:
				return -1;
			default:
				return 0;
		}
	}

	/**
	 * Gets the sinus value from a Block Face treated as an Angle
	 * 
	 * @param face to get the sinus value from
	 * @return sinus value
	 */
	public static double sin(final BlockFace face) {
		switch (face) {
			case NORTH_EAST:
			case NORTH_WEST:
				return -HALFROOTOFTWO;
			case SOUTH_WEST:
			case SOUTH_EAST:
				return HALFROOTOFTWO;
			case NORTH:
				return -1;
			case SOUTH:
				return 1;
			default:
				return 0;
		}
	}

	/**
	 * Gets the angle from a horizontal Block Face
	 * 
	 * @param face to get the angle for
	 * @return face angle
	 */
	public static int faceToYaw(final BlockFace face) {
		return (int)wrapAngle(45 * faceToNotch(face));
	}

	/**
	 * Gets the horizontal Block Face from a given yaw angle<br>
	 * This includes the NORTH_WEST faces
	 * 
	 * @param yaw angle
	 * @return The Block Face of the angle
	 */
	public static BlockFace yawToFace(float yaw) {
		return yawToFace(yaw, true);
	}

	/**
	 * Gets the horizontal Block Face from a given yaw angle
	 * 
	 * @param yaw angle
	 * @param useSubCardinalDirections setting, True to allow NORTH_WEST to be returned
	 * @return The Block Face of the angle
	 */
	public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
		if (useSubCardinalDirections) {
			return RADIAL[Math.round(yaw / 45f) & 0x7];
		} else {
			return AXIS[Math.round(yaw / 90f) & 0x3];
		}
	}


	/**
	 * Wraps the angle to be between -180 and 180 degrees
	 * 
	 * @param angle to wrap
	 * @return [-180 > angle >= 180]
	 */
	public static float wrapAngle(float angle) {
		float wrappedAngle = angle;
		while (wrappedAngle <= -180f) {
			wrappedAngle += 360f;
		}
		while (wrappedAngle > 180f) {
			wrappedAngle -= 360f;
		}
		return wrappedAngle;
	}
}