/*
 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.countercraft.movecraft.mapUpdater.update;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.util.TeleportUtils;
import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.CraftManager;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.Objects;

/**
 * Class that stores the data about a single blocks changes to the map in an unspecified world. The world is retrieved contextually from the submitting craft.
 */
public class EntityUpdateCommand extends UpdateCommand {
    private final Entity entity;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final World world;
    private final Sound sound;
    private final float volume;
    private final MovecraftRotation rotation;

    public EntityUpdateCommand(Entity entity, double x, double y, double z, float yaw, float pitch) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = entity.getWorld();
        this.sound = null;
        this.volume = 0.0f;
        this.rotation = MovecraftRotation.NONE;
    }

    public EntityUpdateCommand(Entity entity, double x, double y, double z, float yaw, float pitch, World world) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.sound = null;
        this.volume = 0.0f;
        this.rotation = MovecraftRotation.NONE;
    }

    public EntityUpdateCommand(Entity entity, double x, double y, double z, float yaw, float pitch, World world, Sound sound, float volume) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.sound = sound;
        this.volume = volume;
        this.rotation = MovecraftRotation.NONE;
    }

    public EntityUpdateCommand(Entity entity, double x, double y, double z, float yaw, float pitch, World world, MovecraftRotation rotation) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.sound = null;
        this.volume = 0.0f;
        this.rotation = rotation;
    }

    public EntityUpdateCommand(Entity entity, double x, double y, double z, float yaw, float pitch, World world, Sound sound, float volume, MovecraftRotation rotation) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = world;
        this.sound = sound;
        this.volume = volume;
        this.rotation = rotation;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public void doUpdate() {
        final Location entityLoc = entity.getLocation();
        Location destLoc = new Location(world, entityLoc.getX() + x, entityLoc.getY() + y, entityLoc.getZ() + z, yaw + entityLoc.getYaw(), pitch + entityLoc.getPitch());
        
        if (yaw == 0.0f) {
            destLoc.setYaw(entityLoc.getYaw());
        }
        if (pitch == 0.0f) {
            destLoc.setPitch(entityLoc.getPitch());
        }
        if (entity instanceof Player) {
            if (sound != null) {
                ((Player) entity).playSound(destLoc, sound, volume, 1.0f);
            }
        }
        TeleportUtils.teleport(entity, destLoc, yaw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity.getUniqueId(), x, y, z, pitch, yaw);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityUpdateCommand)){
            return false;
        }
        EntityUpdateCommand other = (EntityUpdateCommand) obj;
        return this.x == other.x &&
                this.y == other.y &&
                this.z == other.z &&
                this.pitch == other.pitch &&
                this.yaw == other.yaw &&
                this.entity.equals(other.entity);
    }
}