package d3bcSoftware.d3bot.commands.music;

import java.awt.Color;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.GuildMusicManager;
import d3bcSoftware.d3bot.music.MusicManager;
import d3bcSoftware.d3bot.music.TrackScheduler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Displays the current song that is playing.
 * @author Boyd Compton
 */
public class NowPlaying implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Displays what song is currently playing.";
    private final static String HANDLE = "np";
    
    public final static EmbedBuilder NOW_PLAYING_BUILD = new EmbedBuilder().setColor(Color.BLUE);
    public final static String NOW_PLAYING_TITLE = "Now Playing:";
    public final static String NOW_PLAYING = "\n\t" + Format.CODE + "%s" + Format.CODE + "\n" +
            "\t" + Format.ITALICS + "Time Left:" + Format.ITALICS + " "+ Format.CODE + "%s" + Format.CODE + "\n";
    public final static String NOW_PLAYING_QUEUE = "\n" + Format.BOLD_ITALICS + "Now Playing:" + Format.BOLD_ITALICS +
            "\n\t" + Format.CODE + "%s" + Format.CODE + "\n" +
            "\t" + Format.ITALICS + "Time Left:" + Format.ITALICS + " "+ Format.CODE + "%s" + Format.CODE + "\n";
    public final static String UP_NEXT = "\n" + Format.BOLD_ITALICS + "Up Next:" + Format.BOLD_ITALICS +
            "\n\t" + Format.CODE + "%s" + Format.CODE + "\n" +
            "\t" + Format.ITALICS + "Duration:" + Format.ITALICS + " "+ Format.CODE + "%s" + Format.CODE + "\n";
    private final static String NO_SONG = " The music player is currently empty.";
    
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
        
        if(mng.scheduler.currTrack != null) {
            AudioTrack track = mng.scheduler.currTrack, next = mng.scheduler.getNextSong();
            
            synchronized (NOW_PLAYING_BUILD) {
                NOW_PLAYING_BUILD.setDescription(String.format(NOW_PLAYING, 
                        TrackScheduler.getTrackDisplay(track), 
                        MusicManager.getTimestamp(mng.scheduler.getRemainingTime())))
                .setTitle(NOW_PLAYING_TITLE, track.getInfo().uri);
                
                if(next != null)
                    NOW_PLAYING_BUILD.appendDescription(String.format(UP_NEXT, 
                            TrackScheduler.getTrackDisplay(next), 
                            MusicManager.getTimestamp(next.getDuration())));
                e.getChannel().sendMessage(NOW_PLAYING_BUILD.build()).queue();
            }
        } else
            e.getChannel().sendMessage(NO_SONG).queue();
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

