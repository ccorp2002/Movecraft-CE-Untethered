package net.countercraft.movecraft.craft;

import net.countercraft.movecraft.craft.type.CraftType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
//implements PilotedCraft
public class NPCCraftImpl extends BaseCraft {
    private final UUID id = UUID.randomUUID();
    private Player pilot = null;
    private boolean pilotLocked;
    private double pilotLockedX;
    private double pilotLockedY;
    private double pilotLockedZ;
    private Craft parent;

    public NPCCraftImpl(@NotNull CraftType type, @NotNull World world, @Nullable Player pilot) {
        super(type, world);
        //this.pilot = null;
        this.setNotificationPlayer(null);
        this.pilotLocked = false;
        this.pilotLockedX = 0.0;
        this.pilotLockedY = 0.0;
        this.pilotLockedZ = 0.0;
        this.parent = null;
    }

    public NPCCraftImpl(@NotNull CraftType type, @NotNull World world) {
        super(type, world);
        this.setNotificationPlayer(null);
        this.pilotLocked = false;
        this.pilotLockedX = 0.0;
        this.pilotLockedY = 0.0;
        this.pilotLockedZ = 0.0;
        this.parent = null;
    }

    public NPCCraftImpl(@NotNull Craft original) {
        super(original.getType(), original.getWorld());
        this.hitBox = original.getHitBox();
        this.fluidLocations = original.getFluidLocations();
        setCruiseDirection(original.getCruiseDirection());
        setLastTranslation(original.getLastTranslation());
        setAudience(original.getAudience());
        setNotificationPlayer(original.getNotificationPlayer());
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
    public long getLastCruiseUpdate() {
        return System.currentTimeMillis();
    }

    @Nullable
    public Craft getParent() {
        return parent;
    }

    public void setParent(@Nullable Craft parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NPCCraftImpl))
            return false;
        return this.id.equals(((NPCCraftImpl)obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Nullable
    public void setPilot(Player pilot) {
        this.pilot = pilot;
    }

    public @Nullable Player getPilot() {
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
