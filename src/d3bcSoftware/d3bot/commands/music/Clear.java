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
 * Clears the music bot's queue.
 * @author Boyd Compton
 */
public class Clear implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Clears the queue of the muisc player.";
    private final static String HANDLE = "clear";
    
    private final static String CLEAR = Emote.OK + " Cleared the music bot's queue.";
    
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
            scheduler.clear();
            e.getChannel().sendMessage(CLEAR).queue();
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

