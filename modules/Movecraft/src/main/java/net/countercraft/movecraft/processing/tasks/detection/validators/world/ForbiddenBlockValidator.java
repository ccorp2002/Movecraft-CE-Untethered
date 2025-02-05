package net.countercraft.movecraft.processing.tasks.detection.validators.world;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.processing.MovecraftWorld;
import net.countercraft.movecraft.processing.functions.DetectionPredicate;
import net.countercraft.movecraft.processing.functions.Result;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForbiddenBlockValidator implements DetectionPredicate<MovecraftLocation> {
    @Override
    @Contract(pure = true)
    public @NotNull Result validate(@NotNull MovecraftLocation movecraftLocation, @NotNull CraftType type, @NotNull MovecraftWorld world, @Nullable Player player) {
        return type.getMaterialSetProperty(CraftType.FORBIDDEN_BLOCKS).contains(world.getMaterialSync(movecraftLocation)) ? Result.failWithMessage(I18nSupport.getInternationalisedString("Detection - Forbidden block found")) : Result.succeed();
    }
}
