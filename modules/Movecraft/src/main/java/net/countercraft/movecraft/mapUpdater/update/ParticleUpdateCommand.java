package net.countercraft.movecraft.mapUpdater.update;

import net.countercraft.movecraft.config.Settings;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Random;

public class ParticleUpdateCommand extends UpdateCommand {
    private Location location;
    private int smokeStrength;
    private Random rand = new Random();
    private static int silhouetteBlocksSent; //TODO: remove this

    public ParticleUpdateCommand(Location location, int smokeStrength) {
        this.location = location;
        this.smokeStrength = smokeStrength;
    }

    @Override
    public void doUpdate() {
        // put in smoke or effects
        if (smokeStrength == 1) {
            location.getWorld().playEffect(location, Effect.SMOKE, 4);
        }
        if (Settings.SilhouetteViewDistance > 0 && silhouetteBlocksSent < Settings.SilhouetteBlockCount) {
            if (sendSilhouetteToPlayers())
                silhouetteBlocksSent++;
        }

    }

    private boolean sendSilhouetteToPlayers() {
        if (rand.nextInt(100) < 15) {
            return true;
        }
        return false;
    }
}
