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
 * Seeks out a time on the current playing track.
 * @author Boyd Compton
 */
public class Seek implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s [timestamp]" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Seeks a time on the current playing track. Ex: %s%s 1:52";
    private final static String HANDLE = "seek";
    
    private final static String SEEK = Emote.OK + " Setting the position to " 
            + Format.CODE + "%s" + Format.CODE+ ".";
    private final static String INVALID = Emote.QUESTION_MARK + " Unknown timestamp. Current tack only supports "
            + Format.CODE + "0:00" + Format.CODE + "-" + Format.CODE + "%s" + Format.CODE;
    private final static String ERROR = Emote.EXCLAMATION + " " + Format.CODE + "%s" + Format.CODE
            + " is not a valid timestamp. See the following usage.\n%s";
    private final static String NO_SONG = Emote.EXCLAMATION + " No playing song";
    
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
        TrackScheduler scheduler = mng.scheduler;
        
        if(MusicManager.isDJ(e.getMember())) {
            if(mng.scheduler.currTrack == null) {
                e.getChannel().sendMessage(NO_SONG).queue();
                return;
            } else if(args.length <= 0) {
                e.getChannel().sendMessage(help()).queue();
                return;
            }
            
            try {
                long time = MusicManager.fromTimestamp(args[0]);
                
                // Invalid Timestamp
                if(time >= scheduler.currTrack.getDuration())
                    e.getChannel().sendMessage(String.format(INVALID, 
                            MusicManager.getTimestamp(scheduler.currTrack.getDuration()))).queue();
                // Set Position
                else {
                    scheduler.currTrack.setPosition(time);
                    e.getChannel().sendMessage(String.format(SEEK, args[0])).queue();
                }
            } catch( NumberFormatException ignored) {
                e.getChannel().sendMessage(String.format(ERROR, args[0], help())).queue();
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
        return String.format(USAGE, 
                Bot.getPrefix(), 
                HANDLE, 
                String.format(DESC, Bot.getPrefix(), HANDLE));
    }
    
    @Override
    public String getDataPath() {
        return String.format(DATA_PATH, HANDLE);
    }

}

