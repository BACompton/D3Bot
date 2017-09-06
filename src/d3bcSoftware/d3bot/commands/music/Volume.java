package d3bcSoftware.d3bot.commands.music;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.music.GuildMusicManager;
import d3bcSoftware.d3bot.music.MusicManager;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Toggles the single track repeat feature for the music bot.
 * @author Boyd Compton
 */
public class Volume implements Command {
    /*----      Constants       ----*/
    
    private final static int MAX_VOL = 150;
    private final static String USAGE = "%s%s [volume]" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
    private final static String DESC = "Sets the music bot's volume or displays the current volume of the bot if"
            + "no value was given.\n\n Voulme values from 0 - " + MAX_VOL + " are supported.";
    private final static String HANDLE = "volume";
    
    private final static String SET = Emote.OK + " Set volume to "
            + Format.CODE + "%d" + Format.CODE + ".";
    private final static String DISPLAY = " Current Volume: "
            + Format.CODE + "%d" + Format.CODE;
    
    /*----      Command Actions       ----*/
    
    @Override
    public void loadData() {
    }
    
    @Override
    public void saveData() {
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        GuildMusicManager mng = Bot.getMusicManager().getGuildMusicManager(e.getGuild());
        
        if(MusicManager.isDJ(e.getMember())) {
            try {
                int vol = Integer.parseInt(args[0]);
                
                if(vol > MAX_VOL)
                    throw new NumberFormatException();
                
                mng.player.setVolume(vol);
                e.getChannel().sendMessage(String.format(SET, mng.player.getVolume())).queue();
            } catch(NumberFormatException ignored) {
                e.getChannel().sendMessage(help()).queue();
            } catch(IndexOutOfBoundsException ignored) {
                e.getChannel().sendMessage(String.format(DISPLAY, mng.player.getVolume())).queue();
            }
        } else
            e.getChannel().sendMessage(MusicManager.DJ_PERM).queue();
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

}

