package net.countercraft.movecraft.listener;

import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;


public final class InteractListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST) // LOWEST so that it runs before the other events
    public void onPlayerInteract(@NotNull PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
            if (e.getItem() != null && e.getItem().getType() == Settings.PilotTool) {
                // Handle pilot tool left clicks
                e.setCancelled(true);

                Player p = e.getPlayer();
                PlayerCraft craft = CraftManager.getInstance().getCraftByPlayer(p);
                if (craft == null)
                    return;
                CraftType type = craft.getType();
                int currentGear = craft.getCurrentGear();
                if (p.isSneaking() && craft.getPilotLocked()) {
                    // Handle shift right clicks (when not in direct control mode)
                    int gearShifts = type.getIntProperty(CraftType.GEAR_SHIFTS);
                    if (gearShifts <= 1) {
                        p.sendMessage(I18nSupport.getInternationalisedString("Gearshift - Disabled for craft type"));
                        return;
                    }
                    currentGear++;
                    if (currentGear > gearShifts)
                        currentGear = 1;
                    p.sendMessage(I18nSupport.getInternationalisedString("Gearshift - Gear changed")
                            + " " + currentGear + " / " + gearShifts);
                    craft.setCurrentGear(currentGear);
                    return;
                }
                if (craft.getPilotLocked()) {
                    // Allow all players to leave direct control mode
                    craft.setPilotLocked(false);
                    p.sendMessage(I18nSupport.getInternationalisedString("Direct Control - Leaving"));
                }
                else if (!p.hasPermission(
                        "movecraft." + craft.getType().getStringProperty(CraftType.NAME) + ".move")
                        || !craft.getType().getBoolProperty(CraftType.CAN_DIRECT_CONTROL)) {
                    // Deny players from entering direct control mode
                    p.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
                }
            }
            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                // Handle button left clicks
                BlockState state = e.getClickedBlock().getState();
                if (!(state instanceof Switch))
                    return;

                Switch data = (Switch) state.getBlockData();
                if (data.isPowered()) {
                    // Depower the button
                    data.setPowered(false);
                    e.getClickedBlock().setBlockData(data);
                    e.setCancelled(true);
                }
            }
        }
        else if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (e.getItem() == null || e.getItem().getType() != Settings.PilotTool)
                return;

            // Handle pilot tool right clicks
            e.setCancelled(true);

            Player p = e.getPlayer();
            PlayerCraft craft = CraftManager.getInstance().getCraftByPlayer(p);
            if (craft == null)
                return;

            CraftType type = craft.getType();
            int currentGear = craft.getCurrentGear();
            if (p.isSneaking() && !craft.getPilotLocked()) {
                // Handle shift right clicks (when not in direct control mode)
                int gearShifts = type.getIntProperty(CraftType.GEAR_SHIFTS);
                if (gearShifts <= 1) {
                    p.sendMessage(I18nSupport.getInternationalisedString("Gearshift - Disabled for craft type"));
                    return;
                }
                currentGear++;
                if (currentGear > gearShifts)
                    currentGear = 1;
                p.sendMessage(I18nSupport.getInternationalisedString("Gearshift - Gear changed")
                        + " " + currentGear + " / " + gearShifts);
                craft.setCurrentGear(currentGear);
                return;
            }
            int tickCooldown = (int) craft.getType().getPerWorldProperty(
                    CraftType.PER_WORLD_TICK_COOLDOWN, craft.getWorld());
            if (type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_DIRECT_MOVEMENT)
                    && type.getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_TICK_COOLDOWN))
                tickCooldown *= currentGear; // Account for gear shifts
            Long lastTime = craft.getLastMoveTime();
            if (lastTime != null) {
                long ticksElapsed = (System.currentTimeMillis() - lastTime) / 50;

                // if the craft should go slower underwater, make time pass more slowly there
                if (craft.getType().getBoolProperty(CraftType.HALF_SPEED_UNDERWATER)
                        && craft.getHitBox().getMinY() < craft.getWorld().getSeaLevel())
                    ticksElapsed /= 2;

                if (ticksElapsed < tickCooldown)
                    return; // Not enough time has passed, so don't do anything
            }

            if (!p.hasPermission("movecraft." + craft.getType().getStringProperty(CraftType.NAME) + ".move")) {
                p.sendMessage(I18nSupport.getInternationalisedString("Insufficient Permissions"));
                return; // Player doesn't have permission to move this craft, so don't do anything
            }

            if (!MathUtils.locationNearHitBox(craft.getHitBox(), p.getLocation(), 2))
                return; // Player is not near the craft, so don't do anything

            if (craft.getPilotLocked()) {
                // Direct control mode allows vertical movements when right-clicking
                int dy = 1; // Default to up
                if (p.isSneaking())
                    dy = -1; // Down if sneaking
                if (craft.getType().getBoolProperty(CraftType.GEAR_SHIFTS_AFFECT_DIRECT_MOVEMENT))
                    dy *= currentGear; // account for gear shifts

                craft.translate(craft.getWorld(), 0, dy, 0);
                craft.setLastCruiseUpdate(System.currentTimeMillis());
                return;
            }

            double rotation = p.getLocation().getYaw() * Math.PI / 180.0;
            float nx = -(float) Math.sin(rotation);
            float nz = (float) Math.cos(rotation);
            int dx = (Math.abs(nx) >= 0.5 ? 1 : 0) * (int) Math.signum(nx);
            int dz = (Math.abs(nz) > 0.5 ? 1 : 0) * (int) Math.signum(nz);
            dx *= currentGear;
            dz *= currentGear;
            float pitch = p.getLocation().getPitch();
            int dy = -(Math.abs(pitch) >= 25 ? 1 : 0) * (int) Math.signum(pitch);
            if (Math.abs(pitch) >= 75) {
                dx = 0;
                dz = 0;
                if ((currentGear-1) > 0) dy *= (currentGear-1);
            }

            craft.translate(craft.getWorld(), dx, dy, dz);
            craft.setLastCruiseUpdate(System.currentTimeMillis());
        }
    }
    public static int[] getCardinalDirection(Player player) {
            double rotation = (player.getEyeLocation().getYaw() - 180) % 360;
            if (rotation < 0) {
                rotation += 360.0;
            }
            int[] dxyz = new int[3];
            dxyz[0] = 0;
            dxyz[1] = 0;
            dxyz[2] = 0;
            if (0 <= rotation && rotation < 22.5) {
                //return "N";
                dxyz[2] = -1;
            } else if (22.5 <= rotation && rotation < 67.5) {
                //return "NE";
                dxyz[0] = 1;
                dxyz[2] = -1;
            } else if (67.5 <= rotation && rotation < 112.5) {
                //return "E";
                dxyz[0] = 1;
            } else if (112.5 <= rotation && rotation < 157.5) {
                //return "SE";
                dxyz[0] = 1;
                dxyz[2] = 1;
            } else if (157.5 <= rotation && rotation < 202.5) {
                //return "S";
                dxyz[2] = 1;
            } else if (202.5 <= rotation && rotation < 247.5) {
                //return "SW";
                dxyz[0] = -1;
                dxyz[2] = 1;
            } else if (247.5 <= rotation && rotation < 292.5) {
                //return "W";
                dxyz[0] = -1;
            } else if (292.5 <= rotation && rotation < 337.5) {
                //return "NW";
                dxyz[0] = -1;
                dxyz[2] = -1;
            } else if (337.5 <= rotation && rotation < 360.0) {
                //return "N";
                dxyz[2] = -1;
            } else {
                //return null;
                dxyz[0] = 0;
                dxyz[1] = 0;
                dxyz[2] = 0;
            }
            return dxyz;

        }
}
