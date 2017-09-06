package d3bcSoftware.d3bot.music;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import d3bcSoftware.d3bot.Bot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Voice channel activity listener that handles the music bot auto-disconnect feature.
 * @author Boyd Compton
 *
 */
public class MusicListener extends ListenerAdapter{
    /*----      Constants       ----*/
    
    private static final int MAX_THREADS = 4;
    
    /*----      Instance Variables       ----*/
    
    private MusicManager music;
    private Map<Guild, Future<?>> autoDisconnect;
    private ExecutorService executor;
    
    /*----      Constructor       ----*/
    
    public MusicListener(MusicManager music) {
        this.music = music;
        this.autoDisconnect = new HashMap<Guild, Future<?>>();
        executor = Executors.newFixedThreadPool(MAX_THREADS);
    }
    
    /*----      Events       ----*/
    
    @Override 
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        checkJoin(event.getChannelJoined());
        checkLeave(event.getChannelLeft(), event.getMember());
    }
    
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        checkJoin(event.getChannelJoined());
        checkLeave(event.getChannelJoined(), event.getMember());
    }
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Guild g = event.getGuild();
        Member bot = g.getMember(Bot.getBot().getSelfUser());
        
        if(!event.getMember().equals(bot))
            checkLeave(event.getChannelLeft(), event.getMember());
    }
    
    /*----      Helpers       ----*/
    
    public void shutDown() {
        executor.shutdownNow();
    }
    
    private void checkJoin(VoiceChannel join) {
        Guild g = join.getGuild();
        Future<?> auto = autoDisconnect.get(g);
        
        if(auto != null) {
            auto.cancel(true);
            autoDisconnect.remove(g);
        }
            
    }
    
    public void checkLeave(VoiceChannel leave, Member member) {
        Guild g = leave.getGuild();
        Member bot = g.getMember(Bot.getBot().getSelfUser());
        boolean isBot = member.equals(bot);
        int nonBots = 0;
        
        for(Member m: leave.getMembers())
            if(!m.getUser().isBot())
                nonBots++;
        
        if((isBot || leave.getMembers().contains(g.getMember(Bot.getBot().getSelfUser())))
                && nonBots <= 0
                && autoDisconnect.get(g) == null) {
            AutoDisconnect disconnect = new AutoDisconnect(g, this);
            
            Future<?> run = executor.submit(disconnect);
            autoDisconnect.put(g, run);
        }
    }
    
    /*----      Getters & Setters       ----*/
    
    public void remove(Guild g) {
        autoDisconnect.remove(g);
    }
    
    public MusicManager getMusicManager() { return music; }
}
