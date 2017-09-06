package d3bcSoftware.d3bot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.GuildMusicManager;
import d3bcSoftware.d3bot.music.MusicManager;
import d3bcSoftware.d3bot.music.TrackScheduler;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Removes a track from the queue.
 * @author Boyd Compton
 */
public class Remove implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s <index>" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Removes the selected songs from the playlist.";
    private final static String HANDLE = "remove";
    
    private final static String REMOVED = Emote.OK + " Removed " + Format.CODE + "%s" + Format.CODE 
            + " from the playlist";
    private final static String INDEX_ERROR = Emote.X + " Invalid index. Can only use indices 0-%d";
    private final static String NO_QUEUE = Emote.X + " No songs in remove.";
    
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
            int index = -1;
            
            if(args.length > 0) {
                try {
                    index = Integer.parseInt(args[0]) - 1;
                } catch(NumberFormatException ignore) {
                    e.getChannel().sendMessage(help()).queue();
                    return;
                }
            }
            
            // Catch Queue Error
            if(mng.scheduler.currTrack == null && mng.scheduler.queueLength() == 0)
                e.getChannel().sendMessage(NO_QUEUE).queue();
            // Catch Index Error
            else if(index < -1 || index >= mng.scheduler.queueLength())
                e.getChannel().sendMessage(String.format(INDEX_ERROR, mng.scheduler.queueLength())).queue();
            // Remove
            else {
                AudioTrack track = mng.scheduler.remove(index);
                e.getChannel().sendMessage(String.format(REMOVED, TrackScheduler.getTrackDisplay(track))).queue();
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

