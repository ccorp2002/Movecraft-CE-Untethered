package net.countercraft.movecraft.sign;

import net.countercraft.movecraft.util.radar.RadarMap;
import net.countercraft.movecraft.util.radar.RadarUtils;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.SignTranslateEvent;
import net.countercraft.movecraft.CruiseDirection;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

import com.google.common.collect.Lists;

public class ContactsSign implements Listener{

    @EventHandler
    public void onCraftDetect(CraftDetectEvent event){
        World world = event.getCraft().getWorld();
        for(MovecraftLocation location: event.getCraft().getHitBox()){
            var block = location.toBukkit(world).getBlock();
            if(!Tag.SIGNS.isTagged(block.getType())){
                continue;
            }
            BlockState state = block.getState();
            if(state instanceof Sign){
                Sign sign = (Sign) state;
                if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("Contacts:") || ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Radar]")) {
                    sign.setLine(1, "");
                    sign.setLine(2, "");
                    sign.setLine(3, "");
                    sign.update();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignClick(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        BlockState state = event.getClickedBlock().getState();
        if (!(state instanceof Sign))
            return;

        Sign sign = (Sign) state;
        String line = ChatColor.stripColor(sign.getLine(0));
        if (line.equalsIgnoreCase("Contacts:") || line.equalsIgnoreCase("[Radar]")) {
            Craft c = CraftManager.getInstance().getCraftFromBlock(event.getClickedBlock());
            if (c == null) return;
            ArrayList<String> textMap = Lists.newArrayList(RadarMap.getContactsMapForCraft(c,1));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',RadarMap.RADAR_HEADER));
	        for(int i = 0; i < textMap.size(); i++){
                String mapLine = textMap.get(i);
                mapLine = mapLine.replace("+","&r&7+&r");
                mapLine = mapLine.replace("x","&r&cx&r");
                mapLine = mapLine.replace("^","&r&b^&r");
                mapLine = ChatColor.translateAlternateColorCodes('&',mapLine);
                player.sendMessage(mapLine);
            }
            
        }
    }

    @EventHandler
    public final void onSignTranslateEvent(SignTranslateEvent event){
        String[] lines = event.getLines();
        Craft craft = event.getCraft();
        if (!ChatColor.stripColor(lines[0]).equalsIgnoreCase("Contacts:") || ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Radar]")) {
            return;
        }
        int signLine=1;
        for(Craft tcraft : RadarMap.getRadarContacts(craft)) {
            MovecraftLocation center = craft.getHitBox().getMidPoint();
            MovecraftLocation tcenter = tcraft.getHitBox().getMidPoint();
            int distsquared = center.distanceSquared(tcenter);
            // craft has been detected
            String notification = ChatColor.BLUE + tcraft.getType().getStringProperty(CraftType.NAME);
            if(notification.length()>9) {
                notification = notification.substring(0, 7);
            }
            notification += " " + (int)Math.sqrt(distsquared);
            int diffx=center.getX() - tcenter.getX();
            int diffz=center.getZ() - tcenter.getZ();
            if(Math.abs(diffx) > Math.abs(diffz)) {
                if(diffx<0) {
                    notification+=" E";
                } else {
                    notification+=" W";
                }
            } else {
                if(diffz<0) {
                    notification+=" S";
                } else {
                    notification+=" N";
                }
            }
            lines[signLine++] = notification;
            if (signLine >= 4) {
                break;
            }

        }
        if(signLine<4) {
            for(int i=signLine; i<4; i++) {
                lines[signLine]="";
            }
        }
    }


}
