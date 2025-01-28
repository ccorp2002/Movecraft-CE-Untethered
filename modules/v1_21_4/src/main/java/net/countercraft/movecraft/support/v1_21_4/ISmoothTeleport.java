package net.countercraft.movecraft.support.v1_21_4;


import io.papermc.paper.entity.TeleportFlag;
import net.countercraft.movecraft.SmoothTeleport;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Set;

public class ISmoothTeleport extends SmoothTeleport {

    public void teleport(@NotNull Player player, @NotNull Location location) {
        player.teleport(
                location,
                TeleportFlag.Relative.X,
                TeleportFlag.Relative.Y,
                TeleportFlag.Relative.Z,
                TeleportFlag.Relative.PITCH,
                TeleportFlag.Relative.YAW,
                TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY,
                TeleportFlag.EntityState.RETAIN_VEHICLE,
                TeleportFlag.EntityState.RETAIN_PASSENGERS);
    }
    public void teleport(@NotNull Player player, @NotNull Location location, float yawChange, float pitchChange) {
        player.teleport(
                location,
                TeleportFlag.Relative.X,
                TeleportFlag.Relative.Y,
                TeleportFlag.Relative.Z,
                TeleportFlag.Relative.PITCH,
                TeleportFlag.Relative.YAW,
                TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY,
                TeleportFlag.EntityState.RETAIN_VEHICLE,
                TeleportFlag.EntityState.RETAIN_PASSENGERS);
    }

    public void teleport(@NotNull Entity entity, @NotNull Location location) {
        entity.teleport(
                location,
                TeleportFlag.Relative.X,
                TeleportFlag.Relative.Y,
                TeleportFlag.Relative.Z,
                TeleportFlag.Relative.PITCH,
                TeleportFlag.Relative.YAW,
                TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY,
                TeleportFlag.EntityState.RETAIN_VEHICLE,
                TeleportFlag.EntityState.RETAIN_PASSENGERS);
    }
    public void teleport(@NotNull Entity entity, @NotNull Location location, float yawChange, float pitchChange) {
        entity.teleport(
                location,
                TeleportFlag.Relative.X,
                TeleportFlag.Relative.Y,
                TeleportFlag.Relative.Z,
                TeleportFlag.Relative.PITCH,
                TeleportFlag.Relative.YAW,
                TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY,
                TeleportFlag.EntityState.RETAIN_VEHICLE,
                TeleportFlag.EntityState.RETAIN_PASSENGERS);
    }
}
