package boldorf.eversector.map.faction;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import java.awt.Color;

/** A type of relationship between factions, such as war or peace. */
public enum RelationshipType
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
    
    public ColorString toColorString()
        {return new ColorString(description, color);}
}