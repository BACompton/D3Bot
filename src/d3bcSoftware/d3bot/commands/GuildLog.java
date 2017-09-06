package d3bcSoftware.d3bot.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.logging.LogState;
import d3bcSoftware.d3bot.logging.Logger;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Details the GuildLog command. This command allows guilds served by D3-Bot to manage the text channel where the bot
 * will log its activity.
 * @author Boyd Compton
 */
public class GuildLog implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = Format.CODE + "%s%s %s";
    private final static String HANDLE = "log";
    private final static String FILE = "/guilds.json";
    
    private final static String SAVE_GUILD = "Saving guilds' logging channels.";
    private final static String LOAD_GUILD = "Loading guilds' logging channels.";
    private final static String LOAD_ERROR = "Unable to load guilds' logging channels.";
    private final static String SAVE_ERROR = "Unable to save guilds' logging channels.";
    
    private final static String PERM_WARNING = Emote.X + " Administrator permission required to preform this action.";
    private final static String ACTION_UNKN = Emote.QUESTION_MARK 
            + "Unknow action. Please refrer the following usage.\n\n%s";
    private final static String SET_CHANNEL = Emote.CHECK + " Updated logging channel from " 
            + Format.CODE + "%s" + Format.CODE + " to " + Format.CODE + "%s" + Format.CODE + ".";
    private final static String REMOVED_CHANNEL = Emote.CHECK + " Removed Guild's Logging channel.";
    private final static String LOG_CHANNEL = "Current logging channel is " + Format.CODE + "%s" + Format.CODE + ".";
    
    /**
     * Details actions that are supported within the GuildLog command.
     * @author Boyd Compton
     */
    private enum Action {
        SET("set", "Binds the text channel as the guild's logging channel."), 
        REMOVE("remove", "Unbinds a text channel as the guild's logging channel."),
        HELP("?", "Outputs the command usage.");
        
        /*----      Constants       ----*/
        
        private final static String ACTION_DESC = "-%s: %s\n\n";
        private final static String END_NOTE = Format.ITALICS + "Requires administrator permission" + Format.ITALICS;
        
        /*----      Instance Variables       ----*/
        
        public String name;
        private String description;
        
        /*----      Constructors       ----*/
        
        private Action(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        /*----      Helper Functions       ----*/
        
        /**
         * Builds the usage and descriptions for viable action.
         * @return Formatted usage for all available actions.
         */
        public static String usage() {
            String usage = "[";
            String desc = Format.CODE_BLOCK.toString();
            
            // Build the usage and description for each action.
            for(Action a: values()) {
                usage += a.name + "|";
                desc += String.format(ACTION_DESC, a.name, a.description);
            }
            usage = usage.substring(0, usage.length()-1) + "]" + Format.CODE + "\n";
            desc += Format.CODE_BLOCK + "\n" + END_NOTE;
            
            return usage + desc;
        }
        
        /**
         * Retrieves the action with the specified identifier.
         * @param name The action identifier
         * @return The action that exactly matches identifier or null if no action is found.
         */
        public static Action find(String name) {
            for(Action a: values())
                if(a.name.equals(name))
                    return a;
            return null;
        }
    }
    
    /*----      Constructors       ----*/
    
    @Override
    public void loadData() {
        File guildsFile = new File(getDataPath() + FILE);
        
        if(guildsFile.exists()) {
            JSONParser parser = new JSONParser();
            
            try {
                JSONObject guilds = (JSONObject)(parser.parse(new FileReader(guildsFile)));
                Logger logger = Bot.getLogger();
                JDA bot = Bot.getBot();
                
                logger.serverLog(LogState.INFO, LOAD_GUILD);
                
                // Set active active logging channels
                if(bot != null)
                    for(Object key:guilds.keySet()) {
                        Guild g = bot.getGuildById(Long.parseLong((String) key));
                        TextChannel chan = g.getTextChannelById(Long.parseLong((String)guilds.get(key)));
                        logger.setGuildLogging(g, chan);
                    }
            } catch (IOException | ParseException e) {
                Bot.getLogger().serverLog(LogState.WARNING, LOAD_ERROR);
            }
        }
    }
    
    /*----      Command Actions       ----*/
    
    @SuppressWarnings("unchecked")
    @Override
    public void saveData() {
        JSONObject guilds = new JSONObject();
        String guildsStr = "";
        
        if(Bot.getBot() == null)
            return;
        
        // Build JSONObject
        for(Guild g: Bot.getLogger().getGuilds())
            guilds.put(g.getId(), Bot.getLogger().getGuildLogging(g).getId());
        guildsStr = guilds.toJSONString();
        
        try {
            Bot.getLogger().serverLog(LogState.INFO, SAVE_GUILD);
            
            PrintWriter guildPW = new PrintWriter(getDataPath() + FILE);
            guildPW.print(guildsStr);
            guildPW.flush();
            guildPW.close();
        } catch (FileNotFoundException e) {
            Bot.getLogger().serverLog(LogState.WARNING, SAVE_ERROR);
        }
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        TextChannel log = Bot.getLogger().getGuildLogging(e.getGuild());
        String channel = log == null ? "none" : log.getName();
        
        // Detail the current logging channel.
        if(args.length == 0) {
            e.getTextChannel()
                .sendMessage(String.format(LOG_CHANNEL, channel))
                .queue();
        } else if(e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            Action action = Action.find(args[0]);
            
            if(action == Action.SET) {
                TextChannel chan = e.getTextChannel();
                Bot.getLogger().setGuildLogging(e.getGuild(), chan);
                chan.sendMessage(String.format(SET_CHANNEL, channel, chan.getName()))
                    .queue();
            } else if(action == Action.REMOVE) {
                Bot.getLogger().setGuildLogging(e.getGuild(), null);
                e.getTextChannel().sendMessage(REMOVED_CHANNEL).queue();;
            } else if(action == Action.HELP) {
                e.getTextChannel().sendMessage(help()).queue();
            }
            // Response to an unknown action.
            else if(action == null) {
                e.getTextChannel().sendMessage(String.format(ACTION_UNKN, help())).queue();;
            }
        } 
        // Missing Permission response.
        else
            e.getTextChannel().sendMessage(PERM_WARNING).queue();
    }
    
    @Override
    public String getHandle() {
        return HANDLE;
    }
    
    @Override
    public String help() {
        return String.format(USAGE, Bot.getPrefix(), HANDLE, Action.usage());
    }
    
    @Override
    public String getDataPath() {
        return String.format(DATA_PATH, HANDLE);
    }

}
