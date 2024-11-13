package net.countercraft.movecraft.commands;

import net.countercraft.movecraft.MovecraftRotation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class DirectControlCommand implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!command.getName().equalsIgnoreCase("dc")){
            return false;
        }
        if (!Settings.EXTRA_COMMANDS) return false;
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Must Be a Player to use Direct Control");
            return true;
        }
        Player player = (Player)commandSender;
        if (!player.hasPermission("movecraft.commands") && !player.hasPermission("movecraft.commands.dc")) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }
        final Craft craft = CraftManager.getInstance().getCraftByPlayerName(player.getName());
        if(craft==null){
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("You must be piloting a craft"));
            return true;
        }
        if (CraftManager.getInstance().getCraftByPlayerName(player.getName()).getPilotLocked()) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Direct Control is now Disabled");
            CraftManager.getInstance().getCraftByPlayerName(player.getName()).setPilotLocked(false);
            CraftManager.getInstance().getCraftByPlayerName(player.getName()).setPilotLockedX(player.getLocation().getBlockX()+0.5);
            CraftManager.getInstance().getCraftByPlayerName(player.getName()).setPilotLockedY(player.getLocation().getY());
            CraftManager.getInstance().getCraftByPlayerName(player.getName()).setPilotLockedZ(player.getLocation().getBlockZ()+0.5);
            CraftManager.getInstance().getCraftByPlayerName(player.getName()).setPilotLocked(false);
            return true;
        }
        commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Direct Control is now Enabled");
        CraftManager.getInstance().getCraftByPlayerName(player.getName()).setPilotLockedX(player.getLocation().getBlockX()+0.5);
        CraftManager.getInstance().getCraftByPlayerName(player.getName()).setPilotLockedY(player.getLocation().getY());
        CraftManager.getInstance().getCraftByPlayerName(player.getName()).setPilotLockedZ(player.getLocation().getBlockZ()+0.5);
        CraftManager.getInstance().getCraftByPlayerName(player.getName()).setPilotLocked(true);
        return true;
    }
}
