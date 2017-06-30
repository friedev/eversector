package boldorf.eversector.entities;

import asciiPanel.AsciiPanel;
import java.awt.Color;

public enum ReputationRange
{
    HEROIC("Heroic", "Admires", AsciiPanel.brightYellow, 600,
            Integer.MAX_VALUE),
    RESPECTED("Respected", "Respects", AsciiPanel.brightGreen, 150, 599),
    POSITIVE ("Positive", "Likes", AsciiPanel.green, 50, 149),
    NEUTRAL  ("Neutral", "Ignores", null, -49, 49),
    NEGATIVE ("Negative", "Dislikes", AsciiPanel.red, -149, -50),
    DESPISED ("Despised", "Despises", AsciiPanel.brightRed, -599, -150),
    INFAMOUS ("Infamous", "Loathes", AsciiPanel.brightMagenta,
            Integer.MIN_VALUE, -600);

    private String adjective;
    private String verb;
    private Color  color;
    private int    min;
    private int    max;

    ReputationRange(String adjective, String verb, Color color, int min, int max)
    {
        this.adjective = adjective;
        this.verb      = verb;
        this.color     = color;
        this.min       = min;
        this.max        = max;
    }

    public String getAdjective()
        {return adjective;}

    public String getVerb()
        {return verb;}
    
    public Color getColor()
        {return color;}

    public int getMinValue()
        {return min;}

    public int getMaxValue()
        {return max;}
    
    public boolean isInRange(int value)
        {return value >= min && value <= max;}
    
    public static ReputationRange getRange(int value)
    {
        for (ReputationRange range: ReputationRange.values())
            if (range.isInRange(value))
                return range;
        return null;
    }
}