package d3bcSoftware.d3bot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * A interface for a D3-Bot command.
 * @author Boyd Compton
 */
public interface Command {
    public final static String DATA_PATH = "data/%s";
    
    /**
     * Enacts a command saving procedure.
     */
    public void saveData();
    
    /**
     * Enacts the loading procedure for a command.
     */
    public void loadData();
    
    /**
     * Enacts the action of a particular command.
     * @param e The event for the command
     */
    public void action(MessageReceivedEvent e, String[] args);
    
    /**
     * Returns the help message for a command. Typically, this will simply return a commands usage.
     * @return The help message for a command
     */
    public String help();
    
    /**
     * Retrieves the data path to a commands data storage.
     * @return A path to commands data storage
     */
    public String getDataPath();
    
    /**
     * Retrieves the handle for a command.
     * @return A command's Handle
     */
    public String getHandle();
}
