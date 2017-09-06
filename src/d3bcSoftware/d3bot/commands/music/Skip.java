package d3bcSoftware.d3bot.commands.music;

import java.util.ArrayList;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.GuildMusicManager;
import d3bcSoftware.d3bot.music.MusicManager;
import d3bcSoftware.d3bot.music.TrackScheduler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Skips the current track but still keeps it within the queue if queue loop is active.
 * @author Boyd Compton
 */
public class Skip implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Skips the currently playing song.";
    private final static String HANDLE = "skip";
    
    private final static String SKIP = Emote.SKIP + " Moving to next track.";
    private final static String EMPTY = Emote.X + " No song to skip.";
    private final static String NO_VOICE = Emote.X + " No active music player.";
    private final static String VOTE = "Skip Vote: " + Format.CODE + "%d" + Format.CODE 
            + " / " + Format.CODE + "%d" + Format.CODE;
    
    /*----      Instance Variables       ----*/
    
    private ArrayList<Member> vote;
    private AudioTrack voteTrack;
    private int bots;
    
    /*----      Command Actions       ----*/
    
    @Override
    public void loadData() {
        vote = new ArrayList<Member>();
        voteTrack = null;
        bots = 0;
    }
    
    @Override
    public void saveData() {
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        Guild g = e.getGuild();
        VoiceChannel chan = e.getGuild().getAudioManager().getConnectedChannel();
        GuildMusicManager mng = Bot.getMusicManager().getGuildMusicManager(g);
        TrackScheduler scheduler = mng.scheduler;
        
        if(mng.scheduler.currTrack == null) {
            e.getChannel().sendMessage(EMPTY).queue();
            return;
        }
        
        if(chan == null) {
            e.getChannel().sendMessage(NO_VOICE).queue();
            return;
        }
        
        // Update Vote
        if(voteTrack == null || !voteTrack.equals(mng.scheduler.currTrack)) {
            voteTrack = mng.scheduler.currTrack;
            vote.clear();
            
            bots = 0;
            for(Member m: chan.getMembers())
                if(m.getUser().isBot())
                    bots++;
        }
        if(!vote.contains(e.getMember()))
            vote.add(e.getMember());
        
        // Output
        if(MusicManager.isDJ(e.getMember()) || vote.size() >= (chan.getMembers().size() - bots) >> 1) {
            scheduler.skip();
            e.getChannel().sendMessage(SKIP).queue();
        } else {
            e.getChannel().sendMessage(String.format(VOTE, vote.size(), (chan.getMembers().size() - bots) >> 1)).queue();
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

