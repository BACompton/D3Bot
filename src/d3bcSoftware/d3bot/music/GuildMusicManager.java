package d3bcSoftware.d3bot.music;

import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Manager for all music related items for a single guild. This includes items such as the AudioPlayer 
 * and TrackScheduler.
 * @author Boyd Compton
 */
public class GuildMusicManager {
    /*----      Constants       ----*/
    
    public static final String SEARCH = Emote.MAG_R.toString() + Format.BOLD + " Searching "+ Format.BOLD 
            + Format.CODE + "%s" + Format.CODE;
    private static final String LOAD_TRACK = "Adding " + Format.CODE + "%s" + Format.CODE + " to queue. "
            + "\nEstimated Queue time: " + Format.CODE + "%s" + Format.CODE;
    private static final String LOAD_PLAYLIST = "Adding " + Format.CODE + "%d" + Format.CODE 
            + " tracks from playlist " + Format.CODE + "%s" + Format.CODE + " to queue."
            + "\nEstimated Queue time: " + Format.CODE + "%s" + Format.CODE;
    private static final String NO_MATCH = Emote.QUESTION_MARK + " No matches were found for " 
            + Format.CODE + "%s" + Format.CODE;
    private static final String LOAD_FAIL = Emote.EXCLAMATION + " Failed to load " + Format.CODE + "%s" + Format.CODE;
    
    /*----      Instance Variables       ----*/
    
    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    public final AudioPlayerSendHandler sendHandler;
    
    /*----      Constructors       ----*/
    
    /**
     * Creates the all audio related items necessary for discord music player.
     * @param mng Audio player manager used to create the audio player.
     */
    public GuildMusicManager(AudioPlayerManager mng) {
        player = mng.createPlayer();
        scheduler = new TrackScheduler(player);
        sendHandler = new AudioPlayerSendHandler(player);
        player.addListener(scheduler);
    }
    
    /*----      Helpers       ----*/
    
    public void loadAndPlay(TextChannel channel, String display, final String url, boolean addPlaylist, final Member member) {
        final Guild guild = channel.getGuild();
        final Member bot = guild.getMember(Bot.getBot().getSelfUser());
        AudioPlayerManager playerMng = Bot.getMusicManager().getPlayerManager();
        
        String trackurl = stripUnembeded(url);
        
        channel.sendMessage(String.format(SEARCH, display)).queue();
        load(playerMng, channel, display, trackurl, bot, member, guild, (long)0, addPlaylist, true, true);
    }
    
    /**
     * Loads a song into the queue
     * @param playerMng AudioPlayerManager to load the track with
     * @param channel The text channel to log
     * @param display The display query for logging
     * @param url The url to the track
     * @param bot The bot to check for during auto-join procedure
     * @param member The member to used to loacte a voice channel to join
     * @param guild The guild to look for a voice channel within
     * @param position The position to load the track at
     * @param addPlaylist Flags the loaded to load playlists
     * @param log Flags the method to log
     * @param join Flags the method to auto-join
     */
    public void load(AudioPlayerManager playerMng, TextChannel channel, String display, final String url, 
            Member bot, Member member, Guild guild, long position, boolean addPlaylist, boolean log, boolean join) {
        playerMng.loadItemOrdered(this, url, new AudioLoadResultHandler() {
            
            @Override
            public void trackLoaded(AudioTrack track) {
                if(join)
                    attemptJoin(guild, channel, member);
                track.setPosition(position);
                scheduler.queue(track);
                if(log)
                    channel.sendMessage(String.format(LOAD_TRACK, track.getInfo().title,
                            MusicManager.getTimestamp(scheduler.getQueueTime()))).queue();
            }
            
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                
                if(join)
                    attemptJoin(guild, channel, member);
                
                if(addPlaylist) {
                    tracks.forEach(scheduler::queue);
                    if(log)
                        channel.sendMessage(String.format(LOAD_PLAYLIST, tracks.size(), playlist.getName(),
                                MusicManager.getTimestamp(scheduler.getQueueTime()))).queue();
                } else {
                    AudioTrack track = playlist.getSelectedTrack() != null ? playlist.getSelectedTrack() : tracks.get(0);
                    
                    scheduler.queue(track);
                    if(log)
                        channel.sendMessage(String.format(LOAD_TRACK, track.getInfo().title,
                                MusicManager.getTimestamp(scheduler.getQueueTime()))).queue();
                }
            }
            
            @Override
            public void noMatches() {
                if(log)
                    channel.sendMessage(String.format(NO_MATCH, display)).queue();
            }
            
            @Override
            public void loadFailed(FriendlyException e) {
                if(log)
                    channel.sendMessage(String.format(LOAD_FAIL, display)).queue();
            }
        });
    }
    
    /**
     * Attempts to auto-join a voice channel.
     * @param guild The guild to look in
     * @param text The text channel to log to
     * @param bot The bot to look for
     * @param user The user to look for
     */
    private void attemptJoin(Guild guild, TextChannel text, Member user) {
        // Auto-Join
        if(guild.getAudioManager().getConnectedChannel() == null)
            MusicManager.joinVoiceChannel(MusicManager.findVoiceChannel(guild, user),
                    text);
    }
    
    /**
     * Removes the umembeded format from urls
     * @param url The url to embed.
     * @return an embed url (normal url)
     */
    private String stripUnembeded(String url) {
        if(url.startsWith(Format.UNEMBED_S.toString()))
            return url.substring(Format.UNEMBED_S.toString().length(), 
                    url.length() - Format.UNEMBED_E.toString().length());
        return url;
    }
    
}
