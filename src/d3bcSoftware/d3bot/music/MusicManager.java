package d3bcSoftware.d3bot.music;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.PlaylistItems;
import com.google.api.services.youtube.YouTube.Playlists;
import com.google.api.services.youtube.YouTube.Search;
import com.google.api.services.youtube.YouTube.Videos;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.logging.LogState;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

/**
 * Music player manger in charge of handling all the guilds' music managers.
 * @author Boyd Compton
 *
 */
public class MusicManager {
    /*----      Constants       ----*/
    
    public final static String DJ_PERM = Emote.EXCLAMATION + " This action requires a role named "
            + Format.CODE + "DJ" + Format.CODE;
    public static final long MAX_RESULTS = 10;
    
    private static final String LOADING_MANAGER = "Loading the music manager.";
    private static final String LOADING_YT = "Loading youtube search";
    private static final String HOUR_TS = "%d:%02d:%02d", MIN_TS = "%d:%02d", SEC_TS = "%d";
    private static final String YT_FAIL = "YouTube API failed to build";
    
    private final static String JOIN_VOICE = "Joining " + Format.CODE + "%s" + Format.CODE + " %s";
    private final static String JOIN_PERM = Emote.EXCLAMATION.toString() 
            + " Missing permission to connect to " + Format.CODE + "%s"+ Format.CODE;
    
    private final static String SAVE = "Saving guilds' music bot settings.";
    private final static String LOAD = "Loading guilds' music bot settings";
    private final static String SAVE_ERROR = "Unable to save guilds' music bot settings.";
    private final static String LOAD_ERROR = "Unable to load guilds' music bot settings.";
    private final static String DATA_PATH = "data/", FILE = "music.json";
    
    private static final int SECS = 60, MINS = 60, MS_IN_SEC = 1000;
    private static final int MAX_CONTENT = 50;
    public static final int DEF_VOL = 35;
    
    private enum Setting {
        SHUFFLE("shuffle"), REPEAT("repeat"), LOOP("loop"), 
        QUEUE("queue"), CURRENT("curr"), POSITION("position"),
        ANNOUNCE("announce"), VOLUME("volume");
        
        public String key;
        
        private Setting(String key) {
            this.key = key;
        }
        
        @Override
        public String toString() { return key; }
    }
    
    /*----      Instance Variables       ----*/
    
    private AudioPlayerManager playerManager;
    private MusicListener listener;
    private HashMap<Guild, GuildMusicManager> musicManager;
    private PlaylistItems.List youtubePlaylistItem;
    private Playlists.List youtubePlaylist;
    private Videos.List youtubeVideo;
    private Search.List youtubeSearch;
    private YouTube youtube;
    
    /*----      Constructor       ----*/
    
    public MusicManager() {
        Bot.getLogger().serverLog(LogState.INFO, LOADING_MANAGER);
        
        this.playerManager = new DefaultAudioPlayerManager();
        musicManager = new HashMap<Guild, GuildMusicManager>();
        listener = new MusicListener(this);
        
        //TODO: Register Source Manager based on settings file    
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        
        String apiKey = (String)Bot.getSettings().get(Bot.Key.YOUTUBE.getKey());
        youtubeSearch = null;
        youtubePlaylist = null;
        youtubePlaylistItem = null;
        youtubeVideo = null;
        
        if(apiKey != null && !apiKey.isEmpty()) {
            Bot.getLogger().serverLog(LogState.INFO, LOADING_YT);
            try {
                youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                        new HttpRequestInitializer() {
                            @Override
                            public void initialize(HttpRequest arg0) throws IOException {
                            }
                        }).setApplicationName("D3-Bot").build();
                
                youtubeSearch = youtube.search().list("id,snippet")
                        .setKey(apiKey)
                        .setFields("items(id/videoId,id/playlistId,snippet/title,snippet/channelTitle)")
                        .setMaxResults(MAX_RESULTS);
                youtubePlaylist = youtube.playlists().list("id,contentDetails")
                        .setKey(apiKey)
                        .setFields("items(id,contentDetails/itemCount)")
                        .setMaxResults(MAX_RESULTS);
                youtubeVideo = youtube.videos().list("id,contentDetails")
                        .setKey(apiKey)
                        .setFields("items(id,contentDetails/duration)")
                        .setMaxResults(MAX_RESULTS);
                youtubePlaylistItem = youtube.playlistItems().list("contentDetails")
                        .setKey(apiKey)
                        .setFields("items(contentDetails/videoId)")
                        .setMaxResults((long)1);
                
            } catch (GeneralSecurityException | IOException e) {
                youtubeSearch = null;
                Bot.getLogger().serverLog(LogState.WARNING, YT_FAIL);
            }
        }
    }
    
    /*----      Save & Load       ----*/
    
    public void loadData() {
        Bot.getBot().addEventListener(listener);
        
        File settingsFile = new File(DATA_PATH + FILE);
        
        if(settingsFile.exists()) {
            JSONParser parser = new JSONParser();
            
            Bot.getLogger().serverLog(LogState.INFO, LOAD);
            try {
                JSONObject settings = (JSONObject)(parser.parse(new FileReader(settingsFile)));
                JDA bot = Bot.getBot();
                
                for(Object o: settings.keySet()) {
                    Guild g = bot.getGuildById((String)o);
                    JSONObject guildSetting = (JSONObject)settings.get(o);
                    
                    if(g != null) {
                        GuildMusicManager mng = getGuildMusicManager(g);
                        
                        // Load Current Song
                        if(guildSetting.containsKey(Setting.CURRENT.key)) {
                            mng.load(playerManager, null, null, (String)guildSetting.get(Setting.CURRENT.key),
                                    null, null, null, (long)guildSetting.get(Setting.POSITION.key), false, false, false);
                        }
                        
                        // Load Queue
                        if(guildSetting.containsKey(Setting.QUEUE.key)) {
                            JSONArray queue = (JSONArray) guildSetting.get(Setting.QUEUE.key);
                            
                            for(Object url: queue)
                                mng.load(playerManager, null, null, (String)url, 
                                        null, null, null, (long)0, false, false, false);
                        }
                        
                        // Load flags
                        if(guildSetting.containsKey(Setting.ANNOUNCE.key)) {
                            TextChannel text = g.getTextChannelById((String)guildSetting.get(Setting.ANNOUNCE.key));
                            if(text != null)
                                mng.scheduler.setAnnounce(true, text);
                        }
                        if(guildSetting.containsKey(Setting.SHUFFLE.key))
                            mng.scheduler.setShuffle((boolean)(guildSetting.get(Setting.SHUFFLE.key)));
                        if(guildSetting.containsKey(Setting.LOOP.key))
                            mng.scheduler.setQueueLoop((boolean)guildSetting.get(Setting.LOOP.key));
                        if(guildSetting.containsKey(Setting.REPEAT.key))
                            mng.scheduler.setTrackRepeat((boolean)guildSetting.get(Setting.REPEAT.key));
                        if(guildSetting.containsKey(Setting.VOLUME.key))
                            mng.player.setVolume(((Long)guildSetting.get(Setting.VOLUME.key)).intValue());
                    }
                    
                }
            } catch (IOException | ParseException e) {
                Bot.getLogger().serverLog(LogState.WARNING, LOAD_ERROR);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public void saveData() {
        listener.shutDown();
        JSONObject settings = new JSONObject();
        
        Bot.getLogger().serverLog(LogState.INFO, SAVE);
        for(Guild g: musicManager.keySet()) {
            HashMap<Object, Object> guildSettings = new HashMap<Object, Object>();
            GuildMusicManager mng = getGuildMusicManager(g);
            TrackScheduler scheduler = mng.scheduler;
            
            // Disconnect
            disconnect(g);
            
            // Save all Flags
            if(scheduler.isShuffle())
                guildSettings.put(Setting.SHUFFLE, true);
            if(scheduler.isQueueLoop())
                guildSettings.put(Setting.LOOP, true);
            if(scheduler.isTrackRepeat())
                guildSettings.put(Setting.REPEAT, true);
            if(scheduler.announce())
                guildSettings.put(Setting.ANNOUNCE, scheduler.getAnnounceChannel().getId());
            if(mng.player.getVolume() != DEF_VOL)
                guildSettings.put(Setting.VOLUME, mng.player.getVolume());
            
            // Save Track Info
            if(scheduler.currTrack != null) {
                guildSettings.put(Setting.CURRENT, scheduler.currTrack.getInfo().uri);
                guildSettings.put(Setting.POSITION, scheduler.currTrack.getPosition());
            }
            
            // Save Queue
            if(scheduler.getQueue().size() > 0) {
                int skip = scheduler.getIndex(scheduler.currTrack), i = 0;
                JSONArray queue = new JSONArray();
                
                for(AudioTrack track: scheduler.getQueue())
                    if(i++ != skip)
                        queue.add(track.getInfo().uri);
                guildSettings.put(Setting.QUEUE, queue);
            }
            
            if(!guildSettings.isEmpty())
                settings.put(g.getId(), guildSettings);
        }
        
        // Attempt to save
        try {
            PrintWriter settingsPW = new PrintWriter(DATA_PATH + FILE);
            settingsPW.print(settings.toJSONString());
            settingsPW.flush();
            settingsPW.close();
        } catch (FileNotFoundException e) {
            Bot.getLogger().serverLog(LogState.WARNING, SAVE_ERROR);
        }
    }
    
    /*----      YouTube Searches       ----*/
    
    /**
     * Search YouTube for a video to play
     * @param query The search terms
     * @return The search results or null if YouTube search is not supported.
     */
    public List<SearchResult> searchYoutube(String query) {
        if(youtubeSearch != null) {
            youtubeSearch.setType("video").setQ(query);
            try {
                SearchListResponse serchResponse = youtubeSearch.execute();
                return serchResponse.getItems();
            } catch (IOException ignored) {}
        }
        return null;
    }
    
    /**
     * Search YouTube for a playlist to play
     * @param query The search terms
     * @return The search results or null if YouTube search is not supported.
     */
    public List<SearchResult> searchYouttubePlaylist(String query) {
        if(youtubeSearch != null) {
            youtubeSearch.setType("playlist").setQ(query);
            try {
                SearchListResponse serchResponse = youtubeSearch.execute();
                return serchResponse.getItems();
            } catch (IOException ignored) {}
        }
        return null;
    }
    
    /**
     * Finds the video id of the first items in a playlist 
     * @param playlistID The id of the playlist to look in
     * @return The video id of the first video or null if not supported
     */
    public String getStartVideoId(String playlistID) {
        if(youtubePlaylistItem != null) {
            youtubePlaylistItem.setPlaylistId(playlistID);
            
            try {
                PlaylistItemListResponse response = youtubePlaylistItem.execute();
                
                if(!response.getItems().isEmpty())
                    return response.getItems().get(0).getContentDetails().getVideoId();
            } catch (IOException ignored) {}
        }
        return null;
    }
    
    /**
     * Returns an array of video durations.
     * @param results The results to look for.
     * @return the time durations or null if 50+ items were requested or not supported
     */
    public String[] getVideoDurations(List<SearchResult> results) {
        String ids = "";
        
        // Build ids
        for(SearchResult result: results)
            ids += result.getId().getVideoId() + ",";
        ids = ids.substring(0, ids.length() - ",".length());
        
        if(youtubeVideo != null || results.size() < MAX_CONTENT) {
            youtubeVideo.setId(ids);
            
            try {
                VideoListResponse response = youtubeVideo.execute();
                String[] durations = new String[results.size()];
                
                for(int i = 0; i < durations.length; i++) {
                    String dur = response.getItems().get(i).getContentDetails().getDuration();
                    durations[i] = convetDuration(dur);
                }
                return durations;     
                
            } catch (IOException ignored) {}
        }
        return null;
    }
    
    /**
     * Returns an array of playlist lengths.
     * @param results The results to look for.
     * @return the playlist length or null if 50+ items were requested or not supported
     */
    public String[] getPlaylistLength(List<SearchResult> results) {
        String ids = "";
        
        // Build ids
        for(SearchResult result: results)
            ids += result.getId().getPlaylistId() + ",";
        ids = ids.substring(0, ids.length() - ",".length());
        
        if(youtubePlaylist != null || results.size() < MAX_CONTENT) {
            youtubePlaylist.setId(ids);
            
            try {
                PlaylistListResponse response = youtubePlaylist.execute();
                String[] lengths = new String[results.size()];
                
                for(int i = 0; i < lengths.length; i++)
                    lengths[i] = response.getItems().get(i).getContentDetails().getItemCount() + " videos";
                return lengths;     
                
            } catch (IOException ignored) {}
        }
        return null;
    }
    
    /*----      Helpers       ----*/
    
    public static void disconnect(Guild guild) {
        guild.getAudioManager().setSendingHandler(null);
        guild.getAudioManager().closeAudioConnection();
    }
    
    public static String getTimestamp(long ms) {
        int secs = (int)(ms / MS_IN_SEC) % SECS;
        int mins = (int)(ms / (MS_IN_SEC * SECS)) % MINS;
        int hours = (int)(ms / (MS_IN_SEC * SECS * MINS));
        
        if(hours > 0)
            return String.format(HOUR_TS, hours, mins, secs);
        else
            return String.format(MIN_TS, mins, secs);
    }
    
    /**
     * Parses a string timestamp into a long.
     * @param timestamp The timestamp to parse
     * @return The timestamp as a long
     * @throws NumberFormatException If the passed string does not parse into a long.
     */
    public static long fromTimestamp(String timestamp) throws NumberFormatException {
        String[] times = timestamp.split(":");
        int[] convert = {1000, 60, 60};
        long[] time = new long[3];
        
        for(int i = times.length - 1, j = 0; i >= 0; i--, j++) {
            time[i] = Long.parseLong(times[j]);
            if(i < times.length - 1)
                time[i] += time[i+1];
            time[i] *= convert[i];
        }
        
        return time[0]; 
    }
    
    /**
     * Locates the voice channel the provided user is actively in.
     * @param guild The guild to search within
     * @param user The user to look for
     * @return The user's active voice channel or null
     */
    public static VoiceChannel findVoiceChannel(Guild guild, Member user) {
        List<VoiceChannel> chans = guild.getVoiceChannels();
        
        for(VoiceChannel vc: chans)
            if(vc.getMembers().contains(user))
                return vc;
        return null;
    }

    /**
     * Validates the given url format is correct.
     * @param url The url to test
     * @return If properly formatted, true. Otherwise false.
     */
    public static boolean validURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    /**
     * Attempts to open a audio connection to the provided voice channel
     * @param voice The voice channel to join
     * @param text The text channel that is used to record bot's responses.
     */
    public static void joinVoiceChannel(VoiceChannel voice, TextChannel text) {
        Guild guild;
        GuildMusicManager mng;
        
        if(voice == null || text == null)
            return;
        
        guild = voice.getGuild();
        mng = Bot.getMusicManager().getGuildMusicManager(guild);
        guild.getAudioManager().setSendingHandler(Bot.getMusicManager().getGuildMusicManager(guild).sendHandler);
        try {
            String settings = "";
            
            if(mng.scheduler.announce()) {
                mng.scheduler.setAnnounce(true, text);
                settings += " and bound to " + Format.CODE + text.getName() + Format.CODE + " " + Emote.A;
            }
            if(mng.scheduler.isTrackRepeat())
                settings += Emote.LOOP_ONE;
            if(mng.scheduler.isQueueLoop())
                settings += Emote.LOOP;
            if(mng.scheduler.isShuffle())
                settings += Emote.SHUFFLE;
            
            guild.getAudioManager().openAudioConnection(voice);
            text.sendMessage(String.format(JOIN_VOICE, voice.getName(), settings)).queue();
        } catch (PermissionException exception) {
            if(exception.getPermission() == Permission.VOICE_CONNECT)
                text.sendMessage(String.format(JOIN_PERM, voice.getName())).queue();
        }
    }
    
    /**
     * Checks a member for the DJ Role.
     * @param member The member to check
     * @return true if they are a DJ
     */
    public static boolean isDJ(Member member) {
        if(member.hasPermission(Permission.ADMINISTRATOR))
            return true;
        
        for(Role r: member.getRoles())
            if(r.getName().equalsIgnoreCase("DJ"))
                return true;
        return false;
    }
    
    /**
     * Converts duration times from PT#M#S into #:#.
     * @param ytDuraion The YouTube duration
     * @return The duration in the form of #:#
     */
    private String convetDuration(String ytDuraion) {
        String[] durationParts = ytDuraion.substring("PT".length(), ytDuraion.length() - "S".length()).split("[MH]");
        
        if(durationParts.length == 3)
            return String.format(HOUR_TS, (Object[])durationParts);
        else if(durationParts.length == 2)
            return String.format(MIN_TS, (Object[])durationParts);
        else
            return String.format(SEC_TS, (Object[])durationParts);
    }
    
    /*----      Getters & Setters       ----*/
    
    public AudioPlayerManager getPlayerManager() { return playerManager; }

    public GuildMusicManager getGuildMusicManager(Guild g) { 
        GuildMusicManager mng = null;
        
        synchronized (musicManager) {
            mng = musicManager.get(g);
            
            if(mng == null) {
                mng = new GuildMusicManager(playerManager);
                mng.player.setVolume(DEF_VOL);
                musicManager.put(g, mng);
            }
        }
        
        return mng;
    }
    
}
