package d3bcSoftware.d3bot.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.Listener;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * A generic help command for D3bot
 * @author Boyd Compton
 */
public class Help implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s <search> [-p <page number]" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Searches active commands and prints out command usage";
    private final static String HANDLE = "help";
    
    private final static String NO_RESULTS = "No results found for " + Format.CODE + "%s" + Format.CODE ;
    private final static String PAGE_ERROR = Emote.X + " No page with matching index. Only have "
            + Format.CODE + "%d" + Format.CODE + "pages";
    private final static String RESULTS = "Results for " + Format.CODE + "%s" + Format.CODE + ": (%d of %d)\n"
            + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String ITEM = "- %s\n";
            
    private final static int RESULT_LIMIT = 10;
    
    /*----      Command Variables       ----*/
    
    private HashMap<User, Command[]> cachedResults;
    
    /*----      Command Actions       ----*/
    
    @Override
    public void loadData() {
        cachedResults = new HashMap<User, Command[]>();
    }
    
    @Override
    public void saveData() {
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        int page = 0;
        String query  = "";
        Command[] rst = null;
        
        // Build Query and read flags
        boolean pageFlag = false;
        for(String arg: args) {
            if(arg.equalsIgnoreCase("-p"))
                pageFlag = true;
            else if(pageFlag) {
                try {
                    page = Integer.parseInt(arg) - 1;
                } catch (NumberFormatException ignore) {
                    page = -1; // Flags the page number for error
                }
                pageFlag = false;
            } else {
                query += " " + arg;
            }
        }
        if(!query.isEmpty())
            query = query.substring(" ".length());
        
        if(query.isEmpty() && cachedResults.containsKey(e.getAuthor()))
            rst = cachedResults.get(e.getAuthor());
        if(rst == null 
                || (rst != null && rst.length <= 1)){ // if not list or single item
            rst = search(query);
            cachedResults.put(e.getAuthor(), rst);
        }
        
        if(rst.length == 0)
            e.getChannel().sendMessage(String.format(NO_RESULTS, query)).queue();
        else if(rst.length == 1)
            e.getChannel().sendMessage(rst[0].help()).queue();
        else {
            int pages = (rst.length-1) / RESULT_LIMIT;
            
            if(page < 0 || page > pages)
                e.getChannel().sendMessage(String.format(PAGE_ERROR, pages)).queue();
            else {
                String result = "";
                int start = page * RESULT_LIMIT, end = (page+1) * RESULT_LIMIT;
                
                if(query.isEmpty())
                    query = " ";
                if(end > rst.length)
                    end = rst.length;
                
                for(; start < end; start++)
                    result += String.format(ITEM, rst[start].getHandle());
                
                e.getChannel().sendMessage(String.format(RESULTS, query, page+1, pages+1, result)).queue();;
            }
        }
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
    
    /**
     * Searches the registered commands with the given query.
     * @param query the search query to run
     * @return A list of all commands that contain the query.
     */
    private Command[] search(String query) {
        Listener listener = Bot.getListener();
        List<Command> results = new ArrayList<Command>();
        
        for(Command c: listener.getCommands()) {
            if(c.getHandle().equalsIgnoreCase(query))
                return new Command [] {c};
            if(c.help().contains(query))
                results.add(c);
        }
        
        return results.toArray(new Command[] {});
    }
    
}


