package boldorf.eversector;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import boldorf.util.FileManager;
import boldorf.util.Utility;

import java.awt.*;
import java.util.Properties;

import static boldorf.eversector.Main.COLOR_FIELD;

/**
 * A wrapper class for the option keywords in options.properties.
 */
public enum Option
{
    CAPTAIN_NAME("Captain's Name", "Player", false),
    SHIP_NAME("Ship's Name", "Player", false),
    SEED("Seed", 0, false),
    KEEP_SEED("Keep Seed", false, false),
    DISQUALIFIED("Disqualified", false, false),
    FONT("Font", 0, true),
    TILES("Tiles", true, true),
    WIDTH("Display Width", 90, true),
    HEIGHT("Display Height", 50, true),
    MUSIC("Music Volume", 100, true),
    SFX("Sound Effect Volume", 100, true),
    LEADERBOARD("Leaderboard", true, true),
    VOTING("Voting Prompts", true, true);

    public static final String OPTION_TRUE = "Enabled";
    public static final String OPTION_FALSE = "Disabled";

    public static final Color COLOR_TRUE = AsciiPanel.brightGreen;
    public static final Color COLOR_FALSE = AsciiPanel.brightRed;

    /**
     * Various game options in the form of a properties file.
     */
    public static Properties options;

    private final String key;
    private final String defaultValue;
    private final boolean visible;

    Option(String key, String defaultValue, boolean visible)
    {
        this.key = key;
        this.defaultValue = defaultValue;
        this.visible = visible;
    }

    Option(String key, boolean defaultValue, boolean visible)
    {this(key, booleanToString(defaultValue), visible);}

    Option(String key, int defaultValue, boolean visible)
    {this(key, Integer.toString(defaultValue), visible);}

    public ColorString toColorString()
    {
        String property = getProperty();
        return new ColorString(key + ": ").add(new ColorString(getProperty(), getColor()));
    }

    public String getKey()
    {return key;}

    public String getDefault()
    {return defaultValue;}

    public boolean isVisible()
    {return visible;}

    public String getProperty()
    {return options.getProperty(key);}

    public boolean toBoolean()
    {return OPTION_TRUE.equals(getProperty());}

    public boolean isBoolean()
    {return OPTION_TRUE.equals(defaultValue) || OPTION_FALSE.equals(defaultValue);}

    public Integer toInt()
    {return Utility.parseInt(getProperty());}

    public boolean isInt()
    {return Utility.parseInt(getProperty()) != null;}

    public void setProperty(String property)
    {
        options.setProperty(key, property);

        try
        {
            FileManager.save(options, Paths.OPTIONS);
        }
        catch (Exception e)
        {
        }
    }

    public void setProperty(boolean property)
    {setProperty(booleanToString(property));}

    public void setProperty(int property)
    {setProperty(Integer.toString(property));}

    public void toggle()
    {
        if (isBoolean()) { setProperty(booleanToString(!toBoolean())); }
    }

    public void increment()
    {
        if (isInt()) { setProperty(toInt() + 1); }
    }

    public void resetToDefault()
    {setProperty(defaultValue);}

    public Color getColor()
    {
        if (OPTION_TRUE.equals(getProperty())) { return COLOR_TRUE; }
        if (OPTION_FALSE.equals(getProperty())) { return COLOR_FALSE; }
        return COLOR_FIELD;
    }

    private static String booleanToString(boolean option)
    {return option ? OPTION_TRUE : OPTION_FALSE;}

    public static void applyDefaults()
    {
        for (Option option : Option.values())
        { if (option.getProperty() == null) { option.resetToDefault(); } }
    }

    public static Option getOption(String key)
    {
        for (Option option : Option.values())
        { if (option.key.equals(key)) { return option; } }
        return null;
    }
}