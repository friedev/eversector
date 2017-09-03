package boldorf.eversector.storage;

import asciiPanel.AsciiPanel;
import static boldorf.eversector.Main.COLOR_FIELD;
import java.awt.Color;

/** A wrapper class for the option keywords in options.properties. */
public abstract class Options
{
    public static final String
    NAME         = "Name",
    SEED         = "Seed",
    DISQUALIFIED = "Disqualified",
    
    FONT        = "Font",
    TILES       = "Tiles",
    WIDTH       = "Display Width",
    HEIGHT      = "Display Height",
    MUSIC       = "Music Volume",
    SFX         = "Sound Effect Volume",
    TIPS        = "Tips",
    LEADERBOARD = "Leaderboard",
    VOTING      = "Voting Prompts",
    NEWS        = "Faction News",
    KEEP_SEED   = "Keep Seed";
    
    public static final String[]
    MENU_NUMBERS   = {WIDTH, HEIGHT, MUSIC, SFX, FONT},
    MENU_BOOLEANS  = {TILES, TIPS, LEADERBOARD, VOTING, NEWS},
    DEBUG_BOOLEANS = {KEEP_SEED};
    
    public static final String
    OPTION_TRUE  = "Enabled",
    OPTION_FALSE = "Disabled",
    
    MENU_DEFAULT  = OPTION_TRUE,
    DEBUG_DEFAULT = OPTION_FALSE;
    
    public static final Color
    COLOR_TRUE = AsciiPanel.brightGreen,
    COLOR_FALSE = AsciiPanel.brightRed;
    
    public static final int
    DEFAULT_WIDTH  = 90,
    DEFAULT_HEIGHT = 50;
    
    public static Color getColor(String option)
    {
        if (OPTION_TRUE.equals(option))
            return COLOR_TRUE;
        if (OPTION_FALSE.equals(option))
            return COLOR_FALSE;
        return COLOR_FIELD;
    }
    
    public static String getOpposite(String option)
        {return toBoolean(option) ? OPTION_FALSE : OPTION_TRUE;}
    
    public static boolean toBoolean(String option)
        {return OPTION_TRUE.equals(option);}
    
    public static String toString(boolean option)
        {return option ? OPTION_TRUE : OPTION_FALSE;}
}