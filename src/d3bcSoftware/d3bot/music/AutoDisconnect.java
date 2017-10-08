package d3bcSoftware.d3bot.music;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.logging.LogState;
import net.dv8tion.jda.core.entities.Guild;

/**
 * Simple Runnable to Auto-Disconnect the music bot during extended periods of inactivity.
 * @author Boyd Compton
 */
public class AutoDisconnect extends Thread{
    /*----      Constants       ----*/
    
    public static final long WAIT = MusicManager.fromTimestamp("5:00");
    
    private static final String NAME = "%s Auto-Delete";
    private static final String DISCONNECT_LOG = "Disconnecting from " + Format.CODE + "%s" + Format.CODE 
            + " due to inactivity";
    
    /*----      Instance Variables       ----*/
    
    private MusicListener musicListener;
    private Guild guild;
    
    /*----      Constructor       ----*/
    
    public AutoDisconnect(Guild guild, MusicListener musicListener) {
        super(String.format(NAME, guild.getName()));
        this.guild = guild;
        this.musicListener = musicListener;
    }
    
    /*----      Run       ----*/
    
    @Override
    public void run() {
        try {
            sleep(WAIT);
            
            synchronized (musicListener) {
                GuildMusicManager mng = musicListener.getMusicManager().getGuildMusicManager(guild);
                MusicManager.disconnect(guild, mng);
                
                Bot.getLogger().guildLog(guild, 
                        LogState.INFO, 
                        String.format(DISCONNECT_LOG, guild.getAudioManager().getConnectedChannel().getName()));
                
               mng.scheduler.clear();
                musicListener.remove(guild);
            }
        } catch (InterruptedException ignore) {}
    }
}
