package net.countercraft.movecraft;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class SmoothTeleport {
    public abstract void teleport(Entity player, @NotNull Location location, float yawChange, float pitchChange);
    public abstract void teleport(Entity player, @NotNull Location location);
}
