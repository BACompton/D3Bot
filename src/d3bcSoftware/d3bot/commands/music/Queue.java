package d3bcSoftware.d3bot.commands.music;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.GuildMusicManager;
import d3bcSoftware.d3bot.music.MusicManager;
import d3bcSoftware.d3bot.music.TrackScheduler;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Displays the current queue for the music bot.
 * @author Boyd Compton
 */
public class Queue implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s [page]" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Prints out the music player's active queue.";
    private final static String HANDLE = "queue";
    private final static int MAX_ENTRY = 10;
    
    private final static String QUEUE_INFO = Format.BOLD + "Queue %d/%d" + Format.BOLD + "\n%s%s";
    private final static String TRACK_ENTRY = "%d) %s\n";
    private final static String PAGE_ERROR = Emote.X + " Invalid index. Can only use page numbers 1-%d";
    private final static String NO_QUEUE = " Music player's Queue is empty.";
    
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
        int page = 0, pages = (mng.scheduler.queueLength() - 1) / MAX_ENTRY,
                base = 0, end = MAX_ENTRY;
        
        if(args.length > 0) {
            try {
                page = Integer.parseInt(args[0]) - 1;
            } catch(NumberFormatException ignore) {
                e.getChannel().sendMessage(help()).queue();
                return;
            }
        }
        base = page * MAX_ENTRY;
        end = base + MAX_ENTRY;
        if(mng.scheduler.queueLength() < end)
            end = mng.scheduler.queueLength();
        
        
        // Catch Page Error
        if(page < 0 || page > pages)
            e.getChannel().sendMessage(String.format(PAGE_ERROR, pages+1)).queue();
        
        // Build Queue Output
        else {
            String nowPlaying = "",
                    pageOut = Format.CODE_BLOCK.toString();
            
            // Build nowPlaying
            if(mng.scheduler.currTrack != null)
                nowPlaying = String.format(NowPlaying.NOW_PLAYING_QUEUE, 
                        TrackScheduler.getTrackDisplay(mng.scheduler.currTrack),
                        MusicManager.getTimestamp(mng.scheduler.getRemainingTime()));
            
            // Build queue page
            if(mng.scheduler.queueLength() == 0 && !mng.scheduler.isShuffle())
                pageOut += NO_QUEUE;
            else
                for(int i = base; i < end; i++)
                    pageOut += String.format(TRACK_ENTRY, i+1, mng.scheduler.getTrackDetailedDisplay(i));
            if(mng.scheduler.isShuffle() && page == pages)
                pageOut += "\n(Playlist Reshuffle)";
            pageOut += Format.CODE_BLOCK;
            
            e.getChannel().sendMessage(String.format(QUEUE_INFO, 
                    page+1, pages+1, nowPlaying, pageOut)).queue();
        }
        
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

