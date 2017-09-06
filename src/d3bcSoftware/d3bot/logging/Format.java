package d3bcSoftware.d3bot.logging;

/**
 * DiscordFormat is a enumerate that specifics the markdown formatting supported by Discord.
 * @author Boyd Compton
 */
public enum Format {
    ITALICS("*"), BOLD("**"), BOLD_ITALICS("***"), UNDERLINE("__"), UNDERLINE_ITALICS("__*"), 
    UNDERLINE_BOLD("__**"), UNDERLINE_BOLD_ITALICS("__***"), STRIKE("~~"), CODE("`"), CODE_BLOCK("```"),
    UNEMBED_S("<"), UNEMBED_E(">");
    
    /*----      Instance Variables       ----*/
    
    /**
     * The required markdown text to achieve a formatting effect.
     */
    private String txt;
    
    /*----      Constructors       ----*/
    
    private Format(String txt) {
        this.txt = txt;
    }
    
    @Override
    public String toString() {
        return txt;
    }
}
