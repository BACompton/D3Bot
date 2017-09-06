package d3bcSoftware.d3bot.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import d3bcSoftware.d3bot.commands.music.NowPlaying;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Specifies the Queuing behavior for the music player.
 * @author Boyd Compton
 *
 */
public class TrackScheduler extends AudioEventAdapter{
    
    /*----      Constructors       ----*/
    
    private final boolean DEF_TRACK_REPEAT = false, DEF_QUEUE_LOOP = false, DEF_SHUFFLE = false, DEF_ANNOUNCE = false;
    
    // Shuffle: pick random song until all have been selected
    /*----      Instant Variables       ----*/
    
    private boolean trackRepeat, queueLoop, shuffle, announce;
    private TextChannel announceChannel;
    private final AudioPlayer player;
    private final ArrayList<AudioTrack> queue, queueShuffle;
    private long queueTime;
    public AudioTrack currTrack;
    
    /*----      Constructors       ----*/
    
    /**
     * @param player The Audio player the scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        queue = new ArrayList<AudioTrack>();
        queueShuffle = new ArrayList<AudioTrack>();
        trackRepeat = DEF_TRACK_REPEAT;
        queueLoop = DEF_QUEUE_LOOP;
        shuffle = DEF_SHUFFLE;
        announce = DEF_ANNOUNCE;
        announceChannel = null;
        queueTime = 0;
    }
    
    /*----      Queue Actions       ----*/
    
    /**
     * Attempt to start playing the track without interrupting. If it fails add the track to the queue.
     * @param track Audio Track to queue or play
     */
    public void queue(AudioTrack track) {
        if(!player.startTrack(track, true)) {
            queueTime += track.getDuration();
            
            if(shuffle) {
                Random r = new Random();
                int index = r.nextInt(queueShuffle.size() + 1);
                
                if(index < queueShuffle.size()) {
                    AudioTrack temp = queueShuffle.get(index);
                    queueShuffle.set(index, track);
                    queueShuffle.add(temp);
                } else
                    queueShuffle.add(track);
            }
            queue.add(track);
        } else {
            currTrack = track;
        }
    }
    
    /**
     * Skips the currently playing audio track.
     */
    public void skip() {
        if(currTrack != null)
            currTrack.setPosition(currTrack.getDuration());
    }
    
    public void clear() {
        queue.clear();
        queueShuffle.clear();
        remove(-1);
    }
    
    public boolean inQueue(AudioTrack track) {
        for(AudioTrack t: queue) {
            if(similarTrack(t, track))
                return true;
        }
        return false;
    }
    
    public void swap(int from, int to) {
        try {
            AudioTrack tmp = null;
            
            if(shuffle) {
                tmp = queueShuffle.get(from);
                queueShuffle.set(from, queueShuffle.get(to));
                queueShuffle.set(to, tmp);
            } else {
                tmp = queue.get(from);
                queue.set(from, queue.get(to));
                queue.set(to, tmp);
            }
        } catch(IndexOutOfBoundsException ignored) {}
    }
    
    public AudioTrack remove(int index) {
        AudioTrack track = null;
        
        if(index == -1) {
            track = currTrack;
            currTrack = null;
            player.setPaused(false);
            startNextSong();
        }
        
        try {
            if(shuffle) {
                track = queueShuffle.remove(index);
                int qIndex = getIndex(track);
                if(qIndex >= 0) {
                    queue.remove(qIndex);
                }
            }
            else
                track = queue.remove(index);
        } catch(IndexOutOfBoundsException ignored) {}
        
        return track;
    }
    
    public AudioTrack getNextSong() {
        if((shuffle && !queueShuffle.isEmpty()) || (!shuffle && !queue.isEmpty())) {
            return shuffle ? queueShuffle.get(0): queue.get(0);
        } else 
            return null;
    }
    
    /*----      Events       ----*/
    
    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if(announce && announceChannel != null) {
            AudioTrack next = getNextSong();
            EmbedBuilder builder = NowPlaying.NOW_PLAYING_BUILD;
            synchronized (builder) {
                builder.setDescription(String.format(NowPlaying.NOW_PLAYING, 
                        getTrackDisplay(track), 
                        MusicManager.getTimestamp(track.getDuration())))
                .setTitle(NowPlaying.NOW_PLAYING_TITLE, track.getInfo().uri);;
                
                if(next != null)
                    builder.appendDescription(String.format(NowPlaying.UP_NEXT, 
                            getTrackDisplay(next), 
                            MusicManager.getTimestamp(next.getDuration())));
                announceChannel.sendMessage(builder.build()).queue();
            }
        }
        
        this.currTrack = track;
    }
    
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason reason) {
        if(reason.mayStartNext) {
            // Handle Repeating tracks.
            if(trackRepeat) {
                player.startTrack(track.makeClone(), false);
                return;
            }
            
            // Handle how the queue should loop. The track is only added back into the queue if the following
            // is met:
            //  1) Queue is looped
            //  2) Current track is not in the queue
            if(queueLoop && !inQueue(track)) {
                queue.add(track.makeClone());
                queueTime += track.getDuration();
            }
            
            startNextSong();
        }
    }
    
    /*----      Helpers      ----*/
    
    public static boolean similarTrack(AudioTrack t1, AudioTrack t2) {
        AudioTrackInfo t1Info = t1.getInfo(), t2Info = t2.getInfo(); 
        
        return t1Info.uri.equals(t2Info.uri)
                && t1Info.title.equals(t2Info.title)
                && t1Info.author.equals(t2Info.author);
    }
    
    public static String getTrackDetailedDisplay(AudioTrack track) {
        if(track == null)
            return "None";
        return String.format("[%s] %s - %s", MusicManager.getTimestamp(track.getDuration()),
                track.getInfo().title, track.getInfo().author);
    }
    
    public static String getTrackDisplay(AudioTrack track) {
        if(track == null)
            return "None";
        return String.format("%s - %s", track.getInfo().title, track.getInfo().author);
    }
    
    private void startNextSong() {
        // Re-shuffle the queue if necessary.
        if(shuffle && queueShuffle.isEmpty()) {
            if(!queueLoop)
                queue.clear();
            setShuffle(shuffle);
        }
        
        // Find next Song
        if((shuffle && !queueShuffle.isEmpty()) || (!shuffle && !queue.isEmpty())) {
            AudioTrack nextTrack = shuffle ? queueShuffle.remove(0): queue.remove(0);
            queueTime -= nextTrack.getDuration();
            player.startTrack(nextTrack, false);
        } else {
            queueTime = 0;
            currTrack = null;
            player.startTrack(null, false);
        }
    }
    
    public int getIndex(AudioTrack track) {
        for(int i = 0; i < queue.size(); i++) {
            if(similarTrack(queue.get(i), track))
                return i;
        }
        return -1;
    }
    
    /*----      Getters & Setters       ----*/
    
    public List<AudioTrack> getQueue() { return queue; }
    
    public long getRemainingTime() {
        return currTrack.getDuration() - currTrack.getPosition();
    }
    
    public int queueLength() {
        return shuffle ? queueShuffle.size() : queue.size();
    }
    
    public String getTrackDetailedDisplay(int i) {
        AudioTrack track = shuffle ? queueShuffle.get(i) : queue.get(i);
        return String.format("[%s] %s - %s", MusicManager.getTimestamp(track.getDuration()),
                track.getInfo().title, track.getInfo().author);
    }
    
    public String getTrackDisplay(int i) {
        AudioTrack track = shuffle ? queueShuffle.get(i) : queue.get(i);
        return String.format("%s - %s", track.getInfo().title, track.getInfo().author);
    }
    
    public boolean announce() { return announce; }

    public TextChannel getAnnounceChannel() { return announceChannel; }
    
    public void setAnnounce(boolean announce, TextChannel channel) { 
        this.announce = announce; 
        this.announceChannel = channel;
    }
    
    public boolean isTrackRepeat() { return trackRepeat; }

    public void setTrackRepeat(boolean trackRepeat) { this.trackRepeat = trackRepeat; }

    public boolean isQueueLoop() { return queueLoop; }

    public void setQueueLoop(boolean queueLoop) { this.queueLoop = queueLoop; }

    public boolean isShuffle() { return shuffle; }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
        
        if(shuffle) {
            queueShuffle.clear();
            for(AudioTrack track: queue)
                queueShuffle.add(track.makeClone());
            Collections.shuffle(queueShuffle);
            
            queueTime = 0;
            for(AudioTrack track: queueShuffle)
                queueTime += track.getDuration();
        } else
            queueShuffle.clear();
    }
    
    public long getQueueTime() {
        long time = queueTime;
        
        if(currTrack != null)
            time += currTrack.getDuration() - currTrack.getPosition();
        
        return time;
    }
}
