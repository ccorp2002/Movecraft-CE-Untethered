package net.countercraft.movecraft.util;

import net.countercraft.movecraft.SmoothTeleport;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitTeleport extends SmoothTeleport {
    @Override
    public void teleport(Entity player, @NotNull Location location, float yawChange, float pitchChange) {
        location.setYaw(player.getLocation().getYaw() + yawChange);
        location.setPitch(player.getLocation().getPitch() + pitchChange);
        try {
            player.teleport(location,io.papermc.paper.entity.TeleportFlag.Relative.values());
        } catch (Exception exc) {
            player.teleport(location);
        }
    }
    @Override
    public void teleport(Entity player, @NotNull Location location) {
        try {
            player.teleport(location,io.papermc.paper.entity.TeleportFlag.Relative.values());
        } catch (Exception exc) {
            player.teleport(location);
        }
    }
}
