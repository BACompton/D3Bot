package d3bcSoftware.d3bot.commands;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * The Playlist command for d3bot
 * @author Boyd Compton
 */
public class Playlist implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s";
    private final static String HANDLE = "playlist";
    
    /*----      Command Actions       ----*/
    
    @Override
    public void loadData() {
    }
    
    @Override
    public void saveData() {
        
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        e.getTextChannel().sendMessage("PONG").queue();
    }
    
    @Override
    public String getHandle() {
        return HANDLE;
    }

    @Override
    public String help() {
        return String.format(USAGE, Bot.getPrefix(), HANDLE);
    }
    
    @Override
    public String getDataPath() {
        return String.format(DATA_PATH, HANDLE);
    }
    
}
