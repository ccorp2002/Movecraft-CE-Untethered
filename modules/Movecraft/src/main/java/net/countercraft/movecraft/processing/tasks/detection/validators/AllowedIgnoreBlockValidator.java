package net.countercraft.movecraft.processing.tasks.detection.validators;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.processing.functions.IgnoreDetectionPredicate;
import net.countercraft.movecraft.processing.functions.Result;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AllowedIgnoreBlockValidator implements IgnoreDetectionPredicate<MovecraftLocation> {
    @Override
    @Contract(pure = true)
    public @NotNull Result validate(@NotNull MovecraftLocation movecraftLocation, @NotNull CraftType type, @NotNull MovecraftWorld world, @Nullable Player player, @Nullable List ignored) {
        return (type.getMaterialSetProperty(CraftType.ALLOWED_BLOCKS).contains(world.getMaterial(movecraftLocation)) && !(ignored.contains(world.getMaterial(movecraftLocation).toString().toUpperCase()) || ignored.contains(world.getData(movecraftLocation).getAsString(false).toUpperCase()))) ? Result.succeed() : Result.fail();
    }
}
