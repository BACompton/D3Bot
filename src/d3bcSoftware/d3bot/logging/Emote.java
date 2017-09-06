package d3bcSoftware.d3bot.logging;

/**
 * Emote is a enumerate that specifics the emotes supported by Discord.
 * @author death_000
 *
 */
public enum Emote {
    X(":x:"), O(":o:"), QUESTION_MARK(":question:"), CHECK(":heavy_check_mark:"), EXCLAMATION(":exclamation: "),
    YT(":youtube:"), MAG_R(":mag_right:"), GREEN_CHECK(":white_check_mark:"), SHUFFLE(":twisted_rightwards_arrows:"),
    LOOP(":repeat:"), LOOP_ONE(":repeat_one:"), SKIP(":track_next:"), PLAY(":arrow_forward:"), PAUSE(":pause_button:"),
    OK(":ok:"), A(":regional_indicator_a:");
    
    /*----      Instance Variables       ----*/
    
    /**
     * The required text to achieve an emote.
     */
    private String txt;
    
    /*----      Constructors       ----*/
    
    private Emote(String txt) {
        this.txt = txt;
    }
    
    @Override
    public String toString() {
        return txt;
    }
}
