package net.countercraft.movecraft.compat.v1_19_R3;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.WorldHandler;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.util.CollectionUtils;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.util.UnsafeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.util.RandomSource;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.InventoryView;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R3.util.RandomSourceWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("unused")
public class IWorldHandler extends WorldHandler {
    private static final Rotation ROTATION[];
    static {
        ROTATION = new Rotation[3];
        ROTATION[MovecraftRotation.NONE.ordinal()] = Rotation.NONE;
        ROTATION[MovecraftRotation.CLOCKWISE.ordinal()] = Rotation.CLOCKWISE_90;
        ROTATION[MovecraftRotation.ANTICLOCKWISE.ordinal()] = Rotation.COUNTERCLOCKWISE_90;
    }
    private final BlockData air = Bukkit.getServer().createBlockData("minecraft:air");
    private static final Plugin PLUGIN = Bukkit.getPluginManager().getPlugin("Movecraft");
    private final NextTickProvider tickProvider = new NextTickProvider();
    private static final RandomSource RANDOM = new RandomSourceWrapper(new java.util.Random());


    public IWorldHandler() {}

    public boolean doesObjectContainField(Object object, String fieldName) {
        return Arrays.stream(object.getClass().getFields())
                .anyMatch(f -> f.getName().equals(fieldName));
    }
    @Override
    public boolean runTaskInCraftWorld(@NotNull Runnable runMe, Craft craft) {
        return runTaskInWorld(runMe,craft.getWorld());
    }
    @Override
    public boolean runTaskInWorld(@NotNull Runnable runMe, org.bukkit.World world) {
        return false;
    }

    @Override
    public void rotateCraft(@NotNull Craft craft, @NotNull MovecraftLocation originPoint, @NotNull MovecraftRotation rotation) {
        //*******************************************
        //*      Step one: Convert to Positions     *
        //*******************************************
        HashMap<BlockPos, BlockPos> rotatedPositions = new HashMap<>();
        MovecraftRotation counterRotation = rotation == MovecraftRotation.CLOCKWISE ? MovecraftRotation.ANTICLOCKWISE : MovecraftRotation.CLOCKWISE;
        //  HashMap<Location, BlockData> sendAir = new HashMap<>();
        for (MovecraftLocation newLocation : craft.getHitBox()) {
            rotatedPositions.put(locationToPosition(MathUtils.rotateVec(counterRotation, newLocation.subtract(originPoint)).add(originPoint)), locationToPosition(newLocation));
        }
        //*******************************************
        //*         Step two: Get the tiles         *
        //*******************************************
        ServerLevel nativeWorld = ((CraftWorld) craft.getWorld()).getHandle();
        List<TileHolder> tiles = new ArrayList<>();
        //get the tiles
        for (BlockPos position : rotatedPositions.keySet()) {
            //BlockEntity tile = nativeWorld.removeBlockEntity(position);
            BlockEntity tile = removeBlockEntity(nativeWorld, position);
            if (tile == null)
                continue;
    //            tile.a(ROTATION[rotation.ordinal()]);
            //get the nextTick to move with the tile
            tiles.add(new TileHolder(tile, tickProvider.getNextTick(nativeWorld, position), position));
        }

        //*******************************************
        //*   Step three: Translate all the blocks  *
        //*******************************************
        // blockedByWater=false means an ocean-going vessel
        //TODO: Simplify
        //TODO: go by chunks
        //TODO: Don't move unnecessary blocks
        //get the blocks and rotate them
        HashMap<BlockPos, BlockState> blockData = new HashMap<>();
        HashMap<BlockPos, BlockState> redstoneComps = new HashMap<>();
        for (BlockPos position : rotatedPositions.keySet()) {
            final BlockState data = nativeWorld.getBlockState(position).rotate(ROTATION[rotation.ordinal()]);
            blockData.put(position, data);
        }
        //MovecraftLocation.sendBlockUpdated(craft,sendAir);
        //create the new block
        for (Map.Entry<BlockPos, BlockState> entry : blockData.entrySet()) {
            setBlockFastest(nativeWorld, rotatedPositions.get(entry.getKey()), entry.getValue());
            if (isRedstoneComponent(entry.getValue().getBlock())) redstoneComps.put(rotatedPositions.get(entry.getKey()), entry.getValue()); //Determine Redstone Blocks
        }


        //*******************************************
        //*    Step four: replace all the tiles     *
        //*******************************************
        //TODO: go by chunks
        for (TileHolder tileHolder : tiles) {
            moveBlockEntity(nativeWorld, rotatedPositions.get(tileHolder.getTilePosition()), tileHolder.getTile());
            if (tileHolder.getNextTick() == null)
                continue;
            final long currentTime = nativeWorld.J.getGameTime();
            nativeWorld.getBlockTicks().schedule(new ScheduledTick<>((Block) tileHolder.getNextTick().type(), rotatedPositions.get(tileHolder.getNextTick().pos()), tileHolder.getNextTick().triggerTick() - currentTime, tileHolder.getNextTick().priority(), tileHolder.getNextTick().subTickOrder()));
        }

        //*******************************************
        //*   Step five: Destroy the leftovers      *
        //*******************************************
        //TODO: add support for pass-through
        Collection<BlockPos> deletePositions = CollectionUtils.oldFilter(rotatedPositions.keySet(), rotatedPositions.values());
        for (BlockPos position : deletePositions) {
            setBlockFastest(nativeWorld, position, Blocks.AIR.defaultBlockState());
        }
        processLight(craft.getHitBox(),craft.getWorld());
        processRedstone(redstoneComps.keySet(), nativeWorld);
    }

    @Override
    public void translateCraft(@NotNull Craft craft, @NotNull MovecraftLocation displacement, @NotNull org.bukkit.World world) {
        //TODO: Add support for rotations
        //A craftTranslateCommand should only occur if the craft is moving to a valid position
        //*******************************************
        //*      Step one: Convert to Positions     *
        //*******************************************
        BlockPos translateVector = locationToPosition(displacement);
        List<BlockPos> redstoneComps = new ArrayList<>(craft.getHitBox().size());
        List<BlockPos> positions = new ArrayList<>(craft.getHitBox().size());
        craft.getHitBox().forEach((movecraftLocation) -> positions.add(locationToPosition((movecraftLocation)).subtract(translateVector)));
        ServerLevel oldNativeWorld = ((CraftWorld) craft.getWorld()).getHandle();
        ServerLevel nativeWorld = ((CraftWorld) world).getHandle();
        //*******************************************
        //*         Step two: Get the tiles         *
        //*******************************************
        List<TileHolder> tiles = new ArrayList<>();
        //get the tiles
        for (BlockPos position : positions) {
            if (oldNativeWorld.getBlockState(position) == Blocks.AIR.defaultBlockState())
                continue;
            //BlockEntity tile = nativeWorld.removeBlockEntity(position);
            BlockEntity tile = removeBlockEntity(oldNativeWorld, position);
            if (tile == null)
                continue;
            //get the nextTick to move with the tile

            //nativeWorld.capturedTileEntities.remove(position);
            //nativeWorld.getChunkAtWorldCoords(position).getTileEntities().remove(position);
            tiles.add(new TileHolder(tile, tickProvider.getNextTick(oldNativeWorld, position), position));

        }
        //*******************************************
        //*   Step three: Translate all the blocks  *
        //*******************************************
        // blockedByWater=false means an ocean-going vessel
        //TODO: Simplify
        //TODO: go by chunks
        //TODO: Don't move unnecessary blocks
        //get the blocks and translate the positions
        List<BlockState> blockData = new ArrayList<>();
        List<BlockPos> newPositions = new ArrayList<>();
      //  HashMap<Location, BlockData> sendAir = new HashMap<>(positions.size());
        for (BlockPos position : positions) {
            blockData.add(oldNativeWorld.getBlockState(position));
            newPositions.add(position.offset(translateVector));
            BlockPos pos = position;
            //Location bloc = new Location(craft.getWorld(),pos.getX(),pos.getY(),pos.getZ());
            //sendAir.put(bloc,air);
        }
        //MovecraftLocation.sendBlockUpdated(craft,sendAir);
        //create the new block
        //HashMap<Location, BlockData> sendBlocks = new HashMap<>(newPositions.size());
        //final HashMap<BlockPos, BlockState> fireBlocks = new HashMap<>();
        for (int i = 0, positionSize = newPositions.size(); i < positionSize; i++) {
            final BlockState data = blockData.get(i);
            final BlockPos position = newPositions.get(i);
            setBlockFastest(nativeWorld, position, data);
            if (isRedstoneComponent(nativeWorld.getBlockState(position).getBlock())) redstoneComps.add(position); //Determine Redstone Blocks
        }
        //*******************************************
        //*    Step four: replace all the tiles     *
        //*******************************************
        //TODO: go by chunks
        for (TileHolder tileHolder : tiles) {
            moveBlockEntity(nativeWorld, tileHolder.getTilePosition().offset(translateVector), tileHolder.getTile());
            if (tileHolder.getNextTick() == null)
                continue;
            final long currentTime = nativeWorld.getGameTime();
            nativeWorld.getBlockTicks().schedule(new ScheduledTick<>((Block) tileHolder.getNextTick().type(), tileHolder.getTilePosition().offset(translateVector), tileHolder.getNextTick().triggerTick() - currentTime, tileHolder.getNextTick().priority(), tileHolder.getNextTick().subTickOrder()));
        }
        //MovecraftLocation.sendBlockUpdated(craft,sendBlocks);
        //*******************************************
        //*   Step five: Destroy the leftovers      *
        //*******************************************
        List<BlockPos> deletePositions = positions;
        if (oldNativeWorld == nativeWorld) deletePositions = CollectionUtils.oldFilter(positions,newPositions);
        for (BlockPos position : deletePositions) {
            setBlockFastest(oldNativeWorld, position, Blocks.AIR.defaultBlockState());
        }

        //*******************************************
        //*      Step six: Process redstone         *
        //*******************************************
        processLight(craft.getHitBox(),craft.getWorld());
        processRedstone(redstoneComps, nativeWorld);

        //*******************************************
        //*        Step seven: Process fire         *
        //*******************************************X
    }

    @Override
    public void disableShadow(@NotNull Material type) {
        // Disabled
    }

    @Override
    public void processLight(HitBox hitBox, @NotNull World world) {
        long delay = 30L;
        if (hitBox.size() >= 12000)
            delay += 60;
        if (hitBox.size() >= 20000)
            delay += 120;
        if (hitBox.size() >= 40000)
            delay += 240;
        if (hitBox.size() >= 128000)
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                ServerLevel nativeWorld = ((CraftWorld) world).getHandle();
                for (MovecraftLocation loc: hitBox) {
                    nativeWorld.getLightEngine().checkBlock(locationToPosition(loc));
                }
            }
        }.runTaskLater(PLUGIN, (delay*20));
    }

    @Nullable
    private BlockEntity removeBlockEntity(@NotNull Level world, @NotNull BlockPos position){
        return world.getChunkAt(position).blockEntities.remove(position);
    }

    @NotNull
    public BlockPos locationToPosition(@NotNull MovecraftLocation loc) {
        return new BlockPos(loc.getX(), loc.getY(), loc.getZ());
    }

    @NotNull
    public BlockPos locationToPosition(@NotNull Location loc) {
        return new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @NotNull
    public MovecraftLocation positionToLocation(@NotNull BlockPos pos) {
        return new MovecraftLocation(pos.getX(), pos.getY(), pos.getZ());
    }
    public org.bukkit.Chunk getChunkFastest(Location loc) {
        final BlockPos pos = locationToPosition(loc);
        final ServerLevel world = ((CraftWorld) loc.getWorld()).getHandle();
        LevelChunk chunk = (LevelChunk) ((ServerLevel)world).getChunkIfLoaded(pos.getX()>>4,pos.getZ()>>4);
        if (chunk == null) chunk = world.getChunkAt(pos);
        final org.bukkit.Chunk bukkitChunk = new org.bukkit.craftbukkit.v1_19_R3.CraftChunk(chunk);
        return bukkitChunk;
    }
    @Nullable
    public BlockState getNMSBlockFast(@NotNull MovecraftLocation location, World world) {
        if (world == null)
            return null;
        ServerLevel level = ((CraftWorld) world).getHandle();
        BlockPos position = new BlockPos(location.getX(), location.getY(), location.getZ());
        return getBlockFastest(level,position);
    }

    @Nullable
    public BlockState getBlockFastest(@NotNull Level world, @NotNull BlockPos position) {
        LevelChunk chunk = (LevelChunk) ((ServerLevel)world).getChunkIfLoaded(position.getX()>>4,position.getZ()>>4);
        if (chunk == null) chunk = world.getChunkAt(position);
        int chunkSection = (position.getY() >> 4) - chunk.getMinSection();
        LevelChunkSection section = chunk.getSections()[chunkSection];
        BlockState data = section.getBlockState(position.getX() & 15, position.getY() & 15, position.getZ() & 15);
        return data;
    }

    @Nullable
    public org.bukkit.block.BlockState getBukkitState(@NotNull Location location) {
        org.bukkit.block.Block data = getBukkitBlockFast(location);
        if (data == null) return null;
        return data.getState();
    }

    @Nullable
    public org.bukkit.Material toBukkitBlockFast(@NotNull Location location) {
        if (location.getWorld() == null)
            return null;
        return getBukkitBlockFast(location).getType();
    }
    @Nullable
    public org.bukkit.Material toBukkitBlockFast(@NotNull MovecraftLocation location, World world) {
        if (world == null)
            return null;
        return getBukkitBlockFast(location,world).getType();
    }
    @Nullable
    public org.bukkit.block.Block getBukkitBlockFast(@NotNull MovecraftLocation location, World world) {
        if (world == null)
            return null;
        ServerLevel level = ((CraftWorld) world).getHandle();
        BlockPos position = new BlockPos(location.getX(), location.getY(), location.getZ());
        LevelChunk chunk = (LevelChunk) level.getChunkIfLoaded(position.getX() >> 4, position.getZ() >> 4);
        if (chunk == null) chunk = level.getChunkAt(position);
        org.bukkit.block.Block block = (org.bukkit.block.Block)CraftBlock.at(level, position);
        if (block == null) block = location.toBukkit(world).getBlock();
        return block;
    }
    @Nullable
    public org.bukkit.block.Block getBukkitBlockFast(@NotNull Location location) {
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        BlockPos position = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        LevelChunk chunk = (LevelChunk) level.getChunkIfLoaded(position.getX() >> 4, position.getZ() >> 4);
        if (chunk == null) chunk = level.getChunkAt(position);
        org.bukkit.block.Block block = (org.bukkit.block.Block)CraftBlock.at(level, position);
        if (block == null) block = location.getBlock();
        return block;
    }
    private void setBlockFastest(@NotNull Level world, @NotNull BlockPos position, @NotNull BlockState data) {
        LevelChunk chunk = (LevelChunk) world.getChunkAt(position);
        if (chunk == null) chunk = world.getChunkAt(position);
        int chunkSection = (position.getY() >> 4) - chunk.getMinSection();
        LevelChunkSection section = chunk.getSections()[chunkSection];
        if (section == null) {
            // Put a GLASS block to initialize the section. It will be replaced next with the real block.
            chunk.setBlockState(position, Blocks.GLASS.defaultBlockState(), false);
            section = chunk.getSections()[chunkSection];
        }
        if (getBlockFastest(world,position).equals(data)) {
            //Block is already of correct type and data, don't overwrite
            return;
        }
        if (chunk.blockEntities.get(position) != null || data.equals(Blocks.AIR.defaultBlockState())) {
            section.setBlockState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, data);
          //  ((ServerLevel)world).getChunkSource().blockChanged(position);
            ((ServerLevel)world).sendBlockUpdated(position, data, data, 3);
        } else {
            section.setBlockState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, data);
          //  ((ServerLevel)world).getChunkSource().blockChanged(position);
            //((ServerLevel)world).sendBlockUpdated(position, data, data, 1);
            ((ServerLevel)world).getChunkSource().blockChanged(position);
        }
        chunk.setUnsaved(true);
    }


    private void setBlockFast(@NotNull Level world, @NotNull BlockPos position, @NotNull BlockState data) {
        LevelChunk chunk = world.getChunkAt(position);
        int chunkSection = (position.getY() >> 4) - chunk.getMinSection();
        LevelChunkSection section = chunk.getSections()[chunkSection];
        if (section == null) {
            // Put a GLASS block to initialize the section. It will be replaced next with the real block.
            chunk.setBlockState(position, Blocks.GLASS.defaultBlockState(), false);
            section = chunk.getSections()[chunkSection];
        }
        if (section.getBlockState(position.getX() & 15, position.getY() & 15, position.getZ() & 15).equals(data)) {
            //Block is already of correct type and data, don't overwrite
            return;
        }
        section.setBlockState(position.getX() & 15, position.getY() & 15, position.getZ() & 15, data);
        world.sendBlockUpdated(position, data, data, 1);
        chunk.setUnsaved(true);
    }


    private void processRedstone(Collection<BlockPos> redstone, Level world) {
        for (final BlockPos pos : redstone) {
            BlockState data = world.getBlockState(pos);
            world.updateNeighborsAt(pos, data.getBlock());
            world.sendBlockUpdated(pos, data, data, 1);
            if (isToggleableRedstoneComponent(data.getBlock())) {
                data.getBlock().tick(data,(ServerLevel)world,pos,RANDOM);
            }
        }
    }

    private boolean isRedstoneComponent(Block block) {
        return block instanceof RedStoneWireBlock ||
                block instanceof DiodeBlock ||
                block instanceof TargetBlock ||
                block instanceof PressurePlateBlock ||
                block instanceof ButtonBlock ||
                block instanceof BasePressurePlateBlock ||
                block instanceof LeverBlock ||
                block instanceof HopperBlock ||
                block instanceof ObserverBlock ||
                block instanceof DaylightDetectorBlock ||
                block instanceof DispenserBlock ||
                block instanceof RedstoneLampBlock ||
                block instanceof RedstoneTorchBlock ||
                block instanceof ComparatorBlock ||
                block instanceof SculkSensorBlock ||
                block instanceof PistonBaseBlock ||
                block instanceof MovingPistonBlock;
    }
    private boolean isToggleableRedstoneComponent(Block block) {
        return block instanceof PressurePlateBlock ||
                block instanceof ButtonBlock ||
                block instanceof BasePressurePlateBlock ||
                block instanceof RedstoneLampBlock ||
                block instanceof RedstoneTorchBlock;
    }
    @Override
    public @Nullable Location getAccessLocation(@NotNull InventoryView inventoryView) {
        return null;
    }

    @Override
    public void setAccessLocation(@NotNull InventoryView inventoryView, @NotNull Location location) {}

    @Override
    public void setBlockFast(@NotNull Location location, @NotNull BlockData data){
        setBlockFast(location, MovecraftRotation.NONE, data);
    }

    @Override
    public void setBlockFast(@NotNull Location location, @NotNull Material mat){
        setBlockFast(location, MovecraftRotation.NONE, mat.createBlockData());
    }

    public void setBlockFast(@NotNull org.bukkit.World world, @NotNull MovecraftLocation location, @NotNull BlockData data){
        setBlockFast(world, location, MovecraftRotation.NONE, data);
    }

    public void setBlockFast(@NotNull org.bukkit.World world, @NotNull MovecraftLocation location, @NotNull MovecraftRotation rotation, @NotNull BlockData data) {
        BlockState blockData;
        if(data instanceof CraftBlockData){
            blockData = ((CraftBlockData) data).getState();
        } else {
            blockData = (BlockState) data;
        }
        blockData = blockData.rotate(ROTATION[rotation.ordinal()]);
        Level nmsWorld = ((CraftWorld)(world)).getHandle();
        BlockPos BlockPos = locationToPosition(location);
        setBlockFastest(nmsWorld, BlockPos, blockData);
    }

    @Override
    public void setBlockFast(@NotNull Location location, @NotNull MovecraftRotation rotation, @NotNull BlockData data) {
        BlockState blockData;
        if (data instanceof CraftBlockData) {
            blockData = ((CraftBlockData) data).getState();
        }
        else {
            blockData = (BlockState) data;
        }
        blockData = blockData.rotate(ROTATION[rotation.ordinal()]);
        Level world = ((CraftWorld) (location.getWorld())).getHandle();
        BlockPos BlockPos = locationToPosition(MathUtils.bukkit2MovecraftLoc(location));
        setBlockFastest(world, BlockPos, blockData);
    }

    private void moveBlockEntity(@NotNull Level nativeWorld, @NotNull BlockPos newPosition, @NotNull BlockEntity tile) {
        LevelChunk chunk = nativeWorld.getChunkAt(newPosition);
        try {
            var positionField = BlockEntity.class.getDeclaredField("p"); // p is obfuscated worldPosition
            UnsafeUtils.setField(positionField, tile, newPosition);
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        tile.setLevel(nativeWorld);
        tile.clearRemoved();
        if (nativeWorld.captureBlockStates) {
            setBlockFastest(nativeWorld,newPosition,tile.getBlockState());
            nativeWorld.capturedTileEntities.put(newPosition, tile);
            return;
        }
        setBlockFastest(nativeWorld,newPosition,tile.getBlockState());
        chunk.setBlockEntity(tile);
        chunk.blockEntities.put(newPosition, tile);
    }

    private static class TileHolder {
        @NotNull
        private final BlockEntity tile;
        @Nullable
        private final ScheduledTick<?> nextTick;
        @NotNull
        private final BlockPos tilePosition;

        public TileHolder(@NotNull BlockEntity tile, @Nullable ScheduledTick<?> nextTick, @NotNull BlockPos tilePosition) {
            this.tile = tile;
            this.nextTick = nextTick;
            this.tilePosition = tilePosition;
        }


        @NotNull
        public BlockEntity getTile() {
            return tile;
        }

        @Nullable
        public ScheduledTick<?> getNextTick() {
            return nextTick;
        }

        @NotNull
        public BlockPos getTilePosition() {
            return tilePosition;
        }
    }
}
