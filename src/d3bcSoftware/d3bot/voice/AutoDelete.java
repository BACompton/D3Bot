package d3bcSoftware.d3bot.voice;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.logging.LogState;
import d3bcSoftware.d3bot.music.MusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * Simple Runnable to Auto-Delete private voice channels after periods of inactivity.
 * @author Boyd Compton
 */
public class AutoDelete extends Thread {
    /*----      Constants       ----*/
    
    public static final long WAIT = MusicManager.fromTimestamp("5:00");
    
    private static final String NAME = "%s Auto-Delete";
    private static final String DELETE_LOG = "Deleting " + Format.CODE + "%s" + Format.CODE 
            + " due to inactivity";
    
    /*----      Instance Variables       ----*/
    
    private VoiceChannel voice;
    private VoiceListener voiceListener;
    private Guild guild;
    
    /*----      Constructor       ----*/
    
    public AutoDelete(Guild guild, VoiceChannel vc, VoiceListener voiceListener) {
        super(String.format(NAME, vc.getName()));
        this.guild = guild;
        this.voice = vc;
        this.voiceListener = voiceListener;
    }
    
    /*----      Run       ----*/
    
    @Override
    public void run() {
        try {
            sleep(WAIT);
            
            synchronized (DELETE_LOG) {
                GuildVoiceManager mng = voiceListener.getVoiceManager().getManager(guild);
                Member m = mng.findVoiceChannel(voice);
                
                Bot.getLogger().guildLog(guild, 
                        LogState.INFO, String.format(DELETE_LOG, voice.getName()));
                
                voice.delete().queue();
                mng.removeMemberVoice(m, voice);
            }
        } catch (InterruptedException e) {}
    }

}
