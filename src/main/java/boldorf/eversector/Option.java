package boldorf.eversector;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import boldorf.util.FileManager;
import boldorf.util.Utility;

import java.awt.*;
import java.util.Properties;

import static boldorf.eversector.Main.COLOR_FIELD;

/**
 * A game option stored as a property.
 *
 * @author Boldorf Smokebane
 */
public enum Option
{
    /**
     * The name of the ship's captain to be logged on the leaderboard.
     */
    CAPTAIN_NAME("Captain's Name", "Player", false),

    /**
     * The name of the player's ship.
     */
    SHIP_NAME("Ship's Name", "Player", false),

    /**
     * The seed for the random number generator.
     */
    SEED("Seed", 0, false),

    /**
     * If true, will keep the seed saved in the options file for the next game.
     */
    KEEP_SEED("Keep Seed", false, false),

    /**
     * The font used on the game's AsciiPanel. The value of this option refers to an index in Tileset.
     *
     * @see Tileset
     */
    FONT("Font", 0, true),

    /**
     * If true, the tiles version of the selected font will be used.
     */
    TILES("Tiles", true, true),

    /**
     * The width of the display in characters.
     */
    WIDTH("Display Width", 90, true),

    /**
     * The height of the display in characters.
     */
    HEIGHT("Display Height", 50, true),

    /**
     * The volume of the game's soundtrack.
     */
    MUSIC("Music Volume", 100, true),

    /**
     * The volume of all sound effects played.
     */
    SFX("Sound Effect Volume", 100, true),

    /**
     * If true, will show the leaderboard and log scores to it.
     */
    LEADERBOARD("Leaderboard", true, true),

    /**
     * If true, will give the player prompts on which candidate to vote for each election.
     */
    VOTING("Voting Prompts", true, true);

    /**
     * The property representing true.
     */
    public static final String OPTION_TRUE = "Enabled";

    /**
     * The property representing false.
     */
    public static final String OPTION_FALSE = "Disabled";

    /**
     * The color of true options.
     */
    public static final Color COLOR_TRUE = AsciiPanel.brightGreen;

    /**
     * The color of false options.
     */
    public static final Color COLOR_FALSE = AsciiPanel.brightRed;

    /**
     * The key for the font name in font properties files.
     */
    public static final String FONT_NAME = "name";

    /**
     * The key for width in font properties files.
     */
    public static final String FONT_WIDTH = "width";

    /**
     * The key for height in font properties files.
     */
    public static final String FONT_HEIGHT = "height";

    /**
     * The options as properties.
     */
    public static Properties options;

    /**
     * The key of the option.
     */
    private final String key;

    /**
     * The default value of the option.
     */
    private final String defaultValue;

    /**
     * If true, the option can be seen and changed through the opttions menu.
     */
    private final boolean visible;

    /**
     * Creates an option with a string property.
     *
     * @param key          the key of the option
     * @param defaultValue the default value of the option
     * @param visible      if true, the option will be visible in the options menu
     */
    Option(String key, String defaultValue, boolean visible)
    {
        this.key = key;
        this.defaultValue = defaultValue;
        this.visible = visible;
    }

    /**
     * Creates an option with a boolean property.
     *
     * @param key          the key of the option
     * @param defaultValue the default value of the option
     * @param visible      if true, the option will be visible in the options menu
     */
    Option(String key, boolean defaultValue, boolean visible)
    {
        this(key, booleanToString(defaultValue), visible);
    }

    /**
     * Creates an option with an integer property.
     *
     * @param key          the key of the option
     * @param defaultValue the default value of the option
     * @param visible      if true, the option will be visible in the options menu
     */
    Option(String key, int defaultValue, boolean visible)
    {
        this(key, Integer.toString(defaultValue), visible);
    }

    /**
     * To color string color string.
     *
     * @return the color string
     */
    public ColorString toColorString()
    {
        String property = getProperty();
        return new ColorString(key + ": ").add(new ColorString(getProperty(), getColor()));
    }

    /**
     * Gets the key of the option.
     *
     * @return the key of the option
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Gets the default value of the option.
     *
     * @return the default value of the option
     */
    public String getDefault()
    {
        return defaultValue;
    }

    /**
     * Returns true if the option is visible.
     *
     * @return true if the option is visible
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Gets the current property of the option.
     *
     * @return the property of the option
     */
    public String getProperty()
    {
        return options.getProperty(key);
    }

    /**
     * Converts the option's property to a boolean.
     *
     * @return the option's property as a boolean
     */
    public boolean toBoolean()
    {
        return OPTION_TRUE.equals(getProperty());
    }

    /**
     * Returns true if the option's is, by default, a boolean.
     *
     * @return true if the option is a boolean
     */
    public boolean isBoolean()
    {
        return OPTION_TRUE.equals(defaultValue) || OPTION_FALSE.equals(defaultValue);
    }

    /**
     * Converts the option's property to an integer.
     *
     * @return the option's property as an integer
     */
    public Integer toInt()
    {
        return Utility.parseInt(getProperty());
    }

    /**
     * Returns true if the option's is, by default, an integer.
     *
     * @return true if the option is an integer
     */
    public boolean isInt()
    {
        return Utility.parseInt(getProperty()) != null;
    }

    /**
     * Sets the option's property to the given string.
     *
     * @param property the string to assign to the option's property
     */
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

    /**
     * Sets the property to the given boolean.
     *
     * @param property the boolean to assign to the option's property
     */
    public void setProperty(boolean property)
    {
        setProperty(booleanToString(property));
    }

    /**
     * Sets the property to the given integer.
     *
     * @param property the integer to assign to the option's property
     */
    public void setProperty(int property)
    {
        setProperty(Integer.toString(property));
    }

    /**
     * If the option is a boolean, sets it to the opposite value.
     */
    public void toggle()
    {
        if (isBoolean())
        {
            setProperty(booleanToString(!toBoolean()));
        }
    }

    /**
     * If the option is an integer, increase its value by 1.
     */
    public void increment()
    {
        if (isInt())
        {
            setProperty(toInt() + 1);
        }
    }

    /**
     * If the option is an integer, decrease its value by 1.
     */
    public void decrement()
    {
        if (isInt())
        {
            setProperty(toInt() - 1);
        }
    }

    /**
     * Resets te option to its default value.
     */
    public void resetToDefault()
    {
        setProperty(defaultValue);
    }

    /**
     * Gets the color of the option's property, by default.
     *
     * @return the color of the option's property, by default
     * @see #COLOR_TRUE
     * @see #COLOR_FALSE
     */
    public Color getColor()
    {
        if (OPTION_TRUE.equals(getProperty()))
        {
            return COLOR_TRUE;
        }
        if (OPTION_FALSE.equals(getProperty()))
        {
            return COLOR_FALSE;
        }
        return COLOR_FIELD;
    }

    /**
     * Converts the given boolean to its constant string representation.
     *
     * @param option the boolean to convert
     * @return the boolean as a string
     * @see #OPTION_TRUE
     * @see #OPTION_FALSE
     */
    private static String booleanToString(boolean option)
    {
        return option ? OPTION_TRUE : OPTION_FALSE;
    }

    /**
     * Sets all undefined properties to their default values.
     */
    public static void applyDefaults()
    {
        for (Option option : Option.values())
        {
            if (option.getProperty() == null)
            {
                option.resetToDefault();
            }
        }
    }

    /**
     * Gets the option with the given key.
     *
     * @param key the key to find
     * @return the option with the given key
     */
    public static Option getOption(String key)
    {
        for (Option option : Option.values())
        {
            if (option.key.equals(key))
            {
                return option;
            }
        }
        return null;
    }
}