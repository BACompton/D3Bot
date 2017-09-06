package d3bcSoftware.d3bot.commands.music;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.GuildMusicManager;
import d3bcSoftware.d3bot.music.MusicManager;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Pauses the music bot.
 * @author Boyd Compton
 */
public class Pause implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Pauses the Music Bot.";
    private final static String HANDLE = "pause";
    
    private final static String PAUSE = Emote.PAUSE + " Pausing Player.";
    private final static String ALREADY = Emote.X + " Player already paused.";
    
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
            e.getChannel().sendMessage(mng.player.isPaused() ? ALREADY : PAUSE).queue();
            mng.player.setPaused(true);
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

