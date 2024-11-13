package net.countercraft.movecraft;

import net.countercraft.movecraft.craft.Craft;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import java.util.function.Supplier;

public abstract class WorldHandler {
    public abstract boolean doesObjectContainField(Object object, String fieldName);
    public abstract void rotateCraft(@NotNull Craft craft, @NotNull MovecraftLocation originLocation, @NotNull MovecraftRotation rotation);
    public abstract void translateCraft(@NotNull Craft craft, @NotNull MovecraftLocation newLocation, @NotNull World world);
    public abstract void setBlockFast(@NotNull Location location, @NotNull BlockData data);
    public abstract void setBlockFast(@NotNull Location location, @NotNull MovecraftRotation rotation, @NotNull BlockData data);
    public abstract void setBlockFast(@NotNull Location location, @NotNull Material mat);
    public abstract void processLight(HitBox hitBox, @NotNull World world);
    public abstract void disableShadow(@NotNull Material type);
    public abstract @Nullable Location getAccessLocation(@NotNull InventoryView inventoryView);
    public abstract void setAccessLocation(@NotNull InventoryView inventoryView, @NotNull Location location);
    public abstract Material toBukkitBlockFast(@NotNull Location location);
    public abstract Material toBukkitBlockFast(@NotNull MovecraftLocation location, @NotNull World world);
    public abstract org.bukkit.block.Block getBukkitBlockFast(@NotNull MovecraftLocation location, @NotNull World world);
    public abstract org.bukkit.block.Block getBukkitBlockFast(@NotNull Location location);
    public abstract boolean runTaskInCraftWorld(@NotNull Runnable runMe, Craft craft);
    public abstract boolean runTaskInWorld(@NotNull Runnable runMe, @NotNull World world);
    public abstract org.bukkit.Chunk getChunkFastest(@NotNull Location loc);
    public static @NotNull String getPackageName(@NotNull String minecraftVersion) {
        return "v1_" + minecraftVersion.substring(minecraftVersion.indexOf('.') + 1, minecraftVersion.lastIndexOf('.'));
    }
}
