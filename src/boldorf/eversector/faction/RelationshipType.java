package boldorf.eversector.faction;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import java.awt.Color;

/** A type of relationship between factions, such as war or peace. */
public enum RelationshipType implements ColorStringObject
{
    WAR("War", AsciiPanel.brightRed),
    PEACE("Peace", null),
    ALLIANCE("Allied", AsciiPanel.brightGreen);
    
    private String description;
    private Color color;
    
    RelationshipType(String description, Color color)
    {
        this.description = description;
        this.color = color;
    }
    
    @Override
    public String toString()
        {return description;}
    
    public Color getColor()
        {return color;}
    
    @Override
    public ColorString toColorString()
        {return new ColorString(description, color);}
}