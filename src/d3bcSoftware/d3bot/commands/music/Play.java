package d3bcSoftware.d3bot.commands.music;

import java.util.List;

import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.GuildMusicManager;
import d3bcSoftware.d3bot.music.MusicManager;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Encompasses the queuing feature (song and playlist) and the resuming feature for the music bot.
 * @author Boyd Compton
 */
public class Play implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s [-p] [link|[index] query] " + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Queues up a single track into the queue.\n"
            + "If no link or query is given, an attempt to resume the player.\n\n"
            + "-p: Flags the commnad to support playlist loading and exclusives searchs for playlists";
    private final static String HANDLE = "play";
    
    private final static String RESUME = Emote.PLAY + " Resuming the music player";
    private final static String PLAYING = "Player is already playing";
    public final static String SELECT_SEARCH = Emote.OK + " Selecting the result %d from last search";
    private final static String EMPTY_QUEUE = "The queue is currently empty.";
    private final static String INDEX_ERROR = Emote.X + " Only indices from 1-%d are supported.";
    private final static String EMPTY_QUERY = Emote.X + " Either a link or query is required to queue a song.";
    private static final String YT_LINK = "https://www.youtube.com/watch?v=%s";
    private static final String PLAYLIST_LINK = "https://www.youtube.com/watch?v=%s&list=%s";
    
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
        
        // Play Item
        if(args.length > 0) {
            boolean playlist = args[0].equalsIgnoreCase("-p"),
                    outSearch = true;
            int start = playlist ? 1 : 0;
            String query = stripUnembed(args[start]);
            String url = stripUnembed(args[start]);
            
            // Search YouTube
            if(!MusicManager.validURL(url)) {
                List<SearchResult> list = null;
                int index = 0, i = start;
                
                // Find Index
                try {
                    index = Integer.parseInt(args[start]) - 1;
                    
                    if(index >= MusicManager.MAX_RESULTS || index < 0) {
                        e.getChannel().sendMessage(String.format(INDEX_ERROR, MusicManager.MAX_RESULTS)).queue();
                        return;
                    }
                    
                    i++;
                } catch(NumberFormatException ignored) {}
                
                // Build the query
                query = "";
                for(; i < args.length; i++)
                    query += args[i] + " ";
                if(!query.isEmpty()) {
                    if(!playlist)
                        list = Bot.getMusicManager().searchYoutube(query.trim());
                    else
                        list = Bot.getMusicManager().searchYouttubePlaylist(query.trim());
                } else {
                    List<String> search = mng.searches.get(e.getMember());
                    
                    if(search != null && index < search.size()) { // Quick select from last search
                        String[] ids = search.get(index).split(" ");
                        outSearch = false;
                        
                        query = String.format(SELECT_SEARCH, index+1);
                        if(playlist)
                            url = String.format(PLAYLIST_LINK, ids[0], ids[1]);
                        else
                            url = String.format(YT_LINK, ids[0]);
                    } else
                        e.getChannel().sendMessage(EMPTY_QUERY).queue();
                }
                
                if(list != null && !list.isEmpty()) {
                    ResourceId rID = list.get(index).getId();
                    if(playlist)
                        url = String.format(PLAYLIST_LINK, 
                                Bot.getMusicManager().getStartVideoId(rID.getPlaylistId()), 
                                rID.getPlaylistId());
                    else
                        url = String.format(YT_LINK, rID.getVideoId());
                }
            }
            
            mng.searches.remove(e.getMember());
            mng.loadAndPlay(e.getTextChannel(), query, url, playlist, e.getMember(), outSearch);
        // Check for DJ
        } else if(MusicManager.isDJ(e.getMember())) {
            if(mng.player.getPlayingTrack() == null) 
                e.getChannel().sendMessage(PLAYING).queue();
            else if(mng.scheduler.queueLength() <= 0)
                e.getChannel().sendMessage(EMPTY_QUEUE).queue();
            else {
                mng.player.setPaused(false);
                e.getChannel().sendMessage(RESUME).queue();
            }
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
    
    /*----      Helpers       ----*/
    
    /**
     * Strips the < & > from un-embeded links.
     * @param url URL to strip
     * @return The URL without any formatting.
     */
    private String stripUnembed(String url) {
        if(url.startsWith(Format.UNEMBED_S.toString()))
            return url.substring(Format.UNEMBED_S.toString().length(), 
                    url.length() - Format.UNEMBED_E.toString().length());
        return url;
    }
    
}
