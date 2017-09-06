package d3bcSoftware.d3bot;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.security.auth.login.LoginException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import d3bcSoftware.d3bot.logging.LogState;
import d3bcSoftware.d3bot.logging.Logger;
import d3bcSoftware.d3bot.music.MusicManager;
import d3bcSoftware.d3bot.voice.VoiceManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/**
 * D3-Bot's main section where all its functionality is . 
 * @author Boyd Compton
 */
public class Bot {
    /*----      Constants       ----*/
    
    private final static String SETTINGS_PATH = "data/settings.json";
    private final static String LOAD_SETTING = "Loaded %s with value \'%s\'.";
    private final static String SAVE_SETTINGS = "Saving Settings file.";
    private final static String SAVE_FAIL = "Unable to save settings file.";
    
    private final static String MISSING_SET_FILE = "Unable to find settings file. Attempting to create default settings file.";
    private final static String FAIL_SET_FILE = "Unable to create default settings file.";
    private final static String LOAD_SET_ERROR_IO = "Unable to load settings file due to IO.";
    private final static String LOAD_SET_ERROR_PARSE = "Unable to load settings file due to syntax issue.";
    
    private final static String FOLDER_CREATE_ERROR = "%sUnable to create folder %s.";
    
    private final static String DISCORD_CONN_ERROR = "Unable to connect to discord.";
    
    /**
     * Details all the keys within D3-Bot's main setting file.
     * @author Boyd Compton
     */
    public enum Key {
        TOKEN("token", ""), PREFIX("prefix", "!"), YOUTUBE("youtube", "");
        
        /**
         * The key used to find a value.
         */
        private String key;
        
        /**
         * The default value for key
         */
        private Object value;
        
        private Key(String key, Object val) {
            this.key = key;
            this.value = val;
        }
        
        /**
         * Gets the key used to find a value.
         * @return The key of a value
         */
        public String getKey() {
            return key;
        }

        /**
         * The default value for a key
         * @return The default value
         */
        public String getValue() {
            return ""+value;
        }
        
        @Override
        public String toString() { return key; }
    }
    
    /*----      D3-Bot Variables       ----*/
    
    /**
     * D3-Bot's Logging
     */
    private static Logger logger = null;
    private static JSONObject settings = new JSONObject();
    private static String prefix = (String) Key.PREFIX.getValue();
    private static Listener events = null;
    private static JDA bot = null;
    private static MusicManager music = null;
    private static VoiceManager voice = null;
    
    /*----      Main       ----*/
    
    /**
     * Main function used to connect the bot to discord and register the listeners.
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        String token = "";
        File settingsFile = null;
        Object temp = null;
        
        logger = new Logger();
        
        // Opening the D3-Bot's Setting file.
        createFolders();
        settingsFile = new File(SETTINGS_PATH);
        
        if(!settingsFile.exists()) {
            System.out.println(LogState.WARNING.getConsole() + MISSING_SET_FILE);
            
            try {
                PrintWriter file = new PrintWriter(settingsFile);
                file.print("{}");
                file.close();
                
                // Add default values
                for(Key key: Key.values())
                    settings.put(key.getKey(), key.getValue());
                saveSettings();
            } catch (FileNotFoundException e) {
                System.out.println(LogState.ERROR.getConsole() + FAIL_SET_FILE);
            }
        }
        
        try {
            settings = (JSONObject)(parser.parse(new FileReader(settingsFile)));
        } catch(IOException e) {
            System.out.println(LogState.ERROR.getConsole() + LOAD_SET_ERROR_IO);
        } catch(ParseException e) {
            System.out.println(LogState.ERROR.getConsole() + LOAD_SET_ERROR_PARSE);
        }
        
        loadSettings();
        
        // Register Shutdown hook
        Runtime.getRuntime().addShutdownHook(new ShutDownHook());
        
        // Connect to D3-Bot to discord
        temp = settings.get(Key.TOKEN.getKey());
        events = new Listener();
        music = new MusicManager();
        voice = new VoiceManager();
        
        token = temp == null ? "" : (String)temp;
        if(!token.equals("")) {
            try {
                bot = new JDABuilder(AccountType.BOT)
                        .setToken(token)
                        .setAutoReconnect(true)
                        .addEventListener(events)
                        .buildBlocking();
                music.loadData();
                voice.loadData();
                loadCmd();
            } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
                logger.serverLog(LogState.WARNING, DISCORD_CONN_ERROR);
            }
        }
    }
    
    /*----      Helper Functions       ----*/
    
    /**
     * Saves D3-Bot's settings
     */
    public static void saveSettings() {
        try {
            logger.serverLog(LogState.INFO, SAVE_SETTINGS);
            
            PrintWriter settingPW = new PrintWriter(SETTINGS_PATH);
            settingPW.print(settings.toJSONString());
            settingPW.flush();
            settingPW.close();
        } catch (FileNotFoundException e) {
            logger.serverLog(LogState.WARNING, SAVE_FAIL);
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    /**
     * Creates the folder that D3-bot will use to store data.
     */
    private static void createFolders() {
        File[] folders = {
                new File("data"),
                new File("log")
        };
        
        for(File f: folders)
            if(!f.exists())
                try {
                    f.mkdirs();
                } catch(SecurityException e) {
                    System.out.println(String.format(FOLDER_CREATE_ERROR, 
                            LogState.ERROR.getConsole(), f.getName()));
                }
        
    }
    
    private static void loadSettings() {
        // Load the prefix
        Object prefixJSON = settings.get(Key.PREFIX.getKey());
        if(prefixJSON != null) {
            prefix = (String)prefixJSON;
            logger.serverLog(LogState.INFO, String.format(LOAD_SETTING, Key.PREFIX.getKey(), prefix));
        }
    }
    
    private static void loadCmd() {
        // Load commands data
        Collection<Command> cmds = events.getCommands();
        for(Command cmd: cmds)
            cmd.loadData();
    }
    
    /*----      Getters       ----*/
    
    public static Logger getLogger() { return logger; }
    
    public static JSONObject getSettings() { return settings; }
    
    public static Listener getListener() { return events; }
    
    public static String getPrefix() { return prefix; }
    
    public static JDA getBot() { return bot; }
    
    public static MusicManager getMusicManager() { return music; }
    
    public static VoiceManager getVoiceManager() { return voice; }
}
