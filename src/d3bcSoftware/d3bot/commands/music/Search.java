package d3bcSoftware.d3bot.commands.music;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.GuildMusicManager;
import d3bcSoftware.d3bot.music.MusicManager;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Displays the results from a YouTube query for single track and playlist search.
 * @author Boyd Compton
 */
public class Search implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s [-p] <query> " + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Lists the search results from a YouTube query.\n\n"
            + "-p: Flags the commnad to support playlist loading and exclusives searchs for playlists";
    private final static String HANDLE = "search";
    
    private final static String EMPTY_QUERY = "The commands requires a query. Please refer to the following usage:\n%s";
    private final static String RESULT_HEADING = "Search results for " + Format.CODE + "%s" + Format.CODE 
            + ":\n" + Format.CODE_BLOCK;
    private final static String RESULT_ITEM = "%d) [%s] %s - %s\n";
    private final static String NO_RESULT = "No results found for " + Format.CODE + "%s" + Format.CODE;
    
    /*----      Command Actions       ----*/
    
    @Override
    public void loadData() {
    }
    
    @Override
    public void saveData() {
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        // Play Item
        if(args.length > 0) {
            MusicManager music = Bot.getMusicManager();
            boolean playlist = args[0].equalsIgnoreCase("-p");
            int start = playlist ? 1 : 0;
            List<SearchResult> list = null;
            String query = "";
            
            // Build the query
            for(int i = start; i < args.length; i++)
                query += args[i] + " ";
            list = playlist ? music.searchYouttubePlaylist(query.trim())
                    : music.searchYoutube(query.trim());
            
            
            e.getChannel().sendMessage(String.format(GuildMusicManager.SEARCH, query)).queue();
            if(list != null && !list.isEmpty()) {
                GuildMusicManager mng = music.getGuildMusicManager(e.getGuild());
                String[] details = playlist ? music.getPlaylistLength(list)
                        : music.getVideoDurations(list); 
                
                String msg = String.format(RESULT_HEADING, query);
                List<String> cache = new ArrayList<String>();
                
                for(int i = 0; i < list.size(); i++) {
                    SearchResult result = list.get(i);
                    SearchResultSnippet snippet = list.get(i).getSnippet();
                    
                    // Cache Search
                    if(playlist) {
                        cache.add(music.getStartVideoId(result.getId().getPlaylistId()) 
                                + " " + result.getId().getPlaylistId()); 
                    } else
                        cache.add(result.getId().getVideoId());
                    
                    // Output
                    msg += String.format(RESULT_ITEM, i+1, details[i],
                            snippet.getTitle(), snippet.getChannelTitle());
                }
                mng.searches.put(e.getMember(), cache);
                msg += Format.CODE_BLOCK;
                e.getChannel().sendMessage(msg).queue();
            } else {
                e.getChannel().sendMessage(String.format(NO_RESULT, query)).queue();
            }
            
        } else
            e.getChannel().sendMessage(String.format(EMPTY_QUERY, help())).queue();
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