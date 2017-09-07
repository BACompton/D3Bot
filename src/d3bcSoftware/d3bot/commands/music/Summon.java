package d3bcSoftware.d3bot.commands.music;

import java.util.List;

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
 * Summons the music bot to the player or specified voice channel.
 * @author Boyd Compton
 */
public class Summon implements Command{
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s [name]" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Summons D3-Bot to the proived Voice channel name or your current Voice Channel"
            + "if no name was provided.";
    private final static String HANDLE = "summon";
    
    private final static String NO_VOICE = Emote.X + " Command requires you to be in a voice channel.";
    private final static String NO_RESULT = Emote.QUESTION_MARK + " Could not find the provided voice channel.";
    private final static String IN_VOICE = Emote.X + " Already in " + Format.CODE + "%s" + Format.CODE + ".";
    
    /*----      Command Actions       ----*/
    
    @Override
    public void saveData() {
    }

    @Override
    public void loadData() {
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        Guild guild = e.getGuild();
        Member user = e.getMember(), botMember = e.getGuild().getMember(Bot.getBot().getSelfUser());
        VoiceChannel chan = null, 
                bot = guild.getAudioManager().getConnectedChannel();
        
        // Locates the Voice channel by Name
        if(args.length > 0) {
            List<VoiceChannel> chans = guild.getVoiceChannelsByName(args[0], true);
            if(!chans.isEmpty())
                chan = chans.get(0);
        // Locates the users active channel.
        } else
            chan = MusicManager.findVoiceChannel(guild, user);
        
        // Ensure the bot in the channel
        if(bot != null) {
            VoiceChannel check = null;
            for (Member m: bot.getMembers())
                if(m.equals(botMember))
                    check = bot;
            bot = check;
        }
        
        // User was not in a VoiceChannel
        if(chan == null) {
            if(args.length > 0)
                e.getChannel().sendMessage(NO_RESULT).queue();
            else
                e.getChannel().sendMessage(NO_VOICE).queue();
        } else if(bot != null && !MusicManager.isDJ(e.getMember())) 
            e.getChannel().sendMessage(MusicManager.DJ_PERM).queue();
        else if(chan.equals(bot))
            e.getChannel().sendMessage(String.format(IN_VOICE, chan.getName())).queue();
        else {
            Bot.getMusicManager().getGuildMusicManager(guild).player.setPaused(false);
            MusicManager.joinVoiceChannel(chan, e.getTextChannel());
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
