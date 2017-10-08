package d3bcSoftware.d3bot.voice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.logging.LogState;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class VoiceManager {
    /*----      Constants       ----*/
    
    public final static Permission PERM = Permission.MANAGE_CHANNEL;
    public final static String PERM_MSG = Emote.X + " This action requires the "
            + Format.CODE + PERM.getName() + Format.CODE + ".";
    
    private final static String SAVE = "Saving guilds' private voice settings.";
    private final static String LOAD = "Loading guilds' private voice settings";
    private final static String SAVE_ERROR = "Unable to save guilds' private voice settings.";
    private final static String LOAD_ERROR = "Unable to load guilds' private voice settings.";
    private final static String DATA_PATH = "data/", FILE = "voice.json";
    
    public enum Setting {
        CATEGORY(null, "category"), LIMIT(1, "limit"), USER_LIMIT(8, "user"), VOICE("%s's Voice", "voice");
        
        public String key;
        public Object def;
        
        private Setting(Object def, String key) {
            this.key = key;
            this.def = def;
        }
    }
    
    /*----      Instance Variables       ----*/
    
    private HashMap<Guild, GuildVoiceManager> voices;
    private VoiceListener listener;
    
    /*----      Constructor       ----*/
    
    public VoiceManager() {
        voices = new HashMap<Guild, GuildVoiceManager>();
        listener = new VoiceListener(this);
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
                        GuildVoiceManager mng = getManager(g);
                        
                        // Load private voice limits.
                        if(guildSetting.containsKey(Setting.LIMIT.key))
                            mng.setLimit(((Long)guildSetting.get(Setting.LIMIT.key)).intValue());
                        if(guildSetting.containsKey(Setting.USER_LIMIT.key))
                            mng.setUserLimit(((Long)guildSetting.get(Setting.USER_LIMIT.key)).intValue());
                        if(guildSetting.containsKey(Setting.CATEGORY.key))
                            mng.setCategory(g.getCategoryById((String)guildSetting.get(Setting.CATEGORY.key)));
                        
                        // Load private voice channels
                        if(guildSetting.containsKey(Setting.VOICE.key)) {
                            JSONObject members = (JSONObject) guildSetting.get(Setting.VOICE.key);
                            
                            for(Object m: members.keySet()) {
                                JSONArray privateVoices = (JSONArray) members.get(m);
                                Member member = g.getMember(bot.getUserById((String)m));
                                
                                if(member != null)
                                    for(Object chanID: privateVoices) {
                                        VoiceChannel vc = g.getVoiceChannelById((String)chanID);   
                                        
                                        if(vc != null) {
                                            mng.addMemebrVoice(member, vc);
                                            listener.spawn(vc);
                                        }
                                    }
                            }
                        }
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
        for(Guild g: voices.keySet()) {
            HashMap<Object, Object> voiceSettings = new HashMap<Object, Object>(),
                    privateVoices = new HashMap<Object, Object>();
            GuildVoiceManager mng = voices.get(g);
            
            // Save private voice limits
            if(mng.getLimit() != (int)Setting.LIMIT.def)
                voiceSettings.put(Setting.LIMIT.key, mng.getLimit());
            if(mng.getUserLimit() != (int)Setting.USER_LIMIT.def)
                voiceSettings.put(Setting.USER_LIMIT.key, mng.getUserLimit());
            if(mng.getCategory() != (Category) VoiceManager.Setting.CATEGORY.def)
                voiceSettings.put(Setting.CATEGORY.key, mng.getCategory().getId());
            
            // Save private voice channels
            for(Entry<Member, List<VoiceChannel>> entry: mng.getPrivateVoices()) {
                JSONArray vcs = new JSONArray();
                
                for(VoiceChannel vc: entry.getValue())
                    vcs.add(vc.getId());
                privateVoices.put(entry.getKey().getUser().getId(), vcs);
            }
            if(!privateVoices.isEmpty())
                voiceSettings.put(Setting.VOICE.key, privateVoices);
            
            if(!voiceSettings.isEmpty())
                settings.put(g.getId(), voiceSettings);
        }
        
        // Attempt to save
        try {
            PrintWriter voicePW = new PrintWriter(DATA_PATH + FILE);
            voicePW.print(settings.toJSONString());
            voicePW.flush();
            voicePW.close();
        } catch (FileNotFoundException e) {
            Bot.getLogger().serverLog(LogState.WARNING, SAVE_ERROR);
        }
    }
    
    /*----      Getters & Setters       ----*/
    
    public GuildVoiceManager getManager(Guild g) { 
        if(!voices.containsKey(g))
            voices.put(g, new GuildVoiceManager());
        return voices.get(g);
    }
    
    public VoiceListener getListener() { return listener; }
    
}
