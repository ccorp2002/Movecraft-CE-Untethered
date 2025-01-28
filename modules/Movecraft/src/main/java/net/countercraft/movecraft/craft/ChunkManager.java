package net.countercraft.movecraft.craft;

import net.countercraft.movecraft.MovecraftChunk;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.bukkit.plugin.Plugin;

@Deprecated
public class ChunkManager implements Listener {

  private static final Plugin PLUGIN = Bukkit.getPluginManager().getPlugin("Movecraft");
  private static final Set<MovecraftChunk> chunks = new HashSet<>();

  public static void addChunksToLoad(final Iterable<MovecraftChunk> list) {
    //World world = null;
        (new BukkitRunnable() {
            @Override
            public void run() {
                addChunksToLoadAsync(list);
            }
        }).runTaskAsynchronously(PLUGIN);
    }

    public static void addChunksToLoadAsync(final Iterable<MovecraftChunk> list) {
        for (MovecraftChunk chunk : list) {
            if (chunks.add(chunk)) {
                if (!chunk.isLoaded()) {
                    try {
                        CompletableFuture<Chunk> cf = chunk.getWorld().getChunkAtAsync(chunk.getX(), chunk.getZ(),true);
                        cf.thenAccept(chunke -> { });
                    } catch(Exception e) {
                        CompletableFuture<Chunk> cf = chunk.getWorld().getChunkAtAsync(chunk.getX(), chunk.getZ(),true);
                        cf.thenAccept(chunke -> { });
                    }
                }
            }

        }
        (new BukkitRunnable() {
            @Override
            public void run() {
                ChunkManager.removeChunksToLoad(list);
            }
        }).runTaskLaterAsynchronously(PLUGIN, 32*10L);
    }

    public static void removeChunksToLoad(Iterable<MovecraftChunk> list) {
        for (MovecraftChunk chunk : list) {
            chunk.toBukkit().setForceLoaded(false);
            chunks.remove(chunk);
        }
    }

    public static void removeChunkToLoad(MovecraftChunk list) {
        list.toBukkit().setForceLoaded(false);
        chunks.remove(list);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
      if (true) return;
      final MovecraftChunk nchunk = new MovecraftChunk(event.getChunk());
      if (chunks.contains(nchunk)) {
        return;
      }
      new BukkitRunnable() {

            @Override
            public void run() {
                ChunkManager.removeChunkToLoad(nchunk);
            }

        }.runTaskLater(PLUGIN, 32*10L);
    }

    public static Set<MovecraftChunk> getChunks(Iterable<MovecraftLocation> hitBox, World world) {
        return getChunks(hitBox, world, 0,0,0);

    }

    public static Set<MovecraftChunk> getChunks(Iterable<MovecraftLocation> oldHitBox, World world, int dx, int dy, int dz) {
        Set<MovecraftChunk> chunks = new HashSet<>();
        for (MovecraftLocation oldLocation : oldHitBox) {
            var location = oldLocation.translate(dx, dy, dz);
            int chunkX = location.getX() >> 4;
            int chunkZ = location.getZ() >> 4;

            MovecraftChunk chunk = new MovecraftChunk(chunkX, chunkZ, world);
            chunks.add(chunk);

        }
        return chunks;
    }
    public static void checkChunks(Set<MovecraftChunk> chunks) {
        chunks.removeIf(MovecraftChunk::isLoaded);
    }

    public static Future<Boolean> syncLoadChunks(Set<MovecraftChunk> chunks) {
        if (Settings.Debug)
            PLUGIN.getLogger().info("Loading " + chunks.size() + " chunks...");
        if(Bukkit.isPrimaryThread()){
            ChunkManager.addChunksToLoadAsync(chunks);
            return CompletableFuture.completedFuture(true);
        }
        return Bukkit.getScheduler().callSyncMethod(PLUGIN, () -> {
            ChunkManager.addChunksToLoadAsync(chunks);
            return true;
        });
    }

}
