package d3bcSoftware.d3bot.commands.music;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.MusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Dismisses the music bot from a voice channel.
 * @author Boyd Compton
 */
public class Dismiss implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s " + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Disconnects the music bot from a voice channel.";
    private final static String HANDLE = "dismiss";
    
    private final static String DISCONNECT = Emote.OK + " Disconnecting from " + Format.CODE + "%s" + Format.CODE;
    private final static String NO_ACTIVE = Emote.X + " No active voice channel"; 
    private final static String NO_USER_VOICE = Emote.X + " You are with the music bot.";
    
    /*----      Command Actions       ----*/
    
    @Override
    public void loadData() {
    }
    
    @Override
    public void saveData() {
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        Guild guild = e.getGuild();
        VoiceChannel vc = e.getGuild().getAudioManager().getConnectedChannel();
        
        if(vc != null) {
            if(!vc.getMembers().contains(e.getMember())) {
                e.getChannel().sendMessage(NO_USER_VOICE).queue();
                return;
            }
            
            if(!MusicManager.isDJ(e.getMember()))
                for(Member m: vc.getMembers())
                    if(MusicManager.isDJ(m)) {
                        e.getChannel().sendMessage(MusicManager.DJ_PERM).queue();
                        return;
                    }
            
            e.getChannel().sendMessage(String.format(DISCONNECT, vc.getName())).queue();
            guild.getAudioManager().setSendingHandler(null);
            guild.getAudioManager().closeAudioConnection();
        } else
            e.getChannel().sendMessage(NO_ACTIVE).queue();
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
