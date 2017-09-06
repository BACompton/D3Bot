package d3bcSoftware.d3bot;

import java.util.Collection;

/**
 * This is a simple shutdown hook to save all of D3-Bot's data before execution is halted.
 * @author Boyd Compton
 */
public class ShutDownHook extends Thread {
    
    @Override
    public void run() {
        Collection<Command> cmds = Bot.getListener().getCommands();
        
        Bot.saveSettings();
        Bot.getVoiceManager().saveData();
        Bot.getMusicManager().saveData();
        for(Command cmd: cmds)
            cmd.saveData();
    }
    
}
