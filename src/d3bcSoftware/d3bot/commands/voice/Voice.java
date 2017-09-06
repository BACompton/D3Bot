package d3bcSoftware.d3bot.commands.voice;

import java.util.ArrayList;
import java.util.Collection;

import d3bcSoftware.d3bot.Bot;
import d3bcSoftware.d3bot.Command;
import d3bcSoftware.d3bot.logging.Emote;
import d3bcSoftware.d3bot.logging.Format;
import d3bcSoftware.d3bot.voice.GuildVoiceManager;
import d3bcSoftware.d3bot.voice.VoiceManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Creates a private voice channel.
 * @author Boyd Compton
 */
public class Voice implements Command {
    /*----      Constants       ----*/
    
    private final static String USAGE = "%s%s %s";
    private final static String HANDLE = "voice";
    
    private final static String CREATE = Emote.OK + " Created voice channel.";
    private final static String LIMIT = Emote.X + " Already hit the limit of "
            + Format.CODE + "%d" + Format.CODE + " personal voice channel.";
    
    private final static String SET_LIMIT = Emote.OK + " Set private voice channels limit to %d.";
    private final static String DISPLAY_LIMIT = "Private voice limit: " + Format.CODE + "%d" + Format.CODE;
    
    private final static String SET_USER = Emote.OK + " Set private voice channel default user limit to %d.";
    private final static String DISPLAY_USER = "Default user limit: " + Format.CODE + "%d" + Format.CODE;
    
    /**
     * Details actions that are supported within the voice command.
     * @author Boyd Compton
     */
    private enum Action {
        CREATE("create", "Creates a personal voice call.\n"
                + "(Created voice channels will be automatically removed after %s of inactivity)"), 
        LIMIT("limit", "Sets the limit of private channels a person can have "
                + "(Requires permission: " + VoiceManager.PERM.getName() + ")."),
        USER("user", "Sets the default user limit of private channels "
                + "(Requires permission: " + VoiceManager.PERM.getName() + ").");
        
        /*----      Constants       ----*/
        
        private final static String ACTION_USAGE = " %s [value]" + Format.CODE_BLOCK + "%s" + Format.CODE_BLOCK;
        private final static String ACTION_DESC = "-%s: %s\n\n";
        
        /*----      Instance Variables       ----*/
        
        public String name;
        private String description;
        
        /*----      Constructors       ----*/
        
        private Action(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        /*----      Helper Functions       ----*/
        
        public String getDesc() { return String.format(ACTION_DESC, name, description); }
        
        /**
         * Builds the usage and descriptions for a set Action.
         * @return Formatted usage for all available actions.
         */
        public String usageAction() {
            return String.format(ACTION_USAGE, name, description);
        }
        
        /**
         * Builds the usage and descriptions for viable action.
         * @return Formatted usage for all available actions.
         */
        public static String usage() {
            String usage = "[";
            String desc = "";
            
            // Build the usage and description for each action.
            for(Action a: values()) {
                usage += a.name + " | ";
                desc += a.getDesc();
            }
            usage = usage.substring(0, usage.length()-" | ".length()) + "]";
            
            return String.format(ACTION_USAGE, usage, desc);
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
    
    /*----      Instance Variables       ----*/
    
    private Collection<Permission> allow, deny;
    
    /*----      Command Actions       ----*/
    
    @Override
    public void loadData() {
        allow = new ArrayList<Permission>();
        deny = new ArrayList<Permission>();
        
        allow.add(Permission.MANAGE_CHANNEL);
        deny.add(Permission.CREATE_INSTANT_INVITE);
    }
    
    @Override
    public void saveData() {
    }
    
    @Override
    public void action(MessageReceivedEvent e, String[] args) {
        // Determine action
        Action a = null;
        try {
            a = Action.find(args[0]);
            
            if(a == null)
                throw new IndexOutOfBoundsException();
        } catch (IndexOutOfBoundsException ignore) {
            e.getChannel().sendMessage(help()).queue();
            return;
        }
        
        if(a == Action.CREATE)
            createVoice(e);
        else if(a == Action.LIMIT)
            limit(e, args);
        else if(a == Action.USER)
            user(e, args);
        
        
    }
    
    @Override
    public String getHandle() {
        return HANDLE;
    }

    @Override
    public String help() {
        return String.format(USAGE, Bot.getPrefix(), HANDLE, Action.usage());
    }
    
    public String help(Action a) {
        return String.format(USAGE, Bot.getPrefix(), HANDLE, a.usageAction());
    }
    
    @Override
    public String getDataPath() {
        return String.format(DATA_PATH, HANDLE);
    }
    
    /*----      Helpers       ----*/
    
    /**
     * Helper to handle the create voice action
     * @param e The message received event
     */
    private void createVoice(MessageReceivedEvent e) {
        Guild g = e.getGuild();
        VoiceManager voice = Bot.getVoiceManager();
        GuildVoiceManager mng = Bot.getVoiceManager().getManager(g);
        
        if(mng.getMemebrsVoice(e.getMember()).size() < mng.getLimit()) {
            String name = String.format((String)VoiceManager.Setting.VOICE.def, e.getAuthor().getName()),
                    indexFormat = " %d";
            int index = 0;
            
            // Avoids creating duplicate Voice channels with the same name
            if(!g.getVoiceChannelsByName(name, false).isEmpty()) {
                index++;
                
                while(!g.getVoiceChannelsByName(name+String.format(indexFormat, index), false).isEmpty())
                    index++;
                if(index > 0)
                    name += String.format(indexFormat, index);
            }
            
            VoiceChannel vc =(VoiceChannel) g.getController().createVoiceChannel(name)
                    .setUserlimit(mng.getUserLimit())
                    .addPermissionOverride(e.getMember(), allow, deny).complete();
            mng.addMemebrVoice(e.getMember(), vc);
            voice.getListener().spawn(vc);
            
            e.getChannel().sendMessage(CREATE).queue();
        } else
            e.getChannel().sendMessage(String.format(LIMIT, mng.getLimit())).queue();
    }
    
    /**
     * Helper to handle changing the limit to the amount of private channel's a user can create.
     * @param e The message received event
     */
    private void limit(MessageReceivedEvent e, String[] args) {
        GuildVoiceManager mng = Bot.getVoiceManager().getManager(e.getGuild());
        
        if(e.getMember().hasPermission(VoiceManager.PERM)) {
            try {
                mng.setLimit(Integer.parseInt(args[1]));
                e.getChannel().sendMessage(String.format(SET_LIMIT, mng.getLimit())).queue();
            } catch(NumberFormatException ignored) {
                e.getChannel().sendMessage(help(Action.LIMIT)).queue();
            } catch(IndexOutOfBoundsException ignored) {
                e.getChannel().sendMessage(String.format(DISPLAY_LIMIT, mng.getLimit())).queue();
            }
        } else
            e.getChannel().sendMessage(VoiceManager.PERM_MSG).queue();
    }
    
    /**
     * Helper to handle changing the default user limit of private channels.
     * @param e The message received event
     */
    private void user(MessageReceivedEvent e, String[] args) {
        GuildVoiceManager mng = Bot.getVoiceManager().getManager(e.getGuild());
        
        if(e.getMember().hasPermission(VoiceManager.PERM)) {
            try {
                mng.setUserLimit(Integer.parseInt(args[1]));
                e.getChannel().sendMessage(String.format(SET_USER, mng.getUserLimit())).queue();
            } catch(NumberFormatException ignored) {
                e.getChannel().sendMessage(help(Action.USER)).queue();
            } catch(IndexOutOfBoundsException ignored) {
                e.getChannel().sendMessage(String.format(DISPLAY_USER, mng.getUserLimit())).queue();
            }
        } else
            e.getChannel().sendMessage(VoiceManager.PERM_MSG).queue();
    }
}

