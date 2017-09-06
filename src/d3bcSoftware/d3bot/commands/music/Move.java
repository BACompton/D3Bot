package d3bcSoftware.d3bot.commands.music;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.GuildMusicManager;
import d3bcSoftware.d3bot.music.MusicManager;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Moves a track to a new location within queue.
 * @author Boyd Compton
 */
public class Move implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s <index> <index>" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Moves the selected songs to a new position.";
    private final static String HANDLE = "move";
    
    private final static String MOVED = Emote.OK + " Moved " + Format.CODE + "%s" + Format.CODE 
            + " from " + Format.CODE + "%d)" + Format.CODE + " -> " + Format.CODE + "%d)" + Format.CODE + ".";
    private final static String INDEX_ERROR = Emote.X + " Invalid index. Can only use indices 1-%d";
    private final static String NO_QUEUE = Emote.X + " No songs in queue.";
    
    /*----      Command Actions       ----*/
    
    @Override
    public void loadData() {
    }
    
    @Override
    public void saveData() {
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        GuildMusicManager mng = Bot.getMusicManager().getGuildMusicManager(e.getGuild());
        
        if(MusicManager.isDJ(e.getMember())) {
            int from = 0, to = 0;
            
            // Catch Invalid Usage
            if(args.length < 2) {
                e.getChannel().sendMessage(help()).queue();
                return;
            }
            if(mng.scheduler.queueLength() <= 0) {
                e.getChannel().sendMessage(NO_QUEUE).queue();
                return;
            }
            
            try {
                from = Integer.parseInt(args[0]) - 1;
                to = Integer.parseInt(args[1]) - 1;
            } catch(NumberFormatException ignore) {
                e.getChannel().sendMessage(help()).queue();
                return;
            }
            
            // Catch Index Error
            if(from < 0 || from >= mng.scheduler.queueLength()
                    || to < 0 || to >= mng.scheduler.queueLength())
                e.getChannel().sendMessage(String.format(INDEX_ERROR, mng.scheduler.queueLength()));
            // Perform swap
            else {
                mng.scheduler.swap(from, to);
                e.getChannel().sendMessage(String.format(MOVED, 
                        mng.scheduler.getTrackDisplay(to), from+1, to+1)).queue();
            }
        } else
            e.getChannel().sendMessage(MusicManager.DJ_PERM).queue();
        
    }
    
    @Override
    public String getHandle() {
        return HANDLE;
    }

    @Override
    public String help() {
        return String.format(USAGE, Bot.getPrefix(), HANDLE, DESC);
    }
    
    @Override
    public String getDataPath() {
        return String.format(DATA_PATH, HANDLE);
    }

}

