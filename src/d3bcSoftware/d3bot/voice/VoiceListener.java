package d3bcSoftware.d3bot.voice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Voice channel activity listener that handles the private voice chats created by the bot.
 * @author Boyd Compton
 *
 */
public class VoiceListener extends ListenerAdapter{
    /*----      Constants       ----*/
    
    private static final int MAX_THREADS = 4;
    
    /*----      Instance Variables       ----*/
    
    private VoiceManager voice;
    private Map<VoiceChannel, Future<?>> autoDelete;
    private ExecutorService executor;
    
    /*----      Constructor       ----*/
    
    public VoiceListener(VoiceManager voice) {
        this.voice = voice;
        this.autoDelete = new HashMap<VoiceChannel, Future<?>>();
        executor = Executors.newFixedThreadPool(MAX_THREADS);
    }
    
    /*----      Events       ----*/
    
    @Override 
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        Guild g = event.getGuild();
        
        if(voice.getManager(g).findVoiceChannel(event.getChannelJoined()) != null)
            cancel(event.getChannelJoined());
        if(voice.getManager(g).findVoiceChannel(event.getChannelLeft()) != null)
            spawn(event.getChannelLeft());
    }
    
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Guild g = event.getGuild();
        
        if(voice.getManager(g).findVoiceChannel(event.getChannelJoined()) != null)
            cancel(event.getChannelJoined());
    }
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Guild g = event.getGuild();
        
        if(voice.getManager(g).findVoiceChannel(event.getChannelLeft()) != null)
            spawn(event.getChannelLeft());
    }
    
    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        Guild g = event.getGuild();
        Member m = voice.getManager(g).findVoiceChannel(event.getChannel());
        
        if(m != null)
            voice.getManager(event.getGuild()).removeMemberVoice(m, event.getChannel());
        remove(event.getChannel());
    }
    
    /*----      Helpers       ----*/
    
    public void shutDown() {
        executor.shutdown();
    }
    
    private void cancel(VoiceChannel join) {
        Future<?> auto = autoDelete.get(join);
        
        if(auto != null) {
            auto.cancel(true);
            autoDelete.remove(join);
        }
    }
    
    public void spawn(VoiceChannel leave) {
        if(!autoDelete.containsKey(leave) && leave.getMembers().size() <= 0) {
            AutoDelete delete = new AutoDelete(leave.getGuild(), leave, this);
            Future<?> run = executor.submit(delete);
            autoDelete.put(leave, run);
        }
    }
    
    /*----      Getters & Setters       ----*/
    
    public void remove(VoiceChannel vc) {
        autoDelete.remove(vc);
    }
    
    public VoiceManager getVoiceManager() { return voice; }
}
