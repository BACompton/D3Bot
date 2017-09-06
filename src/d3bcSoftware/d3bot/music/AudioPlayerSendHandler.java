package d3bcSoftware.d3bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.audio.AudioSendHandler;

/**
 * Basic implementation of {@link AudioSendHandler} which is wrapper around {@link AudioPlayer}.
 * @author Boyd Compton
 */
public class AudioPlayerSendHandler implements AudioSendHandler{
    
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    
    /**
     * @param audioPlayer The Audio player to wrap around
     */
    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }
    
    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }
    
    @Override
    public byte[] provide20MsAudio() {
        return lastFrame.data;
    }
    
    @Override
    public boolean isOpus() { return true; }
}
