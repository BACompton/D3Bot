package d3bcSoftware.d3bot.voice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

/**
 * Voice channel activity listener that handles the music bot auto-disconnect feature.
 * @author Boyd Compton
 *
 */
public class GuildVoiceManager {
    /*----      Instance Variables       ----*/
    
    private int limit, user;
    private HashMap<Member, List<VoiceChannel>> voice;
    private Category cat;
    
    /*----      Constructor       ----*/
    
    public GuildVoiceManager() {
        cat = (Category) VoiceManager.Setting.CATEGORY.def;
        voice = new HashMap<Member, List<VoiceChannel>>();
        limit = (int)VoiceManager.Setting.LIMIT.def;
        user = (int)VoiceManager.Setting.USER_LIMIT.def;
    }

    /*----      Getters & Setters      ----*/
    
    public Category getCategory() { return cat; }

    public void setCategory(Category cat) { this.cat = cat; }
    
    public int getUserLimit() { return user; }

    public void setUserLimit(int limit) { this.user = limit; }
    
    public int getLimit() { return limit; }

    public void setLimit(int limit) { this.limit = limit; }
    
    public Set<Entry<Member, List<VoiceChannel>>> getPrivateVoices() { return voice.entrySet(); }
    
    public List<VoiceChannel> getMemebrsVoice(Member m) { 
        if(!voice.containsKey(m))
            voice.put(m, new ArrayList<VoiceChannel>());
        return voice.get(m); 
    }
    
    public Member findVoiceChannel(VoiceChannel vc) {
        for(Entry<Member, List<VoiceChannel>> e: voice.entrySet())
            if(e.getValue().contains(vc))
                return e.getKey();
        return null;
    }
    
    public void removeMemberVoice(Member m, VoiceChannel vc) {
        List<VoiceChannel> vcs = voice.get(m);
        
        if(vcs != null) {
            vcs.remove(vc);
            if(vcs.isEmpty())
                voice.remove(m);
        }
    }
    
    public void addMemebrVoice(Member m, VoiceChannel vc) {
        List<VoiceChannel> vcs = voice.get(m);
        
        if(vc == null)
            return;
        
        if(vcs == null) {
            vcs = new ArrayList<VoiceChannel>();
            voice.put(m, vcs);
        }
        vcs.add(vc);
    }
}
