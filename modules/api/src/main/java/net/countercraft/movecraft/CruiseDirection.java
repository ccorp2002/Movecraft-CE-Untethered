package net.countercraft.movecraft;

import org.bukkit.block.BlockFace;

public enum CruiseDirection {
    NORTH((byte) 0x3), //0x3
    SOUTH((byte) 0x2), //0x2
    EAST((byte) 0x4), //0x4
    WEST((byte) 0x5), //0x5
    UP((byte) 0x42), //0x42
    DOWN((byte) 0x43), //0x43
    NONE((byte) 0x0);

    private final byte raw;

    CruiseDirection(byte rawDirection) {
        raw = rawDirection;
    }

    public byte getRaw() {
        return raw;
    }
    public static CruiseDirection fromRaw(byte rawDirection) {
        if(rawDirection == (byte) 0x3)
            return NORTH;
        else if(rawDirection == (byte) 0x2)
            return SOUTH;
        else if(rawDirection == (byte) 0x4)
            return EAST;
        else if(rawDirection == (byte) 0x5)
            return WEST;
        else if(rawDirection == (byte) 0x42)
            return UP;
        else if(rawDirection == (byte) 0x43)
            return DOWN;
        else
            return NONE;
    }
    public CruiseDirection getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            case UP -> DOWN;
            case DOWN -> UP;
            case NONE -> NONE;
        };
    }

    public CruiseDirection getRotated(MovecraftRotation rotation) {
        return switch(rotation) {
            case CLOCKWISE -> switch (this) {
                case NORTH -> EAST;
                case SOUTH -> WEST;
                case EAST -> SOUTH;
                case WEST -> NORTH;
                default -> this;
            };
            case ANTICLOCKWISE -> getRotated(MovecraftRotation.CLOCKWISE).getOpposite();
            case NONE -> this;
        };
    }

    public static boolean isOpposite(CruiseDirection a, CruiseDirection b) {
        if(a == UP && b == DOWN)
            return true;
        else if(a == NORTH && b == SOUTH)
            return true;
        else if(a == EAST && b == WEST)
            return true;
        else 
            return false;
    }

    public static boolean isClockwise(CruiseDirection a, CruiseDirection b) {
        if(a == UP && b == DOWN)
            return false;
        else if((a == NORTH && b == EAST) || (a == EAST && b == SOUTH) || (a == SOUTH && b == WEST) || (a == WEST && b == NORTH))
            return true;
        else if((b == NORTH && a == EAST) || (b == EAST && a == SOUTH) || (b == SOUTH && a == WEST) || (b == WEST && a == NORTH))
            return true;
        else 
            return false;
    }

    public static boolean isAntiClockwise(CruiseDirection a, CruiseDirection b) {
        if(a == UP && b == DOWN)
            return false;
        else if((b == NORTH && a == EAST) || (b == EAST && a == SOUTH) || (b == SOUTH && a == WEST) || (b == WEST && a == NORTH))
            return true;
        else 
            return false;
    }

    public static CruiseDirection fromBlockFace(BlockFace direction) {
        if(direction.getOppositeFace() == BlockFace.NORTH)
            return NORTH;
        else if(direction.getOppositeFace() == BlockFace.SOUTH)
            return SOUTH;
        else if(direction.getOppositeFace() == BlockFace.EAST)
            return EAST;
        else if(direction.getOppositeFace() == BlockFace.WEST)
            return WEST;
        else if(direction.getOppositeFace() == BlockFace.UP)
            return UP;
        else if(direction.getOppositeFace() == BlockFace.DOWN)
            return DOWN;
        else
            return NONE;
    }

    public BlockFace toBlockFace() {
        return switch (this) {
            case NORTH -> BlockFace.NORTH;
            case SOUTH -> BlockFace.SOUTH;
            case EAST -> BlockFace.EAST;
            case WEST -> BlockFace.WEST;
            case UP -> BlockFace.UP;
            case DOWN -> BlockFace.DOWN;
            case NONE -> BlockFace.SELF;
        };
    }
}

