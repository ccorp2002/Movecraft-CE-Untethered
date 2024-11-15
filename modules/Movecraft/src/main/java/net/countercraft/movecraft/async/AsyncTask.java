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
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PlayerCraft;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.scheduler.BukkitRunnable;

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
        if (craft instanceof PlayerCraft && !craft.isAutomated() && (craft.getType().getDoubleProperty(CraftType.FUEL_BURN_RATE) > 1.0)) {
            return CraftManager.getInstance().forceBurnFuel(craft,0,0,false);
        }
        return true;
    }
}
