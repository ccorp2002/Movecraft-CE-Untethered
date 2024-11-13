package net.countercraft.movecraft.commands;

import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.BaseCraft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.craft.type.CraftType;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.util.TopicPaginator;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static net.countercraft.movecraft.util.ChatUtils.MOVECRAFT_COMMAND_PREFIX;

public class CraftReportCommand implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender.getName().equalsIgnoreCase("craftreport"))
            return false;
        if (!commandSender.hasPermission("movecraft.commands.craftreport")) {
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Insufficient Permissions"));
            return true;
        }
        int page;
        try {
            if (args.length == 0)
                page = 1;
            else
                page = Integer.parseInt(args[0]);
        }catch(NumberFormatException e){
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Paginator - Invalid Page") + "\"" + args[0] + "\"");
            return true;
        }
        if (CraftManager.getInstance().isEmpty()){
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Craft Report - None Found"));
            return true;
        }
        TopicPaginator paginator = new TopicPaginator(I18nSupport.getInternationalisedString("Craft Report"));
        for (Craft craft : CraftManager.getInstance()) {
            HitBox hitBox = craft.getHitBox();
            if (craft instanceof BaseCraft) {
                int origin_overall_size = (Integer)((BaseCraft)craft).getDataTag("origin_size");
                int current_overall_size = (Integer)((BaseCraft)craft).getDataTag("current_size");
                paginator.addLine((craft.getSinking() ? ChatColor.RED : craft.getDisabled() ? ChatColor.BLUE : "") +
                        craft.getType().getStringProperty(CraftType.NAME) + " " +
                        ChatColor.RESET +
                        (((BaseCraft)craft).getNotificationPlayer() == null ? "None" : ((BaseCraft)craft).getNotificationPlayer().getDisplayName())+" "+
                        current_overall_size + " / " + origin_overall_size + " @ " +
                        hitBox.getMinX() + "," +
                        hitBox.getMinY() + "," +
                        hitBox.getMinZ() + " - " +
                        String.format("%.2f", 1000 * craft.getMeanCruiseTime()) + "ms");
                
            } else {
                paginator.addLine((craft.getSinking() ? ChatColor.RED : craft.getDisabled() ? ChatColor.BLUE : "") +
                        craft.getType().getStringProperty(CraftType.NAME) + " " +
                        ChatColor.RESET +
                        ((craft).getNotificationPlayer() == null ? "None" : (craft).getNotificationPlayer().getDisplayName())+" "+
                        craft.getOrigBlockCount() + " @ " +
                        hitBox.getMinX() + "," +
                        hitBox.getMinY() + "," +
                        hitBox.getMinZ() + " - " +
                        String.format("%.2f", 1000 * craft.getMeanCruiseTime()) + "ms");

            }
        }
        if(!paginator.isInBounds(page)){
            commandSender.sendMessage(MOVECRAFT_COMMAND_PREFIX + I18nSupport.getInternationalisedString("Paginator - Invalid page") + "\"" + page + "\"");
            return true;
        }
        for(String line : paginator.getPage(page))
            commandSender.sendMessage(line);
        return true;

    }
}
