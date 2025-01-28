
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


package net.countercraft.movecraft.craft;

import org.bukkit.util.Vector;
import org.bukkit.Axis;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Campfire;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Lectern;
import org.bukkit.block.Lockable;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.Structure;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftPreTranslateEvent;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftPreSinkEvent;
import net.countercraft.movecraft.events.FuelBurnEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.TypesReloadedEvent;
import net.countercraft.movecraft.exception.NonCancellableReleaseException;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.processing.CachedMovecraftWorld;
import net.countercraft.movecraft.processing.WorldManager;
import net.countercraft.movecraft.processing.effects.Effect;
import net.countercraft.movecraft.processing.functions.CraftSupplier;
import net.countercraft.movecraft.processing.functions.Result;
import net.countercraft.movecraft.craft.type.property.RequiredBlockProperty;
import net.countercraft.movecraft.craft.type.RequiredBlockEntry;
import net.countercraft.movecraft.processing.tasks.detection.DetectionTask;
import net.countercraft.movecraft.processing.tasks.detection.HitBoxDetectionTask;
import net.countercraft.movecraft.processing.tasks.detection.UnsafeDetectionTask;
import net.countercraft.movecraft.processing.tasks.detection.IgnoreDetectionTask;
import net.countercraft.movecraft.processing.tasks.detection.WorldDetectionTask;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.util.Pair;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.util.CollectionUtils;
import net.countercraft.movecraft.util.hitboxes.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.bukkit.Axis;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Campfire;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Furnace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Lectern;
import org.bukkit.block.Lockable;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.Structure;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.WorldHandler;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftReleaseEvent;
import net.countercraft.movecraft.events.CraftMergeEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.MathUtils;
import net.countercraft.movecraft.events.PlayerCraftMovementEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Maps;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.io.File;
import java.util.Queue;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;



import static net.countercraft.movecraft.util.ItemUtils.reduceItemStack;
import static net.countercraft.movecraft.util.ItemUtils.isSimilar;
import static net.countercraft.movecraft.util.ItemUtils.updateItemDurability;

import static net.countercraft.movecraft.util.ChatUtils.ERROR_PREFIX;

public class CraftManager implements Iterable<Craft>{
    private final String HEADER = "Remote Sign";
    private static CraftManager ourInstance;
    @NotNull public final Set<Craft> crafts = new ConcurrentSkipListSet<>(Comparator.comparingInt(Craft::hashCode));
    @NotNull public final ConcurrentMap<Player, PlayerCraft> craftPlayerIndex = new ConcurrentHashMap<>();
    @NotNull private Set<CraftType> craftTypes;
    public final ArrayList<ItemStack> fuelTypes = new ArrayList<>();
    public final HashMap<BlockData,Double> armorBlocks = Maps.newHashMap(); //Block Data & Survival Rate / 199
    public final HashMap<ItemStack,Double> fuelTypeMap = Maps.newHashMap();
    public final ArrayList<Double> fuelTypeChance = new ArrayList<>();
    @NotNull private final WeakHashMap<Player, Long> overboards = new WeakHashMap<>();
    @NotNull private final Set<Craft> sinking = Collections.newSetFromMap(new ConcurrentHashMap<>());
    @NotNull private final Set<Craft> craftList = crafts;
    @NotNull private final WorldHandler handler = Movecraft.getInstance().getWorldHandler();
    /**
    * Map of players to their current craft.
    */
    private final java.util.Random rand = new java.util.Random();
    @NotNull public final ConcurrentMap<Player, PlayerCraft> playerCrafts = new ConcurrentHashMap<>();
    @NotNull private final ConcurrentMap<Craft, BukkitTask> releaseEvents = new ConcurrentHashMap<>();
    public static final ItemStack wasteItem = new ItemStack(Material.AIR);
    public static final ItemStack bucketItem = new ItemStack(Material.BUCKET,1);
    public static void initialize(boolean loadCraftTypes) {
        ourInstance = new CraftManager(loadCraftTypes);
    }
    public static <T> int getIndex(Set<T> set, T value) {
        int result = 0;
        for (T entry:set) {
            if (entry.equals(value)) return result;
                result++;
            }
        return -1;
    }
    private CraftManager(boolean loadCraftTypes) {
        armorBlocks.clear();
        this.addFuelType(new ItemStack(Material.COAL),75.0);
        this.addFuelType(new ItemStack(Material.CHARCOAL),75.0);
        this.addFuelType(new ItemStack(Material.COAL_BLOCK),125.0);
        this.addFuelType(new ItemStack(Material.DRIED_KELP_BLOCK),125.0);
        this.addFuelType(new ItemStack(Material.BLAZE_ROD),250.0);
        this.addFuelType(new ItemStack(Material.LAVA_BUCKET),750.0);
        if(loadCraftTypes) {
            this.craftTypes = loadCraftTypes();
        }
        else {
            this.craftTypes = new HashSet<>();
        }
    }
    public java.util.Random rand() {
        return this.rand;
    }
    public boolean isCraftActive(@NotNull Craft craft) {
        return getCrafts().contains(craft);
    }
    public boolean isArmorBlock(@NotNull Block block) {
        return armorBlocks.keySet().contains(block.getBlockData());
    }
    public boolean checkArmorBlock(@NotNull Block block) {
        final double chance = getArmorChance(block);
        if (chance <= 1.0d) return false;
        final double possb = rand.nextDouble(100.0d) + 1;
        return (possb >= chance);
    }
    public double getArmorChance(@NotNull Block block) {
        if (isArmorBlock(block)) return armorBlocks.get(block.getBlockData());
        else return 0.0d;
    }
    public void addArmorType(@NotNull BlockData bd, @NotNull Double chance) {
        armorBlocks.put(bd,chance);
    }

    public static CraftManager getInstance() {
        return ourInstance;
    }
    @NotNull
    public List<ItemStack> getFuelTypes() {
        return fuelTypes;
    }
    @NotNull
    public double getFuelIndexBurnChance(int indx) {
      return fuelTypeChance.get(indx);
    }

    public void detectCraftHealthBlocks(Craft craft) {
        craft.setLastBlockCheck(System.currentTimeMillis());
        HashSet<Block> origin_lift = Sets.newHashSet();
        for (RequiredBlockEntry entry : craft.getType().getRequiredBlockProperty(CraftType.FLY_BLOCKS)) {
            for(Material mat : entry.getMaterials()) {
                origin_lift.addAll(((craft).getBlockType(mat)));
            }
        }
        craft.setTrackedBlocks(origin_lift,"lift_locs");
        craft.setDataTag("origin_lift",(Integer)(origin_lift.size()));
        craft.setDataTag("current_lift", (Integer)(origin_lift.size()));
        HashSet<Block> origin_engine = Sets.newHashSet();
        for(RequiredBlockEntry entry : craft.getType().getRequiredBlockProperty(CraftType.MOVE_BLOCKS)) {
            for (Material mat : entry.getMaterials()) {
                origin_engine.addAll(((craft).getBlockType(mat)));
            }
        }
        craft.setLastBlockCheck(System.currentTimeMillis());
        craft.setDataTag("origin_engine", (Integer)(origin_engine.size()));
        craft.setDataTag("current_engine", (Integer)(origin_engine.size()));
        craft.setTrackedBlocks(origin_engine,"engine_locs");
        if (craft.getDataTag("origin_size") == null) craft.setDataTag("origin_size",(Integer)craft.getOrigBlockCount());
        if (craft.getDataTag("current_size") == null) craft.setDataTag("current_size",(Integer)craft.getOrigBlockCount());
    }

    public void updateCraftHealthBlocks(Craft craft) {
        HashSet<Block> origin_lift = Sets.newHashSet();
        for (RequiredBlockEntry entry : craft.getType().getRequiredBlockProperty(CraftType.FLY_BLOCKS)) {
            for(Material mat : entry.getMaterials()) {
                origin_lift.addAll(((craft).getBlockTypeNoCache(mat)));
            }
        }
        craft.setTrackedBlocks(origin_lift,"lift_locs");
        craft.setDataTag("current_lift", (Integer)(origin_lift.size()));
        HashSet<Block> origin_engine = Sets.newHashSet();
        for(RequiredBlockEntry entry : craft.getType().getRequiredBlockProperty(CraftType.MOVE_BLOCKS)) {
            for (Material mat : entry.getMaterials()) {
                origin_engine.addAll(((craft).getBlockTypeNoCache(mat)));
            }
        }
        craft.setLastBlockCheck(System.currentTimeMillis());
        craft.setDataTag("current_engine", (Integer)(origin_engine.size()));
        craft.setTrackedBlocks(origin_engine,"engine_locs");
        if (craft.getDataTag("current_size") == null) craft.setDataTag("current_size",(Integer)craft.getOrigBlockCount());
    }

    public HitBox translateBox(HitBox box, MovecraftLocation v) {
        SetHitBox newBox = new SetHitBox();
        for(MovecraftLocation oldLoc : box){
          MovecraftLocation newLoc = oldLoc.translate(v.x,v.y,v.z);
          newBox.add(newLoc);
        }
        return newBox;
    }
    public HitBox rotateBox(HitBox box, MovecraftLocation p, MovecraftRotation r) {
        SetHitBox newBox = new SetHitBox();
        for(MovecraftLocation oldLoc : box){
          MovecraftLocation newLoc = MathUtils.rotateVec(r,oldLoc.subtract(p)).add(p);
          newBox.add(newLoc);
        }
        return newBox;
    }
    public void teleportCraft(Craft c, Location bl) {
       int dx, dy, dz = 0;
       World dw = c.getWorld();
       if (bl.getWorld() == null) {
          dw = c.getWorld();
       } else {
          dw = bl.getWorld();
       }
       Location center = c.getHitBox().getMidPoint().toBukkit(c.getWorld());
       dx = bl.getBlockX() - center.getBlockX();
       dy = bl.getBlockY() - center.getBlockY();
       dz = bl.getBlockZ() - center.getBlockZ();
       c.translate(dw,dx,dy,dz);
    }

    @NotNull
    public void addFuelType(ItemStack stack, double chance) {
        fuelTypes.add(stack);
        fuelTypeChance.add(chance);
        fuelTypeMap.put(stack,chance);
    }
    @NotNull
    public Set<CraftType> getCraftTypes() {
        return Collections.unmodifiableSet(craftTypes);
    }
    public Craft sink(@NotNull Craft craft) {
        CraftPreSinkEvent event = new CraftPreSinkEvent(craft);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ((BaseCraft)craft).setSinking(false);
            forceRemoveCraft(craft);
            addCraft(craft);
            return craft;
        }
        ((BaseCraft)craft).setSinking(true);
        forceRemoveCraft(craft);
        Craft sunk = new SinkingCraftImpl(craft);
        CraftSinkEvent sinkevent = new CraftSinkEvent(sunk);
        Bukkit.getServer().getPluginManager().callEvent(sinkevent);
        try {
            ((BaseCraft)sunk).craftTags = ((BaseCraft)craft).craftTags;
            ((BaseCraft)sunk).trackedLocations = ((BaseCraft)craft).trackedLocations;
        } catch(Exception exception) {}
        if (sinkevent.isCancelled()) {
            sunk = craft;
        }
        forceRemoveCraft(craft);
        CraftDetectEvent detect = new CraftDetectEvent(sunk, sunk.getHitBox().getMidPoint());
        Bukkit.getServer().getPluginManager().callEvent(detect);
        if (!detect.isCancelled()) addCraft(sunk);
        (sunk).setSinking(true);
        return sunk;
    }

    public Craft quietSink(@NotNull Craft craft) {
        CraftPreSinkEvent event = new CraftPreSinkEvent(craft);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ((BaseCraft)craft).setSinking(false);
            return craft;
        }
        ((BaseCraft)craft).setSinking(true);
        return craft;
    }

    public boolean forceBurnFuelLater(Craft craft, int addAmount, double addChance) {
        if (!(craft instanceof PlayerCraft)) return true;
        if (!craft.hasMovedWithin(100l) || craft.hasMovedWithin(6000l)) {
            int chance = rand.nextInt((int)craft.getBurningFuel()+1);
            if (chance >= 0 || craft.getBurningFuel()+5 >= chance) craft.setBurningFuel(craft.getBurningFuel()+1);
            return true;
        }
        return forceBurnFuel(craft,addAmount,addChance,true);
    }

    public boolean forceBurnFuel(Craft craft) {
        return forceBurnFuel(craft,1,1,true);
    }

    public boolean forceBurnFuel(Craft craft, int addAmount, double addChance) {
        return forceBurnFuel(craft,addAmount,addChance,false);
    }

    public boolean forceBurnFuel(Craft craft, int addAmount, double addChance, boolean forceRun) {
        if (addAmount <= 0) addAmount = 1;
        if (addChance <= 0.0d) addChance = 1.0d;
        if (craft.getBurningFuel() > 1) addAmount+=craft.getBurningFuel();
        boolean found = false;
        if (craft.getSinking() || craft instanceof SinkingCraft) return true;

        if (craft.getNotificationPlayer() == null) return true;

        if (craft.getType().getDoubleProperty(CraftType.FUEL_BURN_RATE) <= 0.0) return true;
        ItemStack wasteItem = this.wasteItem;
        if (craft instanceof PlayerCraftImpl) {
            if (forceRun) addChance = 100d;
            int iters = 0;
            for (ItemStack istack : fuelTypeMap.keySet()) {
                double fuelBurnChance = fuelTypeMap.get(istack);
                final FuelBurnEvent event = new FuelBurnEvent(craft, istack, fuelBurnChance);
                event.setWasteItem(wasteItem);
                Bukkit.getPluginManager().callEvent(event);
                wasteItem = event.getWasteItem();
                fuelBurnChance = event.getFuelBurnChance();
                istack = event.getBurningFuel();
                found = this.forceCheckFuel(craft,1+((int)(craft.getCurrentGear()/2)+1)+addAmount,fuelBurnChance+addChance,istack,wasteItem,null);
                if (found) break;
                iters++;
            }
        }
        craft.setDataTag("has_fuel",found);
        return found;
    }
    public boolean forceCheckFuel(final Craft craft, int fuelBurnRate, double percBurnChance, ItemStack fuelItem, ItemStack wasteItem, Collection<MovecraftLocation> blockCollection) {


        if (craft.getSinking()) return true;
        if (!(craft instanceof PlayerCraft)) return true;
        int chance = 1;
        if (fuelBurnRate > -5) {
            //Movecraft.getInstance().getLogger().log(Level.INFO, "FUEL-BURN RNG: "+fuelBurnRate+" FUEL-BURN CHANCE: "+percBurnChance);
            Block invBlock = null;
            boolean barrelFound = false;
            final Set<MovecraftLocation> blocks = new HashSet<>();
            if ((((BaseCraft)craft)).getRelativeTrackedLocs("fuel_locations").size() > 0) {
                blocks.addAll((((BaseCraft)craft)).getTrackedMovecraftLocs("fuel_locations"));
            } else {
                blocks.addAll((((BaseCraft)craft)).getBlockTypeLocation(Material.FURNACE));
                if (blocks.size() > 0) ((BaseCraft)craft).setTrackedMovecraftLocs("fuel_locations",blocks);
            }
            if (percBurnChance >= 100) percBurnChance = 101d;
            if (blockCollection != null && !blockCollection.isEmpty()) blocks.addAll(blockCollection);
            for (MovecraftLocation b : blocks) {
                invBlock = Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(b,craft.getWorld());
                if (invBlock == null) continue;
                if (invBlock.getState() instanceof Container state) {
                    if (((state).getInventory().getContents()) == null) continue;
                    Inventory inv = state.getInventory();
                    //ListIterator<ItemStack> listIterator1 = state.getInventory().iterator();
                    for (int itr = 0; itr < inv.getSize(); itr++) {
                    //while (listIterator1.hasNext()) {
                        //ItemStack stack = listIterator1.next();
                        ItemStack stack = inv.getItem(itr);
                        if (stack == null) continue;
                        if (fuelItem != null && (isSimilar(stack,fuelItem))) {
                            if ((int)percBurnChance >= 100d) chance = 101;
                            else chance = rand.nextInt((int)percBurnChance);
                            if (chance >= 99) {
                                stack = reduceItemStack(stack,fuelBurnRate);
                                inv.setItem(itr,stack);
                            //    state.update(false,false);
                                return true;
                            }
                            if ((int)chance <= (int)percBurnChance - 5) {
                                stack = reduceItemStack(stack,fuelBurnRate);
                                inv.setItem(itr,stack);
                            //    state.update(false,false);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }

    public Collection<Block> getAdjacentBlocks(Collection<Material> types, Block start, int max) {
      HashSet<Block> blocks = new HashSet<>();
      HashSet<Block> last = new HashSet<>();
      last.add(start);
      Set<Material> allowed = new HashSet();
      boolean found = true;
      final Craft c = getCraftFromBlock(start);
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
            final Craft detectCraft = getCraftFromBlock(block);
            if (detectCraft != null)
              continue;
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
    public int getItemCountFromCraft(Craft craft, ItemStack item, Collection<Block> blockCollection) {


        if (craft.getSinking())
            return 0;
        if (craft instanceof NPCCraftImpl)
            return 0;
        if (craft instanceof SinkingCraftImpl)
            return 0;
        int foundAmount = 0;
        boolean barrelFound = false;
        final Set<Block> blocks;
        if (blockCollection != null && blockCollection.size() > 0) {
            blocks = Sets.newHashSet();
            blocks.addAll(blockCollection);
        } else {
            blocks = getItemContainersFromCraft(craft,item,null);
            blocks.addAll(blockCollection);
        }
        for (Block invBlock : blocks) {
            if (invBlock == null) continue;
            if (invBlock.getState() instanceof Container container) {
                InventoryHolder inventoryHolder1 = (InventoryHolder)container;
                ListIterator<ItemStack> listIterator1 = inventoryHolder1.getInventory().iterator();
                if((((InventoryHolder)invBlock.getState()).getInventory().getContents()) == null)continue;
                while (listIterator1.hasNext()) {
                    ItemStack stack = listIterator1.next();
                    if (stack == null) continue;
                    if (stack != null && (isSimilar(stack,item))) {
                        foundAmount += stack.getAmount();
                    }
                }
            }
        }
        ArrayList<Block> blockList = Lists.newArrayList(((BaseCraft)craft).getTrackedBlocks("storage_blocks"));
        blocks.addAll(blockList);
        ((BaseCraft)craft).setTrackedBlocks(blocks,"storage_blocks");
        blockList.clear();
        blocks.clear();
        return foundAmount;
    }
    public Set<Block> getAndTrackItemsOnCraft(Craft craft, ItemStack item, Material mat, Collection<Block> blockCollection) {


        final Set<Block> blockList = Sets.newHashSet(getItemContainersFromCraft(craft,item,mat,blockCollection));
        if (craft.getSinking())
            return blockList;
        if (craft instanceof NPCCraftImpl)
            return blockList;
        if (craft instanceof SinkingCraftImpl)
            return blockList;
        ((BaseCraft)craft).setTrackedBlocks(item,blockList);
        return blockList;
    }
    public Set<Block> getAndTrackItemsOnCraft(Craft craft, ItemStack item, Collection<Block> blockCollection) {


        final Set<Block> blockList = Sets.newHashSet(getItemContainersFromCraft(craft,item,blockCollection));
        if (craft.getSinking())
            return blockList;
        if (craft instanceof NPCCraftImpl)
            return blockList;
        if (craft instanceof SinkingCraftImpl)
            return blockList;
        ((BaseCraft)craft).setTrackedBlocks(item,blockList);
        return blockList;
    }
    public Set<Block> removeAndTrackItemsOnCraft(Craft craft, ItemStack item, Collection<Block> blockCollection) {

        return removeAndTrackItemsOnCraft(craft,item,blockCollection,1);
    }

    public Set<Block> removeAndTrackItemsOnCraft(Craft craft, ItemStack item, Collection<Block> blockCollection, int amount) {


        final Set<Block> blockList = Sets.newHashSet(getItemContainersFromCraft(craft,item,blockCollection));
        if (craft.getSinking())
            return blockList;
        if (craft instanceof NPCCraftImpl)
            return blockList;
        if (craft instanceof SinkingCraftImpl)
            return blockList;
        int amnt = getItemCountFromCraft(craft,item,blockList);
        if (amnt < amount) {
            blockList.clear();
            return blockList;
        }
        if (amnt <= 0) {
            blockList.clear();
            return blockList;
        }
        int counter = 0;
        for (Block invBlock : blockList) {
            if (invBlock == null) continue;
            if (invBlock.getState() instanceof Container state) {
                if (((state).getInventory().getContents()) == null) continue;
                Inventory inv = state.getInventory();
                //ListIterator<ItemStack> listIterator1 = state.getInventory().iterator();
                for (int bitr = 0; bitr < amount; bitr++) {
                    for (int itr = 0; itr < inv.getSize(); itr++) {
                    //while (listIterator1.hasNext()) {
                        //ItemStack stack = listIterator1.next();
                        ItemStack stack = inv.getItem(itr);
                        if (stack == null) continue;
                        if (isSimilar(item,stack)) {
                            stack = reduceItemStack(stack,1);
                            inv.setItem(itr,stack);
                            counter++;
                        }
                        if (counter >= amount) break;
                    }
                    if (counter >= amount) break;
                }
            }
            if (counter >= amount) break;
        }
        ((BaseCraft)craft).setTrackedBlocks(item,blockList);
        return blockList;
    }

    public Set<Block> getItemContainersFromCraft(Craft craft, ItemStack item, Material mat, Collection<Block> blockCollection) {


        final Set<Block> blockList = Sets.newHashSet();
        if (craft.getSinking())
            return blockList;
        if (craft instanceof NPCCraftImpl)
            return blockList;
        if (craft instanceof SinkingCraftImpl)
            return blockList;
        final Set<Block> blocks = Sets.newHashSet();
        if (blockCollection != null && blockCollection.size() > 0) {
            if (mat == null) mat = Material.AIR;
            blocks.addAll(blockCollection);
        } else {
            if (mat == null) mat = Material.BARREL;
            blocks.addAll((((BaseCraft)craft)).getBlockTypeNOW(mat));
        }
        for (Block invBlock : blocks) {
            if (invBlock == null) continue;
            if (invBlock.getState() instanceof Container container) {
                InventoryHolder inventoryHolder1 = (InventoryHolder)container;
                ListIterator<ItemStack> listIterator1 = inventoryHolder1.getInventory().iterator();
                if((((InventoryHolder)invBlock.getState()).getInventory().getContents()) == null)continue;
                while (listIterator1.hasNext()) {
                    ItemStack stack = listIterator1.next();
                    if (stack == null) continue;
                    if (stack != null && (isSimilar(stack,item))) {
                        blockList.add(invBlock);
                    }
                }
            }
        }
        return blockList;
    }

    public Set<Block> getItemContainersFromCraft(Craft craft, ItemStack item, Collection<Block> blockCollection) {


        final Set<Block> blockList = Sets.newHashSet();
        if (craft.getSinking())
            return blockList;
        if (craft instanceof NPCCraftImpl)
            return blockList;
        if (craft instanceof SinkingCraftImpl)
            return blockList;
        int foundAmount = 0;
        final Set<Block> blocks = Sets.newHashSet();
        blocks.addAll((((BaseCraft)craft)).getBlockTypeNOW(Material.DISPENSER));
        blocks.addAll((((BaseCraft)craft)).getBlockTypeNOW(Material.DROPPER));
        blocks.addAll((((BaseCraft)craft)).getBlockTypeNOW(Material.FURNACE));
        blocks.addAll((((BaseCraft)craft)).getBlockTypeNOW(Material.BLAST_FURNACE));
        blocks.addAll((((BaseCraft)craft)).getBlockTypeNOW(Material.CHEST));
        blocks.addAll((((BaseCraft)craft)).getBlockTypeNOW(Material.TRAPPED_CHEST));
        blocks.addAll((((BaseCraft)craft)).getBlockTypeNOW(Material.BARREL));
        if (blockCollection != null && blockCollection.size() > 0) blocks.addAll(blockCollection);
        for (Block invBlock : blocks) {
            if (invBlock == null) continue;
            if (invBlock.getState() instanceof Container container) {
                InventoryHolder inventoryHolder1 = (InventoryHolder)container;
                ListIterator<ItemStack> listIterator1 = inventoryHolder1.getInventory().iterator();
                if((((InventoryHolder)invBlock.getState()).getInventory().getContents()) == null)continue;
                while (listIterator1.hasNext()) {
                    ItemStack stack = listIterator1.next();
                    if (stack == null) continue;
                    if (stack != null && (isSimilar(stack,item))) {
                        foundAmount += stack.getAmount();
                        blockList.add(invBlock);
                    }
                }
            }
        }
        return blockList;
    }

    @NotNull
    private Set<CraftType> loadCraftTypes() {
        File craftsFile = new File(Movecraft.getInstance().getDataFolder().getAbsolutePath() + "/types");

        if (craftsFile.mkdirs()) {
            Movecraft.getInstance().saveResource("types/Airship.craft", false);
            Movecraft.getInstance().saveResource("types/Airskiff.craft", false);
            Movecraft.getInstance().saveResource("types/BigAirship.craft", false);
            Movecraft.getInstance().saveResource("types/BigSubAirship.craft", false);
            Movecraft.getInstance().saveResource("types/Elevator.craft", false);
            Movecraft.getInstance().saveResource("types/LaunchTorpedo.craft", false);
            Movecraft.getInstance().saveResource("types/Ship.craft", false);
            Movecraft.getInstance().saveResource("types/SubAirship.craft", false);
            Movecraft.getInstance().saveResource("types/Submarine.craft", false);
            Movecraft.getInstance().saveResource("types/Turret.craft", false);
        }

        Set<CraftType> craftTypes = new HashSet<>();
        File[] files = craftsFile.listFiles();
        if (files == null) {
            return craftTypes;
        }

        for (File file : files) {
            if (file.isFile()) {

                if (file.getName().contains(".craft")) {
                    try {
                        CraftType type = new CraftType(file);
                        craftTypes.add(type);
                    }
                    catch (IllegalArgumentException | CraftType.TypeNotFoundException | ParserException | ScannerException e) {
                        Movecraft.getInstance().getLogger().log(Level.SEVERE, I18nSupport.getInternationalisedString("Startup - failure to load craft type") + " '" + file.getName() + "' " + e.getMessage());
                    }
                }
            }
        }
        if (craftTypes.isEmpty()) {
            Movecraft.getInstance().getLogger().log(Level.SEVERE, ERROR_PREFIX + I18nSupport.getInternationalisedString("Startup - No Crafts Found"));
        }
        Movecraft.getInstance().getLogger().log(Level.INFO, String.format(I18nSupport.getInternationalisedString("Startup - Number of craft files loaded"), craftTypes.size()));
        return craftTypes;
    }

    public void reloadCraftTypes() {
        this.craftTypes = loadCraftTypes();
        Bukkit.getServer().getPluginManager().callEvent(new TypesReloadedEvent());
    }

    public void addCraft(@NotNull PlayerCraft c) {
        if(craftPlayerIndex.containsKey(c.getPilot())) {
            throw new IllegalStateException("Players may only have one PlayerCraft associated with them!");
        }
        if (this.crafts.contains(c)) {
            throw new IllegalStateException("No duplicate Craft Objects Allowed!");
        }
        this.crafts.add(c);
        craftPlayerIndex.put(c.getPilot(), c);
    }

    public void add(@NotNull Craft c) {
        addCraft(c);
    }

    public void addCraft(@NotNull Craft c) {
        if(c instanceof PlayerCraft){
            addCraft((PlayerCraft) c);
        }
        else {
            if (this.crafts.contains(c)) {
                throw new IllegalStateException("No duplicate Craft Objects Allowed!");
            }
            this.crafts.add(c);
        }
    }
    public void detect_craft_damage(Craft c){
        if (c == null) return;
        if (c instanceof SubCraft) return;
        if (c instanceof SinkingCraft) return;
        if (!c.isNotProcessing()) return;
        if ((!(c.getDisabled()))) {
            detect_engine_disabled(c);
        }
        if (!(c.getSinking())) {
            detect_lift_sinking(c);
        }
        if (!(c.getSinking())) {
            detect_size_sinking(c);
        }
    }

    public boolean detect_engine_disabled(Craft c) {
        if (c == null) return false;
        if (c instanceof SubCraft) return false;
        if (c.getSinking()) return false;
        if (!c.isNotProcessing()) return false;
        int originalEngine = (Integer)c.getDataTag("origin_engine");
        int currentEngine = (Integer)c.getDataTag("current_engine");
        if (currentEngine < originalEngine) updateCraftHealthBlocks(c);
        double disabledPerc = c.getType().getDoubleProperty(CraftType.DISABLE_PERCENT);
        int count = 0;
        Set<Material> engineTypes = c.getType().getMoveBlocks();
        if (engineTypes.size() <= 0) return false;
        for (final MovecraftLocation b : c.getTrackedMovecraftLocs("engine_locs")) {
            if (b == null) continue;
            if (engineTypes.contains(Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(b,c.getWorld()))) count++;
        }
        double threshold = 0.1d;

        if (disabledPerc > 1.0) threshold = disabledPerc / 100.0d;
        else threshold = disabledPerc;
        if (count > 0) {
            c.setDataTag("current_engine",(Integer)count);
            currentEngine = count;
        }
        int disableAmount = (int)Math.floor((double)currentEngine * threshold);
        if (currentEngine < disableAmount) {
            return true;
        }
        return false;
    }

    public boolean detect_size_sinking(Craft c) {
        if (c == null) return false;
        if (c instanceof SubCraft) return false;
        if (c.getSinking()) return false;
        if (!c.isNotProcessing()) return false;
        if (c.getDataTag("origin_size") == null) c.setDataTag("origin_size",(Integer)c.getOrigBlockCount());
        if (c.getDataTag("current_size") == null) c.setDataTag("current_size",(Integer)c.getOrigBlockCount());
        int originalSize = (Integer)c.getDataTag("origin_size");
        int currentSize = (Integer)c.getDataTag("current_size");
        double overallSinkPerc = c.getType().getDoubleProperty(CraftType.OVERALL_SINK_PERCENT);
        int count = 0;
        double threshold = 0.1d;

        if (overallSinkPerc > 1.0) threshold = overallSinkPerc / 100.0d;
        else threshold = overallSinkPerc;

        int sinkAmount = (int)Math.floor((double)originalSize * threshold);
        if ((double)threshold > 0.1d) {
            if (((double)currentSize - (currentSize / 1.5) <= ((double)originalSize)*((double)overallSinkPerc)) && currentSize >= originalSize) {
                try {
                    if (c.getNotificationPlayer() != null) c.getNotificationPlayer().sendActionBar(ChatColor.RED+"BLOCKS :"+ChatColor.RESET+"[ "+ChatColor.DARK_RED+(int)currentSize+ChatColor.RESET+" / "+ChatColor.RED+ChatColor.BOLD+(int)originalSize+ChatColor.RESET+" ]");
                } catch (Exception exc) {}
            } else {
                try {
                if (c.getNotificationPlayer() != null) c.getNotificationPlayer().sendActionBar(ChatColor.AQUA+"BLOCKS :"+ChatColor.RESET+"[ "+ChatColor.DARK_AQUA+(int)currentSize+ChatColor.RESET+" / "+ChatColor.AQUA+ChatColor.BOLD+(int)originalSize+ChatColor.RESET+" ]");
                } catch (Exception exc) {}
            }
        } else {
            try {
                if (c.getNotificationPlayer() != null) c.getNotificationPlayer().sendActionBar(ChatColor.AQUA+"BLOCKS :"+ChatColor.RESET+"[ "+ChatColor.DARK_AQUA+(int)currentSize+ChatColor.RESET+" / "+ChatColor.AQUA+ChatColor.BOLD+(int)originalSize+ChatColor.RESET+" ]");
            } catch (Exception exc) {}
        }
        if (currentSize < sinkAmount) {
            return true;
        }
        return false;
    }

    public boolean detect_lift_sinking(Craft c) {
        if (c == null) return false;
        if (c instanceof SubCraft) return false;
        if (c.getSinking()) return false;
        if (!c.isNotProcessing()) return false;
        int originalLift = (Integer)c.getDataTag("origin_lift");
        int currentLift = (Integer)c.getDataTag("current_lift");
        if (currentLift < originalLift) updateCraftHealthBlocks(c);
        double liftSinkPerc = c.getType().getDoubleProperty(CraftType.SINK_PERCENT);
        int count = 0;
        Set<Material> liftTypes = c.getType().getFlyBlocks();
        if (liftTypes.size() <= 0) return false;
        for (final MovecraftLocation b : c.getTrackedMovecraftLocs("lift_locs")) {
            if (b == null) continue;
            if (liftTypes.contains(Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(b,c.getWorld()))) count++;
        }
        double threshold = 0.1d;

        if (liftSinkPerc > 1.0) threshold = liftSinkPerc / 100.0d;
        else threshold = liftSinkPerc;
        if (count > 0 && (Integer)c.getDataTag("current_lift") > count) {
            c.setDataTag("current_lift",(Integer)count);
            currentLift = count;
        }
        int sinkAmount = (int)Math.floor((double)originalLift * threshold);
        if (currentLift < sinkAmount) {
            Movecraft.getInstance().getLogger().info("Craft "+c.toString()+"'s Current Lift "+currentLift+", Original Lift "+originalLift+", Sink Threshold "+sinkAmount+"");
            return true;
        }
        return false;
    }

    public double get_lift_required(Craft c) {
        if (c == null) return 0.0;
        if (c instanceof SubCraft) return 0.0;
        if (c.getSinking()) return 0.0;
        if (!c.isNotProcessing()) return 0.0;
        int originalLift = (Integer)c.getDataTag("origin_lift");
        int currentLift = (Integer)c.getDataTag("current_lift");
        double liftSinkPerc = c.getType().getDoubleProperty(CraftType.SINK_PERCENT);
        int count = 0;
        Set<Material> liftTypes = c.getType().getFlyBlocks();
        if (liftTypes.size() <= 0) return 0.0;
        for (final MovecraftLocation b : c.getTrackedMovecraftLocs("lift_locs")) {
            if (b == null) continue;
            if (liftTypes.contains(Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(b,c.getWorld()))) count++;
        }
        double threshold = 0.1d;

        if (liftSinkPerc > 1.0) threshold = liftSinkPerc / 100.0d;
        else threshold = liftSinkPerc;
        return liftSinkPerc*100.0d;
    }


    public Set<MovecraftLocation> getInteriorAirBlocks(Craft craft, Block start, int max) {
        final HashSet<MovecraftLocation> locs = new HashSet<>();
        HashSet<Block> last = new HashSet<>();
        last.add(start);
        if (max <= 0) max = 25600;
        if (craft.getOrigBlockCount()>=950000) return locs;
        boolean found = true;
        while (found) {
            found = false;
            HashSet<MovecraftLocation> toAdd = new HashSet<>();
            for (Block b : last) {
                HashSet<Block> faces = new HashSet<>();
                faces.add(b.getRelative(BlockFace.DOWN));
                faces.add(b.getRelative(BlockFace.EAST));
                faces.add(b.getRelative(BlockFace.NORTH));
                faces.add(b.getRelative(BlockFace.SOUTH));
                faces.add(b.getRelative(BlockFace.UP));
                faces.add(b.getRelative(BlockFace.WEST));
                for (Block block : faces) {
                    if (locs.contains(MathUtils.bukkit2MovecraftLoc(block.getLocation()))) continue;
                    if (craft.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(block.getLocation()))) continue;
                    Craft detectCraft = getCraftFromBlock(block);
                    if ((craft != null && detectCraft != null) && (detectCraft.equals(craft) == false)) continue;
                    if (!craft.getHitBox().inBounds(MathUtils.bukkit2MovecraftLoc(block.getLocation()))) continue;
                    if (block.getType() == Material.AIR) {
                        toAdd.add(MathUtils.bukkit2MovecraftLoc(block.getLocation()));
                        found = true;
                    }
                }
            }
            locs.addAll(toAdd);
            if (locs.size() > max) {
                found = false;
                return locs;
            }
        }
        return locs;
    }
    public Set<MovecraftLocation> getInteriorAirBlocks(Craft craft, HitBox box, Block start, int max) {
        final HashSet<MovecraftLocation> locs = new HashSet<>();
        HashSet<Block> last = new HashSet<>();
        last.add(start);
        if (max <= 0) max = 25600;
        if (craft.getOrigBlockCount()>=950000)
            return locs;
        boolean found = true;
        while (found) {
            found = false;
            HashSet<MovecraftLocation> toAdd = new HashSet<>();
            for (Block b : last) {
                HashSet<Block> faces = new HashSet<>();
                faces.add(b.getRelative(BlockFace.DOWN));
                faces.add(b.getRelative(BlockFace.EAST));
                faces.add(b.getRelative(BlockFace.NORTH));
                faces.add(b.getRelative(BlockFace.SOUTH));
                faces.add(b.getRelative(BlockFace.UP));
                faces.add(b.getRelative(BlockFace.WEST));
                for (Block block : faces) {
                    if (locs.contains(MathUtils.bukkit2MovecraftLoc(block.getLocation()))) continue;
                    if (box.contains(MathUtils.bukkit2MovecraftLoc(block.getLocation()))) continue;
                    Craft detectCraft = getCraftFromBlock(block);
                    if ((craft != null && detectCraft != null) && (detectCraft.equals(craft) == false)) continue;
                    if (!box.inBounds(MathUtils.bukkit2MovecraftLoc(block.getLocation()))) continue;
                    if (block.getType() == Material.AIR) {
                        toAdd.add(MathUtils.bukkit2MovecraftLoc(block.getLocation()));
                        found = true;
                    }
                }
            }
            locs.addAll(toAdd);
            if (locs.size() > max) {
                found = false;
                return locs;
            }
        }
        return locs;
    }

    public HitBox detectCraftExterior(Craft craft) {
        if (((BaseCraft)craft).getTrackedLocations("valid_exterior").size() <= 0) {
            final HitBox hitBox = new BitmapHitBox(craft.getHitBox());
            final HitBox boundingHitBox = new BitmapHitBox(craft.getHitBox().boundingHitBox());
            final HitBox invertBox = new SetHitBox(Sets.difference(boundingHitBox.asSet(), hitBox.asSet()));
            final HitBox validExterior = detectInvertedBox(craft);
            final HitBox confirmedExtBox = new BitmapHitBox(detectValidExterior(craft));
            //final HitBox confirmedExtBox = new BitmapHitBox(verifyDetectedExterior(invertBox.asSet(),validExterior));
            ((BaseCraft)craft).setTrackedMovecraftLocs("valid_exterior",confirmedExtBox.asSet());
            return confirmedExtBox;
        } else {
            return (new BitmapHitBox(((BaseCraft)craft).getTrackedMovecraftLocs("valid_exterior")));
        }
    }
    

    public BitmapHitBox detectCraftInterior(final Craft craft) {
        final BitmapHitBox interior = new BitmapHitBox();
        if (!craft.getType().getBoolProperty(CraftType.DETECT_INTERIOR)) return interior;
        final WorldHandler handler = Movecraft.getInstance().getWorldHandler();
        final World badWorld = craft.getWorld();
        final int waterLine = craft.getWaterLine();

        final BitmapHitBox invertedHitBox = new BitmapHitBox(craft.getHitBox().boundingHitBox().difference(craft.getHitBox()));
        //A set of locations that are confirmed to be "exterior" locations
        final BitmapHitBox exterior = new BitmapHitBox();

        //place phased blocks
        final int minX = craft.getHitBox().getMinX();
        final int maxX = craft.getHitBox().getMaxX();
        final int minY = craft.getHitBox().getMinY();
        final int maxY = craft.getHitBox().getMaxY();
        final int minZ = craft.getHitBox().getMinZ();
        final int maxZ = craft.getHitBox().getMaxZ();
        final HitBox[] surfaces = {
                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(minX, maxY, maxZ)),
                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, minZ)),
                new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(minX, maxY, maxZ)),
                new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(maxX, maxY, minZ)),
                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, minY, maxZ))};
        //Valid exterior starts as the 6 surface planes of the HitBox with the locations that lie in the HitBox removed
        final BitmapHitBox validExterior = new BitmapHitBox();
        for (HitBox hitBox : surfaces) {
            validExterior.addAll(hitBox.difference(craft.getHitBox()));
        }
        final SetHitBox visited = new SetHitBox();
        for (final MovecraftLocation location : validExterior) {
            if (craft.getHitBox().contains(location) || exterior.contains(location)) {
                continue;
            }
            //use a modified BFS for multiple origin elements
            final Queue<MovecraftLocation> queue = new LinkedList<>();
            queue.add(location);
            while (!queue.isEmpty()) {
                final MovecraftLocation node = queue.poll();
                //If the node is already a valid member of the exterior of the HitBox, continued search is unitary.
                for (final MovecraftLocation neighbor : CollectionUtils.neighbors(invertedHitBox, node)) {
                    if (visited.add(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
        }
        exterior.addAll(visited);
        craft.setTrackedMovecraftLocs("valid_exterior",exterior.asSet());
        final Set<MovecraftLocation> interiorSet = new HashSet<>();
        for (MovecraftLocation loc : (invertedHitBox.difference(exterior))) {
            final Material mat = handler.toBukkitBlockFast(loc,badWorld);
            if (mat.isAir()) {
                if (exterior.contains(loc)) continue;
                if (craft.getHitBox().contains(loc)) continue;
                interiorSet.add(loc);
            }
            if (craft.getType().getMaterialSetProperty(CraftType.INTERIOR_BLOCKS).contains(mat)) {
                if (exterior.contains(loc)) continue;
                if (craft.getHitBox().contains(loc)) continue;
                interiorSet.add(loc);
            }
        }
        if (waterLine != -64 && waterLine != -128) {
            for (MovecraftLocation loc : craft.getHitBox().boundingHitBox()) {
                if (loc.getY() <= waterLine && (waterLine != -64 && waterLine != -128)) {
                    if (exterior.contains(loc)) continue;
                    if (craft.getHitBox().contains(loc)) continue;
                    final Material mat = handler.toBukkitBlockFast(loc,badWorld);
                    if (!mat.isAir()) continue;
                    interiorSet.add(loc);
                }
            }
        }
        if (craft.getHitBox().size()+(int)(craft.getHitBox().size()/1.25) >= interiorSet.size()) {
            //interior.addAll(interiorSet);
            craft.setTrackedMovecraftLocs("air",interiorSet);
            ((MutableHitBox)craft.getHitBox()).addAll(interiorSet);
            //craft.setHitBox(craft.getHitBox().union(interior));
            BlockData waterData = Movecraft.getInstance().getWaterBlockData();
            if (waterLine != -64 && waterLine != -128) return interior;
            for (final MovecraftLocation location : craft.getHitBox()) {
                if (location.getY() <= waterLine) {
                    craft.getPhaseBlocks().put(location.toBukkit(badWorld), waterData);
                }
            }
        }
        return interior;
    }

    public HitBox detectInterior(Craft craft) {
        return detectCraftInterior(craft);
    }

    public HitBox detectExterior(Craft craft) {
        BitmapHitBox full = new BitmapHitBox(craft.getHitBox());
        World world = craft.getWorld();
        int minX = full.getMinX();
        int maxX = full.getMaxX();
        int minY = full.getMinY();
        int maxY = full.getMaxY();
        int minZ = full.getMinZ();
        int maxZ = full.getMaxZ();
        HitBox hitBox = new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, maxZ));
        BitmapHitBox internalHitbox = new BitmapHitBox(hitBox);
        for (MovecraftLocation loc : internalHitbox) {
            if (loc.toBukkit(world).getBlock().getType().isAir()) {
                internalHitbox.remove(loc);
            }
        }
        return internalHitbox;
    }

    public Set<MovecraftLocation> verifyDetectedExterior(Set<MovecraftLocation> invertedHitBox, HitBox validExterior) {
        var shifts = new MovecraftLocation[]{new MovecraftLocation(0,-1,0),
                new MovecraftLocation(1,0,0),
                new MovecraftLocation(-1,0,0),
                new MovecraftLocation(0,0,1),
                new MovecraftLocation(0,0,-1)};
        Set<MovecraftLocation> visited = new LinkedHashSet<>(validExterior.asSet());
        Queue<MovecraftLocation> queue = new ArrayDeque<>();
        for(var node : validExterior){
            //If the node is already a valid member of the exterior of the HitBox, continued search is unitary.
            for(var shift : shifts){
                var shifted = node.add(shift);
                if(invertedHitBox.contains(shifted) && visited.add(shifted)){
                    queue.add(shifted);
                }
            }
        }
        while (!queue.isEmpty()) {
            var node = queue.poll();
            //If the node is already a valid member of the exterior of the HitBox, continued search is unitary.
            for(var shift : shifts){
                var shifted = node.add(shift);
                if(invertedHitBox.contains(shifted) && visited.add(shifted)){
                    queue.add(shifted);
                }
            }
        }
        return visited;
    }
    public HitBox detectValidExterior(Craft craft) {
        var invertedHitBox = Sets.difference(craft.getHitBox().boundingHitBox().asSet(), craft.getHitBox().asSet());
        int minX = craft.getHitBox().getMinX();
        int maxX = craft.getHitBox().getMaxX();
        int minY = craft.getHitBox().getMinY();
        int maxY = craft.getHitBox().getMaxY();
        int minZ = craft.getHitBox().getMinZ();
        int maxZ = craft.getHitBox().getMaxZ();
        HitBox[] surfaces = {
                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(minX, maxY, maxZ)),
                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, minZ)),
                new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(minX, maxY, maxZ)),
                new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(maxX, maxY, minZ)),
                new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, minY, maxZ))};
        SetHitBox validExterior = new SetHitBox();
        for (HitBox hitBox : surfaces) {
            validExterior.addAll(Sets.difference(hitBox.asSet(),craft.getHitBox().asSet()));
        }
        BitmapHitBox validExteriorBitMap = new BitmapHitBox(validExterior);
        return validExteriorBitMap;
    }

    public BitmapHitBox detectInvertedBox(Craft craft) {
      var invertedHitBox = Sets.difference(craft.getHitBox().boundingHitBox().asSet(), craft.getHitBox().asSet());
      int minX = craft.getHitBox().getMinX();
      int maxX = craft.getHitBox().getMaxX();
      int minY = craft.getHitBox().getMinY();
      int maxY = craft.getHitBox().getMaxY();
      int minZ = craft.getHitBox().getMinZ();
      int maxZ = craft.getHitBox().getMaxZ();
      HitBox[] surfaces = {
              new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(minX, maxY, maxZ)),
              new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, maxY, minZ)),
              new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(minX, maxY, maxZ)),
              new SolidHitBox(new MovecraftLocation(maxX, minY, maxZ), new MovecraftLocation(maxX, maxY, minZ)),
              new SolidHitBox(new MovecraftLocation(minX, minY, minZ), new MovecraftLocation(maxX, minY, maxZ))};
      SetHitBox validExterior = new SetHitBox();
      for (HitBox hitBox : surfaces) {
          validExterior.addAll(Sets.difference(hitBox.asSet(),craft.getHitBox().asSet()));
      }
      BitmapHitBox validExteriorBitMap = new BitmapHitBox(validExterior);
      return validExteriorBitMap;
    }

    public void release(@NotNull Craft craft, @NotNull CraftReleaseEvent.Reason reason, boolean force) {
        if (craft == null) return;
        CraftReleaseEvent e = new CraftReleaseEvent(craft, reason);
        Bukkit.getServer().getPluginManager().callEvent(e);
        if (e.isCancelled()) {
            if (force)
                throw new NonCancellableReleaseException();
            else
                return;
        }
        crafts.remove(craft);
        if(craft instanceof PlayerCraft)
            playerCrafts.remove(((PlayerCraft) craft).getPilot());

        Movecraft.getInstance().getLogger().info("RELEASE REASON : "+reason);
        if(craft.getHitBox().isEmpty())
            Movecraft.getInstance().getLogger().warning(I18nSupport.getInternationalisedString(
                    "Release - Empty Craft Release Console"));
        else {
            if (craft instanceof PlayerCraft)
                craft.getAudience().sendMessage(Component.text(I18nSupport.getInternationalisedString(
                        "Release - Craft has been released")));
            if (craft instanceof BaseCraft)
                Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString(
                        "Release - Player has released a craft console"),
                        ((BaseCraft) craft).getNotificationPlayer(),
                        craft.getType().getStringProperty(CraftType.NAME),
                        craft.getHitBox().size(),
                        craft.getHitBox().getMinX(),
                        craft.getHitBox().getMinZ())
                );
            else
                Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString(
                        "Release - Null Craft Release Console"),
                        craft.getType().getStringProperty(CraftType.NAME),
                        craft.getHitBox().size(),
                        craft.getHitBox().getMinX(),
                        craft.getHitBox().getMinZ())
                );
        }
        if (craft instanceof BaseCraft) {
            ((BaseCraft)craft).getCraftTags().clear();
            ((BaseCraft)craft).getRawTrackedMap().clear();
        }
    }

    public void removeCraft(@NotNull Craft c, @NotNull CraftReleaseEvent.Reason reason) {
        if (c == null) return;
        CraftReleaseEvent e = new CraftReleaseEvent(c, reason);
        Bukkit.getServer().getPluginManager().callEvent(e);
        if (e.isCancelled())
            return;

        Craft craft = c;
        removeReleaseTask(c);
        Movecraft.getInstance().getLogger().info("RELEASE REASON : "+reason);
        crafts.remove(c);
        if(c instanceof PlayerCraft)
          this.craftPlayerIndex.remove(((PlayerCraft) c).getPilot());
        // if its sinking, just remove the craft without notifying or checking
        if(craft.getHitBox().isEmpty())
            Movecraft.getInstance().getLogger().warning(I18nSupport.getInternationalisedString("Release - Empty Craft Release Console"));
        else {
            if (c instanceof PlayerCraft)
                craft.getAudience().sendMessage(Component.text(I18nSupport.getInternationalisedString("Release - Craft has been released")));
            if (c instanceof BaseCraft) {
                BaseCraft bc = (BaseCraft)c;
                if (c instanceof PilotedCraft)
                    Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString("Release - Player has released a craft console"), ((PilotedCraft) c).getPilot().getName(), craft.getType().getStringProperty(CraftType.NAME), craft.getHitBox().size()-bc.getBlockNameExact("air").size(), craft.getHitBox().getMinX(), craft.getHitBox().getMinZ()));
                else
                    Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString("Release - Null Craft Release Console"), craft.getType().getStringProperty(CraftType.NAME), craft.getHitBox().size()-bc.getBlockNameExact("air").size(), craft.getHitBox().getMinX(), craft.getHitBox().getMinZ()));
            }
        }
    }

    public void forceRemoveCraft(Craft c) {
        if (c == null) return;
        if(c instanceof PlayerCraft) {
            this.craftPlayerIndex.remove(((PlayerCraft) c).getPilot());
        }

        CraftReleaseEvent e = new CraftReleaseEvent(c, CraftReleaseEvent.Reason.FORCE);
        Bukkit.getServer().getPluginManager().callEvent(e);
        this.crafts.remove(c);
    }

    /**
     * Detect a craft and add it to the craft manager
     *
     * @param startPoint the starting point of the detection process
     * @param type the type of craft to detect
     * @param supplier the supplier run post-detection to create the craft.
     *   Note: This is where you can construct a custom Craft object if you want to, or tailor the detection process.
     * @param world the world to detect in
     * @param player the player who is causing the detection
     *   Note: This is only used for logging and forwarded to the supplier.
     *   - It is highly encouraged to pass in a non-null value if a player is causing the detection.
     *   - If player is null, this will bypass protections like pilot signs and the like.
     * @param audience the audience to send detection messages to
     * @param postDetection the function run post-supplying to perform post-detection actions.
     *   Note: This is where you can perform any post-detection actions, such as starting a torpedo cruising.
     */

    public Craft detect(@NotNull MovecraftLocation startPoint,
                        @NotNull CraftType type, @NotNull CraftSupplier supplier,
                        @NotNull World world, @Nullable Player player,
                        @NotNull Audience audience,
                        @NotNull Function<Craft, Effect> postDetection) {
        /*boolean ran = false;
        if (Settings.IS_MULTITHREADED && Settings.Debug) {
            try {
                WorldDetectionTask supplied = new WorldDetectionTask(
                        startPoint, CachedMovecraftWorld.of(world),
                        type, supplier,
                        world, player,
                        audience,
                        postDetection
                );
                handler.runTaskInWorld(supplied.get(Settings.Debug), world);
                ran = true;
            } catch(Exception exc){
                exc.printStackTrace();
            }
        }*/
        
        WorldManager.INSTANCE.submit(new DetectionTask(
                startPoint, CachedMovecraftWorld.of(world),
                type, supplier,
                world, player,
                audience,
                postDetection
        ));
        if (getCraftFromBlock(startPoint.toBukkit(world).getBlock()) instanceof BaseCraft craft) {
            return craft;
        }
        return getCraftFromBlock(startPoint.toBukkit(world).getBlock());
    }
    public Craft detectCraftUnsafe(@NotNull MovecraftLocation startPoint,
                        @NotNull CraftType type, @NotNull CraftSupplier supplier,
                        @NotNull World world, @Nullable Player player,
                        @NotNull Audience audience,
                        @NotNull Function<Craft, Effect> postDetection) {
        boolean ran = false;
        if (!ran) {
            WorldManager.INSTANCE.submit(new UnsafeDetectionTask(
                    startPoint, CachedMovecraftWorld.of(world),
                    type, supplier,
                    world, player,
                    audience,
                    postDetection
            ));
        }
        if (getCraftFromBlock(startPoint.toBukkit(world).getBlock()) instanceof BaseCraft craft) {
            return craft;
        }
        return getCraftFromBlock(startPoint.toBukkit(world).getBlock());
    }

    public Craft detectIgnoreBlockData(@NotNull List<String> mats, @NotNull MovecraftLocation startPoint,
                        @NotNull CraftType type, @NotNull CraftSupplier supplier,
                        @NotNull World world, @Nullable Player player,
                        @NotNull Audience audience,
                        @NotNull Function<Craft, Effect> postDetection) {
        boolean ran = false;
        if (!ran) {
            WorldManager.INSTANCE.submit(new IgnoreDetectionTask(mats,
                    startPoint, CachedMovecraftWorld.of(world),
                    type, supplier,
                    world, player,
                    audience,
                    postDetection
            ));
        }
        if (getCraftFromBlock(startPoint.toBukkit(world).getBlock()) instanceof BaseCraft craft) {
            return craft;
        }
        return getCraftFromBlock(startPoint.toBukkit(world).getBlock());
    }

    public Craft detectHitBox(@NotNull List<String> mats, @NotNull MovecraftLocation startPoint,
                        @NotNull CraftType type, @NotNull CraftSupplier supplier,
                        @NotNull World world, @Nullable Player player,
                        @NotNull Audience audience,
                        @NotNull Function<Craft, Effect> postDetection) {

        WorldManager.INSTANCE.submit(new HitBoxDetectionTask(mats,
                startPoint, CachedMovecraftWorld.of(world),
                type, supplier,
                world, player,
                audience,
                postDetection
        ));
        return getCraftFromBlock(startPoint.toBukkit(world).getBlock());
    }

    @NotNull
    public Set<Craft> getCraftsInWorld(@NotNull World w) {
        Set<Craft> crafts = new HashSet<>();
        for (Craft c : craftList) {
            if (c instanceof Craft) {
                if (((Craft)c).getWorld().equals(w))
                    crafts.add(((Craft)c));
            }
        }
        return crafts;
    }

    @Contract("null -> null")
    @Nullable
    public PlayerCraft getCraftByPlayer(@Nullable Player p) {
        if(p == null)
            return null;
        return craftPlayerIndex.get(p);
    }


    public PlayerCraft getCraftByPlayerName(String name) {
        for (var entry : craftPlayerIndex.entrySet()) {
            if (entry.getKey() != null && entry.getKey().getName().equals(name))
                return entry.getValue();
        }
        return null;
    }

    public void removeCraftByPlayer(Player player) {
        PilotedCraft craft = craftPlayerIndex.remove(player);
        if (craft != null) {
            forceRemoveCraft(craft);
        }
    }

    public HitBox generateHitbox(@NotNull CraftType craftType, Block b, String player) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        World world = (World)b.getWorld();
        var parent = this.getCraftFromBlock(b);
        if ((parent != null) && (parent instanceof PlayerCraftImpl)){
            BitmapHitBox box = new BitmapHitBox(parent.getHitBox());
            box.remove(startPoint);
            parent.setHitBox(box);
        }
        final List<String> ignored = Lists.newArrayList();
        Craft result = this.detectHitBox(ignored,
                startPoint,
                craftType, (type, w, p, parents) -> {
                    return new Pair<>(Result.succeed(),
                            new NPCCraftImpl(type, w, p));
                },
                world, null,
                Movecraft.getAdventure().console(),
                craft -> () -> {
                    if (parent != null) {
                        var newHitbox = parent.getHitBox().difference(craft.getHitBox());
                        parent.setHitBox(newHitbox);
                        parent.setOrigBlockCount(parent.getOrigBlockCount() - craft.getHitBox().size());
                    }
                    forceRemoveCraft(craft);
                    Movecraft.getInstance().getLogger().warning("Generating Hitbox: "+craft.getHitBox()+" @ "+startPoint+" In world:"+ world.getName());
                }
        );
        Movecraft.getInstance().getLogger().warning("Verifying Generated Hitbox: "+result+" @ "+startPoint+" In world:"+ world.getName());
      HitBox hbox = result.getHitBox();
      forceRemoveCraft(result);
      return hbox;
    }

    public Craft detectCraftFromBlocks(@NotNull CraftType craftType, Block start, Collection<Block> blocks) {
        MovecraftLocation startPoint = new MovecraftLocation(start.getX(), start.getY(), start.getZ());
        World world = (World)start.getWorld();
        var parent = this.getCraftFromBlock(start);
        if ((parent != null) && (parent instanceof PlayerCraftImpl)){
            BitmapHitBox box = new BitmapHitBox(parent.getHitBox());
            box.remove(startPoint);
            parent.setHitBox(box);
        }
        final BitmapHitBox box = new BitmapHitBox();
        box.add(startPoint);
        for (Block block : blocks) {
            box.add(MathUtils.bukkit2MovecraftLoc(block.getLocation()));
        }
        NPCCraftImpl craft = new NPCCraftImpl(craftType, world);
        craft.setHitBox(box);
        craft.setWorld(world);
        return craft;
    }

    public Craft forceUnsafeCraftPilot(@NotNull CraftType craftType, Block b, String player) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        World world = (World)b.getWorld();
        var parent = this.getCraftFromBlock(b);
        if ((parent != null) && (parent instanceof PlayerCraftImpl)){
            BitmapHitBox box = new BitmapHitBox(parent.getHitBox());
            box.remove(startPoint);
            parent.setHitBox(box);
        }
        this.detectCraftUnsafe(
                startPoint,
                craftType, (type, w, p, parents) -> {
                    return new Pair<>(Result.succeed(),
                            new NPCCraftImpl(type, w, p));
                },
                world, null,
                Movecraft.getAdventure().console(),
                craft -> () -> {
                    if (parent != null) {
                        var newHitbox = parent.getHitBox().difference(craft.getHitBox());
                        parent.setHitBox(newHitbox);
                        parent.setOrigBlockCount(parent.getOrigBlockCount() - craft.getHitBox().size());
                    }
                    Movecraft.getInstance().getLogger().warning("Piloting AutoCraft: "+craft+" @ "+startPoint+" In world:"+ world.getName());
                    //Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
                }
        );
        Movecraft.getInstance().getLogger().warning("Verifying AutoCraft: "+this.getCraftFromBlock(b)+" @ "+startPoint+" In world:"+ world.getName());
      return (this.getCraftFromBlock(b));
    }



    public Craft forceCraftPilotIgnoreMat(@NotNull CraftType craftType, Block b, Player player, List<String> matIgnored) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        World world = (World)b.getWorld();
        this.detectIgnoreBlockData(
                matIgnored,
                startPoint,
                craftType, (type, w, p, parents) -> {
                        if (parents.size() > 0)
                            return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                    "Detection - Failed - Already commanding a craft")), null);

                        return new Pair<>(Result.succeed(),
                                new PlayerCraftImpl(type, w, p));
                },
                world, player,
                Movecraft.getAdventure().player(player),
                craft -> () -> {
                    //Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
                    if (craft instanceof PlayerCraftImpl) { // Subtract craft from the parent
                        // Release old craft if it exists
                        Craft oldCraft = this.getCraftByPlayer(player);
                        if (oldCraft != null)
                            this.removeCraft(oldCraft, CraftReleaseEvent.Reason.PLAYER);
                    }
                }
        );
        return (this.getCraftFromBlock(b));
    }

    public Craft forceAutoCraftPilot(@NotNull CraftType craftType, Block b, final String player) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        World world = (World)b.getWorld();
        var parent = this.getCraftFromBlock(b);
        if ((parent != null) && !(parent instanceof NPCCraftImpl)){
            BitmapHitBox box = new BitmapHitBox(parent.getHitBox());
            box.remove(startPoint);
            parent.setHitBox(box);
            forceRemoveCraft(parent);
        }
        Craft output = this.detect(
                startPoint,
                craftType, (type, w, p, parents) -> {
                   if (parents.size() == 1)
                            return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                    "Detection - Failed - Already commanding a craft")), null);

                        return new Pair<>(Result.succeed(),
                                new NPCCraftImpl(type, w, p));
                },
                world, null,
                Movecraft.getAdventure().console(),
                craft -> () -> {
                    if (parent != null) {
                        var newHitbox = parent.getHitBox().difference(craft.getHitBox());
                        parent.setHitBox(newHitbox);
                        parent.setOrigBlockCount(parent.getOrigBlockCount() - craft.getHitBox().size());
                    }
                    Movecraft.getInstance().getLogger().warning("Piloting NPCCraft: "+craft+" @ "+craft.getMidPoint()+" In world:"+ world.getName());
                    if (craft instanceof NPCCraftImpl) {
                        craft.setAutomated(true);
                        craft.setNPCTag(player);
                    }
                    detectCraftHealthBlocks(craft);
                    craft.setProcessing(false);
                    //Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
                }
        );
        Movecraft.getInstance().getLogger().warning("Verifying NPCCraft: "+output+" @ "+startPoint+" In world:"+ world.getName());
        return output;
    }


    public Craft forceCraftSplitPilot(@NotNull CraftType craftType, Block b, Player player) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        World world = (World)b.getWorld();
        Craft originCraft = (this.getCraftFromBlock(b));
        originCraft.removeBlock(b);
        this.detect(
                startPoint,
                craftType, (type, w, p, parents) -> {
                    return new Pair<>(Result.succeed(),
                            new PlayerCraftImpl(type, w, p));
                },
                world, player,
                Movecraft.getAdventure().player(player),
                craft -> () -> {
                    //Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
                }
        );
        Craft fighterCraft = (this.getCraftFromBlock(b));
        if (!fighterCraft.equals(originCraft)) {
            originCraft.setHitBox(originCraft.getHitBox().difference(fighterCraft.getHitBox()));
        }
        return fighterCraft;
    }


    public Craft forceCraftPilot(@NotNull CraftType craftType, Block b, Player player) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        World world = (World)b.getWorld();
        this.detect(
                startPoint,
                craftType, (type, w, p, parents) -> {
                    if (type.getBoolProperty(CraftType.CRUISE_ON_PILOT)) {
                        if (parents.size() >= 1)
                            return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                    "Detection - Failed - Already commanding a craft")), null);
                        if (parents.size() == 1) {
                            Craft parent = parents.iterator().next();
                            return new Pair<>(Result.succeed(),
                                    new CruiseOnPilotSubCraft(type, world, p, parent));
                        }

                        return new Pair<>(Result.succeed(),
                                new CruiseOnPilotCraft(type, world, p));
                    }
                    else {
                        if (parents.size() > 0)
                            return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                    "Detection - Failed - Already commanding a craft")), null);

                        return new Pair<>(Result.succeed(),
                                new PlayerCraftImpl(type, w, p));
                    }
                },
                world, player,
                Movecraft.getAdventure().player(player),
                craft -> () -> {
                    //Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
                    if (craft instanceof SubCraft) { // Subtract craft from the parent
                        Craft parent = ((SubCraft) craft).getParent();
                        var newHitbox = parent.getHitBox().difference(craft.getHitBox());
                        parent.setHitBox(newHitbox);
                        parent.setOrigBlockCount(parent.getOrigBlockCount() - craft.getHitBox().size());
                    } else {
                        // Release old craft if it exists
                        Craft oldCraft = this.getCraftByPlayer(player);
                        if (oldCraft != null)
                            this.removeCraft(oldCraft, CraftReleaseEvent.Reason.PLAYER);
                    }
                }
        );
        return (this.getCraftFromBlock(b));
    }

    public Craft forceUnsafeSubCraftMove(@NotNull CraftType craftType, String player, Block b, @Nullable Location targetLoc) {
      MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
      World world = (World)b.getWorld();
      var parent = this.getCraftFromBlock(b);
      if ((parent != null) && (parent instanceof PlayerCraftImpl)){
          BitmapHitBox box = new BitmapHitBox(parent.getHitBox());
          box.remove(startPoint);
          parent.setHitBox(box);
      }
      this.detect(
              startPoint,
              craftType, (type, w, p, parents) -> {
                 if (parents.size() > 1)
                          return new Pair<>(Result.failWithMessage(I18nSupport.getInternationalisedString(
                                  "Detection - Failed - Already commanding a craft")), null);

                      return new Pair<>(Result.succeed(),
                              new NPCCraftImpl(type, w, p));
              },
              world, null,
              Movecraft.getAdventure().console(),
              craft -> () -> {
                  if (parent != null) {
                      var newHitbox = parent.getHitBox().difference(craft.getHitBox());
                      parent.setHitBox(newHitbox);
                      parent.setOrigBlockCount(parent.getOrigBlockCount() - craft.getHitBox().size());
                  }
                  Movecraft.getInstance().getLogger().warning("Piloting AutoCraft: "+craft+" @ "+startPoint+" In world:"+ world.getName());
                  //Bukkit.getServer().getPluginManager().callEvent(new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER));
              }
      );
      Movecraft.getInstance().getLogger().warning("Verifying AutoCraft: "+this.getCraftFromBlock(b)+" @ "+startPoint+" In world:"+ world.getName());
    Craft craft = parent;
    if (this.getCraftFromBlock(b) != null) {
        ((NPCCraftImpl)this.getCraftFromBlock(b)).setAutomated(false);
        ((NPCCraftImpl)this.getCraftFromBlock(b)).setNPCTag(player);
        ((NPCCraftImpl)this.getCraftFromBlock(b)).translate((int)targetLoc.getX(),(int)targetLoc.getY(),(int)targetLoc.getZ());
        craft = (this.getCraftFromBlock(b));
    }
    return craft;
  }

    public void forceCraftRotate(@NotNull Craft c, Block b, @NotNull MovecraftRotation rotation) {
        MovecraftLocation startPoint = new MovecraftLocation(b.getX(), b.getY(), b.getZ());
        Movecraft.getInstance().getLogger().warning("Forcing Craft to Rotate: "+c+" @ "+startPoint+" @ Rotation: "+rotation);
        c.rotate(rotation, startPoint, false);
    }

    @Nullable
    @Deprecated
    public Player getPlayerFromCraft(@NotNull Craft c) {
        for (var entry : craftPlayerIndex.entrySet()) {
            if (entry.getValue() == c)
                return entry.getKey();
        }
        return null;
    }
    public BaseCraft getCraftFromPlayer(@NotNull Player p) {
        for (var entry : craftPlayerIndex.entrySet()) {
            if (entry.getKey() == p)
                return (BaseCraft)entry.getValue();
        }
        return null;
    }

    @NotNull
    public Set<PlayerCraft> getPlayerCraftsInWorld(World world) {
        Set<PlayerCraft> crafts = new HashSet<>();
        for (PlayerCraft craft : craftPlayerIndex.values()) {
            if (craft.getWorld() == world)
                crafts.add(craft);
        }
        return crafts;
    }
    public Set<PlayerCraft> getPlayerCrafts() {
        Set<PlayerCraft> crafts = new HashSet<>();
        for (PlayerCraft craft : craftPlayerIndex.values()) {
          if (craft instanceof PlayerCraft){
              crafts.add(craft);
          }
        }
      return crafts;
    }
    public Set<PlayerCraft> getPlayerCraftList() {
        Set<PlayerCraft> crafts = new HashSet<>();
        for (PlayerCraft craft : craftPlayerIndex.values()) {
          if (craft instanceof PlayerCraft){
              crafts.add(craft);
          }
        }
      return crafts;
    }

    public static <K, V> Map<K, V> reverseMap(Map<K, V> map) {
        LinkedHashMap<K, V> reversed = new LinkedHashMap<>();
        List<K> keys = new ArrayList<>(map.keySet());
        Collections.reverse(keys);
        keys.forEach((key) -> reversed.put(key, map.get(key)));
        return reversed;
    }

    public static boolean containsIgnoreCase(String src, String what) {
        final int length = what.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, what, 0, length))
                return true;
        }

        return false;
    }

    public Craft getCraftFromBlockState(BlockState bs){
        if (bs == null) return null;
        for (Craft i : getCraftsInWorld(bs.getWorld())) {
            if (i == null) continue;
            if (!Tags.FALL_THROUGH_BLOCKS.contains(bs.getType())) {
                if (MathUtils.locationNearHitBox(i.getHitBox(),(MathUtils.bukkit2MovecraftLoc(bs.getLocation())),1d)) return i;
                //if (i.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(bs.getLocation()))) return i;
            }
            if (MathUtils.locationNearHitBox(i.getHitBox(),(MathUtils.bukkit2MovecraftLoc(bs.getLocation())),1d)) return i;
            //if (i.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(bs.getLocation()))) return i;
        }
        return null;
    }

    public Craft getCraftFromBlock(Block b){
        if (b == null) return null;
        for (Craft i : getCraftsInWorld(b.getWorld())) {
            if (i == null) continue;
            if (!Tags.FALL_THROUGH_BLOCKS.contains(b.getType())) {
                if (MathUtils.locationNearHitBox(i.getHitBox(),(MathUtils.bukkit2MovecraftLoc(b.getLocation())),1d)) return i;
                //if (i.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation()))) return i;
            }
            if (MathUtils.locationNearHitBox(i.getHitBox(),(MathUtils.bukkit2MovecraftLoc(b.getLocation())),1d)) return i;
            //if (i.getHitBox().contains(MathUtils.bukkit2MovecraftLoc(b.getLocation()))) return i;
        }
        return null;
    }

    public boolean isEqualSign(Sign test, String target) {
        return !ChatColor.stripColor(test.getLine(0)).equalsIgnoreCase(HEADER) && ( ChatColor.stripColor(test.getLine(0)).equalsIgnoreCase(target)
                || ChatColor.stripColor(test.getLine(1)).equalsIgnoreCase(target)
                || ChatColor.stripColor(test.getLine(2)).equalsIgnoreCase(target)
                || ChatColor.stripColor(test.getLine(3)).equalsIgnoreCase(target) );
    }
    public boolean isForbidden(Sign test) {
        for (int i = 0; i < 4; i++) {
            String t = test.getLine(i).toLowerCase();
            if(Settings.ForbiddenRemoteSigns.contains(t))
                return true;
        }
        return false;
    }
    public Set<Location> checkCraftBorders(Craft craft) {
        Set<Location> mergePoints = new HashSet<>();
        final EnumSet<Material> ALLOWED_BLOCKS = craft.getType().getMaterialSetProperty(CraftType.ALLOWED_BLOCKS);
        final EnumSet<Material> FORBIDDEN_BLOCKS = craft.getType().getMaterialSetProperty(CraftType.FORBIDDEN_BLOCKS);
        final MovecraftLocation[] SHIFTS = {
                //x
                new MovecraftLocation(-1, 0, 0),
                new MovecraftLocation(-1, -1, 0),
                new MovecraftLocation(-1,1,0),
                new MovecraftLocation(1, -1, 0),
                new MovecraftLocation(1, 1, 0),
                new MovecraftLocation(1, 0, 0),
                //z
                new MovecraftLocation(0, 1, 1),
                new MovecraftLocation(0, 0, 1),
                new MovecraftLocation(0, -1, 1),
                new MovecraftLocation(0, 1, -1),
                new MovecraftLocation(0, 0, -1),
                new MovecraftLocation(0, -1, -1),
                //y
                new MovecraftLocation(0, 1, 0),
                new MovecraftLocation(0, -1, 0)};
        //Check each location in the hitbox
        for (MovecraftLocation ml : craft.getHitBox()){
            //Check the surroundings of each location
            for (MovecraftLocation shift : SHIFTS){
                MovecraftLocation test = ml.add(shift);
                //Ignore locations contained in the craft's hitbox
                if (craft.getHitBox().contains(test)){
                    continue;
                }
                Block testBlock = test.toBukkit(craft.getWorld()).getBlock();
                Material testMaterial = testBlock.getType();
                //Break the loop if an allowed block is found adjacent to the craft's hitbox
                if (ALLOWED_BLOCKS.contains(testMaterial)){
                    mergePoints.add(testBlock.getLocation());
                }
                //Do the same if a forbidden block is found
                else if (FORBIDDEN_BLOCKS.contains(testMaterial)){
                    mergePoints.add(testBlock.getLocation());
                }
            }
        }
        //Return the string representation of the merging point and alert the pilot
        return mergePoints;
    }


    @Deprecated
    public void removePlayerFromCraft(Craft c) {
        if (!(c instanceof PlayerCraft)) {
            return;
        }
        removeReleaseTask(c);
        Player p = ((PlayerCraft) c).getPilot();
        p.sendMessage(I18nSupport.getInternationalisedString("Release - Craft has been released message"));
        Movecraft.getInstance().getLogger().info(String.format(I18nSupport.getInternationalisedString("Release - Player has released a craft console"), p.getName(), c.getType().getStringProperty(CraftType.NAME), c.getHitBox().size(), c.getHitBox().getMinX(), c.getHitBox().getMinZ()));
        c.setNotificationPlayer(null);
        craftPlayerIndex.remove(p);
    }


    @Deprecated
    public final void addReleaseTask(Craft c) {
        if (c instanceof Craft) {
          Craft craft = (Craft)c;
          Player p = getPlayerFromCraft(craft);
          if (p != null) {
              p.sendMessage(I18nSupport.getInternationalisedString("Release - Player has left craft"));
          }
          BukkitTask releaseTask = new BukkitRunnable() {
              @Override
              public void run() {
                  removeCraft(craft, CraftReleaseEvent.Reason.PLAYER);
                  // I'm aware this is not ideal, but you shouldn't be using this anyways.
              }
          }.runTaskLater(Movecraft.getInstance(), (20 * 15));
          releaseEvents.put(craft, releaseTask);
      }

    }

    @Deprecated
    public final void removeReleaseTask(Object c) {
        Player p = getPlayerFromCraft(((Craft)c));
        if (p != null) {
            if (releaseEvents.containsKey(c)) {
                if (releaseEvents.get(c) != null)
                    releaseEvents.get(c).cancel();
                releaseEvents.remove(c);
            }
        }
    }

    @Deprecated
    public boolean isReleasing(Object craft) {
        return releaseEvents.containsKey(craft);
    }

    public Set<Craft> getNormalCraftList() {
        Set<Craft> crafts = new HashSet<>();
        for (Craft craft : craftList) {
            if (craft instanceof Craft) {
                crafts.add(((Craft)craft));
            }
        }
        return crafts;
    }
    @NotNull
    public Set<Craft> getCrafts() {
        return Collections.unmodifiableSet(craftList);
    }

    @NotNull
    public Set<Craft> getCraftList() {
        return getCrafts();
    }

    @Nullable
    public CraftType getCraftTypeFromString(String s) {
        for (CraftType t : craftTypes) {
            if (s.equalsIgnoreCase(t.getName())) {
                return t;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return crafts.isEmpty();
    }

    @NotNull
    @Override
    public Iterator<Craft> iterator() {
        return Collections.unmodifiableSet(craftList).iterator();
    }

    public void addOverboard(Player player) {
        overboards.put(player, System.currentTimeMillis());
    }

    @NotNull
    public long getTimeFromOverboard(Player player) {
        return overboards.getOrDefault(player, 0L);
    }

    @Nullable
    public Craft fastNearestCraftToLoc(Location loc) {
        Craft ret = null;
        long closestDistSquared = Long.MAX_VALUE;
        Set<Craft> craftsList = this.getCraftsInWorld(loc.getWorld());
        for (Craft i : craftsList) {
            if (i.getHitBox().isEmpty())
                continue;
            int midX = (i.getHitBox().getMaxX() + i.getHitBox().getMinX()) >> 1;
            int midZ = (i.getHitBox().getMaxZ() + i.getHitBox().getMinZ()) >> 1;
            long distSquared = (long) (Math.pow(midX -  loc.getX(), 2) + Math.pow(midZ - (int) loc.getZ(), 2));
            if (distSquared < closestDistSquared) {
                closestDistSquared = distSquared;
                ret = i;
            }
        }
        return ret;
    }
}
