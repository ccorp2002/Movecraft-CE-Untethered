package net.countercraft.movecraft.craft;

import net.countercraft.movecraft.craft.type.CraftType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerCraftImpl extends BaseCraft implements PlayerCraft {
    private final UUID id = UUID.randomUUID();
    private Player pilot;
    private boolean pilotLocked;
    private double pilotLockedX;
    private double pilotLockedY;
    private double pilotLockedZ;

    public PlayerCraftImpl(@NotNull CraftType type, @NotNull World world, @NotNull Player pilot) {
        super(type, world);
        this.pilot = pilot;
        setNotificationPlayer(pilot);
        this.pilotLocked = false;
        this.pilotLockedX = 0.0;
        this.pilotLockedY = 0.0;
        this.pilotLockedZ = 0.0;
        this.addPassenger(pilot);
    }
    public PlayerCraftImpl(@NotNull Craft original, @NotNull Player pilot) {
        super(original.getType(), original.getWorld());
        hitBox = original.getHitBox();
        fluidLocations = original.getFluidLocations();
        setCruiseDirection(original.getCruiseDirection());
        setLastTranslation(original.getLastTranslation());
        setAudience(original.getAudience());
        setNotificationPlayer(pilot);
        setPilot(pilot);
        setWorld(original.getWorld());
        this.pilotLocked = false;
        this.pilotLockedX = 0.0;
        this.pilotLockedY = 0.0;
        this.pilotLockedZ = 0.0;
        if (original instanceof BaseCraft) {
            this.setPassengers(((BaseCraft)original).getPassengers());
            this.trackedLocations.putAll(((BaseCraft)original).trackedLocations);
            this.craftTags.putAll(((BaseCraft)original).craftTags);
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlayerCraftImpl))
            return false;

        return super.equals(obj);
    }

    @NotNull
    public void setPilot(Player pilot) {
        this.pilot = pilot;
    }
    @Override
    public @NotNull Player getPilot() {
        return pilot;
    }

    public boolean getPilotLocked() {
        return pilotLocked;
    }

    public void setPilotLocked(boolean pilotLocked) {
        this.pilotLocked = pilotLocked;
    }

    public double getPilotLockedX() {
        return pilotLockedX;
    }

    public void setPilotLockedX(double pilotLockedX) {
        this.pilotLockedX = pilotLockedX;
    }

    public double getPilotLockedY() {
        return pilotLockedY;
    }

    public void setPilotLockedY(double pilotLockedY) {
        this.pilotLockedY = pilotLockedY;
    }

    public double getPilotLockedZ() {
        return pilotLockedZ;
    }

    public void setPilotLockedZ(double pilotLockedZ) {
        this.pilotLockedZ = pilotLockedZ;
    }
}
