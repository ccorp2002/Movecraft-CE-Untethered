package net.countercraft.movecraft.support;

import net.countercraft.movecraft.MovecraftLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AsyncChunk<T extends Chunk> {

    private static final Constructor<?> constructor;
    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        String mcver = Bukkit.getServer().getMinecraftVersion();
        String NMS = "";

        NMS = version;
        if (mcver.contains(".19.4")) {
            NMS = "v1_19_R3";
        } if (mcver.contains(".20.4") || mcver.contains("_20_R3") || version.contains("_20_R3")) {
            NMS = "v1_20";
        } if (mcver.contains(".21.2") || mcver.contains(".21.3")) {
            NMS = "v1_21_3";
        } if (mcver.contains(".21.4") || mcver.contains(".21.5")) {
            NMS = "v1_21_4";
        }
        Constructor<?> temp = null;
        try {
            Class.forName("net.countercraft.movecraft.support." + NMS + ".IAsyncChunk");
            final Class<?> clazz = Class.forName("net.countercraft.movecraft.support." + NMS + ".IAsyncChunk");
            if (AsyncChunk.class.isAssignableFrom(clazz)) {
                temp = clazz.getConstructor(Chunk.class);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException exception) {
            Bukkit.getLogger().severe(String.format("Error in registering Chunk accessor for version %s from the classpath.", version));
            exception.printStackTrace();
        }
        constructor = temp;
    }

    @NotNull
    public static AsyncChunk<?> of(@NotNull Chunk chunk){
        try {
            if (constructor == null) throw new RuntimeException();
            return (AsyncChunk<?>) constructor.newInstance(chunk);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull protected final T chunk;

    protected AsyncChunk(@NotNull Chunk chunk) {
        this.chunk = adapt(chunk);
    }

    @NotNull
    protected abstract T adapt(@NotNull Chunk chunk);

    @NotNull
    public abstract Material getType(@NotNull MovecraftLocation location);

    @NotNull
    public abstract BlockData getData(@NotNull MovecraftLocation location);

    @NotNull
    public abstract BlockState getState(@NotNull MovecraftLocation location);
}
