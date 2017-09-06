package d3bcSoftware.d3bot.logging;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Contains D3-bot's logging functionality for server-side and discord server logging. 
 * @author Boyd Compton
 */
public class Logger {
    /*----      Constants       ----*/
    
    public final static byte INACTIVE = 0;
    
    /*----      Instance Variables       ----*/
    
    private byte console;
    private HashMap<Guild, TextChannel> discord;
    private PrintWriter log;
    
    /*----      Constructors       ----*/
    
    /**
     * Default logger constructor that set the following:
     *  <ul>
     *  <li>Logging file to the local date time in ISO format</li>
     *  <li>Logging channels an empty map</li>
     *  </ul>
     */
    public Logger() {
        this("log/"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))+".log");
    }
    
    /**
     * Creates a Logger with the file and channel to the provided parameters.
     * @param file The logging file
     * @param channel The Logging channel
     */
    public Logger(String file) {
        discord = new HashMap<Guild, TextChannel>();
        console = (byte)(LogState.ERROR.flag() | LogState.WARNING.flag() | LogState.INFO.flag());
        log = null;
        
        try {
            log = new PrintWriter(file);
        } catch(FileNotFoundException e) {
            System.out.println(LogState.ERROR.getConsole() + "Unable to create log file!");
        }
    }
    
    /*----      Logging       ----*/
    
    /**
     * Pushes a Log message the server
     * @param state The message's logging state
     * @param msg The message to push
     */
    public void serverLog(LogState state, String msg) {
        String formatMsg = String.format("[%s] %s%s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), 
                state.getConsole(), 
                msg);
        
        if((state.flag() & console) != INACTIVE)
            System.out.println(formatMsg);
        if(log != null) {
            log.println(formatMsg);
            log.flush();
        }
    }
    
    /**
     * Pushes a Log message to the specified Guild's logging text channel.
     * @param g the Guild
     * @param state The message's logging state
     * @param msg The message to push
     */
    public void guildLog(Guild g, LogState state, String msg) {
        TextChannel c = getGuildLogging(g);
        
        if(c != null)
            c.sendMessageFormat("%s%s", state.getChat(), msg).queue();
    }
    
    /**
     * Pushes a Log message to the specified Guild's logging text channel and server's log.
     * @param g the Guild
     * @param state The message's logging state
     * @param msg The message to push
     */
    public void serverGuildLog(Guild g, LogState state, String msg) {
        serverLog(state, String.format("[%s] %s", g.getName(), msg));
        guildLog(g, state, msg);
    }
    
    /*----      Getters & Setters       ----*/
    
    /**
     * Change which types messages will get passed to the console logger.
     * @param states
     */
    public void setConsoleLog(LogState... states) {
        console = 0;
        addConsoleLog(states);
    }
    
    /**
     * Adds additional message type flags to the console logger.
     * @param states
     */
    public void addConsoleLog(LogState... states) {
        for(LogState s: states)
            console = (byte)(console | s.flag());
    }
    
    /**
     * Sets the specified Guild's logging text channel.
     * 
     * If the channels is null, the Guild's logging will be removed.
     * @param g The guild
     * @param c The logging channel
     */
    public void setGuildLogging(Guild g, TextChannel c) {
        if(c == null)
            discord.remove(g);
        else if(c.getGuild().equals(g))
            discord.put(g, c);
    }
    
    /**
     * Retrieves the logging channel for the specified logging channel.
     * @param g The Guild
     * @return The Logging channel
     */
    public TextChannel getGuildLogging(Guild g) {
        return discord.get(g);
    }
    
    /**
     * Retrieves all the guilds with an active logging text channel.
     * @return the set of all guilds with logging channels.
     */
    public Set<Guild> getGuilds() { return discord.keySet(); }
    
}
