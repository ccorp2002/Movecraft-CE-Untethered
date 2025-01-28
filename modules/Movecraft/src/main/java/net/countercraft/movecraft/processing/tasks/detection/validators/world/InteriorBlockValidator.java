package net.countercraft.movecraft.processing.tasks.detection.validators.world;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.processing.functions.DetectionPredicate;
import net.countercraft.movecraft.processing.functions.Result;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InteriorBlockValidator implements DetectionPredicate<MovecraftLocation> {
    @Override
    public @NotNull Result validate(@NotNull MovecraftLocation movecraftLocation, @NotNull CraftType type, @NotNull MovecraftWorld world, @Nullable Player player) {
        return type.getMaterialSetProperty(CraftType.INTERIOR_BLOCKS).contains(world.getMaterialSync(movecraftLocation)) || (world.getMaterialSync(movecraftLocation) == Material.AIR) ? Result.succeed() : Result.fail();
    }
}
