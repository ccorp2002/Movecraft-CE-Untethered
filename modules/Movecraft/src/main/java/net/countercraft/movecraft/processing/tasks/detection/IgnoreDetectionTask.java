package net.countercraft.movecraft.processing.tasks.detection;

import com.google.common.base.Functions;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.WorldHandler;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.SubCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftPilotEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.processing.WorldManager;
import net.countercraft.movecraft.processing.effects.Effect;
import net.countercraft.movecraft.processing.functions.CraftSupplier;
import net.countercraft.movecraft.processing.functions.DetectionPredicate;
import net.countercraft.movecraft.processing.functions.Result;
import net.countercraft.movecraft.processing.tasks.detection.validators.*;
import net.countercraft.movecraft.util.AtomicLocationSet;
import net.countercraft.movecraft.util.CollectionUtils;
import net.countercraft.movecraft.util.Tags;
import net.countercraft.movecraft.util.hitboxes.BitmapHitBox;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.util.hitboxes.SetHitBox;
import net.countercraft.movecraft.util.hitboxes.SolidHitBox;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IgnoreDetectionTask implements Supplier<Effect> {
    private final static MovecraftLocation[] SHIFTS = {
            new MovecraftLocation(0, 1, 1),
            new MovecraftLocation(0, 0, 1),
            new MovecraftLocation(0, -1, 1),
            new MovecraftLocation(0, 1, 0),
            new MovecraftLocation(1, 1 ,0),
            new MovecraftLocation(1, 0 ,0),
            new MovecraftLocation(1, -1 ,0),
            new MovecraftLocation(0, 1, -1),
            new MovecraftLocation(0, 0, -1),
            new MovecraftLocation(0, -1, -1),
            new MovecraftLocation(0, -1, 0),
            new MovecraftLocation(-1, 1, 0),
            new MovecraftLocation(-1, 0, 0),
            new MovecraftLocation(-1, -1, 0)
    };
    private static final AllowedIgnoreBlockValidator ALLOWED_BLOCK_VALIDATOR = new AllowedIgnoreBlockValidator();
    private static final ForbiddenBlockValidator FORBIDDEN_BLOCK_VALIDATOR = new ForbiddenBlockValidator();
    private static final List<DetectionPredicate<MovecraftLocation>> VALIDATORS = List.of(
            new ForbiddenSignStringValidator(),
            new NameSignValidator(),
            new PilotSignValidator()
    );
    private static final List<DetectionPredicate<Map<Material, Deque<MovecraftLocation>>>> COMPLETION_VALIDATORS = List.of(
            new SizeValidator(),
            new FlyBlockValidator()
    );
    private static final List<DetectionPredicate<Map<Material, Deque<MovecraftLocation>>>> VISITED_VALIDATORS = List.of(
            new WaterContactValidator()
    );



    private final List<String> mat;
    private final MovecraftLocation startLocation;
    private final MovecraftWorld movecraftWorld;
    private final CraftType type;
    private final CraftSupplier supplier;
    private final World world;
    private final Player player;
    private final Audience audience;
    private final Function<Craft, Effect> postDetection;

    private final LongAdder size = new LongAdder();
    private final Set<MovecraftLocation> visited = new AtomicLocationSet();
    private final ConcurrentMap<Material, Deque<MovecraftLocation>> materials = new ConcurrentHashMap<>();
    private final ConcurrentMap<Material, Deque<MovecraftLocation>> visitedMaterials = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<MovecraftLocation> fluid = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<MovecraftLocation> illegal = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<MovecraftLocation> legal = new ConcurrentLinkedDeque<>();


    public IgnoreDetectionTask(@NotNull List<String> mat, @NotNull MovecraftLocation startLocation, @NotNull MovecraftWorld movecraftWorld,
                            @NotNull CraftType type, @NotNull CraftSupplier supplier,
                            @NotNull World world, @Nullable Player player,
                            @NotNull Audience audience,
                            @NotNull Function<Craft, Effect> postDetection) {
        this.mat = mat;
        this.startLocation = startLocation;
        this.movecraftWorld = movecraftWorld;
        this.type = type;
        this.supplier = supplier;

        this.world = world;
        this.player = player;
        this.audience = audience;
        this.postDetection = postDetection;
    }

    private Effect detectInterior(Craft craft) {
        craft.setDataTag("origin_size",craft.getOrigBlockCount());
        craft.setDataTag("current_size",craft.getOrigBlockCount());
        if (!craft.getType().getBoolProperty(CraftType.DETECT_INTERIOR)) return () -> {};
        final WorldHandler handler = Movecraft.getInstance().getWorldHandler();
        final World badWorld = WorldManager.INSTANCE.executeMain(craft::getWorld);
        final int waterLine = WorldManager.INSTANCE.executeMain(craft::getWaterLine);

        final BitmapHitBox invertedHitBox = new BitmapHitBox(craft.getHitBox().boundingHitBox().difference(craft.getHitBox()));
        //A set of locations that are confirmed to be "exterior" locations
        final BitmapHitBox exterior = new BitmapHitBox();
        final BitmapHitBox interior = new BitmapHitBox();

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
        //Check to see which locations in the from set are actually outside of the craft

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
        craft.setTrackedMovecraftLocs("valid_exterior",exterior.asSet());
        final Set<MovecraftLocation> interiorSet = new HashSet<>();
        final HitBox difference = (invertedHitBox.difference(exterior));
        for (MovecraftLocation loc : difference.asSet()) {
            final Material mat = movecraftWorld.getMaterial(loc);
            if (mat.isAir()) {
                if (exterior.contains(loc)) continue;
                if (craft.getHitBox().contains(loc)) continue;
                interiorSet.add(loc);
            }
            if (type.getMaterialSetProperty(CraftType.INTERIOR_BLOCKS).contains(mat)) {
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
                    final Material mat = movecraftWorld.getMaterial(loc);
                    if (!mat.isAir()) continue;
                    interiorSet.add(loc);
                }
            }
        }
        if (craft.getHitBox().size()*1.75 >= interiorSet.size()) {
            interior.addAll(interiorSet);
            craft.setTrackedMovecraftLocs("air",interiorSet);
            craft.setHitBox(craft.getHitBox().union(interior));
        }
        if (waterLine != -64 && waterLine != -128) return () -> {};
        var waterData = Movecraft.getInstance().getWaterBlockData();
        return () -> {
            for (MovecraftLocation location : craft.getHitBox()) {
                if (location.getY() <= waterLine) {
                    craft.getPhaseBlocks().put(location.toBukkit(badWorld), waterData);
                }
            }
        };
    }

    @NotNull
    private Set<Craft> findParents(@NotNull HitBox hitBox) {
        SolidHitBox solidHitBox = hitBox.boundingHitBox();
        Set<Craft> nearby = new HashSet<>();
        for(Craft c : CraftManager.getInstance()) {
            // Add the craft to nearby if their bounding boxes intersect
            SolidHitBox otherSolidBox = c.getHitBox().boundingHitBox();
            if(solidHitBox.intersects(otherSolidBox))
                nearby.add(c);
        }

        Set<Craft> parents = new HashSet<>();
        for(var loc : hitBox) {
            if(nearby.size() == 0)
                break;

            for(Craft c : nearby) {
                if(c.getHitBox().contains(loc)) {
                    parents.add(c);
                }
            }
            // Clear out crafts from nearby as they get added to parents
            nearby.removeAll(parents);
        }
        return parents;
    }

    @Override
    public Effect get() {
        frontier();
        if (!illegal.isEmpty())
            return null;

        var result = COMPLETION_VALIDATORS.stream().reduce(DetectionPredicate::and).orElse(
                (a, b, c, d) -> Result.fail()
        ).validate(materials, type, movecraftWorld, player);
        result = result.isSucess() ? VISITED_VALIDATORS.stream().reduce(DetectionPredicate::and).orElse(
                (a, b, c, d) -> Result.fail()
        ).validate(visitedMaterials, type, movecraftWorld, player) : result;
        if (!result.isSucess()) {
            String message = result.getMessage();
            return () -> audience.sendMessage(Component.text(message));
        }

        var hitbox = new BitmapHitBox(legal);
        var parents = findParents(hitbox);

        var supplied = supplier.apply(type, world, player, parents);
        result = supplied.getLeft();
        Craft craft = supplied.getRight();

        if (type.getBoolProperty(CraftType.MUST_BE_SUBCRAFT) && !(craft instanceof SubCraft)) {
            result = Result.failWithMessage(I18nSupport.getInternationalisedString("Detection - Must Be Subcraft"));
        }

        if (!result.isSucess()) {
            String message = result.getMessage();
            return () -> audience.sendMessage(Component.text(message));
        }

        craft.setAudience(audience);
        craft.setHitBox(hitbox);
        craft.setWorld(world);
        craft.setFluidLocations(new BitmapHitBox(fluid));
        craft.setOrigBlockCount(craft.getHitBox().size());
        final CraftDetectEvent event = new CraftDetectEvent(craft, startLocation);

        WorldManager.INSTANCE.executeMain(() -> Bukkit.getPluginManager().callEvent(event));
        if (event.isCancelled())
            return () -> craft.getAudience().sendMessage(Component.text(event.getFailMessage()));

        return ((Effect) () -> {
            // Notify player and console
            craft.getAudience().sendMessage(Component.text(String.format(
                    "%s Size: %s",
                    I18nSupport.getInternationalisedString("Detection - Successfully piloted craft"),
                    craft.getOrigBlockCount()
            )));
            Movecraft.getInstance().getLogger().info(String.format(
                    I18nSupport.getInternationalisedString("Detection - Success - Log Output"),
                    player == null ? "null" : player.getName(),
                    craft.getType().getStringProperty(CraftType.NAME),
                    craft.getHitBox().size(),
                    craft.getHitBox().getMinX(),
                    craft.getHitBox().getMinZ()
            ));
        }).andThen(
                // Apply post detection effect
                postDetection.apply(craft)
        ).andThen(
                // Add craft to CraftManager
                () -> CraftManager.getInstance().add(craft)
        ).andThen(
                () -> {}
        ).andThen(
                () -> {
                    final CraftPilotEvent craftPevent = new CraftPilotEvent(craft, CraftPilotEvent.Reason.PLAYER);
                    Bukkit.getPluginManager().callEvent(craftPevent);
                }
        );
    }

    private void frontier() {
        ConcurrentLinkedQueue<MovecraftLocation> currentFrontier = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<MovecraftLocation> nextFrontier = new ConcurrentLinkedQueue<>();
        currentFrontier.add(startLocation);
        currentFrontier.addAll(Arrays.stream(SHIFTS).map(startLocation::add).collect(Collectors.toList()));
        visited.addAll(currentFrontier);
        int threads = Runtime.getRuntime().availableProcessors();
        while(!currentFrontier.isEmpty() && size.intValue() < type.getIntProperty(CraftType.MAX_SIZE) + threads) {
            List<ForkJoinTask<?>> tasks = new ArrayList<>();
            for(int j = 0; j < threads ; j++) {
                tasks.add(ForkJoinPool.commonPool().submit(new DetectAction(currentFrontier, nextFrontier, this.mat)));
            }

            for(var task : tasks) {
                task.join();
                if(task.getException() != null)
                    task.getException().printStackTrace();
            }
            currentFrontier = nextFrontier;
            nextFrontier = new ConcurrentLinkedQueue<>();
        }
    }

    @Override
    public String toString(){
        return String.format("DetectionTask{%s:%s:%s}", player, type, startLocation);
    }

    private class DetectAction implements Runnable {
        private final ConcurrentLinkedQueue<MovecraftLocation> currentFrontier;
        private final ConcurrentLinkedQueue<MovecraftLocation> nextFrontier;
        private final List<String> mat;


        private DetectAction(ConcurrentLinkedQueue<MovecraftLocation> currentFrontier, ConcurrentLinkedQueue<MovecraftLocation> nextFrontier, List<String> mat) {
            this.currentFrontier = currentFrontier;
            this.nextFrontier = nextFrontier;
            this.mat = mat;
        }

        @Override
        public void run() {
            MovecraftLocation probe;
            while((probe = currentFrontier.poll()) != null) {
                visitedMaterials.computeIfAbsent(movecraftWorld.getMaterial(probe), Functions.forSupplier(ConcurrentLinkedDeque::new)).add(probe);
                if(!ALLOWED_BLOCK_VALIDATOR.validate(probe, type, movecraftWorld, player, mat).isSucess())
                    continue;
                DetectionPredicate<MovecraftLocation> chain = FORBIDDEN_BLOCK_VALIDATOR;
                for(var validator : VALIDATORS) {
                    chain = chain.and(validator);
                }
                var result = chain.validate(probe, type, movecraftWorld, player);
                if (mat.contains(movecraftWorld.getData(probe).getAsString(false).toUpperCase()) || mat.contains(movecraftWorld.getMaterial(probe).toString().toUpperCase())) {
                    continue;
                }
                if(result.isSucess()) {
                    legal.add(probe);
                    if (mat.contains(movecraftWorld.getData(probe).getAsString(false).toUpperCase()) || mat.contains(movecraftWorld.getMaterial(probe).toString().toUpperCase())) {
                        continue;
                    }
                    if(Tags.FLUID.contains(movecraftWorld.getMaterial(probe)))
                        fluid.add(probe);
                    size.increment();
                    materials.computeIfAbsent(movecraftWorld.getMaterial(probe), Functions.forSupplier(ConcurrentLinkedDeque::new)).add(probe);

                    for(MovecraftLocation shift : SHIFTS) {
                        var shifted = probe.add(shift);
                        if (mat.contains(movecraftWorld.getData(shifted).getAsString(false).toUpperCase()) || mat.contains(movecraftWorld.getMaterial(shifted).toString().toUpperCase())) {
                            nextFrontier.remove(shifted);
                            continue;
                        }
                        if(visited.add(shifted)) {
                            nextFrontier.add(shifted);
                        }
                    }
                }
                else {
                    illegal.add(probe);
                    audience.sendMessage(Component.text(result.getMessage()));
                }
            }
        }
    }
}
