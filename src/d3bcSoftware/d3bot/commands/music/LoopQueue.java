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
 * Toggles the queue loop feature of music bot.
 * @author Boyd Compton
 */
public class LoopQueue implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Toggles the queue loop feature of the Music Bot.";
    private final static String HANDLE = "loopQueue";
    
    private final static String ENABLE = Emote.LOOP + " Enabled Loop Queue.";
    private final static String DISABLE = Emote.OK + " Disabled Loop Queue.";
    
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
            scheduler.setQueueLoop(!scheduler.isQueueLoop());
            e.getChannel().sendMessage(scheduler.isQueueLoop() ? ENABLE : DISABLE).queue();
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
