package net.countercraft.movecraft.processing.tasks.translation.effects;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.SinkingCraft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.util.hitboxes.BitmapHitBox;
import net.countercraft.movecraft.mapUpdater.update.EntityUpdateCommand;
import net.countercraft.movecraft.processing.effects.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import net.countercraft.movecraft.util.MathUtils;

public class TeleportationEffect implements Effect {
    private final @NotNull Craft craft;
    private final @NotNull MovecraftLocation translation;
    private final @NotNull World world;

    public TeleportationEffect(@NotNull Craft craft, @NotNull MovecraftLocation translation, @NotNull World world){
        this.craft = craft;
        this.translation = translation;
        this.world = world;
    }

    @Override
    public void run() {
        if (!craft.getType().getBoolProperty(CraftType.MOVE_ENTITIES) || craft.getSinking() && craft.getType().getBoolProperty(CraftType.ONLY_MOVE_PLAYERS)) {
            return;
        }
        Location midpoint = craft.getHitBox().getMidPoint().toBukkit(craft.getWorld());
        for (Entity entity : craft.getWorld().getNearbyEntities(midpoint, craft.getHitBox().getXLength() / 2.0 + 1, craft.getHitBox().getYLength() / 2.0 + 1.25, craft.getHitBox().getZLength() / 2.0 + 1)) {
            if (!MathUtils.locationNearHitBox(craft.getHitBox().boundingHitBox(),entity.getLocation(),2)) {
              if (!MathUtils.locationNearHitBox((craft).getHitBox(),entity.getLocation(),2)) {
                continue;
              }
            }
            if (((BaseCraft)craft).getPassengers().contains(entity)) {
                EntityUpdateCommand eUp = new EntityUpdateCommand(entity, translation.getX(), translation.getY(), translation.getZ(), 0, 0, world);
                eUp.doUpdate();
            } else {
                continue;
            }
            if (entity.getType() == EntityType.PLAYER) {
                if(craft.getSinking()){
                    continue;
                }
                EntityUpdateCommand eUp = new EntityUpdateCommand(entity, translation.getX(), translation.getY(), translation.getZ(), 0, 0, world);
                eUp.doUpdate();
            } else if (!craft.getType().getBoolProperty(CraftType.ONLY_MOVE_PLAYERS) || entity.getType() == EntityType.TNT) {
                EntityUpdateCommand eUp = new EntityUpdateCommand(entity, translation.getX(), translation.getY(), translation.getZ(), 0, 0, world);
                eUp.doUpdate();
            }
        }
    }
}
