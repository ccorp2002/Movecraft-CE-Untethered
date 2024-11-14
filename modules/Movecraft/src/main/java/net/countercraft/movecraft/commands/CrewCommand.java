package net.countercraft.movecraft.commands;

import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.localisation.I18nSupport;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class CrewCommand implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(!command.getName().equalsIgnoreCase("crew")){
            return false;
        }
        if (!Settings.ENABLE_CREW) return true;
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Must Be a Player to use the Crew Command");
            return true;
        }
        Player player = (Player)commandSender;
        if (!player.hasPermission("movecraft.commands") && !player.hasPermission("movecraft.commands.crew")) {
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }
        final Craft craft = CraftManager.getInstance().getCraftByPlayerName(player.getName());
        if(craft==null){
            player.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("You must be piloting a craft"));
            return true;
        }
        if(args.length == 1){
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Specified Player must be Online!");
                return false;
            }
            if (!target.getWorld().equals(craft.getWorld())) {
                commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Specified Player must in the world: "+craft.getWorld().getName()+"!");
                return false;
            }
            if (craft.hasPassenger(target)) {
                commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Removed "+target.getName()+" from Craft's Passenger List.");
                craft.removePassenger(target);
                return true;
            }
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + "Added "+target.getName()+" to Craft's Passenger List.");
            craft.addPassenger(target);
            return true;
        }
        return true;
    }
}
