package d3bcSoftware.d3bot;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.reflections.Reflections;

import d3bcSoftware.d3bot.logging.LogState;
import d3bcSoftware.d3bot.logging.Logger;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * D3-Bot's main event listener. This class also handles registering bot commands and bot command execution.
 * @author Boyd Compton
 *
 */
public class Listener extends ListenerAdapter {
    /*----      Constants       ----*/
    
    private final static String CMD_LOAD_ERROR = "%s could not loaded.";
    private final static String CMD_LOAD = "Loaded commands:%s";
    private final static String CMD_DATA_CREATE = "Created command data paths:%s";
    private final static String CMD_ENTRY = "\n\t-%s";
    private final static String NONE = " none";
    
    private final static String DISCORD_LOGIN_INFO = "Logged in as %s.";
    private final static String DISCORD_GUILD_HEAD = "Currently Serving the following Guilds:";
    private final static String DISCORD_GUILD_MSG = "\n\t-%s";
    
    /*----      Instance Variables       ----*/
    
    private HashMap<String, Command> cmds;
    
    /*----      Constructors       ----*/
    
    public Listener() {
        cmds = new HashMap<String, Command>();
        
        // Get all commands placed within the current executable.
        Reflections r = new Reflections("d3bcSoftware.d3bot");
        Set<Class<? extends Command>> classes = r.getSubTypesOf(Command.class);
        
        String loaded = "", created = "";
        for(Class<? extends Command> c: classes)
            try {
                // Register Command
                Command cmd = c.newInstance();
                cmds.put(cmd.getHandle().toLowerCase(), cmd);
                loaded += String.format(CMD_ENTRY, cmd.getHandle());
                
                // Create Commands data directory
                File cmdPath = new File(cmd.getDataPath());
                if(cmdPath.mkdirs())
                    created += String.format(CMD_ENTRY, cmd.getHandle());
            } catch (InstantiationException | IllegalAccessException e) {
                Bot.getLogger().serverLog(LogState.ERROR, String.format(CMD_LOAD_ERROR, c.getName()));
            }
        Bot.getLogger().serverLog(LogState.INFO, String.format(CMD_LOAD, loaded.isEmpty() ? NONE : loaded));
        Bot.getLogger().serverLog(LogState.INFO, String.format(CMD_DATA_CREATE, created.isEmpty() ? NONE : created));
    }
    
    /*----      Events       ----*/
    
    @Override
    public void onReady(ReadyEvent e) {
        Logger log = Bot.getLogger();
        // Log who the D3-Bot logged in as.
        log.serverLog(LogState.INFO, String.format(DISCORD_LOGIN_INFO, e.getJDA().getSelfUser().getName()));
        
        // Log the guilds being served by D3-Bot
        String guilds = DISCORD_GUILD_HEAD;
        for(Guild g:e.getJDA().getGuilds())
            guilds += String.format(DISCORD_GUILD_MSG, g.getName());
        log.serverLog(LogState.INFO, guilds);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        String msg = e.getMessage().getContent(), 
                handle = "";
        
        //TODO:additional regex checking
        if(e.getAuthor().isBot()) return;
        
        if(msg.startsWith(Bot.getPrefix())) {
            handle = msg.split(" ", 2)[0].substring(Bot.getPrefix().length()).toLowerCase();
            Command cmd = cmds.get(handle);
            
            if(cmd != null) {
                String[] argsArr = new String[0];
                String args = msg.substring(handle.length()+Bot.getPrefix().length()).trim();
                
                if(!args.equals(""))
                    argsArr = args.split(" ");
                cmd.action(e, argsArr);
            }
        }
    }
    
    /*----      Getters & Setters       ----*/
    
    public Collection<Command> getCommands() {
        return cmds.values();
    }
}
