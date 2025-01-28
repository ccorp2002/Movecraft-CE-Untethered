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

package net.countercraft.movecraft.async;

import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraftImpl;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.FuelBurnEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.Tags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public abstract class AsyncTask extends BukkitRunnable {
    protected final Craft craft;
    protected AsyncTask(Craft c) {
        this.craft = c;
    }

    public void run() {
        try {
            execute();
            Movecraft.getInstance().getAsyncManager().submitCompletedTask(this);
        } catch (Exception e) {
            Movecraft.getInstance().getLogger().log(Level.SEVERE, I18nSupport.getInternationalisedString("Internal - Error - Processor thread encountered an error"));
            e.printStackTrace();
        }
    }

    public abstract org.bukkit.World getWorld();

    protected abstract void execute() throws InterruptedException, ExecutionException;

    public Craft getCraft() {
        return this.craft;
    }

    public boolean checkFuel() {
        return true;
    }
}
