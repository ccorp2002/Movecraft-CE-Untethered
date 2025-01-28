package net.countercraft.movecraft.mapUpdater.update;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.TrackedLocation;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.WorldHandler;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.PlayerCraftImpl;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.SignTranslateEvent;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.util.hitboxes.SetHitBox;
import net.countercraft.movecraft.util.hitboxes.SolidHitBox;
import net.countercraft.movecraft.util.pathfinding.RunPathfinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;

import java.lang.Runnable;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

public class CraftTranslateCommand extends UpdateCommand {
    @NotNull private final Craft craft;
    @NotNull private final MovecraftLocation displacement;
    @NotNull private final World world;

    public CraftTranslateCommand(@NotNull Craft craft, @NotNull MovecraftLocation displacement) {
        this.craft = craft;
        this.displacement = displacement;
        this.world = craft.getWorld();
    }

    public CraftTranslateCommand(@NotNull Craft craft, @NotNull MovecraftLocation displacement, @NotNull World world) {
        this.craft = craft;
        this.displacement = displacement;
        this.world = world;
    }




    @Override
    public void doUpdate() {
        final Logger logger = Movecraft.getInstance().getLogger();
        if(craft.getHitBox().isEmpty()){
            logger.warning("Attempted to move craft with empty HashHitBox!");
            CraftManager.getInstance().release(craft, CraftReleaseEvent.Reason.EMPTY, false);
            return;
        }
        /*if (craft instanceof BaseCraft) {
            if (((BaseCraft)craft).getRawTrackedMap().size() > 0) {
                for (Object key : ((BaseCraft)craft).getRawTrackedMap().keySet()) {
                    final List<TrackedLocation> clone = new ArrayList<>();
                    for (TrackedLocation tracked : ((BaseCraft)craft).getRawTrackedMap().get(key)) {
                        if (tracked != null) {
                            clone.add(((TrackedLocation)tracked).translateTracked(displacement));
                        }
                    }
                    if (!clone.isEmpty() || clone != null) {
                        ((BaseCraft)craft).getRawTrackedMap().put(key,clone);
                    }
                }
            }
        }*/
        final World oldWorld = craft.getWorld();
        final Set<Material> passthroughBlocks = new HashSet<>(
                craft.getType().getMaterialSetProperty(CraftType.PASSTHROUGH_BLOCKS));
        boolean ran = false;
        final WorldHandler handler = Movecraft.getInstance().getWorldHandler();
        final Runnable runMe = new Runnable() {
            @Override
            public void run() {
                if ((craft).getRawTrackedMap().size() > 0) {
                    for (Object key : (craft).getRawTrackedMap().keySet()) {
                        final ArrayList<TrackedLocation> clone = new ArrayList<>();
                        for (final TrackedLocation tracked : (craft).getRawTrackedMap().get(key)) {
                            if (tracked != null) {
                                clone.add(((TrackedLocation) tracked).translateTracked(displacement));
                            }
                        }
                        if (!clone.isEmpty() || clone != null || clone.size() > 0) {
                            (craft).getRawTrackedMap().put(key,clone);
                        }
                    }
                }
                long time = System.nanoTime();
                int waterline = craft.getWaterLine();
                if (craft.getSinking()) {
                    passthroughBlocks.addAll(Tags.FLUID);
                    passthroughBlocks.addAll(Tag.LEAVES.getValues());
                    passthroughBlocks.addAll(Tags.SINKING_PASSTHROUGH);
                }
                final SetHitBox originalLocations = new SetHitBox();
                for (MovecraftLocation movecraftLocation : craft.getHitBox()) {
                    originalLocations.add(movecraftLocation.subtract(displacement));
                }
                final Set<MovecraftLocation> to = Sets.difference(craft.getHitBox().asSet(), originalLocations.asSet());
                //place phased blocks
                for (MovecraftLocation location : to) {
                    BlockData data = location.toBukkit(craft.getWorld()).getBlock().getBlockData();
                    if ((data instanceof Waterlogged))
                        ((Waterlogged)data).setWaterlogged(false);
                    if (passthroughBlocks.contains(data.getMaterial())) {
                        craft.getPhaseBlocks().put(location.toBukkit(craft.getWorld()), data);
                    }
                }
                if (craft.getOrigBlockCount()>=450000 || (world.getName().contains("Void") || world.getName().contains("Orbit") || world.getName().contains("Space")) || (!craft.getType().getBoolProperty(CraftType.DETECT_INTERIOR))) {

                    /*if (craft.getWorld().equals(world)) {
                        ran = handler.runTaskInWorld(() -> {
                                handler.translateCraft(craft,displacement,world); 
                            }, world);
                    } else {
                        ran = true;
                        handler.translateCraft(craft,displacement,world);
                    }*/
                    handler.translateCraft(craft,displacement,world);
                    craft.setWorld(world);
                    //trigger sign events
                    //waterlog();
                    time = System.nanoTime() - time;
                    if(Settings.Debug)
                        logger.info("Total time: " + (time / 1e6) + " milliseconds. Moving with cooldown of " + craft.getTickCooldown() + ". Speed of: " + String.format("%.2f", craft.getSpeed()) + ". Displacement of: " + displacement);

                    // Only add cruise time if cruising
                    if(craft.getCruising() && displacement.getY() == 0 && (displacement.getX() == 0 || displacement.getZ() == 0)) {}
                    craft.addCruiseTime(time / 1e9f);
                    sendSignEvents();
                    if (!craft.isNotProcessing()) craft.setProcessing(false);
                    return;
                } else {
                    //The subtraction of the set of coordinates in the HitBox cube and the HitBox itself

                    //Check to see which locations in the from set are actually outside of the craft
                    Set<MovecraftLocation> failed = new HashSet<>();
                    List<MovecraftLocation> confirmed = new ArrayList<>();
                    
                    try {
                        if (CraftManager.getInstance().getCraftList().contains(craft)) {
                            if ((craft).getTrackedLocations("valid_exterior").size() >= 1) {
                                confirmed = (craft).getTrackedMovecraftLocs("valid_exterior");
                            } else {
                                CraftManager.getInstance().detectCraftExterior(craft);
                                confirmed = (craft).getTrackedMovecraftLocs("valid_exterior");
                            }
                            failed.addAll(craft.getTrackedMovecraftLocs("failed"));
                            if (failed.size() <= 1) {
                                final Set<MovecraftLocation> invertedHitBox = Sets.difference(craft.getHitBox().boundingHitBox().asSet(), craft.getHitBox().asSet());
                                failed = (Sets.difference(invertedHitBox, Sets.newHashSet(confirmed)));
                                (craft).setTrackedMovecraftLocs("failed",failed);
                            }
                        } else {
                            throw new IllegalStateException("Craft not found in CraftManager, defaulting to Basic Movecraft");
                        }
                    } catch (Exception exc) {
                        if (Settings.Debug) logger.warning("Craft not found in CraftManager, defaulting to Base Movecraft");
                        final var invertedHitBox = Sets.difference(
                                craft.getHitBox().boundingHitBox().asSet(), craft.getHitBox().asSet());

                        //place phased blocks
                        final Set<Location> overlap = Sets.newHashSet(craft.getPhaseBlocks().keySet());
                        overlap.removeIf((location -> !craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(location))));
                        final int minX = craft.getHitBox().getMinX();
                        final int maxX = craft.getHitBox().getMaxX();
                        final int minY = craft.getHitBox().getMinY();
                        final int maxY = overlap.isEmpty() ? craft.getHitBox().getMaxY() : Collections.max(overlap,
                                Comparator.comparingInt(Location::getBlockY)).getBlockY();
                        final int minZ = craft.getHitBox().getMinZ();
                        final int maxZ = craft.getHitBox().getMaxZ();
                        final HitBox[] surfaces = {
                                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(minX, maxY, maxZ)),
                                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, minZ)),
                                new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(minX, maxY, maxZ)),
                                new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(maxX, maxY, minZ)),
                                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, minY, maxZ))
                        };
                        final SetHitBox validExterior = new SetHitBox();
                        for (HitBox hitBox : surfaces) {
                            validExterior.addAll(Sets.difference(hitBox.asSet(),craft.getHitBox().asSet()));
                        }

                        //A set of locations that are confirmed to be "exterior" locations
                        failed.addAll(Sets.difference(invertedHitBox, Sets.newHashSet(confirmed)));
                        for (MovecraftLocation location : failed) {
                            if (location.getY() <= waterline) {
                                craft.getPhaseBlocks().put(location.toBukkit(oldWorld), Movecraft.getInstance().getWaterBlockData());
                            }
                            if (!passthroughBlocks.contains(location.toBukkit(world).getBlock().getType()))
                                continue;
                            var data = location.toBukkit(world).getBlock().getBlockData();
                            craft.getPhaseBlocks().put(location.toBukkit(world), data);
                        }
                        for (MovecraftLocation l : failed){
                            MovecraftLocation orig = l.subtract(displacement);
                            if (craft.getHitBox().contains(orig) || failed.contains(orig)){
                                continue;
                            }
                            confirmed.add(orig);
                        }
                        (craft).setTrackedMovecraftLocs("failed",failed);
                    }
                    //translate the craft
                    /*if (craft.getWorld().equals(world)) {
                        ran = handler.runTaskInWorld(() -> {
                                handler.translateCraft(craft,displacement,world); 
                            }, world);
                    } else {
                        ran = true;
                        handler.translateCraft(craft,displacement,world);
                    }*/
                    handler.translateCraft(craft,displacement,world);
                    craft.setWorld(world);
                    //trigger sign events
                    sendSignEvents();
                    //place confirmed blocks if they have been un-phased
                    final SetHitBox airBox = new SetHitBox((craft).getTrackedMovecraftLocs("air"));
                    for (MovecraftLocation location : confirmed) {
                        if (airBox.contains(location)) continue;
                        final Location bukkit = location.toBukkit(craft.getWorld());
                        if (!(craft.getPhaseBlocks().containsKey(bukkit))) continue;

                        //Do not place if it is at a collapsed HitBox location
                        if (!craft.getCollapsedHitBox().isEmpty() && craft.getCollapsedHitBox().contains(location)) continue;
                        var phaseBlock = craft.getPhaseBlocks().remove(bukkit);
                        if (handler.toBukkitBlockFast(bukkit).isAir()){
                            handler.setBlockFast(bukkit, phaseBlock);
                        } else if (Tags.FLUID.contains(handler.toBukkitBlockFast(bukkit))){
                            handler.setBlockFast(bukkit, phaseBlock);
                        }
                    }
                    for (MovecraftLocation location : originalLocations) {
                        if (airBox.contains(location)) continue;
                        final Location bukkit = location.toBukkit(oldWorld);
                        if(!craft.getHitBox().contains(location) && craft.getPhaseBlocks().containsKey(bukkit)){
                            var phaseBlock = craft.getPhaseBlocks().remove(bukkit);
                            if (handler.toBukkitBlockFast(bukkit).isAir()){
                                handler.setBlockFast(bukkit, phaseBlock);
                            } else if (Tags.FLUID.contains(handler.toBukkitBlockFast(bukkit))){
                                handler.setBlockFast(bukkit, phaseBlock);
                            }
                        }
                    }

                    for (MovecraftLocation location : airBox) {
                        Location bukkit = location.toBukkit(craft.getWorld());
                        if (handler.toBukkitBlockFast(bukkit).isAir()){
                            continue;
                        } else if (Tags.FLUID.contains(handler.toBukkitBlockFast(bukkit))){
                            handler.setBlockFast(bukkit,Movecraft.getInstance().getAirBlockData());
                        }
                    }
                    if (waterline >= -120) {
                        for (MovecraftLocation location : craft.getHitBox().boundingHitBox()) {
                            if (airBox.contains(location)) continue;
                            if (location.getY() > waterline) continue;
                            if(!craft.getHitBox().contains(location)){
                                Location bukkit = location.toBukkit(craft.getWorld());
                                if (handler.toBukkitBlockFast(bukkit).isAir()){
                                    handler.setBlockFast(bukkit, Movecraft.getInstance().getWaterBlockData());
                                } else if (Tags.FLUID.contains(handler.toBukkitBlockFast(bukkit))){
                                    handler.setBlockFast(bukkit,Movecraft.getInstance().getWaterBlockData());
                                }
                            }
                        }
                    } 
                    if (!craft.isNotProcessing()) craft.setProcessing(false);
                    time = System.nanoTime() - time;
                    if(Settings.Debug)
                        logger.info("Total time: " + (time / 1e6) + " milliseconds. Moving with cooldown of " + craft.getTickCooldown() + ". Speed of: " + String.format("%.2f", craft.getSpeed()) + ". Displacement of: " + displacement);

                    // Only add cruise time if cruising
                    if(craft.getCruising() && displacement.getY() == 0 && (displacement.getX() == 0 || displacement.getZ() == 0)) {}
                    craft.addCruiseTime(time / 1e9f);
                }
                if (waterline >= -120) waterlog(handler);
            }
        };
        if (oldWorld.equals(world) && world.equals(craft.getWorld())) {
            ran = handler.runTaskInWorld(runMe,oldWorld);
        } else {
            ran = false;
        }
        if (!ran) {
            Bukkit.getScheduler().runTask(Movecraft.getInstance(), runMe);
        }
    }

    @NotNull
    public Collection<MovecraftLocation> verifyExterior(Collection<MovecraftLocation> invertedHitBox, SetHitBox validExterior) {
        if (getCraft() instanceof BaseCraft) {
            if ((((BaseCraft)getCraft()).getTrackedMovecraftLocs("exterior")) instanceof Collection<MovecraftLocation>) {
                List<MovecraftLocation> exterior = new ArrayList<>(((BaseCraft)getCraft()).getTrackedMovecraftLocs("exterior"));
                if (!(exterior.isEmpty()))
                //System.out.println("FOUND EXISTING HITBOX-LOCS" + exterior);
                    return exterior;
            }
        }
        MovecraftLocation[] shifts = new MovecraftLocation[]{new MovecraftLocation(0, -1, 0), new MovecraftLocation(1, 0, 0), new MovecraftLocation(-1, 0, 0), new MovecraftLocation(0, 0, 1), new MovecraftLocation(0, 0, -1)};
        LinkedHashSet<MovecraftLocation> visited = new LinkedHashSet<MovecraftLocation>(validExterior.asSet());
        ArrayDeque<MovecraftLocation> queue = new ArrayDeque<MovecraftLocation>();
        for (MovecraftLocation node : validExterior) {
            MovecraftLocation[] arrmovecraftLocation = shifts;
            int n = arrmovecraftLocation.length;
            for (int i = 0; i < n; ++i) {
                MovecraftLocation shift = arrmovecraftLocation[i];
                MovecraftLocation shifted = node.add(shift);
                if (!invertedHitBox.contains(shifted) || !visited.add(shifted)) continue;
                queue.add(shifted);
            }
        }
        while (!queue.isEmpty()) {
            MovecraftLocation node = (MovecraftLocation)queue.poll();
            for (MovecraftLocation shift : shifts) {
                MovecraftLocation shifted = node.add(shift);
                if (!invertedHitBox.contains(shifted) || !visited.add(shifted)) continue;
                queue.add(shifted);
            }
        }
        if (getCraft() instanceof BaseCraft) {
          //System.out.println("FOUND HITBOX" + newSetBox);
            ((BaseCraft)getCraft()).setTrackedMovecraftLocs("exterior",visited);
        }
        return visited;
    }

    private void waterlog(final WorldHandler handler) {
        if (getCraft().getHitBox().size() >= 25000) return;
        for (MovecraftLocation location : getCraft().getHitBox()) {
            final BlockData data = handler.getBukkitBlockFast(location, getCraft().getWorld()).getBlockData();
            if (data instanceof Waterlogged wlogged) {
                wlogged.setWaterlogged(false);
                Movecraft.getInstance().getWorldHandler().setBlockFast(location.toBukkit(this.world),wlogged);
            }
        }
    }

    @NotNull
    private LinkedList<MovecraftLocation> hullSearch(SetHitBox validExterior) {
        MovecraftLocation[] shifts = new MovecraftLocation[]{new MovecraftLocation(0, -1, 0), new MovecraftLocation(1, 0, 0), new MovecraftLocation(-1, 0, 0), new MovecraftLocation(0, 0, 1), new MovecraftLocation(0, 0, -1)};
        LinkedList<MovecraftLocation> hull = new LinkedList<MovecraftLocation>();
        HitBox craftBox = this.craft.getHitBox();
        LinkedList<MovecraftLocation> queue = Lists.newLinkedList(validExterior);
        SetHitBox visited = new SetHitBox(validExterior);
        while (!queue.isEmpty()) {
            MovecraftLocation top = (MovecraftLocation)queue.poll();
            if (craftBox.contains(top)) {
                hull.add(top);
            }
            for (MovecraftLocation shift : shifts) {
                MovecraftLocation shifted = top.add(shift);
                if (!craftBox.inBounds(shifted) || !visited.add(shifted)) continue;
                queue.add(shifted);
            }
        }
        return hull;
    }

    private void sendSignEvents(){
        Object2ObjectMap<String[], List<MovecraftLocation>> signs = new Object2ObjectOpenCustomHashMap<>(new Hash.Strategy<String[]>() {
            @Override
            public int hashCode(String[] strings) {
                return Arrays.hashCode(strings);
            }

            @Override
            public boolean equals(String[] a, String[] b) {
                return Arrays.equals(a, b);
            }
        });
        Map<MovecraftLocation, Sign> signStates = new HashMap<>();

        for (MovecraftLocation location : craft.getHitBox()) {
            Block block = location.toBukkit(craft.getWorld()).getBlock();
            if(!Tag.SIGNS.isTagged(block.getType())){
                continue;
            }
            BlockState state = block.getState();
            if (state instanceof Sign sign) {
                if(!signs.containsKey(sign.getLines()))
                    signs.put(sign.getLines(), new ArrayList<>());
                signs.get(sign.getLines()).add(location);
                signStates.put(location, sign);
            }
        }
        for(Map.Entry<String[], List<MovecraftLocation>> entry : signs.entrySet()){
            SignTranslateEvent event = new SignTranslateEvent(craft, entry.getKey(), entry.getValue());
            Bukkit.getServer().getPluginManager().callEvent(event);
            // if(!event.isUpdated()){
            //     continue;
            // }
            // TODO: This is implemented only to fix client caching
            //  ideally we wouldn't do the update and would instead fake it out to the player
            for(MovecraftLocation location : entry.getValue()){
                Block block = location.toBukkit(craft.getWorld()).getBlock();
                BlockState state = block.getState();
                if (!(state instanceof Sign)) {
                    continue;
                }
                Sign sign = signStates.get(location);
                for(int i = 0; i<4; i++){
                    sign.setLine(i, entry.getKey()[i]);
                }
                sign.update(false, false);
            }
        }
    }

    @NotNull
    public Craft getCraft(){
        return craft;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof CraftTranslateCommand)){
            return false;
        }
        CraftTranslateCommand other = (CraftTranslateCommand) obj;
        return other.craft.equals(this.craft) &&
                other.displacement.equals(this.displacement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(craft, displacement);
    }
}
