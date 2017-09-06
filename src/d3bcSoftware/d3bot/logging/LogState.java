package d3bcSoftware.d3bot.logging;

/**
 * Details the different logging states a message can have within D3-Bot.
 * @author Boyd Compton
 */
public enum LogState {
    /*----      States       ----*/
    
    ERROR("[Error] ", Format.BOLD + "ERROR: " + Format.BOLD, (byte)(1)), 
    WARNING("[Warning] ", Format.BOLD + "WARNING: " + Format.BOLD, (byte)(1 << 1)), 
    INFO("[Info] ", Format.BOLD + "INFO: " + Format.BOLD, (byte)(1 << 2));
    
    /*----      Instance Variables       ----*/
    
    private String console, chat;
    private byte flag;
    
    /*----      Constructors       ----*/
    
    private LogState(String consle, String chat, byte flag) {
        this.console = consle;
        this.chat = chat;
        this.flag = flag;
    }
    
    /*----      Getters & Setters       ----*/
    
    /**
     * Returns the message prefix for the console/server side log.
     * @return the message prefix
     */
    public String getConsole() {
        return console;
    }
    
    /**
     * Returns the message prefix for Discord chat logs.
     * @return message prefix
     */
    public String getChat() {
        return chat;
    }
    
    /**
     * Returns the flag for console output.
     * @return the flagged bit
     */
    public byte flag() {
        return flag;
    }
}
