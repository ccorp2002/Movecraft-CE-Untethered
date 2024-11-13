package net.countercraft.movecraft.craft;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.TrackedLocation;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.async.rotation.RotationTask;
import net.countercraft.movecraft.async.translation.TranslationTask;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.CachedMovecraftWorld;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.processing.WorldManager;
import net.countercraft.movecraft.util.Counter;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.TimingData;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.util.hitboxes.SolidHitBox;
import net.countercraft.movecraft.util.hitboxes.MutableHitBox;
import net.countercraft.movecraft.util.hitboxes.SetHitBox;
import net.countercraft.movecraft.util.hitboxes.BitmapHitBox;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.countercraft.movecraft.util.CollectionUtils;
import net.countercraft.movecraft.util.MathUtils;
//import com.github.milkdrinkers.customblockdata.*;



import org.bukkit.util.Vector;
import org.bukkit.util.BlockVector;
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
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
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
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.WeakHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static net.countercraft.movecraft.util.SignUtils.getFacing;

public abstract class BaseCraft implements Craft {
    @NotNull
    protected final CraftType type;
    @NotNull
    protected final MutableHitBox collapsedHitBox;
    @NotNull
    private Counter<Material> materials;
    @NotNull
    private final AtomicBoolean processing = new AtomicBoolean();
    private final long origPilotTime;
    @NotNull
    public final Map<Location, BlockData> phaseBlocks = new HashMap<>();
    @Nullable
    public final Set<Entity> passengers = new HashSet<>();
    @Nullable
    public Set<Craft> contacts = new HashSet<>();
    @NotNull
    public HitBox hitBox;
    @NotNull
    protected MutableHitBox fluidLocations;
    @NotNull
    private World w;
    @NotNull TimingData stats = new TimingData();
    private boolean cruising;
    private boolean disabled;
    private CruiseDirection cruiseDirection = CruiseDirection.NONE;
    private long lastCruiseUpdate = 0;
    private long lastBlockCheck;
    private long lastRotateTime = 0;
    private long lastTeleportTime;
    private int lastDX, lastDY, lastDZ;
    private int currentGear = 1;
    private int burningFuel = 2;
    private int fuel = this.burningFuel;
    private int origBlockCount;
    @Nullable
    private Player notificationPlayer;
    @NotNull
    private Audience audience;
    public boolean isAuto = false;
    public int speedMod = -1;
    @NotNull
    private String name = "";
    private String NPCTag = "N/A";
    @NotNull
    private MovecraftLocation lastTranslation = new MovecraftLocation(0, 0, 0);
    public boolean sinking;
    private HashMap<Object, Collection<TrackedLocation>> trackedLocations = new HashMap<>(100_000,0.5f);

    private HashMap<String, Object> craftTags = new HashMap<>(100_000,0.5f);
    public MovecraftLocation midPoint = null;
    private long lastMoveTime;

    private final UUID uuid = UUID.randomUUID();

    public String asString() {
        String str = "";
        String craftType = this.type.getName();
        Location loc = this.getLocation();
        String sloc = loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ()+","+loc.getWorld().getName();
        String pilotUUID = "";
        String extra = "";
        if (this.isAutomated()) {
            pilotUUID = pilotUUID + getNPCTag();
            extra = "";
        } else if (this instanceof PlayerCraftImpl && getNotificationPlayer() != null) {
            pilotUUID = pilotUUID + getNotificationPlayer().getUniqueId().toString();
            extra = "";
        } else {}
        str = ""+this.getUniqueId()+"@"+this.getClass().getSimpleName()+"|CraftType="+craftType+"|Location="+sloc+"|Pilot="+pilotUUID+"|Extra="+extra+"";
        return str;
    }

    public BaseCraft(@NotNull CraftType type, @NotNull World world) {
        Hidden.uuidToCraft.put(uuid, this);
        this.type = type;
        this.w = world;
        this.speedMod = -1;
        hitBox = new SetHitBox();
        collapsedHitBox = new SetHitBox();
        fluidLocations = new SetHitBox();
        lastCruiseUpdate = System.currentTimeMillis() - 10000;
        cruising = false;
        sinking = false;
        disabled = false;
        isAuto = false;
        lastMoveTime = System.currentTimeMillis();
        origPilotTime = System.currentTimeMillis();
        materials = new Counter<>();
        audience = Movecraft.getAdventure().console();
    }
    public boolean hasMovedWithin(long time) {
        long ticksElapsed = (System.currentTimeMillis() - getLastMoveTime()) / 50;
        return (ticksElapsed <= Math.abs(time));
    }

    public Location getLocation() {
        return getMidPoint().toBukkit(this.getWorld());
    }

    public MovecraftLocation getMidPoint() {
        if (this.midPoint == null) this.midPoint = this.getHitBox().getMidPoint();
        if (!(this.midPoint.equals(getHitBox().getMidPoint()))) this.midPoint = this.getHitBox().getMidPoint();
        return this.midPoint;
    }

    public MovecraftLocation getBottomCenter() {
        MovecraftLocation point = new MovecraftLocation(getMidPoint().getX(),this.getHitBox().getMinY(),getMidPoint().getZ());
        return point;
    }

    public boolean isNotProcessing() {
        return !processing.get();
    }

    public void setProcessing(boolean processing) {
        this.processing.set(processing);
    }

    @NotNull
    public MutableHitBox getMutableHitBox() {
        return (MutableHitBox)this.hitBox;
    }

    @NotNull
    public HitBox getHitBox() {
        return this.hitBox;
    }

    public void setHitBox(@NotNull HitBox hitBox) {
        this.hitBox = hitBox;
    }

    @NotNull
    public CraftType getType() {
        return type;
    }
    public boolean isAutomated() {
        return isAuto;
    }

    public void setAutomated(@NotNull boolean auto) {
        this.isAuto = auto;
    }
    
    public void setDataTag(String key, Object data) {
        this.craftTags.put(key, data);
    }
    
    @Nullable
    public Object removeDataTag(String key) {
        Object tag = this.craftTags.getOrDefault(key, null);
        this.craftTags.remove(key);
        return tag;
    }
    
    @Nullable
    public <V> V getDataTag(String key) {
        return (V) this.craftTags.getOrDefault(key, null);
    }
    
    public Collection<Object> getAllTagValues() {
        return this.craftTags.values();
    }
    
    public boolean hasDataKey(String key) {
        return this.craftTags.containsKey(key);
    }
    
    public boolean hasDataValue(Object value) {
        return this.craftTags.containsValue(value);
    }

    public void setSpeedMod(int speedMod) {
        this.speedMod = speedMod;
    }

    public int getSpeedMod() {
        return this.speedMod;
    }

    public void setNPCTag(String tag) {
        this.NPCTag = tag;
    }

    public String getNPCTag() {
        return this.NPCTag;
    }

    public boolean hasNPCTag() {
        if (this.NPCTag.contains("null")) {
            return false;
        }
        return true;
    }
    public void updateMaterials(Counter<Material> counter) {
        materials = counter;
    }

    
    public Counter<Material> getMaterials() {
        return materials;
    }
    
    @Nullable
    public Set<TrackedLocation> getRelativeTrackedLocs(@NotNull Object key) {
        HashSet<TrackedLocation> mlocs = new HashSet<>();
        if (this.trackedLocations.containsKey(key) == false)
            return mlocs;
        for (Object o : this.trackedLocations.get(key)) {
            if (o instanceof TrackedLocation) {
                mlocs.add((TrackedLocation)o);
            }
        }
        return mlocs;
    }

    public Collection<Block> getBlockData(BlockData bkd){
        final ArrayList<Block> blocks = new ArrayList<>();
        if (isTrackingKey(bkd)) {
            blocks.addAll(getTrackedBlocks(bkd));
            for (Block b : getTrackedBlocks(bkd)) {
                if (b.getBlockData().equals(bkd)) continue;
                blocks.remove(b);
            }
            this.setTrackedBlocks(blocks,bkd);
            if (blocks.size() > 0) return blocks;
        }
        for (MovecraftLocation l : hitBox){
            if (Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()).getBlockData().equals(bkd)) {
                blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));

            }
        }
        this.setTrackedBlocks(blocks,bkd);
        return blocks;
    }
    public Collection<Block> getBlockNameExact(String s){
        final Set<Block> blocks = new HashSet<>();
        final Set<MovecraftLocation> mlocs = new HashSet<>();
        if (this.isTrackingKey(s.toLowerCase())) {
            for (MovecraftLocation l : this.getTrackedMovecraftLocsAboard(s.toLowerCase())) {
                if (Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(l,this.getWorld()).name().equalsIgnoreCase(s)) {
                    blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));
                    mlocs.add(l);
                }
            }
            this.setTrackedMovecraftLocs(s.toLowerCase(),mlocs);
            return blocks;
        }
        for (MovecraftLocation l : (this.hitBox)){
            if (Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(l,this.getWorld()).name().equalsIgnoreCase(s)) {
                blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));
                mlocs.add(l);
            }
        }

        this.setTrackedMovecraftLocs(s.toLowerCase(),mlocs);
        return blocks;
    }
    public Collection<Block> getBlockNameNoCache(String s){
        final Set<Block> blocks = new HashSet<>();
        for (MovecraftLocation l : (this.hitBox)){
            if (CraftManager.containsIgnoreCase(Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(l,this.getWorld()).name(), s)) {
                blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));

            }
        }
       return blocks;
    }

    public Collection<Block> getBlockName(String s){
        return getBlockName(s,false);
    }

    public Collection<Block> getBlockName(String s, Boolean async){
        final Set<Block> blocks = new HashSet<>();
        final Set<MovecraftLocation> mlocs = new HashSet<>();
        async = false;
        if (this.isTrackingKey(s.toLowerCase())) {
            for (MovecraftLocation l : this.getTrackedMovecraftLocsAboard(s.toLowerCase())) {
                if (!(this.getHitBox().contains(l))) continue;
                if (!CraftManager.containsIgnoreCase(Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(l,this.getWorld()).name(), s)) continue;
                blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));
                mlocs.add(l);
            }
            this.setTrackedMovecraftLocs(s.toLowerCase(),mlocs);
            return blocks;
        }
        for (MovecraftLocation l : (this.hitBox)){
            if (CraftManager.containsIgnoreCase(Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(l,this.getWorld()).name(), s)) {
                blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));
                mlocs.add(l);
            }
        }

        this.setTrackedMovecraftLocs(s.toLowerCase(),mlocs);
        return blocks;
    }

    public Collection<Block> getBlockTypeNOW(Material mat){
        //ChunkManager.addChunksToLoad(ChunkManager.getChunks((new SetHitBox(this.hitBox)).locations,this.w));
        final Set<Block> blocks = new HashSet<>();
        if (this.isTrackingKey(mat)) {
            for (MovecraftLocation l : this.getTrackedLocs(mat)) {
                if (!(this.getHitBox().contains(l))) continue;
                blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));
            }
            this.setTrackedBlocks(blocks,mat);
            return blocks;
        }
        final Set<MovecraftLocation> mlocs = new HashSet<>();
        for (MovecraftLocation l : this.hitBox){
            if (Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(l,this.getWorld()) == (mat)) {
                blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));
                mlocs.add(l);
            }
        }
        this.setTrackedMovecraftLocs(mat,mlocs);
        return blocks;
    }

    public Collection<Block> getBlockType(Material mat){
        //ChunkManager.addChunksToLoad(ChunkManager.getChunks((new SetHitBox(this.hitBox)).locations,this.w));
        final Set<Block> blocks = new HashSet<>();
        if (this.isTrackingKey(mat)) {
            for (MovecraftLocation l : this.getTrackedLocs(mat)) {
                if (!(this.getHitBox().contains(l))) continue;
                blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));
            }
            this.setTrackedBlocks(blocks,mat);
            return blocks;
        }
        final Set<MovecraftLocation> mlocs = new HashSet<>();
        for (MovecraftLocation l : this.hitBox){
            if (Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(l,this.getWorld()) == (mat)) {
                blocks.add(Movecraft.getInstance().getWorldHandler().getBukkitBlockFast(l,this.getWorld()));
                mlocs.add(l);
            }
        }
        this.setTrackedMovecraftLocs(mat,mlocs);
        return blocks;
    }

    public Collection<MovecraftLocation> getBlockTypeLocation(Material mat){
        if (this.isTrackingKey(mat)) {
            return this.getTrackedMovecraftLocs(mat);
        }
        final Set<MovecraftLocation> mlocs = new HashSet<>();
        for (MovecraftLocation l : this.hitBox){
            if (Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(l,this.getWorld()) == (mat)) {
                mlocs.add(l);
            }
        }
        this.setTrackedMovecraftLocs(mat,mlocs);
        return mlocs;
    }

    public UUID getCuuid() {
     return getUniqueId();
    }

    @Override
    public UUID getUUID() {
     return getUniqueId();
    }

    public UUID getUniqueId() {
     return this.uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseCraft))
            return false;

        return this.getUUID().equals(((BaseCraft) obj).getUUID());
    }

    @Override
    public int hashCode() {
        return this.getUUID().hashCode();
    }

    @NotNull
    public MovecraftWorld getMovecraftWorld() {
        return CachedMovecraftWorld.of(w);
    }

    @NotNull
    public World getWorld() {
        if (WorldManager.INSTANCE.isRunning() && !Bukkit.isPrimaryThread()) {
            var exception = new Throwable("Invoking most methods on worlds while the world manager is running WILL cause deadlock.");
            Bukkit.getLogger().log(Level.SEVERE, exception, exception::getMessage);
        }
        return w;
    }
    public void teleport(Location bl) {
        int dx, dy, dz = 0;
        World dw = bl.getWorld();
        if (bl.getWorld() == null) {
            dw = this.getWorld();
        } else {
            dw = bl.getWorld();
        }
        int currentGear = getCurrentGear();
        Location center = this.getHitBox().getMidPoint().toBukkit(this.getWorld());
        if (!dw.equals(this.getWorld())) {
            dx = bl.getBlockX() - center.getBlockX();
            dy = bl.getBlockY() - center.getBlockY();
            dz = bl.getBlockZ() - center.getBlockZ();
        } else {
            dx = bl.getBlockX() - center.getBlockX();
            dy = bl.getBlockY() - center.getBlockY();
            dz = bl.getBlockZ() - center.getBlockZ();
        }
        setCurrentGear(1);
        Movecraft.getInstance().getAsyncManager().submitTask(new TranslationTask(this, dw, dx, dy, dz), this);
        setCurrentGear(currentGear);
    }
    public void teleport(@Nullable World world, int x, int y, int z) {
        int dx, dy, dz = 0;
        World dw = world;
        if (world == null) {
            dw = this.getWorld();
        } else {
            dw = world;
        }
        int currentGear = getCurrentGear();
        Location center = this.getHitBox().getMidPoint().toBukkit(this.getWorld());
        if (!dw.equals(this.getWorld())) {
            dx = x - center.getBlockX();
            dy = y - center.getBlockY();
            dz = z - center.getBlockZ();
        } else {
            dx = x - center.getBlockX();
            dy = y - center.getBlockY();
            dz = z - center.getBlockZ();
        }
        setCurrentGear(1);
        Movecraft.getInstance().getAsyncManager().submitTask(new TranslationTask(this, dw, dx, dy, dz), this);
        setCurrentGear(currentGear);
    }

    public void setWorld(@NotNull World world) {
        this.w = world;
    }

    @Deprecated
    public void translate(int dx, int dy, int dz) {
        translate(w, dx, dy, dz);
    }

    
    public void translate(@NotNull World world, int dx, int dy, int dz) {
        var v = type.getObjectProperty(CraftType.DISABLE_TELEPORT_TO_WORLDS);
        if (!(v instanceof Collection<?>))
            throw new IllegalStateException("DISABLE_TELEPORT_TO_WORLDS must be of type Collection");
        var disableTeleportToWorlds = ((Collection<?>) v);
        disableTeleportToWorlds.forEach(i -> {
            if (!(i instanceof String))
                throw new IllegalStateException("Values in DISABLE_TELEPORT_TO_WORLDS must be of type String");
        });
        if (!(this instanceof SinkingCraft) && !this.sinking) { // sinking crafts can move in any direction
            if (!world.equals(w)
                    && !(getType().getBoolProperty(CraftType.CAN_SWITCH_WORLD)
                            || disableTeleportToWorlds.contains(world.getName())))
                world = w;
            if (!getType().getBoolProperty(CraftType.ALLOW_HORIZONTAL_MOVEMENT)) {
                dx = 0;
                dz = 0;
            }
            if (!getType().getBoolProperty(CraftType.ALLOW_VERTICAL_MOVEMENT))
                dy = 0;
        }
        if (!world.equals(getWorld()) && world != null) {
            Movecraft.getInstance().getAsyncManager().submitTask(new TranslationTask(this, world, dx, dy, dz), this);
            return;
        }
        // check to see if the craft is trying to move in a direction not permitted by the type
        if (!world.equals(w) && !(getType().getBoolProperty(CraftType.CAN_SWITCH_WORLD) || disableTeleportToWorlds.contains(world.getName())) && !this.getSinking()) {
            world = w;
        }
        if (!getType().getBoolProperty(CraftType.ALLOW_HORIZONTAL_MOVEMENT) && !getSinking()) {
            dx = 0;
            dz = 0;
        }
        if (!getType().getBoolProperty(CraftType.ALLOW_VERTICAL_MOVEMENT) && !getSinking()) {
            dy = 0;
        }
        if (dx == 0 && dy == 0 && dz == 0 && world.equals(w)) {
            return;
        }

        if (!getType().getBoolProperty(CraftType.ALLOW_VERTICAL_TAKEOFF_AND_LANDING) && dy != 0 && !getSinking()) {
            if (dx == 0 && dz == 0) {
                return;
            }
        }
        long ticksElapsed = (System.currentTimeMillis() - getLastMoveTime()) / 15;
        if (ticksElapsed <= getTickCooldown()) {
            return;
        }
        if (!isNotProcessing()) return;

        Movecraft.getInstance().getAsyncManager().submitTask(new TranslationTask(this, world, dx, dy, dz), this);

        updateLastMoveTime();
        setLastTranslation(new MovecraftLocation(dx, dy, dz));
    }

    public void addBlock(Block b) {
        MutableHitBox box = (MutableHitBox)(this.hitBox);
        MovecraftLocation mloc = MathUtils.bukkit2MovecraftLoc(b.getLocation());
        box.add(mloc);
        setDataTag("current_size",(Integer)getDataTag("current_size")+1);
        this.setHitBox(box);
    }
    public void removeBlock(Block b) {
        MutableHitBox box = (MutableHitBox)(this.hitBox);
        MovecraftLocation mloc = MathUtils.bukkit2MovecraftLoc(b.getLocation());
        box.remove(mloc);
        setDataTag("current_size",(Integer)getDataTag("current_size")-1);
        this.setHitBox(box);
    }
    public void removeMaterial(Material m) {
        MutableHitBox box = (MutableHitBox)(this.hitBox);
        Set<MovecraftLocation> mlocs = new HashSet<>();
        for (Block b : getBlockTypeNOW(m)) {
            mlocs.add(MathUtils.bukkit2MovecraftLoc(b.getLocation()));
        }
        setDataTag("current_size",(Integer)getDataTag("current_size")-mlocs.size());
        box.removeAll(mlocs);
        this.setHitBox(box);
    }
    
    public boolean isTracking(@NotNull Object tracked) {
        boolean found = false;
        if (tracked instanceof TrackedLocation) {
            for (Object key : getRawTrackedMap().keySet()) {
                if (getRawTrackedMap().get(key).contains(tracked)) {
                    found = true;
                    return found;
                }
            }
        }
        return found;
    }
    
    public boolean isTrackingKey(@NotNull final Object key) {
        boolean found = false;
        if (getRawTrackedMap().keySet().contains(key)) {
            found = true;
            return found;
        }
        return found;
    }

    public Object removeTrackingkey(@NotNull Object key) {
        return getRawTrackedMap().remove(key);
    }

    public void setTrackedLocs(@NotNull Collection<TrackedLocation> tracked, @NotNull Object key) {
        getRawTrackedMap().put(key, tracked);
    }


    public void setTrackedLocs(@NotNull Object key, @NotNull Collection<TrackedLocation> tracked) {
        getRawTrackedMap().put(key, tracked);
    }

    public void setTrackedMovecraftLocs(@NotNull Object key, @NotNull Collection<MovecraftLocation> tracked) {
        final Set<TrackedLocation> mlocs = new HashSet<>();
        for (Object o : tracked) {
            if (o instanceof MovecraftLocation) {
                final TrackedLocation mloc = new TrackedLocation(this.getHitBox().getMidPoint(),((MovecraftLocation)o));
                mlocs.add(mloc);
                continue;
            }
            break;
        }
        getRawTrackedMap().put(key, mlocs);
    }
    public void setTrackedMovecraftLocs(@NotNull Collection<MovecraftLocation> tracked, @NotNull Object key) {
        setTrackedMovecraftLocs(key, tracked);
    }
    
    @NotNull
    public ArrayList<MovecraftLocation> getTrackedMovecraftLocs(@NotNull Object key) {
        final ArrayList<MovecraftLocation> trackedLocs = new ArrayList<>();
        if (!getRawTrackedMap().containsKey(key))
            return trackedLocs;
        for (TrackedLocation o : getTrackedLocations(key)) {
            trackedLocs.add((MovecraftLocation)((TrackedLocation)o).getLocation());
        }
        return trackedLocs;
    }
    
    @NotNull
    public Collection<MovecraftLocation> getTrackedLocs(@NotNull Object key) {
        final ArrayList<MovecraftLocation> trackedLocs = new ArrayList<>();
        if (!getRawTrackedMap().containsKey(key))
            return trackedLocs;
        boolean bool = false;
        if (key instanceof Material) bool = true;
        if (key instanceof BlockData) bool = true;
        for (TrackedLocation o : getTrackedLocations(key,bool)) {
            trackedLocs.add((MovecraftLocation)((TrackedLocation)o).getLocation());
        }
        return trackedLocs;
    }
    
    @NotNull
    public ArrayList<MovecraftLocation> getTrackedMovecraftLocsAboard(@NotNull Object key) {
        final ArrayList<MovecraftLocation> trackedLocs = new ArrayList<>();
        if (!getRawTrackedMap().containsKey(key))
            return trackedLocs;
        for (TrackedLocation o : getTrackedLocations(key,true)) {
            trackedLocs.add((MovecraftLocation)((TrackedLocation)o).getLocation());
        }
        return trackedLocs;
    }
    @NotNull
    public HashSet<MovecraftLocation> getTrackedMovecraftLocsSet(@NotNull Object key) {
        final HashSet<MovecraftLocation> trackedLocs = new HashSet<>();
        if (!getRawTrackedMap().containsKey(key))
            return trackedLocs;
        for (TrackedLocation o : getTrackedLocations(key)) {
            trackedLocs.add((MovecraftLocation)((TrackedLocation)o).getLocation());
        }
        return trackedLocs;
    }
    
    @NotNull
    public ArrayList<TrackedLocation> getTrackedLocations(@NotNull Object key) {
        return getTrackedLocations(key,false);
    }
    
    @NotNull
    public ArrayList<TrackedLocation> getTrackedLocations(@NotNull Object key, boolean isAboard) {
        if (!getRawTrackedMap().containsKey(key))
            return new ArrayList<TrackedLocation>(0);
        final ArrayList<TrackedLocation> trackedLocs = new ArrayList<>(getRawTrackedMap().get(key).size());
        for (TrackedLocation o : getRawTrackedMap().get(key)) {
            if (isAboard) {
                if (!this.hitBox.contains(o.getLocation())) continue;
            }
            trackedLocs.add(o);
        }
        if (isAboard) {
            getRawTrackedMap().put(key,trackedLocs);
        }
        return trackedLocs;
    }
    
    @Nullable
    public ArrayList<Block> getTrackedBlocks(@NotNull Object key) {
        final ArrayList<Block> blocks = new ArrayList<>();
        for (TrackedLocation o : getTrackedLocations(key,true)) {
            blocks.add(((MovecraftLocation)((TrackedLocation)o).getLocation()).toBukkit(this.getWorld()).getBlock());
        }
        return blocks;
    }

    public void setTrackedBlocks(Object key, @NotNull Collection<Block> blocks) {
        setTrackedBlocks(blocks,key);
    }

    public void setTrackedBlocks(@NotNull Collection<Block> blocks, Object key) {
        final Set<MovecraftLocation> trackedLocs = new HashSet<>();
        for (Block block : blocks) {
            if (block.getWorld().getName().equals(this.getWorld().getName())) {
                final MovecraftLocation loc = MathUtils.bukkit2MovecraftLoc(block.getLocation());
                trackedLocs.add(loc);
            }
        }
        this.setTrackedMovecraftLocs(key, trackedLocs);
    }
    
    public void addTrackedMovecraftLocation(Object key, @NotNull MovecraftLocation loc) {
        List<TrackedLocation> trackedLocs = new ArrayList<>();
        if (getRawTrackedMap().containsKey(key)) {
            trackedLocs.addAll(getRawTrackedMap().get(key));
        }
        final TrackedLocation tloc = new TrackedLocation(this.getHitBox().getMidPoint(),loc);
        trackedLocs.add(tloc);
        getRawTrackedMap().put(key,trackedLocs);
    }

    public HitBox translateBox(HitBox box, MovecraftLocation v) {
        BitmapHitBox newBox = new BitmapHitBox();
        for(MovecraftLocation oldLoc : box) {
            MovecraftLocation newLoc = oldLoc.translate(v.x,v.y,v.z);
            newBox.add(newLoc);
        }
        return newBox;
    }
    
    public HitBox rotateBox(HitBox box, MovecraftLocation p, MovecraftRotation r) {
        BitmapHitBox newBox = new BitmapHitBox();
        for(MovecraftLocation oldLoc : box){
            MovecraftLocation newLoc = MathUtils.rotateVec(r,oldLoc.subtract(p)).add(p);
            newBox.add(newLoc);
        }
        return newBox;
    }
    public boolean hasPassenger(@NotNull Entity passenger) {
        return this.passengers.contains(passenger);
    }
    public void clearPassengers() {
        this.passengers.clear();
    }
    public void setPassengers(@NotNull Collection<Entity> passengers) {
        this.passengers.clear();
        this.passengers.addAll(passengers);
    }
    public void addPassenger(@NotNull Entity passenger) {
        this.passengers.add(passenger);
    }
    public void addPassengers(@NotNull Collection<Entity> passengers) {
        this.passengers.addAll(passengers);
    }
    public void removePassenger(@NotNull Entity passenger) {
        this.passengers.remove(passenger);
    }
    public Set<Entity> getPassengers() {
        return this.passengers;
    }
    
    public void rotate(MovecraftRotation rotation) {
        this.rotate(rotation, getMidPoint(), false);
    }
    
    public void rotate(MovecraftRotation rotation, MovecraftLocation originPoint) {
        this.rotate(rotation, originPoint, false);
    }
    
    public void rotate(MovecraftRotation rotation, MovecraftLocation originPoint, boolean isSubCraft) {
        if (getLastMoveTime() + 1e9 > System.nanoTime()) {
            return;
        }
        long ticksElapsed = (System.currentTimeMillis() - getLastMoveTime()) / 60;
        if (ticksElapsed <= getSpeed() && !isSubCraft) {
            return;
        }
        if (!isNotProcessing()) return;
        Movecraft.getInstance().getAsyncManager().submitTask(new RotationTask(this, originPoint, rotation, getWorld(), isSubCraft), this);
        updateLastMoveTime();
        setLastRotateTime(System.currentTimeMillis());
    }
    /**
     * Gets the crafts that have made contact with this craft
     *
     * @return a set of crafts on contact with this craft
     */
    @NotNull
    
    public Set<Craft> getContacts() {
        contacts.clear();
        /*for (Craft contact : CraftManager.getInstance().getCraftsInWorld(w)) {
            MovecraftLocation ccenter = getHitBox().getMidPoint();
            MovecraftLocation tcenter = contact.getHitBox().getMidPoint();
            int distsquared = ccenter.distanceSquared(tcenter);
            double detectionMultiplier;
            if (tcenter.getY() > 65) // TODO: fix the water line
                detectionMultiplier = (double) contact.getType().getPerWorldProperty(CraftType.PER_WORLD_DETECTION_MULTIPLIER, contact.getWorld());
            else
                detectionMultiplier = (double) contact.getType().getPerWorldProperty(CraftType.PER_WORLD_UNDERWATER_DETECTION_MULTIPLIER, contact.getWorld());
            int detectionRange = (int) (contact.getOrigBlockCount() * detectionMultiplier);
            detectionRange = detectionRange * 10;
            if (distsquared > detectionRange || contact.getNotificationPlayer() == getNotificationPlayer()) {
                continue;
            }
            contacts.add(contact);
        }*/
        return contacts;
    }

    
    public void resetSigns(@NotNull Sign clicked) {
        for (final Block b : getBlockName("SIGN")) {
            if (!(b.getState() instanceof Sign)) {
                continue;
            }
            final Sign sign = (Sign) b.getState();
            if (sign.equals(clicked)) {
                continue;
            }
            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Cruise: ON")) {
                sign.setLine(0, "Cruise: OFF");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Cruise: OFF")
                    && ChatColor.stripColor(clicked.getLine(0)).equalsIgnoreCase("Cruise: ON")
                    && getFacing(sign) == getFacing(clicked)) {
                sign.setLine(0, "Cruise: ON");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Ascend: ON")) {
                sign.setLine(0, "Ascend: OFF");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Ascend: OFF")
                    && ChatColor.stripColor(clicked.getLine(0)).equalsIgnoreCase("Ascend: ON")) {
                sign.setLine(0, "Ascend: ON");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Descend: ON")) {
                sign.setLine(0, "Descend: OFF");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Descend: OFF")
                    && ChatColor.stripColor(clicked.getLine(0)).equalsIgnoreCase("Descend: ON")) {
                sign.setLine(0, "Descend: ON");
            }
            try {
                sign.setWaxed(true);
                sign.setEditable(false);
            } catch (Exception exc) {
                sign.setEditable(true);
            }
            sign.update();
        }
    }

    
    public void resetSigns() {
        for (final Block b : getBlockName("SIGN")) {

            final Sign sign = (Sign) b.getState();
            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Contacts:") || ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Radar]")) {
                sign.setLine(1, "");
                sign.setLine(2, "");
                sign.setLine(3, "");
            }
            //DEBUG
            //System.out.println("Found Sign? "+b.getBlockData().toString()+" LINES: 1: "+sign.getLine(0)+" 2: "+sign.getLine(1)+" 3: "+sign.getLine(2));
            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Cruise: ON") || ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Cruise: OFF")) {
                sign.setLine(0, "Cruise: OFF");
                if (!(sign.getBlockData() instanceof WallSign)) {
                    continue;
                }
                if (getCruiseDirection() == CruiseDirection.NONE) {
                    CruiseDirection cdir = CruiseDirection.fromBlockFace(((WallSign) sign.getBlockData()).getFacing());
                    this.cruiseDirection = cdir;
                    //DEBUG
                    //System.out.println("New Direction? "+cdir.toString()+" : "+this.cruiseDirection.toString());
                }
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Crew:") || ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[crew]")) {
                sign.setLine(0, "Crew:");

            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Ascend: ON")) {
                sign.setLine(0, "Ascend: OFF");
            }
            else if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Descend: ON")) {
                sign.setLine(0, "Descend: OFF");
            }
            try {
                sign.setWaxed(true);
                sign.setEditable(false);
            } catch (Exception exc) {
                sign.setEditable(true);
            }
            sign.update();
        }
    }

    
    public boolean getCruising() {
        return cruising;
    }

    
    public void setCruising(boolean cruising) {
        audience.sendActionBar(Component.text().content("Cruising " + (cruising ? "enabled" : "disabled")));
        this.cruising = cruising;
    }

    
    public void setSinking(boolean sinking) {
        this.sinking = sinking;
    }
    public boolean getSinking() {
        return sinking || this instanceof SinkingCraft;
    }

    
    public void sink() {
      CraftManager.getInstance().sink(this);
    }

    public void sinkQuietly() {
      CraftManager.getInstance().quietSink(this);
    }

    
    public boolean getDisabled() {
        return disabled;
    }

    
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    
    public CruiseDirection getCruiseDirection() {
        return cruiseDirection;
    }

    
    public void setCruiseDirection(CruiseDirection cruiseDirection) {
        this.cruiseDirection = cruiseDirection;
    }

    public long getLastCruiseUpdate() {
        return lastCruiseUpdate;
    }

    
    public void setLastCruiseUpdate(long update) {
        this.lastCruiseUpdate = update;
    }

    
    public long getLastBlockCheck() {
        return lastBlockCheck;
    }

    
    public void setLastBlockCheck(long update) {
        this.lastBlockCheck = update;
    }

    
    @NotNull
    public MovecraftLocation getLastTranslation() {
        return this.lastTranslation;
    }

    
    public void setLastTranslation(@NotNull MovecraftLocation lastTranslation) {
        this.lastTranslation = lastTranslation;
    }


    public int getBurningFuel() {
        return burningFuel;
    }

    
    public void setBurningFuel(int burningFuel) {
        this.burningFuel = burningFuel;
    }

    
    public int getOrigBlockCount() {
        return origBlockCount;
    }

    
    public void setOrigBlockCount(int origBlockCount) {
        this.origBlockCount = origBlockCount;
    }

    @Nullable
    @Deprecated
    public Player getNotificationPlayer() {
        return notificationPlayer;
    }

    @Deprecated
    public void setNotificationPlayer(@Nullable Player notificationPlayer) {
        this.notificationPlayer = notificationPlayer;
    }

    
    public long getOrigPilotTime() {
        return origPilotTime;
    }

    public long getLastMoveTime() {
        return this.lastMoveTime;
    }

    public void updateLastMoveTime() {
        this.lastMoveTime = System.currentTimeMillis();
    }

    
    public double getMeanCruiseTime() {
        return stats.getRecentAverage();
    }

    
    public void addCruiseTime(float cruiseTime) {
        stats.accept(cruiseTime);
    }

    
    public int getTickCooldown() {
        if (sinking)
            return type.getIntProperty(CraftType.SINK_RATE_TICKS);
        if (this instanceof SinkingCraft)
            return type.getIntProperty(CraftType.SINK_RATE_TICKS);
        if (materials.isEmpty()) {
            for (MovecraftLocation location : hitBox) {
                materials.add(location.toBukkit(w).getBlock().getType());
            }
        }

        int chestPenalty = 0;
        for (Material m : Tags.CHESTS) {
            chestPenalty += materials.get(m);
        }
        chestPenalty += (int)type.getDoubleProperty(CraftType.CHEST_PENALTY)*1.75;
        int tpsMod = 3;

        if (cruising) {
            if (cruiseDirection == CruiseDirection.UP || cruiseDirection == CruiseDirection.DOWN)
                return (int)1 + ((int) type.getPerWorldProperty(CraftType.PER_WORLD_VERT_CRUISE_TICK_COOLDOWN, w) + chestPenalty) * (int)(type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? currentGear : 1) + tpsMod;
        }
        // Ascent or Descent

        // Dynamic Fly Block Speed
        int cruiseTickCooldown = (int) type.getPerWorldProperty(CraftType.PER_WORLD_CRUISE_TICK_COOLDOWN, w) + tpsMod;
        if (type.getDoubleProperty(CraftType.DYNAMIC_FLY_BLOCK_SPEED_FACTOR) != 0) {
            EnumSet<Material> flyBlockMaterials = type.getMaterialSetProperty(CraftType.DYNAMIC_FLY_BLOCK);
            double count = 0;
            for (Material m : flyBlockMaterials) {
                count += materials.get(m);
            }
            double ratio = count / hitBox.size();
            double speed = (type.getDoubleProperty(CraftType.DYNAMIC_FLY_BLOCK_SPEED_FACTOR) * 1.5)
                    * (ratio - 0.5)
                        + (20.0 / cruiseTickCooldown) + 1;
            return Math.max((int) Math.round((20.0 * ((int) type.getPerWorldProperty(CraftType.PER_WORLD_CRUISE_SKIP_BLOCKS, w) + 1)) / speed) * (type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? currentGear : 1), 1);
        }
        
        if (type.getDoubleProperty(CraftType.DYNAMIC_LAG_SPEED_FACTOR) == 0.0
                || type.getDoubleProperty(CraftType.DYNAMIC_LAG_POWER_FACTOR) == 0.0
                || Math.abs(type.getDoubleProperty(CraftType.DYNAMIC_LAG_POWER_FACTOR)) > 1.0)
            return (cruiseTickCooldown) * (type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? currentGear : 1);
        if (stats.getCount() == 0)
            return (int) Math.round(20.0 * ((cruiseTickCooldown + 1.0) / type.getDoubleProperty(CraftType.DYNAMIC_LAG_MIN_SPEED)) * (type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? currentGear : 1));
        int cruiseSkipBlocks = (int) type.getPerWorldProperty(CraftType.PER_WORLD_CRUISE_SKIP_BLOCKS, w);
        
        if (Settings.Debug) {
            Bukkit.getLogger().info("Skip: " + cruiseSkipBlocks);
            Bukkit.getLogger().info("Tick: " + cruiseTickCooldown);
            Bukkit.getLogger().info("SpeedFactor: " + type.getDoubleProperty(CraftType.DYNAMIC_LAG_SPEED_FACTOR));
            Bukkit.getLogger().info("PowerFactor: " + type.getDoubleProperty(CraftType.DYNAMIC_LAG_POWER_FACTOR));
            Bukkit.getLogger().info("MinSpeed: " + type.getDoubleProperty(CraftType.DYNAMIC_LAG_MIN_SPEED));
            Bukkit.getLogger().info("CruiseTime: " + getMeanCruiseTime() * 1000.0 + "ms");
        }
        // Dynamic Lag Speed
        double speed = 20.0 * (cruiseSkipBlocks + 1.0) / (float) cruiseTickCooldown;
        speed -= type.getDoubleProperty(CraftType.DYNAMIC_LAG_SPEED_FACTOR) * Math.pow(getMeanCruiseTime() * 1000.0, type.getDoubleProperty(CraftType.DYNAMIC_LAG_POWER_FACTOR));
        speed = Math.max(type.getDoubleProperty(CraftType.DYNAMIC_LAG_MIN_SPEED), speed);
        if (speed == 0.0d) speed = 1d;
        return (int) (Math.round((20.0 * (cruiseSkipBlocks + 5.0)) / speed) * (type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN) ? currentGear : 1))+getSpeedMod(); //Negative SpeedMod means Faster, Positive SpeedMod Means Slower.
        //In theory, the chest penalty is not needed for a DynamicLag craft.
    }
    /**
     * gets the speed of a craft in blocks per second.
     *
     * @return the speed of the craft
     */
    
    public double getSpeed() {
        if (cruiseDirection == CruiseDirection.UP || cruiseDirection == CruiseDirection.DOWN) {
            return 20 * ((int) type.getPerWorldProperty(CraftType.PER_WORLD_VERT_CRUISE_SKIP_BLOCKS, w) + 1) / (double) getTickCooldown();
        }
        else {
            return 20 * ((int) type.getPerWorldProperty(CraftType.PER_WORLD_CRUISE_SKIP_BLOCKS, w) + 1) / (double) getTickCooldown();
        }
    }

    
    public long getLastRotateTime() {
        return lastRotateTime;
    }

    
    public void setLastRotateTime(long lastRotateTime) {
        this.lastRotateTime = lastRotateTime;
    }


    public int getWaterLine() {
        //TODO: Remove this temporary system in favor of passthrough blocks
        // Find the waterline from the surrounding terrain or from the static level in the craft type
        if (getWorld().getName().contains("Void") || getWorld().getName().contains("Space") || getWorld().getName().contains("Zone")) return -128;
        if (hitBox.getMinY() >= 500) return -128;
        int waterLine = type.getIntProperty(CraftType.STATIC_WATER_LEVEL);
        if ((type.getIntProperty(CraftType.STATIC_WATER_LEVEL) != -64 && type.getIntProperty(CraftType.STATIC_WATER_LEVEL) != -128) || hitBox.isEmpty()) {
            return type.getIntProperty(CraftType.STATIC_WATER_LEVEL);
        }

        // figure out the water level by examining blocks next to the outer boundaries of the craft
        for (int posY = hitBox.getMaxY() + 1; posY >= hitBox.getMinY() - 2; posY--) {
            int numWater = 0;
            int numAir = 0;
            int posX;
            int posZ;
            posZ = hitBox.getMinZ() - 1;
            for (posX = hitBox.getMinX() - 1; posX <= hitBox.getMaxX() + 1; posX++) {
                final MovecraftLocation loc = new MovecraftLocation(posX, posY, posZ);
                Material material = Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(loc,w);
                if (hitBox.contains(new MovecraftLocation(posX, posY, posZ)))
                    continue;
                if (Tags.FLUID.contains(material))
                    numWater++;
                if (material.isAir())
                    numAir++;
            }
            posZ = hitBox.getMaxZ() + 1;
            for (posX = hitBox.getMinX() - 1; posX <= hitBox.getMaxX() + 1; posX++) {
                final MovecraftLocation loc = new MovecraftLocation(posX, posY, posZ);
                Material material = Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(loc,w);
                if (hitBox.contains(new MovecraftLocation(posX, posY, posZ)))
                    continue;
                if (Tags.FLUID.contains(material))
                    numWater++;
                if (material.isAir())
                    numAir++;
            }
            posX = hitBox.getMinX() - 1;
            for (posZ = hitBox.getMinZ(); posZ <= hitBox.getMaxZ(); posZ++) {
                final MovecraftLocation loc = new MovecraftLocation(posX, posY, posZ);
                Material material = Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(loc,w);
                if (hitBox.contains(new MovecraftLocation(posX, posY, posZ)))
                    continue;
                if (Tags.FLUID.contains(material))
                    numWater++;
                if (material.isAir())
                    numAir++;
            }
            posX = hitBox.getMaxX() + 1;
            for (posZ = hitBox.getMinZ(); posZ <= hitBox.getMaxZ(); posZ++) {
                final MovecraftLocation loc = new MovecraftLocation(posX, posY, posZ);
                Material material = Movecraft.getInstance().getWorldHandler().toBukkitBlockFast(loc,w);
                if (hitBox.contains(new MovecraftLocation(posX, posY, posZ)))
                    continue;
                if (Tags.FLUID.contains(material))
                    numWater++;
                if (material.isAir())
                    numAir++;
            }
            if (numWater > numAir) {
                waterLine = posY;
                break;
            }
        }
        return waterLine;
    }

    
    public @NotNull Map<Location, BlockData> getPhaseBlocks() {
        return phaseBlocks;
    }

    public @NotNull Map<Object, Collection<TrackedLocation>> getRawTrackedMap() {
        return this.trackedLocations;
    }

    public @NotNull Map<String, Object> getCraftTags() {
        return this.craftTags;
    }

    
    @NotNull
    public String getName() {
        return this.name;
    }

    
    public void setName(@NotNull String name) {
        this.name = name;
    }

    
    @NotNull
    public MutableHitBox getCollapsedHitBox() {
        return collapsedHitBox;
    }

    
    @NotNull
    public MutableHitBox getFluidLocations() {
        return fluidLocations;
    }

    
    public void setFluidLocations(@NotNull MutableHitBox fluidLocations) {
        this.fluidLocations = fluidLocations;
    }

    
    public long getLastTeleportTime() {
        return lastTeleportTime;
    }

    
    public void setLastTeleportTime(long lastTeleportTime) {
        this.lastTeleportTime = lastTeleportTime;
    }

    
    public int getCurrentGear() {
        return currentGear;
    }

    
    public void setCurrentGear(int currentGear) {
        this.currentGear = Math.min(Math.max(currentGear, 1), type.getIntProperty(CraftType.GEAR_SHIFTS));
    }

    
    @NotNull
    public Audience getAudience() {
        return audience;
    }

    
    public void setAudience(@NotNull Audience audience) {
        this.audience = audience;
    }
}
