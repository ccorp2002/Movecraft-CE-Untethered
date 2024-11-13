package net.countercraft.movecraft.events;

import net.countercraft.movecraft.craft.Craft;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FuelBurnEvent extends CraftEvent{
    private static final HandlerList HANDLERS = new HandlerList();
    private ItemStack burningFuel = new ItemStack(Material.COAL);
    private ItemStack wasteItem = new ItemStack(Material.AIR);
    private double fuelBurnChance = 25.0d;
    public FuelBurnEvent(@NotNull Craft craft, ItemStack burningFuel, double fuelBurnChance) {
        super(craft);
        this.burningFuel = burningFuel;
        this.fuelBurnChance = fuelBurnChance;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ItemStack getBurningFuel() {
        return burningFuel;
    }

    public void setBurningFuel(ItemStack burningFuel) {
        this.burningFuel = burningFuel;
    }

    public ItemStack getWasteItem() {
        return wasteItem;
    }

    public void setWasteItem(ItemStack wasteItem) {
        this.wasteItem = wasteItem;
    }

    public double getFuelBurnChance() {
        return fuelBurnChance;
    }

    public void setFuelBurnChance(double fuelBurnRate) {
        this.fuelBurnChance = fuelBurnChance;
    }
}
