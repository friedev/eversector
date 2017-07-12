package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import static boldorf.eversector.Main.COLOR_FIELD;

/**
 * 
 */
public class Keybinding implements ColorStringObject
{
    private String function;
    private String[] keys;
    
    public Keybinding(String action, String... keys)
    {
        this.function = action;
        this.keys = keys;
    }
    
    public Keybinding(String action, char... keys)
        {this(action, charsToStrings(keys));}
    
    private static String[] charsToStrings(char[] chars)
    {
        String[] keyStrings = new String[chars.length];
        for (int i = 0; i < chars.length; i++)
            keyStrings[i] = Character.toString(chars[i]);
        return keyStrings;
    }
    
    public String getFunction()
        {return function;}
    
    public String[] getKeys()
        {return keys;}
    
    @Override
    public ColorString toColorString()
    {
        ColorString colorString = new ColorString(keys[0], COLOR_FIELD);
        
        if (keys.length > 1)
            for (int i = 1; i < keys.length; i++)
                colorString.add("/").add(new ColorString(keys[i], COLOR_FIELD));
        
        colorString.add(": ").add(function);
        return colorString;
    }
}